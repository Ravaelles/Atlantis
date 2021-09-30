// Original work Copyright (c) 2015, 2017, Igor Dimitrijevic
// Modified work Copyright (c) 2017-2018 OpenBW Team

//////////////////////////////////////////////////////////////////////////
//
// This file is part of the BWEM Library.
// BWEM is free software, licensed under the MIT/X11 License.
// A copy of the license is provided with the library in the LICENSE file.
// Copyright (c) 2015, 2017, Igor Dimitrijevic
//
//////////////////////////////////////////////////////////////////////////

package bwem;

import bwapi.*;
import bwem.util.BwemExt;
import bwem.util.CheckMode;
import bwem.util.Pred;

import java.util.*;

import static bwem.AreaId.UNINITIALIZED;

/**
 * This class is an exact copy of native class, with these methods changed:
 * - assignStartingLocationsToSuitableBases
 */
public abstract class BWMap {
    final List<Pair<Pair<AreaId, AreaId>, WalkPosition>> rawFrontier =
            new ArrayList<>();
    final Game game;
    final List<Unit> mineralPatches;
    final List<Player> players;
    final List<Unit> vespeneGeysers;
    final List<Unit> units;
    private boolean automaticPathUpdate = false;
    private final Graph graph;
    private final NeighboringAreaChooser neighboringAreaChooser;
    TerrainData terrainData = null;
    NeutralData neutralData = null;
    Altitude highestAltitude;
    final Asserter asserter;

    BWMap(final Game game, final Asserter asserter) {
        this.game = game;
        this.players = game.getPlayers();
        this.mineralPatches = game.getMinerals();
        this.vespeneGeysers = game.getGeysers();
        this.units = game.getAllUnits();
        this.graph = new Graph(this);
        this.neighboringAreaChooser = new NeighboringAreaChooser();
        this.asserter = asserter;
    }

    public TerrainData getData() {
        return this.terrainData;
    }

    public boolean isInitialized() {
        return (this.terrainData != null);
    }

    Graph getGraph() {
        return graph;
    }

    public List<Pair<Pair<AreaId, AreaId>, WalkPosition>> getRawFrontier() {
        return rawFrontier;
    }

    public boolean automaticPathUpdate() {
        return automaticPathUpdate;
    }

    public void enableAutomaticPathAnalysis() {
        automaticPathUpdate = true;
    }

    public void assignStartingLocationsToSuitableBases() {
        boolean atLeastOneFailed = false;
        for (final TilePosition startingLocation : getData().getMapData().getStartingLocations()) {
            boolean isAssigned = false;

            for (final Base base : getBases()) {
                if (BwemExt.queenWiseDist(base.getLocation(), startingLocation)
                        <= BwemExt.MAX_TILES_BETWEEN_STARTING_LOCATION_AND_ITS_ASSIGNED_BASE) {
                    base.assignStartingLocation(startingLocation);
                    isAssigned = true;
                }
            }

            if (!atLeastOneFailed && !isAssigned) {
                atLeastOneFailed = true;
            }
        }

        // @Overriden
//        if (atLeastOneFailed) {
//            asserter.throwIllegalStateException("At least one starting location was not assigned to a base.");
//        }
    }

    public List<TilePosition> getUnassignedStartingLocations() {
        final List<TilePosition> remainingStartingLocations =
                new ArrayList<>(getData().getMapData().getStartingLocations());

        for (final Base base : getBases()) {
            if (remainingStartingLocations.isEmpty()) {
                break;
            } else if (base.isStartingLocation()
                    && base.getLocation().equals(remainingStartingLocations.get(0))) {
                remainingStartingLocations.remove(0);
            }
        }

        return remainingStartingLocations;
    }

    public Altitude getHighestAltitude() {
        return highestAltitude;
    }

    public List<Base> getBases() {
        return getGraph().getBases();
    }

