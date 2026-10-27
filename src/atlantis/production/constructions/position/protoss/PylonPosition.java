package atlantis.production.constructions.position.protoss;

import atlantis.game.A;
import atlantis.map.AMap;
import atlantis.map.base.ABaseLocation;
import atlantis.map.base.Bases;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.position.FindPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;

public class PylonPosition {
    public static HasPosition nextPosition() {
//        return APositionFinder.findStandardPosition(
        return FindPosition.findForBuilding(
            FreeWorkers.get().first(),
            AUnitType.Protoss_Pylon,
            null,
            defineNearTo(),
            37
        );
    }

    private static HasPosition defineNearTo() {
        int pylons = Count.pylons();

        if (pylons >= 4 && Count.bases() >= 2) {
            return Select.ourBases().random();
        }

        return Select.mainOrAnyBuilding();
    }

    /**
     * Search origin for the first Pylon: the base, moved a short way towards the
     * resource line, so the Pylon ends up beside the Nexus rather than on top of it.
     *
     * <p>
     * <b>This used to compute a "centre of resources" and then step back from it,
     * and the whole chain is gone (measured 2026-10-10, `_AI/CHALLENGES/OpenBW.md`).</b>
     * It did `geyser.translateTilesTowards(mineralsCenter, 50)` - fifty tiles past the
     * mineral centre, which on TauCross produced a point
     * <b>36 tiles outside the map</b> (`raw=[-36,88]`). `ensureWithinBounds()` then
     * returned <b>null</b> (nothing within its 8-tile radius of a tile off the map),
     * the `if (centerOfResources != null)` guard skipped the step that was supposed to
     * move the origin off the base, and the method silently returned
     * <b>the Nexus position itself</b> (`distBase = 0.84`). The placement search then
     * started on top of the Nexus, every low-radius candidate was legitimately
     * "occupied", and the bot never placed its first Pylon.
     * </p>
     *
     * <p>
     * The intent was always "a few tiles to the side of the base" - the 50-tile
     * detour through a "resource centre" was never needed to express it. The
     * direction is kept (towards the resource line, away from the map corner) and the
     * distance is the 7 tiles the old code was trying to reach.
     * </p>
     */
    public static APosition nearToPositionForFirstPylon() {
        AUnit base = Select.main();
        if (base == null) {
            return fallback();
        }

        // The direction towards the resource line is the natural first choice, but it
        // is only a candidate: on a corner base (measured on TauCross: Nexus at
        // (117,7)-(120,9), i.e. 7 tiles from the top edge) the minerals sit *towards*
        // that edge, and 7 tiles that way lands straight inside the 7-tile margin
        // `ProtossTooCloseToMapBoundaries` reserves for a Pylon. A blind direction is
        // what kept the search in a corner; the direction must be one that has room.
        HasPosition towards = ABaseLocation.mineralsCenter(base);
        if (towards == null) {
            towards = Select.geysers().inRadius(10, base).first();
        }

        return firstPylonOriginNear(base, towards);
    }

    /**
     * The base, moved {@link #FIRST_PYLON_TILES_FROM_BASE} tiles away from itself, in
     * a direction that actually leads somewhere: towards the resources when that has
     * room, otherwise away from the nearest map edge.
     *
     * <p>
     * A base in a corner is the case that matters. Its resource line commonly points
     * at the very edge the base is near, so "towards the minerals" and "into the
     * boundary margin" are the same direction - and the search then spends every ring
     * on tiles the boundary rule will refuse. Preferring the resource direction only
     * when the result is comfortably inside the map keeps the intended Protoss layout
     * where it is possible and degrades to "straight into open ground" where it is not.
     * </p>
     */
    private static APosition firstPylonOriginNear(AUnit base, HasPosition towards) {
        // "Towards something" only means something if it is a DIFFERENT point. When the
        // map has no minerals to measure from, `ABaseLocation.mineralsCenter` returns the
        // base itself (`if (center == null) return base`), and translating towards
        // yourself moves by exactly zero - which puts the origin back on the Nexus and
        // recreates the whole original failure one level deeper. Measured 2026-10-10 in
        // the stub world: mineralsCenter = the Nexus, origin = the Nexus, dist = 0.0.
        if (towards != null && base.distTo(towards) > 1) {
            APosition towardsResources = base.translateTilesTowards(firstPylonReach(), towards);
            if (isWellInsideMap(towardsResources)) {
                return towardsResources.makeBuildableFarFromBounds(5);
            }
        }

        // Resources are absent or point into an edge: step away from the closest edge
        // instead, which is the direction with room by construction.
        APosition awayFromEdge = base.translateTilesTowards(
            firstPylonReach(), awayFromNearestEdge(base)
        );
        if (!isWellInsideMap(awayFromEdge)) {
            return fallback();
        }

        return awayFromEdge.makeBuildableFarFromBounds(5);
    }

    /**
     * True when the position is inside the map with a Pylon's own margin to spare, so
     * the boundary rule cannot refuse every tile the search would reach from it.
     */
    private static boolean isWellInsideMap(APosition position) {
        if (position == null || position.isOutOfBounds()) return false;

        return position.distToMapBorders() > PYLON_EDGE_MARGIN_TILES;
    }

