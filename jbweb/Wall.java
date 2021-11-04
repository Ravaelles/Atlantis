package jbweb;

import bwapi.*;
import bwem.*;

import java.util.*;

public class Wall {
    private UnitType tightType;
    private Position centroid;
    private TilePosition opening, initialPathStart, initialPathEnd, pathStart, pathEnd, creationStart;
    private List<TilePosition> defenses = new ArrayList<>();
    private List<TilePosition> smallTiles = new ArrayList<>();
    private List<TilePosition> mediumTiles = new ArrayList<>();
    private List<TilePosition> largeTiles = new ArrayList<>();
    private List<Position> notableLocations = new ArrayList<>();
    private ListIterator<UnitType> typeIterator;
    private List<UnitType> rawBuildings;
    private List<UnitType> rawDefenses;
    private List<Area> accessibleNeighbors;
    private HashMap<TilePosition, UnitType> currentLayout;
    private HashMap<TilePosition, UnitType> bestLayout = new HashMap<>();
    private Area area;
    private ChokePoint choke;
    private Base base;
    private double chokeAngle, bestWallScore, jpsDist;
    private boolean pylonWall, openWall, requireTight, movedStart, pylonWallPiece, allowLifted, flatRamp;
    private Station closestStation;

    public Wall(Area _area, ChokePoint _choke, List<UnitType> _buildings, List<UnitType> _defenses, UnitType _tightType, boolean _requireTight, boolean _openWall) {
        area = _area;
        choke = _choke;
        rawBuildings = _buildings;
        rawDefenses = _defenses;
        tightType = _tightType;
        requireTight = _requireTight;
        openWall = _openWall;

        // Create Wall layout and find basic features
        initialize();
        addPieces();
        currentLayout = bestLayout;
        centroid = findCentroid();
        opening = findOpening();

        // Add defenses
        addDefenses();

        // Verify opening and cleanup Wall
        opening = findOpening();
        cleanup();
    }

    /// Returns the Chokepoint associated with this Wall.
    public ChokePoint getChokePoint() {
        return choke;
    }

    /// Returns the Area associated with this Wall.
    public Area getArea() {
        return area;
    }

    /// Returns the defense locations associated with this Wall.
    public List<TilePosition> getDefenses() {
        return defenses;
    }

    /// Returns the TilePosition belonging to the opening of the wall.
    public TilePosition getOpening() {
        return opening;
    }

    /// Returns the TilePosition belonging to the centroid of the wall pieces.
    public Position getCentroid() {
        return centroid;
    }

    /// Returns the TilePosition belonging to large UnitType buildings.
    public List<TilePosition> getLargeTiles() {
        return largeTiles;
    }

    /// Returns the TilePosition belonging to medium UnitType buildings.
    public List<TilePosition> getMediumTiles() {
        return mediumTiles;
    }

    /// Returns the TilePosition belonging to small UnitType buildings.
    public List<TilePosition> getSmallTiles() {
        return smallTiles;
    }

    /// Returns the raw vector of the buildings the wall was initialized with.
    public List<UnitType> getRawBuildings() {
        return rawBuildings;
    }

    /// Returns the raw vector of the defenses the wall was initialized with.
    public List<UnitType> getRawDefenses() {
        return rawDefenses;
    }

    /// Returns true if the Wall only contains Pylons.
    public boolean isPylonWall() {
        return pylonWall;
    }

    /// Adds a piece at the TilePosition based on the UnitType.
    public void addToWallPieces(TilePosition here, UnitType building) {
        if (building.tileWidth() >= 4)
            largeTiles.add(here);
        else if (building.tileWidth() >= 3)
            mediumTiles.add(here);
        else if (building != rawDefenses.get(rawDefenses.size()-1))
            defenses.add(here);
        else if (building.tileWidth() >= 2)
            smallTiles.add(here);
    }

    private Position findCentroid() {
        // Create current centroid using all buildings except Pylons
        Position currentCentroid = new Position(0, 0);
        int sizeWall = rawBuildings.size();
        for (TilePosition tile : bestLayout.keySet()) {
            UnitType type = bestLayout.get(tile);
            if (type != UnitType.Protoss_Pylon) {
                currentCentroid = new Position(currentCentroid.x + tile.toPosition().x + type.tileSize().toPosition().x/2,
                        currentCentroid.y + tile.toPosition().y + type.tileSize().toPosition().y/2);
            } else {
                sizeWall--;
            }
        }

        // Create a centroid if we only have a Pylon wall
        if (sizeWall == 0) {
            sizeWall = bestLayout.size();
            for (TilePosition tile : bestLayout.keySet()) {
                UnitType type = bestLayout.get(tile);
                currentCentroid = new Position(currentCentroid.x + tile.toPosition().x + type.tileSize().toPosition().x/2,
                        currentCentroid.y + tile.toPosition().y + type.tileSize().toPosition().y/2);
            }
        }

        return new Position(currentCentroid.x/sizeWall, currentCentroid.y/sizeWall);
    }

    private TilePosition findOpening() {
        if (!openWall) {
            return TilePosition.Invalid;
        }

        // Set any tiles on the path as reserved, so we don't build on them
        Path currentPath = findPathOut();
        TilePosition currentOpening = TilePosition.Invalid;

        // Check which tile is closest to each part on the path, set as opening
        double distBest = Double.MAX_VALUE;
        for (TilePosition pathTile : currentPath.getTiles()){
            Position closestChokeGeo = JBWEB.getClosestChokeTile (choke, new Position(pathTile));
            double dist = closestChokeGeo.getDistance(new Position(pathTile));
            Position centerPath = new Position(pathTile.x + 16, pathTile.y + 16);

            boolean angleOkay = true;
            boolean distOkay = false;

            // Check if the angle and distance is okay
            for (TilePosition tileLayout : currentLayout.keySet()) {
                UnitType typeLayout = currentLayout.get(tileLayout);
                if (typeLayout == UnitType.Protoss_Pylon) {
                    continue;
                }

                Position centerPiece = new Position(tileLayout.toPosition().x + typeLayout.tileWidth() * 16,
                        tileLayout.toPosition().y + typeLayout.tileHeight() * 16);
                double openingAngle = JBWEB.getAngle(new Pair<>(centerPiece, centerPath));
                double openingDist = centerPiece.getDistance(centerPath);

                if (Math.abs(chokeAngle - openingAngle) > 35.0)
                    angleOkay = false;
                if (openingDist < 320.0)
                    distOkay = true;
            }
            if (distOkay && angleOkay && dist < distBest) {
                distBest = dist;
                currentOpening = pathTile;
            }
        }

        // If we don't have an opening, assign the closest path tile to wall centroid as opening
        if (!currentOpening.isValid(JBWEB.game)) {
            for (TilePosition pathTile : currentPath.getTiles()) {
                Position p = new Position(pathTile);
                double dist = centroid.getDistance(p);
                if (dist < distBest) {
                    distBest = dist;
                    currentOpening = pathTile;
                }
            }
        }

        return currentOpening;
    }