    public List<ChokePoint> getChokePoints() {
        return getGraph().getChokePoints();
    }

    public NeutralData getNeutralData() {
        return this.neutralData;
    }

    public void onUnitDestroyed(Unit u) {
        if (u.getType().isMineralField()) {
            onMineralDestroyed(u);
        } else {
            onStaticBuildingDestroyed(u);
        }
    }

    private void onMineralDestroyed(Unit u) {
        for (int i = 0; i < getNeutralData().getMinerals().size(); ++i) {
            Mineral mineral = getNeutralData().getMinerals().get(i);
            if (mineral.getUnit().equals(u)) {
                onMineralDestroyed(mineral);
                mineral.simulateCPPObjectDestructor(); /* IMPORTANT! These actions are performed in the "~Neutral" dtor in BWEM 1.4.1 C++. */
                getNeutralData().getMinerals().remove(i);
                return;
            }
        }
        asserter.throwIllegalStateException("unit is not a Mineral");
    }

    /**
     * This method could be placed in {@link #onMineralDestroyed(Unit)}. This
     * remains as a separate method for portability consistency.
     */
    private void onMineralDestroyed(Mineral pMineral) {
        for (Area area : getGraph().getAreas()) {
            ((AreaInitializer) area).onMineralDestroyed(pMineral);
        }
    }

    private void onStaticBuildingDestroyed(Unit u) {
        for (int i = 0; i < getNeutralData().getStaticBuildings().size(); ++i) {
            StaticBuilding building = getNeutralData().getStaticBuildings().get(i);
            if (building.getUnit().equals(u)) {
                building
                        .simulateCPPObjectDestructor(); /* IMPORTANT! These actions are performed in the "~Neutral" dtor in BWEM 1.4.1 C++. */
                getNeutralData().getStaticBuildings().remove(i);
                return;
            }
        }
    }

    public List<Area> getAreas() {
        return getGraph().getAreas();
    }

    // Returns an Area given its id. Range = 1..size()
    public Area getArea(AreaId id) {
        return graph.getArea(id);
    }

    public Area getArea(WalkPosition w) {
        return graph.getArea(w);
    }

    public Area getArea(TilePosition t) {
        return graph.getArea(t);
    }

    public Area getNearestArea(WalkPosition w) {
        return graph.getNearestArea(w);
    }

    public Area getNearestArea(TilePosition t) {
        return graph.getNearestArea(t);
    }

    // graph.cpp:30:Area * mainArea(MapImpl * pMap, TilePosition topLeft, TilePosition size)
    // Note: The original C++ code appears to return the last discovered area instead of the area with
    // the highest frequency.
    // Bytekeeper: Further analysis shows there is usually one exactly one area, so we just return thatv
    // TODO: Determine if we desire the last discovered area or the area with the highest frequency.
    public Area getMainArea(final TilePosition topLeft, final TilePosition size) {
        // ----------------------------------------------------------------------
        // Any area.
        // ----------------------------------------------------------------------
        for (int dy = 0; dy < size.getY(); ++dy)
            for (int dx = 0; dx < size.getX(); ++dx) {
                final Area area = getArea(topLeft.add(new TilePosition(dx, dy)));
                if (area != null) return area;
            }
        // ----------------------------------------------------------------------
        return null;
    }

    public int getPathLength(Position a, Position b) {
        return graph.getPathingResult(a, b).map(PathingResult::getLength).orElse(-1);
    }

    // TODO: This might be a bad method: What is the difference between no path and "same area"?
    public CPPath getPath(Position a, Position b) {
        return graph.getPath(a, b).orElse(CPPath.EMPTY_PATH);
    }