    /**
     * The map centre as seen from the base - i.e. the direction away from the nearest
     * edge. Used when the resource line has no room.
     */
    private static HasPosition awayFromNearestEdge(AUnit base) {
        return APosition.create(
            AMap.getMapWidthInTiles() / 2,
            AMap.getMapHeightInTiles() / 2
        );
    }

    /**
     * The Pylon's reserved distance from a map edge while supply is low - the same
     * number {@code ProtossTooCloseToMapBoundaries} enforces (7 tiles), kept here so
     * the origin and the rule that judges candidates agree.
     */
    public static final int PYLON_EDGE_MARGIN_TILES = 7;

    /**
     * How far the first Pylon's search origin sits from the Nexus. The old code was
     * reaching for 7 tiles (`translateTilesTowards(-7, ...)`) before that step was
     * skipped; kept as a named constant so the number is visible where it is decided.
     */
    public static final double FIRST_PYLON_TILES_FROM_BASE = 7;

    /**
     * Opening reach (owner): the first Pylon adjoins the Nexus - at or under
     * 20 supply the builder walks as little as possible, so the search starts
     * two tiles out instead of seven. Past that the layout doctrine owns the
     * origin again.
     */
    private static double firstPylonReach() {
        return A.supplyUsed() <= 20 ? 2 : FIRST_PYLON_TILES_FROM_BASE;
    }

    public static APosition nearToPositionForSecondPylon() {
//        return initialNearTo;

        AUnit base = Select.main();
        AChoke mainChoke = Chokes.mainChoke();
        if (base == null || mainChoke == null) return fallback();

        return base.translateTilesTowards(mainChoke, 8);
    }

    // =========================================================

    /**
     * Fallback origin when the map cannot answer: our first building's position.
     * Non-null in a live game (the Nexus exists), and the placement search handles a
     * base-centred origin by walking outwards from it.
     */
    private static APosition fallback() {
        AUnit first = Select.ourBuildings().first();

        return first != null ? first.position() : null;
    }

    public static HasPosition nearToForPylon(HasPosition nearTo) {
//        int supply = A.supplyTotal();
        int pylons = Count.pylons();
        HasPosition entry = nearTo;

        // First pylon should be close to Nexus for shorter travel dist
        if (pylons <= 0) {
            nearTo = nearToPositionForFirstPylon();
            lastReason = "first-pylon";
//                AAdvancedPainter.paintPosition(nearTo, "PylonPosition");
        }

        // First pylon should be oriented towards the nearest choke
        else if (pylons <= 1) {
            nearTo = nearToPositionForSecondPylon();
            lastReason = "second-pylon-choke";
        }

        // First pylon should be oriented towards the nearest choke
        else if (pylons == 5) {
            nearTo = Select.mainOrAnyBuilding().translatePercentTowards(Chokes.mainChoke(), 50);
            lastReason = "fifth-pylon-mid";
        }

        // Owner: Pylons #3-#4 adjoin the main, never the natural. The old
        // fallthrough (.exclude(natural).last()) leaked the natural in
        // whenever Bases.natural() was null with two bases (custom maps,
        // choke-detection gap): exclude(null) excludes nothing and last()
        // is the newest base - the natural Nexus. Pinned in
        // FirstFourPylonsStayAtMainTest.
        if (nearTo == null && pylons < 4) {
            nearTo = Select.mainOrAnyBuilding();
            lastReason = "first-four-main-pin";
        }

//        AUnit main = Select.main();
//        if (main != null) {
//            if (main.friendsNear().buildings().atMost(13)) nearTo = main;
//        }

//        if (ARandom.chance(70)) {
//            if (nearTo == null) nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).nearestTo(EnemyUnits.nearestEnemyBuilding());
//        }
        if (A.supplyFree() <= 1 && A.hasMinerals(300)) {
//            if (nearTo == null && ARandom.chance(70) && Count.bases() >= 3) nearTo = Select.ourBases().last();
            if (nearTo == null) {
                nearTo = Select.mainOrAnyBuildingPosition();
                lastReason = "supply-blocked-main";
            }
        }
        if (nearTo == null) {
            nearTo = Select.ourBasesWithUnfinished().exclude(Bases.natural()).last();
            lastReason = "exclude-natural-last";
        }
        if (nearTo == null) {
            nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).random();
            lastReason = "random-pylon";
        }
        if (nearTo == entry && entry != null) lastReason = "caller-provided";
//        if (nearTo == null) nearTo = Select.ourOfType(AUnitType.Protoss_Pylon).last();

//        System.err.println("pylons = " + pylons);

        return nearTo;
    }

    /**
     * Which nearTo branch sited the last Pylon (NEXT #214a): the board
     * "PylonSite" key reads this, so a far-away warp names the rule that
     * chose its origin instead of leaving tile forensics. Test seam:
     * per-siting state, reset around each pin.
     */
    public static String lastReason = "-";

    public static void clearForTests() {
        lastReason = "-";
    }
}