    private Path findPathOut() {
        // Check that the path points are possible to reach
        checkPathPoints();
        Position startCenter = new Position(pathStart.toPosition().x + 16, pathStart.toPosition().y + 16);
        Position endCenter = new Position(pathEnd.toPosition().x + 16, pathEnd.toPosition().y + 16);

        // Get a new path
        Path newPath = new Path();
        allowLifted = false;
        newPath.bfsPath(endCenter, startCenter, this);
        return newPath;
    }

    private boolean powerCheck(UnitType type, TilePosition here) {
        if (type != UnitType.Protoss_Pylon || pylonWall)
            return true;

        // TODO: Create a generic BWEB function that takes 2 tiles and tells you if the 1st tile will power the 2nd tile
        for (TilePosition tileLayout : currentLayout.keySet()) {
            UnitType typeLayout = currentLayout.get(tileLayout);
            if (typeLayout == UnitType.Protoss_Pylon) {
                continue;
            }

            if (typeLayout.tileWidth() == 4) {
                boolean powersThis = false;
                if (tileLayout.y - here.y == -5 || tileLayout.y - here.y == 4) {
                    if (tileLayout.x - here.x >= -4 && tileLayout.x - here.x <= 1) {
                        powersThis = true;
                    }
                }
                if (tileLayout.y - here.y == -4 || tileLayout.y - here.y == 3) {
                    if (tileLayout.x - here.x >= -7 && tileLayout.x - here.x <= 4) {
                        powersThis = true;
                    }
                }
                if (tileLayout.y - here.y == -3 || tileLayout.y - here.y == 2) {
                    if (tileLayout.x - here.x >= -8 && tileLayout.x - here.x <= 5) {
                        powersThis = true;
                    }
                }
                if (tileLayout.y - here.y >= -2 && tileLayout.y - here.y <= 1) {
                    if (tileLayout.x - here.x >= -8 && tileLayout.x - here.x <= 6) {
                        powersThis = true;
                    }
                }
                if (!powersThis) {
                    return false;
                }
            } else {
                boolean powersThis = false;
                if (tileLayout.y - here.y == 4) {
                    if (tileLayout.x - here.x >= -3 && tileLayout.x - here.x <= 2) {
                        powersThis = true;
                    }
                }
                if (tileLayout.y - here.y == -4 || tileLayout.y - here.y == 3) {
                    if (tileLayout.x - here.x >= -6 && tileLayout.x - here.x <= 5) {
                        powersThis = true;
                    }
                }
                if (tileLayout.y - here.y >= -3 && tileLayout.y - here.y <= 2) {
                    if (tileLayout.x - here.x >= -7 && tileLayout.x - here.x <= 6) {
                        powersThis = true;
                    }
                }
                if (!powersThis) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean angleCheck(UnitType type, TilePosition here) {
        Position centerHere = new Position(here.toPosition().x + type.tileWidth()*16,
                here.toPosition().y + type.tileHeight()*16);

        // If we want a closed wall, we don't care the angle of the buildings
        if (!openWall || (type == UnitType.Protoss_Pylon && !pylonWall && !pylonWallPiece)) {
            return true;
        }

        // Check if the angle is okay between all pieces in the current layout
        for (TilePosition tileLayout : currentLayout.keySet()) {
            UnitType typeLayout = currentLayout.get(tileLayout);
            if (typeLayout == UnitType.Protoss_Pylon)
                continue;

            Position centerPiece = new Position(tileLayout.toPosition().x + typeLayout.tileWidth()*16,
                    tileLayout.toPosition().y + typeLayout.tileHeight()*16);
            double wallAngle = JBWEB.getAngle(new Pair<>(centerPiece, centerHere));

            if (Math.abs(chokeAngle - wallAngle) > 20.0) {
                return false;
            }
        }
        return true;
    }

    private boolean placeCheck(UnitType type, TilePosition here) {
        // Allow Pylon to overlap station defenses
        if (type == UnitType.Protoss_Pylon) {
            if (closestStation != null && here != closestStation.getDefenseLocations().get(closestStation.getDefenseLocations().size()-1)) {
                return true;
            }
        }

        // Check if placement is valid
        if (JBWEB.isReserved(here, type.tileWidth(), type.tileHeight())
            || !JBWEB.isPlaceable(type, here)
            || (!openWall && JBWEB.tilesWithinArea(area, here, type.tileWidth(), type.tileHeight()) == 0)
            || (openWall && JBWEB.tilesWithinArea(area, here, type.tileWidth(), type.tileHeight()) == 0 &&
                (type == UnitType.Protoss_Pylon || (JBWEB.mapBWEM.getMap().getArea(here) != null &&
                        choke.getAreas().getFirst() != JBWEB.mapBWEM.getMap().getArea(here) &&
                        choke.getAreas().getSecond() != JBWEB.mapBWEM.getMap().getArea(here))))) {
            return false;
        }
        return true;
    }

    // Functions for each dimension check
    private int gapRight(UnitType parent, int dimR) {
        return (parent.tileWidth() * 16) - parent.dimensionLeft() + dimR;
    }

    private int gapLeft(UnitType parent, int dimL) {
        return (parent.tileWidth() * 16) - parent.dimensionRight() - 1 + dimL;
    }

    private int gapUp(UnitType parent, int dimU) {
        return (parent.tileHeight() * 16) - parent.dimensionDown() - 1 + dimU;
    }

    private int gapDown(UnitType parent, int dimD) {
        return (parent.tileHeight() * 16) - parent.dimensionUp() + dimD;
    }


    // Check if the building is terrain tight when placed here
    private boolean terrainTightCheck(WalkPosition w, boolean check) {
        TilePosition t = new TilePosition(w);

        // If the walk position is invalid or un-walkable
        if (tightType != UnitType.None && check && (!w.isValid(JBWEB.game) || !JBWEB.game.isWalkable(w))) {
            return true;
        }

        // If we don't care about walling tight and the tile isn't walkable
        if (!requireTight && !JBWEB.isWalkable(t)) {
            return true;
        }

        // If there's a mineral field or geyser here
        if (JBWEB.isUsed(t, 1, 1).isResourceContainer()) {
            return true;
        }
        return false;
    };


    // Iterate vertical tiles adjacent of this placement
    private List<Pair<Boolean, Integer>> checkVerticalSide(WalkPosition start, boolean check, String gap, int dim, int walkWidth,
                                                   boolean terrainTight, boolean parentTight, int p1Tight, int p2Tight,
                                                   boolean checkL, boolean checkR, int vertTight, int horizTight) {
        for (int x = start.x - 1; x < start.x + walkWidth + 1; x++) {
            WalkPosition w = new WalkPosition(x, start.y);
            TilePosition t = new TilePosition(w);
            UnitType parent = JBWEB.isUsed(t, 1, 1);
            boolean leftCorner = x < start.x;
            boolean rightCorner = x >= start.x + walkWidth;

            int gapValue = 0;
            if (gap.equals("Right")) {
                gapValue = gapRight(parent, dim);
            } else if (gap.equals("Left")) {
                gapValue = gapLeft(parent, dim);
            } else if (gap.equals("Up")) {
                gapValue = gapUp(parent, dim);
            } else if (gap.equals("Down")) {
                gapValue = gapDown(parent, dim);
            }

            // If this is a corner
            if (leftCorner || rightCorner) {
                // Check if it's tight with the terrain
                if (!terrainTight && terrainTightCheck(w, check) && leftCorner ? terrainTightCheck(w, checkL) : terrainTightCheck(w, checkR)) {
                    terrainTight = true;
                }
                // Check if it's tight with a parent
                if (!parentTight && parent != rawBuildings.get(rawBuildings.size()-1) && (!requireTight || (gapValue < vertTight && (leftCorner ? gapValue < horizTight : gapValue < horizTight)))) {
                    parentTight = true;
                }
            } else {
                // Check if it's tight with the terrain
                if (!terrainTight && terrainTightCheck(w, check)) {
                    terrainTight = true;
                }
                // Check if it's tight with a parent
                if (!parentTight && parent != rawBuildings.get(rawBuildings.size()-1) && (!requireTight || gapValue < vertTight)) {
                    parentTight = true;
                }
            }

            // Check to see which node it is closest to (0 is don't check, 1 is not tight, 2 is tight)
            if (!openWall && !JBWEB.isWalkable(t) && w.getDistance(choke.getCenter()) < 4) {
                if (w.getDistance(choke.getNodePosition(ChokePoint.Node.END1)) < w.getDistance(choke.getNodePosition(ChokePoint.Node.END2))) {
                    if (p1Tight == 0) {
                        p1Tight = 1;
                    }
                    if (terrainTight) {
                        p1Tight = 2;
                    }
                } else if (p2Tight == 0) {
                    if (p2Tight == 0) {
                        p2Tight = 1;
                    }
                    if (terrainTight) {
                        p2Tight = 2;
                    }
                }
            }
        }

        List<Pair<Boolean, Integer>> checkValue = new ArrayList<>();
        checkValue.add(new Pair<>(terrainTight, p1Tight));
        checkValue.add(new Pair<>(parentTight, p2Tight));
        return checkValue;
    }

    // Iterate horizontal tiles adjacent of this placement
    private List<Pair<Boolean, Integer>> checkHorizontalSide(WalkPosition start, boolean check, String gap, int dim, int walkHeight,
                                                     boolean terrainTight, boolean parentTight, int p1Tight, int p2Tight,
                                                     boolean checkU, boolean checkD, int vertTight, int horizTight) {
        for (int y = start.y - 1; y < start.y + walkHeight + 1; y++) {
            WalkPosition w = new WalkPosition(start.x, y);
            TilePosition t = new TilePosition(w);
            UnitType parent = JBWEB.isUsed(t, 1, 1);
            boolean topCorner = y < start.y;
            boolean downCorner = y >= start.y + walkHeight;

            int gapValue = 0;
            if (gap.equals("Right")) {
                gapValue = gapRight(parent, dim);
            } else if (gap.equals("Left")) {
                gapValue = gapLeft(parent, dim);
            } else if (gap.equals("Up")) {
                gapValue = gapUp(parent, dim);
            } else if (gap.equals("Down")) {
                gapValue = gapDown(parent, dim);
            }

            // If this is a corner
            if (topCorner || downCorner) {
                // Check if it's tight with the terrain
                if (!terrainTight && terrainTightCheck(w, check) && topCorner ? terrainTightCheck(w, checkU) : terrainTightCheck(w, checkD)) {
                    terrainTight = true;
                }
                // Check if it's tight with a parent
                if (!parentTight && parent != rawBuildings.get(rawBuildings.size()-1) && (!requireTight || (gapValue < horizTight && (topCorner ? gapValue < vertTight : gapValue < vertTight)))) {
                    parentTight = true;
                }
            } else {
                // Check if it's tight with the terrain
                if (!terrainTight && terrainTightCheck(w, check)) {
                    terrainTight = true;
                }
                // Check if it's tight with a parent
                if (!parentTight && parent != rawBuildings.get(rawBuildings.size()-1) && (!requireTight || gapValue < horizTight)) {
                    parentTight = true;
                }
            }

            // Check to see which node it is closest to (0 is don't check, 1 is not tight, 2 is tight)
            if (!openWall && !JBWEB.isWalkable(t) && w.getDistance(choke.getCenter()) < 4) {
                if (w.getDistance(choke.getNodePosition(ChokePoint.Node.END1)) < w.getDistance(choke.getNodePosition(ChokePoint.Node.END2))) {
                    if (p1Tight == 0) {
                        p1Tight = 1;
                    }
                    if (terrainTight) {
                        p1Tight = 2;
                    }
                } else if (p2Tight == 0) {
                    if (p2Tight == 0) {
                        p2Tight = 1;
                    }
                    if (terrainTight) {
                        p2Tight = 2;
                    }
                }
            }
        }

        List<Pair<Boolean, Integer>> checkValue = new ArrayList<>();
        checkValue.add(new Pair<>(terrainTight, p1Tight));
        checkValue.add(new Pair<>(parentTight, p2Tight));
        return checkValue;
    }


    private boolean tightCheck(UnitType type, TilePosition here) {
        // If this is a powering pylon and we are not making a pylon wall, we don't care if it's tight
        if (type == UnitType.Protoss_Pylon && !pylonWall && !pylonWallPiece) {
            return true;
        }

        // Dimensions of current buildings UnitType
        int dimL = (type.tileWidth() * 16) - type.dimensionLeft();
        int dimR = (type.tileWidth() * 16) - type.dimensionRight() - 1;
        int dimU = (type.tileHeight() * 16) - type.dimensionUp();
        int dimD = (type.tileHeight() * 16) - type.dimensionDown() - 1;
        int walkHeight = type.tileHeight() * 4;
        int walkWidth = type.tileWidth() * 4;

        // Dimension of UnitType to check tightness for
        int vertTight = (tightType == UnitType.None) ? 32 : tightType.height();
        int horizTight = (tightType == UnitType.None) ? 32 : tightType.width();

        // Checks each side of the building to see if it is valid for walling purposes
        boolean checkL = dimL < horizTight;
        boolean checkR = dimR < horizTight;
        boolean checkU = dimU < vertTight;
        boolean checkD = dimD < vertTight;

        // Figures out how many extra tiles we can check tightness for
        int extraL = pylonWall || !requireTight ? 0 : Math.max(0, (horizTight - dimL) / 8);
        int extraR = pylonWall || !requireTight ? 0 : Math.max(0, (horizTight - dimR) / 8);
        int extraU = pylonWall || !requireTight ? 0 : Math.max(0, (vertTight - dimU) / 8);
        int extraD = pylonWall || !requireTight ? 0 : Math.max(0, (vertTight - dimD) / 8);

        // Setup boundary WalkPositions to check for tightness
        WalkPosition left =  new WalkPosition(here.x - (1 + extraL), here.y);
        WalkPosition right = new WalkPosition(here.x + walkWidth + extraR, here.y);
        WalkPosition up =  new WalkPosition(here.x, here.y - (1 + extraU));
        WalkPosition down =  new WalkPosition(here.x, here.y + walkHeight + extraD);

        // Used for determining if the tightness we found is suitable
        boolean firstBuilding = currentLayout.size() == 0;
        boolean lastBuilding = currentLayout.size() == (rawBuildings.size() - 1);
        boolean terrainTight = false;
        boolean parentTight = false;
        int p1Tight = 0;
        int p2Tight = 0;

        // For each side, check if it's terrain tight or tight with any adjacent buildings
        List<Pair<Boolean, Integer>> v;
        v = checkVerticalSide(up, checkU, "Up", dimR, walkWidth, terrainTight, parentTight, p1Tight, p2Tight, checkL, checkR, vertTight, horizTight);
        v = checkVerticalSide(down, checkD, "Down", dimL, walkWidth, v.get(0).getFirst(), v.get(1).getFirst(), v.get(0).getSecond(), v.get(1).getSecond(), checkL, checkR, vertTight, horizTight);
        v = checkHorizontalSide(left, checkL, "Left", dimU, walkHeight, v.get(0).getFirst(), v.get(1).getFirst(), v.get(0).getSecond(), v.get(1).getSecond(), checkU, checkD, vertTight, horizTight);
        v = checkHorizontalSide(right, checkR, "Right", dimD, walkHeight, v.get(0).getFirst(), v.get(1).getFirst(), v.get(0).getSecond(), v.get(1).getSecond(), checkU, checkD, vertTight, horizTight);

        terrainTight = v.get(0).getFirst();
        parentTight = v.get(1).getFirst();
        p1Tight = v.get(0).getSecond();
        p2Tight = v.get(1).getSecond();

        // If we want a closed wall, we need all buildings to be tight at the tightness resolution...
        if (!openWall) {
            if (!lastBuilding && !firstBuilding) {      // ...to the parent if not first building
                return parentTight;
            }
            if (firstBuilding) {                        // ...to the terrain if first building
                return terrainTight && p1Tight != 1 && p2Tight != 1;
            }
            if (lastBuilding) {                         // ...to the parent and terrain if last building
                return terrainTight && parentTight && p1Tight != 1 && p2Tight != 1;
            }
        }

        // If we want an open wall, we need this building to be tight at tile resolution to a parent or terrain
        else if (openWall) {
            return (terrainTight || parentTight);
        }
        return false;
    }

    private boolean spawnCheck(UnitType type, TilePosition here) {
        // TODO: Check if units spawn in bad spots, just returns true for now
        checkPathPoints();
        Position startCenter = new Position(pathStart.toPosition().x + 16, pathStart.toPosition().y + 16);
        Position endCenter = new Position(pathEnd.toPosition().x + 16, pathEnd.toPosition().y + 16);
        Path pathOut;
        return true;
    }

    boolean wallWalkable(TilePosition tile) {
        // Checks for any collision and inverts the return value
        if (!tile.isValid(JBWEB.game)
                || (JBWEB.mapBWEM.getMap().getArea(tile) != null && JBWEB.mapBWEM.getMap().getArea(tile) != area
                && JBWEB.mapBWEM.getMap().getArea(tile) == accessibleNeighbors.get(accessibleNeighbors.size()-1))
            || JBWEB.isReserved(tile, 1, 1) || !JBWEB.isWalkable(tile)
            || (allowLifted && JBWEB.isUsed(tile, 1, 1) != UnitType.Terran_Barracks && JBWEB.isUsed(tile, 1, 1) != UnitType.None)
            || (!allowLifted && JBWEB.isUsed(tile, 1, 1) != UnitType.None && JBWEB.isUsed(tile, 1, 1) != UnitType.Zerg_Larva)
            || (openWall && (tile).getDistance(pathEnd) - 64.0 > jpsDist / 32)){
            return false;
        }
        return true;
    }

    private void initialize() {
        // Clear failed counters
        Walls.failedPlacement = 0;
        Walls.failedAngle = 0;
        Walls.failedPath = 0;
        Walls.failedTight = 0;
        Walls.failedSpawn = 0;
        Walls.failedPower = 0;

        // Set BWAPI::Points to invalid (default constructor is None)
        centroid = Position.Invalid;
        opening = TilePosition.Invalid;
        pathStart = TilePosition.Invalid;
        pathEnd = TilePosition.Invalid;
        initialPathStart = TilePosition.Invalid;
        initialPathEnd = TilePosition.Invalid;

        // Set important terrain features
        bestWallScore = 0;
        accessibleNeighbors = area.getAccessibleNeighbors();
        chokeAngle = JBWEB.getAngle(new Pair<>(new Position(choke.getNodePosition(ChokePoint.Node.END1).toPosition().x + 4, choke.getNodePosition(ChokePoint.Node.END1).toPosition().y + 4),
                new Position(choke.getNodePosition(ChokePoint.Node.END2).toPosition().x + 4, choke.getNodePosition(ChokePoint.Node.END2).toPosition().y + 4)));

        int count = 0;
        for (UnitType rawBuilding : rawBuildings) {
            if (rawBuilding == UnitType.Protoss_Pylon) {
                count++;
            }
        }
        pylonWall = count > 1;

        creationStart = new TilePosition(choke.getCenter());
        base = !area.getBases().isEmpty() ? area.getBases().get(0) : null;
        flatRamp = JBWEB.game.isBuildable(new TilePosition(choke.getCenter()));
        closestStation = Stations.getClosestStation(new TilePosition(choke.getCenter()));

        // Check if a Pylon should be put in the wall to help the size of the Wall or away from the wall for protection
        Position p1 = choke.getNodePosition(ChokePoint.Node.END1).toPosition();
        Position p2 = choke.getNodePosition(ChokePoint.Node.END2).toPosition();
        pylonWallPiece = Math.abs(p1.x - p2.x) * 8 >= 320 || Math.abs(p1.y - p2.y) * 8 >= 256 || p1.getDistance(p2) * 8 >= 288;

        // Create a jps path for limiting BFS exploration using the distance of the jps path
        Path jpsPath = new Path();
        initializePathPoints();
        checkPathPoints();
        jpsPath.createUnitPath(new Position(pathStart), new Position(pathEnd), this);
        jpsDist = jpsPath.getDistance();

        // If we can't reach the end/start points, the Wall is likely not possible and won't be attempted
        if (!jpsPath.isReachable())
            return;

        // Create notable locations to keep Wall pieces within proximity of
        if (base != null) {
            notableLocations.add(base.getCenter());
            notableLocations.add(new Position(initialPathStart.toPosition().x + 16, initialPathStart.toPosition().y + 16));
            notableLocations.add(new Position((base.getCenter().x + initialPathStart.toPosition().x)/2, (base.getCenter().y + initialPathStart.toPosition().y)/2));
        } else {
            notableLocations.add(new Position(initialPathStart.toPosition().x + 16, initialPathStart.toPosition().y + 16));
            notableLocations.add(new Position(initialPathEnd.toPosition().x + 16, initialPathEnd.toPosition().y + 16));
        }

        // Sort all the pieces and iterate over them to find the best wall - by Hannes
        if (UnitType.Protoss_Pylon != rawBuildings.get(rawBuildings.size()-1)) {
            List<UnitType> tmpList = new ArrayList<>();
            List<Integer> indexes = new ArrayList<>();
            for (UnitType rawBuilding : rawBuildings) {
                if (rawBuilding == UnitType.Protoss_Pylon) {
                    tmpList.add(rawBuilding);
                    indexes.add(rawBuildings.indexOf(rawBuilding));
                }
            }
            for (Integer i : indexes) {
                rawBuildings.remove(i);
            }
            Collections.sort(rawBuildings);
            rawBuildings.addAll(tmpList);
        } else if (UnitType.Zerg_Hatchery != rawBuildings.get(rawBuildings.size()-1)) {
            List<UnitType> tmpList = new ArrayList<>();
            List<Integer> indexes = new ArrayList<>();
            for (UnitType rawBuilding : rawBuildings) {
                if (rawBuilding == UnitType.Zerg_Hatchery) {
                    tmpList.add(rawBuilding);
                    indexes.add(rawBuildings.indexOf(rawBuilding));
                }
            }
            for (Integer i : indexes) {
                rawBuildings.remove(i);
            }
            Collections.sort(rawBuildings);
            tmpList.addAll(rawBuildings);
            rawBuildings = tmpList;
        } else {
            Collections.sort(rawBuildings);
        }

        // If there is a base in this area, and we're creating an open wall, move creation start within 10 tiles of it
        if (openWall && base != null) {
            Position startCenter = new Position(creationStart.toPosition().x + 16, creationStart.toPosition().y + 16);
            double distBest = Double.MAX_VALUE;
            Position moveTowards = new Position((initialPathStart.toPosition().x + base.getCenter().x)/2, (initialPathStart.toPosition().y + base.getCenter().x)/2);

            // Iterate 3x3 around the current TilePosition and try to get within 5 tiles
            while (startCenter.getDistance(moveTowards) > 320.0) {
                TilePosition initialStart = creationStart;
                for (int x = initialStart.x - 1; x <= initialStart.x + 1; x++) {
                    for (int y = initialStart.y - 1; y <= initialStart.y + 1; y++) {
                        TilePosition t = new TilePosition(x, y);
                        if (!t.isValid(JBWEB.game)) {
                            continue;
                        }

                        Position p = new Position(t.toPosition().x + 16, t.toPosition().y + 16);
                        double dist = p.getDistance(moveTowards);

                        if (dist < distBest) {
                            distBest = dist;
                            creationStart = t;
                            startCenter = p;
                            movedStart = true;
                            break;
                        }
                    }
                }
            }
        }

        // If the creation start position isn't buildable, move towards the top of this area to find a buildable location
        while (openWall && !JBWEB.game.isBuildable(creationStart)) {
            double distBest = Double.MAX_VALUE;
            TilePosition initialStart = creationStart;
            for (int x = initialStart.x - 1; x <= initialStart.x + 1; x++) {
                for (int y = initialStart.y - 1; y <= initialStart.y + 1; y++) {
                    TilePosition t = new TilePosition(x, y);
                    if (!t.isValid(JBWEB.game)) {
                        continue;
                    }

                    Position p = new Position(t);
                    double dist = p.getDistance(new Position(area.getTop()));

                    if (dist < distBest) {
                        distBest = dist;
                        creationStart = t;
                        movedStart = true;
                    }
                }
            }
        }
    }

    private void initializePathPoints() {
        Pair<Position, Position> line = new Pair<>(new Position(choke.getNodePosition(ChokePoint.Node.END1).toPosition().x + 4, choke.getNodePosition(ChokePoint.Node.END1).toPosition().y + 4),
                new Position(choke.getNodePosition(ChokePoint.Node.END2).toPosition().y + 4, choke.getNodePosition(ChokePoint.Node.END2).toPosition().y + 4));
        Pair<Position, Position> perpLine = openWall ? JBWEB.perpendicularLine(line, 160.0) : JBWEB.perpendicularLine(line, 96.0);
        Position lineStart = perpLine.getFirst().getDistance(new Position(area.getTop())) > perpLine.getSecond().getDistance(new Position(area.getTop())) ? perpLine.getSecond() : perpLine.getFirst();
        Position lineEnd = perpLine.getFirst().getDistance(new Position(area.getTop())) > perpLine.getSecond().getDistance(new Position(area.getTop())) ? perpLine.getFirst() : perpLine.getSecond();
        boolean isMain = closestStation != null && closestStation.isMain();
        boolean isNatural = closestStation != null && closestStation.isNatural();

        // If it's a natural wall, path between the closest main and end of the perpendicular line
        if (isNatural) {
            Station closestMain = Stations.getClosestMainStation(new TilePosition(choke.getCenter()));
            initialPathStart = closestMain != null ? new TilePosition(JBWEB.mapBWEM.getMap().getPath(closestStation.getBWEMBase().getCenter(), closestMain.getBWEMBase().getCenter()).get(0).getCenter()) : new TilePosition(lineStart);
            initialPathEnd = new TilePosition(lineEnd);
        }

        // If it's a main wall, path between a point between the roughly the choke and the area top
        else if (isMain) {
            initialPathEnd = new TilePosition((choke.getCenter().toPosition().x + lineEnd.x)/2, (choke.getCenter().toPosition().y + lineEnd.y)/2);
            initialPathStart = new TilePosition((area.getTop().toPosition().x + lineStart.x)/2, (area.getTop().toPosition().y + lineStart.y)/2);
        }

        // Other walls
        else {
            initialPathStart = new TilePosition(lineStart);
            initialPathEnd = new TilePosition(lineEnd);
        }

        pathStart = initialPathStart;
        pathEnd = initialPathEnd;
    }

    private boolean neighbourArea(Area area) {
        for (Area subArea : area.getAccessibleNeighbors()) {
            if (area == subArea) {
                return true;
            }
        }
        return false;
    }

    private boolean notValidPathPoint(TilePosition testTile) {
        return !testTile.isValid(JBWEB.game)
                || !JBWEB.isWalkable(testTile)
                || JBWEB.isReserved(testTile, 1, 1)
                || JBWEB.isUsed(testTile, 1, 1) != UnitType.None;
    }

    private void checkPathPoints() {
        // Push the path start as far from the path end if it's not in a valid location
        double distBest = 0.0;
        if (notValidPathPoint(pathStart)) {
            for (int x = initialPathStart.x - 4; x < initialPathStart.x + 4; x++) {
                for (int y = initialPathStart.y - 4; y < initialPathStart.y + 4; y++) {
                    TilePosition t = new TilePosition(x, y);
                    double dist = t.getDistance(initialPathEnd);
                    if (notValidPathPoint(t))
                        continue;

                    if (dist > distBest) {
                        pathStart = t;
                        distBest = dist;
                    }
                }
            }
        }

        // Push the path end as far from the path start if it's not in a valid location
        distBest = 0.0;
        if (notValidPathPoint(pathEnd)) {
            for (int x = initialPathEnd.x - 4; x < initialPathEnd.x + 4; x++) {
                for (int y = initialPathEnd.y - 4; y < initialPathEnd.y + 4; y++) {
                    TilePosition t = new TilePosition(x, y);
                    double dist = t.getDistance(initialPathStart);
                    if (notValidPathPoint(t))
                        continue;

                    if (dist > distBest) {
                        pathEnd = t;
                        distBest = dist;
                    }
                }
            }
        }
    }

    private boolean nextPermutationRawBuildings(int start, int end) {
        int length = end - start + 1;

        if (length <= 1) {
            return false;
        }

        // Find the longest non-increasing suffix and find the pivot
        int last = length - 2;
        while (last >= 0) {
            // Compare UnitType string lexicographically
            if (rawBuildings.get(last).toString().compareTo(rawBuildings.get(last + 1).toString()) < 0) {
                break;
            }
            last--;
        }

        // If there is no increasing pair there is no higher order permutation
        if (last < 0) {
            return false;
        }

        int nextGreater = length - 1;

        // Find the rightmost successor to the pivot
        for (int i = length - 1; i > last; i--) {
            // Compare UnitType string lexicographically
            if (rawBuildings.get(i).toString().compareTo(rawBuildings.get(last).toString()) > 0) {
                nextGreater = i;
                break;
            }
        }

        // Swap the successor and the pivot
        int left = nextGreater;
        int right = last;
        UnitType temp = rawBuildings.get(left);
        rawBuildings.add(left, rawBuildings.get(right));
        rawBuildings.add(right, temp);

        // Reverse the sub-array starting from left to the right, both inclusive
        left = last + 1;
        right = length - 1;
        while (left < right) {
            temp = rawBuildings.get(left);
            rawBuildings.add(left++, rawBuildings.get(right));
            rawBuildings.add(right--, temp);
        }

        // Return true as the next_permutation is done
        return true;
    }

    private void addPieces() {
        // For each permutation, try to make a wall combination that is better than the current best
        do {
            currentLayout.clear();
            typeIterator = rawBuildings.listIterator();
            addNextPiece(creationStart);
        } while (JBWEB.game.self().getRace() == Race.Zerg ? nextPermutationRawBuildings(rawBuildings.indexOf(UnitType.Zerg_Hatchery), rawBuildings.size()-1)
            : nextPermutationRawBuildings(0, rawBuildings.indexOf(UnitType.Protoss_Pylon)));

        for (TilePosition tile : bestLayout.keySet()) {
            UnitType type = bestLayout.get(tile);
            addToWallPieces(tile, type);
            JBWEB.addReserve(tile, type.tileWidth(), type.tileHeight());
            JBWEB.addUsed(tile, type);
        }
    }

    private void addNextPiece(TilePosition start) {
        // Get the value without incrementing
        UnitType type = typeIterator.next();
        typeIterator.previous();
        int radius = (openWall || typeIterator == rawBuildings.iterator()) ? 8 : 4;

        for (int x = start.x - radius; x < start.x + radius; x++) {
            for (int y = start.y - radius; y < start.y + radius; y++) {
                TilePosition tile = new TilePosition(x, y);

                if (!tile.isValid(JBWEB.game)) {
                    continue;
                }

                Position center = new Position(tile.toPosition().x + type.tileWidth()*16, tile.toPosition().y + type.tileHeight()*16);
                Position closestGeo = JBWEB.getClosestChokeTile(choke, center);

                // Open walls need to be placed within proximity of notable features
                if (openWall) {
                    Position closestNotable = Position.Invalid;
                    double closestNotableDist = Double.MAX_VALUE;
                    for (Position pos : notableLocations) {
                        double dist = pos.getDistance(center);
                        if (dist < closestNotableDist) {
                            closestNotable = pos;
                            closestNotableDist = dist;
                        }
                    }
                    if (center.getDistance(closestNotable) >= 256.0 || center.getDistance(closestNotable) >= closestGeo.getDistance(closestNotable) + 48.0) {

                        continue;
                    }
                }

                // Try not to seal the wall poorly
                if (!openWall && flatRamp) {
                    double m1 = Math.min(new Position(tile).getDistance(new Position(choke.getCenter())),
                            new Position(new TilePosition(tile.toPosition().x + type.tileWidth(), tile.toPosition().y)).getDistance(new Position(choke.getCenter())));
                    double m2 = Math.min(new Position(new TilePosition(tile.toPosition().x, tile.toPosition().y + type.tileHeight())).getDistance(new Position(choke.getCenter())),
                            new Position(new TilePosition(tile.toPosition().x + type.tileWidth(), tile.toPosition().y + type.tileHeight())).getDistance(new Position(choke.getCenter())));
                    double dist = Math.min(m1, m2);
                    if (dist < 64.0) {
                        continue;
                    }
                }

                // Required checks for this wall to be valid
                if (!powerCheck(type, tile)) {
                    Walls.failedPower++;
                    continue;
                }
                if (!angleCheck(type, tile)) {
                    Walls.failedAngle++;
                    continue;
                }
                if (!placeCheck(type, tile)) {
                    Walls.failedPlacement++;
                    continue;
                }
                if (!tightCheck(type, tile)) {
                    Walls.failedTight++;
                    continue;
                }
                if (!spawnCheck(type, tile)) {
                    Walls.failedSpawn++;
                    continue;
                }

                // 1) Store the current type, increase the iterator
                currentLayout.put(tile, type);
                JBWEB.addUsed(tile, type);
                typeIterator.next();

                // 2) If at the end, score wall
                if (!typeIterator.hasNext()) {
                    scoreWall();
                } else {
                    if (openWall) {
                        addNextPiece(start);
                    } else {
                        addNextPiece(tile);
                    }
                }


                // 3) Erase this current placement and repeat
                if (typeIterator != rawBuildings.listIterator()) {
                    typeIterator.previous();
                }

                currentLayout.remove(tile);
                JBWEB.removeUsed(tile, type.tileWidth(), type.tileHeight());
            }
        }
    }

    private void addDefenses() {
        // Prevent adding defenses if we don't have a wall
        if (bestLayout.isEmpty()) {
            return;
        }

        // Find the furthest non Pylon building to the chokepoint
        double furthest = 0.0;
        for (TilePosition tile : largeTiles) {
            Position center = new Position(tile.toPosition().x + 64, tile.toPosition().y + 48);
            Position closestGeo = JBWEB.getClosestChokeTile(choke, center);
            double dist = center.getDistance(closestGeo);
            if (dist > furthest) {
                furthest = dist;
            }
        }

        for (TilePosition tile : mediumTiles) {
            Position center = new Position(tile.toPosition().x + 48, tile.toPosition().y + 32);
            Position closestGeo = JBWEB.getClosestChokeTile(choke, center);
            double dist = center.getDistance(closestGeo);
            if (dist > furthest) {
                furthest = dist;
            }
        }

        // Find the furthest Pylon building to the chokepoint if it's a Pylon wall
        if (pylonWall) {
            for (TilePosition tile : smallTiles) {
                Position center = new Position(tile.toPosition().x + 32, tile.toPosition().y + 32);
                Position closestGeo = JBWEB.getClosestChokeTile(choke, center);
                double dist = center.getDistance(closestGeo);
                if (dist > furthest)
                    furthest = dist;
            }
        }

        Station closestStation = Stations.getClosestStation(new TilePosition(choke.getCenter()));
        for (UnitType building : rawDefenses) {
            TilePosition start = new TilePosition(centroid);
            int width = building.tileWidth() * 32;
            int height = building.tileHeight() * 32;
            Position openingCenter = new Position(opening.toPosition().x + 16, opening.toPosition().y + 16);
            double arbitraryCloseMetric = JBWEB.game.self().getRace() == Race.Zerg ? 32.0 : 160.0;

            // Iterate around wall centroid to find a suitable position
            double scoreBest = Double.MAX_VALUE;
            TilePosition tileBest = TilePosition.Invalid;
            for (int x = start.x - 12; x <= start.x + 12; x++) {
                for (int y = start.y - 12; y <= start.y + 12; y++) {
                    TilePosition t = new TilePosition(x, y);
                    Position center = new Position(t.toPosition().x + width/2, t.toPosition().y + height/2);
                    Position closestGeo = JBWEB.getClosestChokeTile(choke, center);
                    boolean overlapsDefense = closestStation != null && t != closestStation.getDefenseLocations().get(closestStation.getDefenseLocations().size()-1) && t.equals(defenses.get(defenses.size()-1));

                    double dist = center.getDistance(closestGeo);
                    boolean tooClose = dist < furthest || center.getDistance(openingCenter) < arbitraryCloseMetric;
                    boolean tooFar = center.getDistance(centroid) > 200.0;

                    if (!overlapsDefense) {
                        if (!t.isValid(JBWEB.game)
                            || JBWEB.isReserved(t, building.tileWidth(), building.tileHeight())
                            || !JBWEB.isPlaceable(building, t)
                            || JBWEB.tilesWithinArea(area, t, building.tileWidth(), building.tileHeight()) == 0
                            || tooClose
                            || tooFar){
                            continue;
                        }
                    }

                    double score = dist + center.getDistance(openingCenter);

                    if (score < scoreBest) {
                        JBWEB.addUsed(t, building);
                        Path pathOut = findPathOut();
                        if ((openWall && pathOut.isReachable()) || !openWall) {
                            tileBest = t;
                            scoreBest = score;
                        }
                        JBWEB.removeUsed(t, building.tileWidth(), building.tileHeight());
                    }
                }
            }

            // If tile is valid, add to wall
            if (tileBest.isValid(JBWEB.game)) {
                defenses.add(tileBest);
                JBWEB.addReserve(tileBest, building.tileWidth(), building.tileHeight());
            }

            // Otherwise we can't place anymore
            else {
                break;
            }
        }
    }

    private void scoreWall() {
        // Create a path searching for an opening
        Path pathOut = findPathOut();

        // If we want an open wall and it's not reachable, or we want a closed wall and it is reachable
        if ((openWall && !pathOut.isReachable()) || (!openWall && pathOut.isReachable())) {
            Walls.failedPath++;
            return;
        }

        // Find distance for each piece to the closest choke tile to the path start point
        double dist = 1.0;
        Position optimalChokeTile = pathStart.getDistance(new TilePosition(choke.getNodePosition(ChokePoint.Node.END1))) <
                pathStart.getDistance(new TilePosition(choke.getNodePosition(ChokePoint.Node.END2))) ?
                new Position(choke.getNodePosition(ChokePoint.Node.END1)) : new Position(choke.getNodePosition(ChokePoint.Node.END2));
        for (TilePosition tile : currentLayout.keySet()) {
            UnitType type = currentLayout.get(tile);
            Position center = new Position(tile.toPosition().x + type.tileWidth()*16, tile.toPosition().y + type.tileHeight()*16);
            double chokeDist = optimalChokeTile.getDistance(center);
            if (type == UnitType.Protoss_Pylon && !pylonWall && !pylonWallPiece) {
                dist += -chokeDist;
            } else {
                dist += chokeDist;
            }
        }

        // Calculate current centroid if a closed wall
        Position currentCentroid = findCentroid();
        Position currentOpening = new Position(findOpening().toPosition().x + 16, findOpening().toPosition().y + 16);

        // Score wall and store if better than current best layout
        double score = !openWall ? dist : 1.0 / dist;
        if (score > bestWallScore) {
            bestLayout = currentLayout;
            bestWallScore = score;
        }
    }

    private void cleanup() {
        // Add a reserved path
        if (openWall && !bestLayout.isEmpty()) {
            Path currentPath = findPathOut();
            for (TilePosition tile : currentPath.getTiles()) {
                JBWEB.addReserve (tile, 1, 1);
            }
        }

        // Remove used from tiles
        for (TilePosition tile : smallTiles){
            JBWEB.removeUsed (tile, 2, 2);
        }
        for (TilePosition tile : mediumTiles){
            JBWEB.removeUsed (tile, 3, 2);
        }
        for (TilePosition tile : largeTiles){
            JBWEB.removeUsed (tile, 4, 3);
        }
        for (TilePosition tile : defenses){
            JBWEB.removeUsed (tile, 2, 2);
        }
    }

    /// Returns the number of ground defenses associated with this Wall.
    public int getGroundDefenseCount() {
        // Returns how many visible ground defensive structures exist in this Walls defense locations
        int count = 0;
        for (TilePosition defense : defenses) {
            UnitType type = JBWEB.isUsed(defense, 1, 1);
            if (type == UnitType.Protoss_Photon_Cannon
                    || type == UnitType.Zerg_Sunken_Colony
                    || type == UnitType.Terran_Bunker) {
                count++;
            }
        }
        return count;
    }

    /// Returns the number of air defenses associated with this Wall.
    public int getAirDefenseCount() {
        // Returns how many visible air defensive structures exist in this Walls defense locations
        int count = 0;
        for (TilePosition defense : defenses) {
            UnitType type = JBWEB.isUsed(defense, 1, 1);
            if (type == UnitType.Protoss_Photon_Cannon
                    || type == UnitType.Zerg_Spore_Colony
                    || type == UnitType.Terran_Missile_Turret) {
                count++;
            }
        }
        return count;
    }

    /// Draws all the features of the Wall.
    public void draw() {
        List<Position> anglePositions = new ArrayList<>();
        Color color = JBWEB.game.self().getColor();
        Text textColor = color.id == 185 ? Text.DarkGreen : JBWEB.game.self().getTextColor();

        // Draw boxes around each feature
        boolean drawBoxes = true;
        if (drawBoxes) {
            for (TilePosition tile : smallTiles) {
                JBWEB.game.drawBoxMap(new Position(tile), new Position(tile.toPosition().x + 65, tile.toPosition().y + 65), color);
                JBWEB.game.drawTextMap(new Position(tile.toPosition().x + 4, tile.toPosition().y + 4), "%cW", JBWEB.game.self().getTextColor());
                anglePositions.add(new Position(tile.toPosition().x + 32, tile.toPosition().y + 32));
            }
            for (TilePosition tile : mediumTiles) {
                JBWEB.game.drawBoxMap(new Position(tile), new Position(tile.toPosition().x + 97, tile.toPosition().y + 65), color);
                JBWEB.game.drawTextMap(new Position(tile.toPosition().x + 4, tile.toPosition().y + 4), "%cW", JBWEB.game.self().getTextColor());
                anglePositions.add(new Position(tile.toPosition().x + 48, tile.toPosition().y + 32));
            }
            for (TilePosition tile : largeTiles) {
                JBWEB.game.drawBoxMap(new Position(tile), new Position(tile.toPosition().x + 129, tile.toPosition().y + 97), color);
                JBWEB.game.drawTextMap(new Position(tile.toPosition().x + 4, tile.toPosition().y + 4), "%cW", JBWEB.game.self().getTextColor());
                anglePositions.add(new Position(tile.toPosition().x + 64, tile.toPosition().y + 48));
            }
            for (TilePosition tile : defenses) {
                JBWEB.game.drawBoxMap(new Position(tile), new Position(tile.toPosition().x + 65, tile.toPosition().y + 65), color);
                JBWEB.game.drawTextMap(new Position(tile.toPosition().x + 4, tile.toPosition().y + 4), "%cW", JBWEB.game.self().getTextColor());
            }
        }

        // Draw angles of each piece
        boolean drawAngles = false;
        if (drawAngles) {
            for (Position pos1 : anglePositions) {
                for (Position pos2 : anglePositions) {
                    if (pos1 == pos2) {
                        continue;
                    }

                    JBWEB.game.drawLineMap(pos1, pos2, color);
                    JBWEB.game.drawTextMap(new Position((pos1.x + pos2.x)/ 2, (pos1.y + pos2.y)/ 2), "%c%.2f", textColor);
                }
            }
        }

        // Draw opening
        JBWEB.game.drawBoxMap(new Position(opening), new Position(opening.toPosition().x + 33, opening.toPosition().y + 33), color, true);

        // Draw the line and angle of the ChokePoint
        Position p1 = choke.getNodePosition(ChokePoint.Node.END1).toPosition();
        Position p2 = choke.getNodePosition(ChokePoint.Node.END2).toPosition();
        JBWEB.game.drawTextMap(new Position(choke.getCenter()), "%c%.2f", Text.Grey);
        JBWEB.game.drawLineMap(new Position(p1), new Position(p2), Color.Grey);

        // Draw the path points
        JBWEB.game.drawCircleMap(new Position(pathStart), 6, Color.Black, true);
        JBWEB.game.drawCircleMap(new Position(pathEnd), 6, Color.White, true);
    }
}