    public TilePosition breadthFirstSearch(
        TilePosition start, Pred<Tile, TilePosition> findCond, Pred<Tile, TilePosition> visitCond, boolean connect8) {
        if (findCond.test(getData().getTile(start), start)) {
            return start;
        }

        final Set<TilePosition> visited =
                new TreeSet<>(
                        Comparator.comparing((TilePosition tilePosition) -> tilePosition.x)
                            .thenComparing((TilePosition tilePosition) -> tilePosition.y));
        Queue<TilePosition> toVisit = new ArrayDeque<>();

        toVisit.add(start);
        visited.add(start);

        TilePosition[] dir8 = {
                new TilePosition(-1, -1),
                new TilePosition(0, -1),
                new TilePosition(1, -1),
                new TilePosition(-1, 0),
                new TilePosition(1, 0),
                new TilePosition(-1, 1),
                new TilePosition(0, 1),
                new TilePosition(1, 1)
        };
        TilePosition[] dir4 = {
                new TilePosition(0, -1),
                new TilePosition(-1, 0),
                new TilePosition(+1, 0),
                new TilePosition(0, +1)
        };
        TilePosition[] directions = connect8 ? dir8 : dir4;

        while (!toVisit.isEmpty()) {
            TilePosition current = toVisit.remove();
            for (TilePosition delta : directions) {
                TilePosition next = current.add(delta);
                if (getData().getMapData().isValid(next)) {
                    Tile nextTile = getData().getTile(next, CheckMode.NO_CHECK);
                    if (findCond.test(nextTile, next)) {
                        return next;
                    }
                    if (visitCond.test(nextTile, next) && !visited.contains(next)) {
                        toVisit.add(next);
                        visited.add(next);
                    }
                }
            }
        }

        // TODO: Are we supposed to return start or not?
        asserter.throwIllegalStateException("");
        return start;
    }

    public TilePosition breadthFirstSearch(TilePosition start, Pred<Tile, TilePosition> findCond, Pred<Tile, TilePosition> visitCond) {
        return breadthFirstSearch(start, findCond, visitCond, true);
    }

    public WalkPosition breadthFirstSearch(
        WalkPosition start, Pred<MiniTile, WalkPosition> findCond, Pred<MiniTile, WalkPosition> visitCond, boolean connect8) {
        if (findCond.test(getData().getMiniTile(start), start)) {
            return start;
        }

        final Set<WalkPosition> visited =
                new TreeSet<>(
                    Comparator.comparing((WalkPosition pos) -> pos.x)
                        .thenComparing((WalkPosition pos) -> pos.y));
        final Queue<WalkPosition> toVisit = new ArrayDeque<>();

        toVisit.add(start);
        visited.add(start);

        final WalkPosition[] dir8 = {
                new WalkPosition(-1, -1),
                new WalkPosition(0, -1),
                new WalkPosition(1, -1),
                new WalkPosition(-1, 0),
                new WalkPosition(1, 0),
                new WalkPosition(-1, 1),
                new WalkPosition(0, 1),
                new WalkPosition(1, 1)
        };
        final WalkPosition[] dir4 = {
                new WalkPosition(0, -1),
                new WalkPosition(-1, 0),
                new WalkPosition(1, 0),
                new WalkPosition(0, 1)
        };
        final WalkPosition[] directions = connect8 ? dir8 : dir4;

        while (!toVisit.isEmpty()) {
            final WalkPosition current = toVisit.remove();
            for (final WalkPosition delta : directions) {
                final WalkPosition next = current.add(delta);
                if (getData().getMapData().isValid(next)) {
                    final MiniTile miniTile = getData().getMiniTile(next, CheckMode.NO_CHECK);
                    if (findCond.test(miniTile, next)) {
                        return next;
                    }
                    if (visitCond.test(miniTile, next) && !visited.contains(next)) {
                        toVisit.add(next);
                        visited.add(next);
                    }
                }
            }
        }

        // TODO: Are we supposed to return start or not?
        asserter.throwIllegalStateException("");
        return start;
    }

    public WalkPosition breadthFirstSearch(
            final WalkPosition start, Pred<MiniTile, WalkPosition> findCond, Pred<MiniTile, WalkPosition> visitCond) {
        return breadthFirstSearch(start, findCond, visitCond, true);
    }

    public Tile getTile(final TilePosition tilePosition) {
        return getData().getTile(tilePosition);
    }

    public Position getCenter() {
        return getData().getMapData().getCenter();
    }

    public List<TilePosition> getStartingLocations() {
        return getData().getMapData().getStartingLocations();
    }

    private List<Unit> filterPlayerUnits(final Collection<Unit> units, final Player player) {
        //        return this.units.stream().filter(u -> u instanceof PlayerUnit
        //                && ((PlayerUnit)u).getPlayer().equals(player)).map(u ->
        // (PlayerUnit)u).collect(Collectors.toList());
        final List<Unit> ret = new ArrayList<>();
        for (final Unit u : units) {
            if (!(u.getType().isMineralField() || u.getType()
                .equals(UnitType.Resource_Vespene_Geyser)) && u.getPlayer().equals(player)) {
                ret.add(u);
            }
        }
        return ret;
    }

    List<Unit> filterNeutralPlayerUnits(
        final Collection<Unit> units, final Collection<Player> players) {
        final List<Unit> ret = new ArrayList<>();
        for (final Player player : players) {
            if (player.isNeutral()) {
                ret.addAll(filterPlayerUnits(units, player));
            }
        }
        return ret;
    }

    void setAreaIdInTile(final TilePosition t) {
        final Tile tile = getData().getTile(t);
        if (!(tile.getAreaId().intValue() == 0)) { // initialized to 0
            asserter.throwIllegalStateException("");
        }

        for (int dy = 0; dy < 4; ++dy) {
            for (int dx = 0; dx < 4; ++dx) {
                final AreaId id =
                        getData()
                                .getMiniTile(t.toWalkPosition().add(new WalkPosition(dx, dy)), CheckMode.NO_CHECK)
                                .getAreaId();
                if (id.intValue() != 0) {
                    if (tile.getAreaId().intValue() == 0) {
                        tile.setAreaId(id);
                    } else if (!tile.getAreaId().equals(id)) {
                        tile.setAreaId(UNINITIALIZED);
                        return;
                    }
                }
            }
        }
    }

    Pair<AreaId, AreaId> findNeighboringAreas(final WalkPosition p) {
        final Pair<AreaId, AreaId> result = new Pair<>(null, null);

        final WalkPosition[] deltas = {
                new WalkPosition(0, -1),
                new WalkPosition(-1, 0),
                new WalkPosition(+1, 0),
                new WalkPosition(0, +1)
        };
        for (final WalkPosition delta : deltas) {
            if (getData().getMapData().isValid(p.add(delta))) {
                final AreaId areaId = getData().getMiniTile(p.add(delta), CheckMode.NO_CHECK).getAreaId();
                if (areaId.intValue() > 0) {
                    if (result.getLeft() == null) {
                        result.setLeft(areaId);
                    } else if (!result.getLeft().equals(areaId)) {
                        if (result.getRight() == null ||
                            areaId.intValue() < result.getRight().intValue()) {
                            result.setRight(areaId);
                        }
                    }
                }
            }
        }

        return result;
    }

    AreaId chooseNeighboringArea(final AreaId a, final AreaId b) {
        return this.neighboringAreaChooser.chooseNeighboringArea(a, b);
    }

    private static class NeighboringAreaChooser {
        private final BitSet areaPairFlag = new BitSet(800000);

        AreaId chooseNeighboringArea(final AreaId a, final AreaId b) {
            int aId = a.intValue();
            int bId = b.intValue();

            int cantor = (aId + bId) * (aId + bId + 1);
            if (aId > bId) {
                cantor += bId;
            } else {
                cantor += aId;
            }

            areaPairFlag.flip(cantor);
            return areaPairFlag.get(cantor) ? a : b;
        }
    }
}
