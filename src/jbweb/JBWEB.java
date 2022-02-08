package jbweb;

import bwapi.*;
import bwem.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JBWEB {
    static Game game;
    static BWEM mapBWEM;
    private static Position mainPosition = Position.Invalid;
    private static Position naturalPosition = Position.Invalid;
    private static TilePosition mainTile = TilePosition.Invalid;
    private static TilePosition naturalTile = TilePosition.Invalid;
    private static Area naturalArea = null;
    private static Area mainArea = null;
    private static ChokePoint naturalChoke = null;
    private static ChokePoint mainChoke = null;

    private static boolean drawReserveOverlap, drawUsed, drawWalk, drawArea;

    private static HashMap<Key, Boolean> lastKeyState = new HashMap<>();
    private static HashMap<ChokePoint, List<TilePosition>> chokeTiles = new HashMap<>();
    private static HashMap<ChokePoint, Pair<Position, Position>> chokeLines = new HashMap<>();

    private static int overlapGrid[][] = new int[256][256];
    private static UnitType usedGrid[][] = new UnitType[256][256];
    static boolean walkGrid[][] = new boolean[256][256];
    private static final boolean logInfo = true;

    private static void findLines() {
        for (Area area : mapBWEM.getMap().getAreas()) {
            for (ChokePoint choke : area.getChokePoints()) {
                Position p1, p2;
                int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
                double sumX = 0, sumY = 0;
                double sumXY = 0, sumX2 = 0, sumY2 = 0;

                for (WalkPosition geo : choke.getGeometry()) {
                    if (geo.x < minX) minX = geo.x;
                    if (geo.y < minY) minY = geo.y;
                    if (geo.x > maxX) maxX = geo.x;
                    if (geo.y > maxY) maxY = geo.y;

                    sumX += geo.x;
                    sumY += geo.y;
                    sumXY += geo.x * geo.y;
                    sumX2 += geo.x * geo.x;
                    sumY2 += geo.y * geo.y;
                }

                double size = choke.getGeometry().size();
                double xMean = sumX / size;
                double yMean = sumY / size;
                double denominator, slope, yInt;
                if ((maxY - minY) > (maxX - minX)) {
                    denominator = (sumXY - sumY * xMean);

                    // Handle vertical line error
                    if (Math.abs(denominator) < 16000.0) {
                        slope = 0;
                        yInt = yMean;
                    } else {
                        slope = (sumY2 - sumY * yMean) / denominator;
                        yInt = yMean - slope * xMean;
                    }
                } else {
                    denominator = sumX2 - sumX * xMean;

                    // Handle vertical line error
                    if (Math.abs(denominator) < 1.0) {
                        slope = Double.MAX_VALUE;
                        yInt = yMean;
                    } else {
                        slope = (sumXY - sumX * yMean) / denominator;
                        yInt = yMean - slope * xMean;
                    }
                }

                int x1 = new Position(choke.getNodePosition(ChokePoint.Node.END1)).x;
                int y1 = (int) (Math.round((double) x1 * slope)) + (int) (Math.round(yInt));
                p1 = new Position(x1, y1);

                int x2 = new Position(choke.getNodePosition(ChokePoint.Node.END2)).x;
                int y2 = (int) (Math.round((double) x2 * slope)) + (int) (Math.round(yInt));
                p2 = new Position(x2, y2);

                // In case we failed
                if (p1 == p2 || !p1.isValid(game) || !p2.isValid(game)) {
                    p1 = new Position(choke.getNodePosition(ChokePoint.Node.END1));
                    p2 = new Position(choke.getNodePosition(ChokePoint.Node.END2));
                }

                chokeLines.put(choke, new Pair<>(p1, p2));
            }
        }
    }

    private static void findMain() {
        mainTile = game.self().getStartLocation();
        mainPosition = new Position(mainTile.toPosition().x + 64, mainTile.toPosition().y + 48);
        mainArea = mapBWEM.getMap().getArea(mainTile);
    }

    private static void findNatural() {
        double distBest = Double.MAX_VALUE;
        for (Area area : mapBWEM.getMap().getAreas()) {
            for (Base base : area.getBases()) {
                // Must have gas, be accesible and at least 5 mineral patches
                if (base.isStartingLocation()
                        || base.getGeysers().isEmpty()
                        || area.getAccessibleNeighbors().isEmpty()
                        || base.getMinerals().size() < 5)
                    continue;

                double dist = getGroundDistance(base.getCenter(), mainPosition);

                if (dist < distBest) {
                    distBest = dist;
                    naturalArea = base.getArea();
                    naturalTile = base.getLocation();
                    naturalPosition = new Position(naturalTile.toPosition().x + 64, naturalTile.toPosition().y + 48);
                }
            }
        }
        if (naturalArea == null) {
            naturalArea = mainArea;
        }
    }

    private static void findMainChoke() {
        // Add all main chokes to a set
        List<ChokePoint> mainChokes = new ArrayList<>(mainArea.getChokePoints());

        if (mainChokes.size() == 1) {
            mainChoke = mainChokes.iterator().next();
            return;
        }

        // Add all natural chokes to a set
        List<ChokePoint> naturalChokes = new ArrayList<>(naturalArea.getChokePoints());

        // If the natural area has only one chokepoint, then our main choke leads out of our base, find a choke that doesn't belong to the natural as well
        if (naturalArea != null && naturalArea.getChokePoints().size() == 1) {
            double distBest = Double.MAX_VALUE;
            for (ChokePoint choke : mainArea.getChokePoints()) {
                double dist = getGroundDistance(choke.getCenter().toPosition(), mainPosition);
                if (dist < distBest && findNode(naturalChokes, choke) == naturalChokes.get(naturalChokes.size()-1)) {
                    mainChoke = choke;
                    distBest = dist;
                }
            }
        }

        // Find a chokepoint that belongs to main and natural areas
        else if (naturalArea != null) {
            double distBest = Double.MAX_VALUE;
            for (ChokePoint choke : naturalArea.getChokePoints()) {
                double dist = getGroundDistance(choke.getCenter().toPosition(), mainPosition);
                if (dist < distBest && findNode(mainChokes, choke) != mainChokes.get(mainChokes.size()-1)) {
                    mainChoke = choke;
                    distBest = dist;
                }
            }
        }

        // If we didn't find a main choke that belongs to main and natural, check if a path exists between both positions
        if (mainChoke == null && mainPosition.isValid(game) && naturalPosition.isValid(game)) {
            double distBest = Double.MAX_VALUE;
            for (ChokePoint choke : mapBWEM.getMap().getPath(mainPosition, naturalPosition)) {
                double width = choke.getNodePosition(ChokePoint.Node.END1).getDistance(choke.getNodePosition(ChokePoint.Node.END2));
                if (width < distBest) {
                    mainChoke = choke;
                    distBest = width;
                }
            }
        }

        // If we still don't have a main choke, grab the closest chokepoint to our start
        if (mainChoke == null) {
            double distBest = Double.MAX_VALUE;
            for (ChokePoint choke : mainArea.getChokePoints()) {
                double dist = choke.getCenter().getDistance(mainPosition.toWalkPosition());
                if (dist < distBest) {
                    mainChoke = choke;
                    distBest = dist;
                }
            }
        }
    }

    private static void findNaturalChoke() {
        if (!naturalPosition.isValid(game)) {
            naturalChoke = mainChoke;
            return;
        }

        List<ChokePoint> nonChokes = new ArrayList<>();
        for (ChokePoint choke : mapBWEM.getMap().getPath(mainPosition, naturalPosition)) {
            nonChokes.add(choke);
        }

        // If the natural area has only one chokepoint, then choose as that
        if (naturalArea != null && naturalArea.getChokePoints().size() == 1) {
            naturalChoke = naturalArea.getChokePoints().get(0);
        }

        // Find area that shares the choke we need to defend
        else {
            double distBest = Double.MAX_VALUE;
            Area second = null;
            if (naturalArea != null) {
                for (Area area : naturalArea.getAccessibleNeighbors()) {
                    WalkPosition center = area.getTop();
                    double dist = center.getDistance(mapBWEM.getMap().getCenter().toWalkPosition());

                    boolean wrongArea = false;
                    for (ChokePoint choke : area.getChokePoints()) {
                        if ((!choke.isBlocked() && choke.getNodePosition(ChokePoint.Node.END1).getDistance(choke.getNodePosition(ChokePoint.Node.END2)) <= 2)
                                || findNode(nonChokes, choke) != nonChokes.get(nonChokes.size()-1)) {
                            wrongArea = true;
                        }
                    }
                    if (wrongArea) {
                        continue;
                    }

                    if (center.isValid(game) && dist < distBest) {
                        second = area;
                        distBest = dist;
                    }
                }

                // Find second choke based on the connected area
                distBest = Double.MAX_VALUE;
                for (ChokePoint choke : naturalArea.getChokePoints()) {
                    if (choke.getCenter() == mainChoke.getCenter()
                            || choke.isBlocked()
                            || choke.getGeometry().size() <= 3
                            || (choke.getAreas().getFirst() != second && choke.getAreas().getSecond() != second)) {
                        continue;
                    }

                    double dist = choke.getCenter().getDistance(game.self().getStartLocation().toWalkPosition());
                    if (dist < distBest) {
                        naturalChoke = choke;
                        distBest = dist;
                    }
                }
            }
        }
    }

    private static void findNeutrals() {
        // Add overlap for neutrals
        for (Unit unit : game.getNeutralUnits()) {
            if (unit != null && unit.exists() && unit.getType().topSpeed() == 0.0)
                addReserve(unit.getTilePosition(), unit.getType().tileWidth(), unit.getType().tileHeight());
            if (unit.getType().isBuilding())
                addUsed(unit.getTilePosition(), unit.getType());
        }
    }

    /// Writes to the log.txt file
    public static void easyWrite(String stuff) {
        if (logInfo) {
            try {
                FileWriter writeFile = new FileWriter("bwapi-data/write/BWEB_Log.txt");
                writeFile.write(stuff);
                writeFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static ChokePoint findNode(List<ChokePoint> treeSet, ChokePoint choke) {
        for (ChokePoint cp : treeSet) {
            if (cp == choke) return cp;
        }
        return null;
    }

    /// Draws all JBWEB::Walls, JBWEB::Stations, and JBWEB::Blocks when called. Call this every frame if you need debugging information.
    public static void draw() {
        WalkPosition mouse = new WalkPosition(game.getMousePosition().x + game.getScreenPosition().x,
                game.getMousePosition().y + game.getScreenPosition().y);
        Area mouseArea = mouse.isValid(game) ? mapBWEM.getMap().getArea(mouse) : null;
        boolean k1 = game.getKeyState(Key.K_1);
        boolean k2 = game.getKeyState(Key.K_2);
        boolean k3 = game.getKeyState(Key.K_3);
        boolean k4 = game.getKeyState(Key.K_4);

        drawReserveOverlap = (k1 && k1 != lastKeyState.get(Key.K_1)) != drawReserveOverlap;
        drawUsed = (k2 && k2 != lastKeyState.get(Key.K_2)) != drawUsed;
        drawWalk = (k3 && k3 != lastKeyState.get(Key.K_3)) != drawWalk;
        drawArea = (k4 && k4 != lastKeyState.get(Key.K_4)) != drawArea;

        lastKeyState.put(Key.K_1, k1);
        lastKeyState.put(Key.K_2, k2);
        lastKeyState.put(Key.K_3, k3);
        lastKeyState.put(Key.K_4, k4);

        // Detect a keypress for drawing information
        if (drawReserveOverlap || drawUsed || drawWalk || drawArea) {

            for (int x = 0; x < game.mapWidth(); x++) {
                for (int y = 0; y < game.mapHeight(); y++) {
                    TilePosition t = new TilePosition(x, y);

                    // Draw boxes around TilePositions that are reserved or overlapping important map features
                    if (drawReserveOverlap) {
                        if (overlapGrid[x][y] >= 1) {
                            Position leftTop = new Position(t.toPosition().x + 4, t.toPosition().y + 4);
                            Position rightBottom = new Position(t.toPosition().x + 29, t.toPosition().y + 29);
                            game.drawBoxMap(leftTop, rightBottom, Color.Grey, false);
                        }
                    }

                    // Draw boxes around TilePositions that are used
                    if (drawUsed) {
                        UnitType type = usedGrid[x][y];
                        if (type != UnitType.None) {
                            Position leftTop = new Position(t.toPosition().x + 8, t.toPosition().y + 8);
                            Position rightBottom = new Position(t.toPosition().x + 25, t.toPosition().y + 25);
                            game.drawBoxMap(leftTop, rightBottom, Color.Black, true);
                        }
                    }

                    // Draw boxes around fully walkable TilePositions
                    if (drawWalk) {
                        if (walkGrid[x][y]) {
                            Position leftTop = new Position(t);
                            Position rightBottom = new Position(t.toPosition().x + 33, t.toPosition().y + 33);
                            game.drawBoxMap(leftTop, rightBottom, Color.Black, false);
                        }
                    }

                    // Draw boxes around any TilePosition that shares an Area with mouses current Area
                    if (drawArea) {
                        if (mapBWEM.getMap().getArea(t) == mouseArea) {
                            Position leftTop = new Position(t);
                            Position rightBottom = new Position(t.toPosition().x + 33, t.toPosition().y + 33);
                            game.drawBoxMap(leftTop, rightBottom, Color.Green, false);
                        }
                    }
                }
            }
        }

        Walls.draw();
        Blocks.draw();
        Stations.draw();
    }

    /// Called on game start to initialize JBWEB
    public static void onStart(Game _game, BWEM _mapBWEM) {
        game = _game;
        mapBWEM = _mapBWEM;
        // Initializes usedGrid and walkGrid
        for (int x = 0; x < game.mapWidth(); x++) {
            for (int y = 0; y < game.mapHeight(); y++) {
                usedGrid[x][y] = UnitType.None;

                int cnt = 0;
                for (int dx = x * 4; dx < (x * 4) + 4; dx++) {
                    for (int dy = y * 4; dy < (y * 4) + 4; dy++) {
                        WalkPosition w = new WalkPosition(dx, dy);
                        if (w.isValid(game) && game.isWalkable(w))
                            cnt++;
                    }
                }

                if (cnt >= 14)
                    walkGrid[x][y] = true;
            }
        }

        for (Unit gas : game.getGeysers()) {
            for (int x = gas.getTilePosition().x; x < gas.getTilePosition().x + 4; x++) {
                for (int y = gas.getTilePosition().y; y < gas.getTilePosition().y + 2; y++) {
                    walkGrid[x][y] = false;
                }
            }
        }

        for (Area area : mapBWEM.getMap().getAreas()){
            for (ChokePoint choke : area.getChokePoints()){
                List<TilePosition> tileGeography = new ArrayList<>();
                for (WalkPosition geo : choke.getGeometry()) {
                    tileGeography.add(geo.toTilePosition());
                }
                chokeTiles.put(choke, tileGeography);
            }
        }

        findNeutrals();
        findMain();
        findNatural();
        findMainChoke();
        findNaturalChoke();
        findLines();
    }

    /// Stores used tiles if it is a building. Increments defense counters for any stations where the placed building is a static defense unit.
    public static void onUnitDiscover(Unit unit) {
        TilePosition tile = unit.getTilePosition();
        UnitType type = unit.getType();

        boolean gameStart = game.getFrameCount() == 0;
        boolean okayToAdd = (unit.getType().isBuilding() && !unit.isFlying())
            || (gameStart && unit.getType().topSpeed() == 0.0);

        // Add used tiles
        if (okayToAdd) {
            for (int x = tile.x; x < tile.x + type.tileWidth(); x++) {
                for (int y = tile.y; y < tile.y + type.tileHeight(); y++) {
                    TilePosition t = new TilePosition(x, y);
                    if (!t.isValid(game))
                        continue;
                    usedGrid[x][y] = type;
                }
            }

            // Clear pathfinding cache
            Pathfinding.clearCache();
        }
    }

    /// Removes used tiles if it is a building. Decrements defense counters for any stations where the destroyed building is a static defense unit.
    public static void onUnitDestroy(Unit unit) {
        TilePosition tile = unit.getTilePosition();
        UnitType type = unit.getType();

        boolean gameStart = game.getFrameCount() == 0;
        boolean okayToRemove = (unit.getType().isBuilding() && !unit.isFlying())
            || (!gameStart && unit.getType().topSpeed() == 0.0);

        // Add used tiles
        if (okayToRemove) {
            for (int x = tile.x; x < tile.x + type.tileWidth(); x++) {
                for (int y = tile.y; y < tile.y + type.tileHeight(); y++) {
                    TilePosition t = new TilePosition(x, y);
                    if (!t.isValid(game))
                        continue;
                    usedGrid[x][y] = UnitType.None;
                }
            }

            // Clear pathfinding cache
            Pathfinding.clearCache();
        }
    }

    ///  Calls JBWEB::onUnitDiscover.
    public static void onUnitMorph(Unit unit) {
        onUnitDiscover(unit);
    }

    /// Adds a section of BWAPI::TilePositions to the BWEB overlap grid.
    public static void addReserve(TilePosition t, int w, int h) {
        for (int x = t.x; x < t.x + w; x++) {
            for (int y = t.y; y < t.y + h; y++) {
                TilePosition t2 = new TilePosition(x, y);
                if (t2.isValid(game)) {
                    overlapGrid[x][y] = 1;
                }
            }
        }
    }

    /// Removes a section of BWAPI::TilePositions from the BWEB overlap grid.
    public static void removeReserve(TilePosition t, int w, int h) {
        for (int x = t.x; x < t.x + w; x++) {
            for (int y = t.y; y < t.y + h; y++) {
                TilePosition t2 = new TilePosition(x, y);
                if (t2.isValid(game)) {
                    overlapGrid[x][y] = 0;
                }
            }
        }
    }

    /// Returns true if a section of BWAPI::TilePositions are within BWEBs overlap grid.
    public static boolean isReserved(TilePosition here, int width, int height) {
        for (int x = here.x; x < here.x + width; x++) {
            for (int y = here.y; y < here.y + height; y++) {
                TilePosition t = new TilePosition(x, y);
                if (!t.isValid(game)) {
                    continue;
                }
                if (overlapGrid[x][y] > 0){
                    return true;
                }
            }
        }
        return false;
    }

    /// Adds a section of BWAPI::TilePositions to the BWEB used grid.
    public static void addUsed(TilePosition t, UnitType type) {
        for (int x = t.x; x < t.x + type.tileWidth(); x++) {
            for (int y = t.y; y < t.y + type.tileHeight(); y++)
                if (new TilePosition(x, y).isValid(game))
                    usedGrid[x][y] = type;
        }
    }

    /// Removes a section of BWAPI::TilePositions from the BWEB used grid.
    public static void removeUsed(TilePosition t, int w, int h) {
        for (int x = t.x; x < t.x + w; x++) {
            for (int y = t.y; y < t.y + h; y++) {
                TilePosition t2 = new TilePosition(x, y);
                if (t2.isValid(game)) {
                    usedGrid[x][y] = UnitType.None;
                }
            }
        }
    }

    /// Returns the first UnitType found in a section of BWAPI::TilePositions, if it is within BWEBs used grid.
    /// <param name="tile"> The BWAPI::TilePosition you want to check.
    /// <param name="width"> The width of BWAPI::TilePositions to check. Default should be 1.
    /// <param name="height"> The height of BWAPI::TilePositions to check. Default should be 1.
    public static UnitType isUsed(TilePosition here, int width, int height) {
        for (int x = here.x; x < here.x + width; x++) {
            for (int y = here.y; y < here.y + height; y++) {
                TilePosition t = new TilePosition(x, y);
                if (!t.isValid(game)) {
                    continue;
                }
                if (usedGrid[x][y] != UnitType.None)
                return usedGrid[x][y];
            }
        }
        return UnitType.None;
    }

    /// Returns true if a BWAPI::TilePosition is fully walkable.
    /// <param name="tile"> The BWAPI::TilePosition you want to check.
    public static boolean isWalkable(TilePosition here) {
        return walkGrid[here.x][here.y];
    }

    /// Returns true if the given BWAPI::UnitType is placeable at the given BWAPI::TilePosition.
    /// <param name="type"> The BWAPI::UnitType of the structure you want to build.
    /// <param name="tile"> The BWAPI::TilePosition you want to build on.
    public static boolean isPlaceable(UnitType type, TilePosition location) {
        if (type.requiresCreep()) {
            for (int x = location.x; x < location.x + type.tileWidth(); x++) {
                TilePosition creepTile = new TilePosition(x, location.y + type.tileHeight());
                if (!game.isBuildable(creepTile))
                    return false;
            }
        }

        if (type.isResourceDepot() && !game.canBuildHere(location, type)) {
            return false;
        }

        for (int x = location.x; x < location.x + type.tileWidth(); x++) {
            for (int y = location.y; y < location.y + type.tileHeight(); y++) {

                TilePosition tile = new TilePosition(x, y);
                if (!tile.isValid(game)
                        || !game.isBuildable(tile)
                        || !game.isWalkable(tile.toWalkPosition())
                        || isUsed(tile, 1, 1) != UnitType.None) {
                    return false;
                }
            }
        }
        return true;
    }

    /// Returns how many BWAPI::TilePosition are within a BWEM::Area.
    /// <param name="area"> The BWEM::Area to check.
    /// <param name="tile"> The BWAPI::TilePosition to check.
    /// <param name="width"> The width of BWAPI::TilePositions to check. Default should be 1.
    /// <param name="height"> The height of BWAPI::TilePositions to check. Default should be 1.
    public static int tilesWithinArea(Area area, TilePosition here, int width, int height) {
        int cnt = 0;
        for (int x = here.x; x < here.x + width; x++) {
            for (int y = here.y; y < here.y + height; y++) {
                TilePosition t = new TilePosition(x, y);
                if (!t.isValid(game)) {
                    return 0;
                }

                if (mapBWEM.getMap().getArea(t) == area) {
                    cnt++;
                }
            }
        }
        return cnt;
    }

    private static Position validatePoint(WalkPosition w) {
        double distBest = 0.0;
        Position posBest = new Position(w);
        for (int x = w.x - 1; x < w.x + 1; x++) {
            for (int y = w.y - 1; y < w.y + 1; y++) {
                WalkPosition w2 = new WalkPosition(x, y);
                if (!w.isValid(game) || mapBWEM.getMap().getArea(w2) == null) {
                    continue;
                }

                Position p = new Position(w);
                double dist = p.getDistance(mapBWEM.getMap().getCenter());
                if (dist > distBest) {
                    distBest = dist;
                    posBest = p;
                }
            }
        }
        return posBest;
    }

    private static Position fastClosestNode(ChokePoint cp, Position last) {
        Position n1 = new Position(cp.getNodePosition(ChokePoint.Node.END1));
        Position n2 = new Position(cp.getNodePosition(ChokePoint.Node.END2));
        Position n3 = new Position(cp.getCenter());

        double d1 = n1.getDistance(last);
        double d2 = n2.getDistance(last);
        double d3 = n3.getDistance(last);

        return d1 < d2 ? (d1 < d3 ? n1 : n3) : (d2 < d3 ? n2 : n3);
    }

    // Find the closest chokepoint node
    private static Position accurateClosestNode(ChokePoint cp, Position start) {
        return getClosestChokeTile(cp, start);
    }

    /// Returns the estimated ground distance from one Position type to another Position type.
    /// <param name="start"> The first Position.
    /// <param name="end"> The second Position.
    public static double getGroundDistance(Position s, Position e) {
        Position start = new Position(s);
        Position end = new Position(e);
        double dist = 0.0;
        Position last = start;

        // Return DBL_MAX if not valid path points or not walkable path points
        if (!start.isValid(game) || !end.isValid(game)) {
            return Double.MAX_VALUE;
        }

        // Check if we're in a valid area, if not try to find a different nearby WalkPosition
        if (mapBWEM.getMap().getArea(new WalkPosition(start)) == null) {
            start = validatePoint(new WalkPosition(start));
        }
        if (mapBWEM.getMap().getArea(new WalkPosition(end)) == null) {
            end = validatePoint(new WalkPosition(end));
        }

        // If not valid still, return DBL_MAX
        if (!start.isValid(game) || !end.isValid(game) || mapBWEM.getMap().getArea(new WalkPosition(start)) == null ||
                mapBWEM.getMap().getArea(new WalkPosition(end)) == null ||
                !mapBWEM.getMap().getArea(new WalkPosition(start)).isAccessibleFrom((mapBWEM.getMap().getArea(new WalkPosition(end))))) {
            return Double.MAX_VALUE;
        }

        // For each chokepoint, add the distance to the closest chokepoint node
        boolean first = true;
        for (ChokePoint cpp : mapBWEM.getMap().getPath(start, end)) {
            boolean large = cpp.getNodePosition(ChokePoint.Node.END1).getDistance(cpp.getNodePosition(ChokePoint.Node.END2)) > 40;
            Position next = first && !large ? accurateClosestNode(cpp, start) : fastClosestNode(cpp, last);
            dist += next.getDistance(last);
            last = next;
            first = false;
        }

        return dist + last.getDistance(end);
    }

    /// Returns the angle of a pair of BWAPI::Point in degrees.
    public static double getAngle(Pair<Position, Position> p) {
        Position left = p.getFirst().x < p.getSecond().x ? p.getFirst() : p.getSecond();
        Position right = left == p.getFirst() ? p.getSecond() : p.getFirst();
        double dy = left.y - right.y;
        double dx = left.x - right.x;
        return (Math.abs(dx) > 1.0 ? Math.atan(dy / dx) * 180.0 / 3.14 : 90.0);
    }

    /// Returns the closest BWAPI::Position that makes up the geometry of a BWEM::ChokePoint to another BWAPI::Position.
    public static Position getClosestChokeTile(ChokePoint choke, Position here) {
        double best = Double.MAX_VALUE;
        Position posBest = Position.Invalid;
        for (TilePosition tile : getChokeTiles(choke)) {
            Position p = new Position(tile.toPosition().x + 16, tile.toPosition().y + 16);
            double dist = p.getDistance(here);
            if (dist < best) {
                posBest = p;
                best = dist;
            }
        }
        return posBest;
    }

    /// Returns a set of BWAPI::TilePositions that make up the geometry of a BWEM::ChokePoint.
    public static List<TilePosition> getChokeTiles(ChokePoint choke) {
        if (choke != null) {
            return chokeTiles.get(choke);
        }
        return null;
    }

    /// Returns two BWAPI::Positions representing a line of best fit for a given BWEM::ChokePoint.
    public static Pair<Position, Position> lineOfBestFit(ChokePoint choke) {
        if (choke != null) {
            return chokeLines.get(choke);
        }
        return null;
    }

    /// Returns two BWAPI::Positions perpendicular to a line at a given distance away in pixels.
    public static Pair<Position, Position> perpendicularLine(Pair<Position, Position> points, double length) {
        Position n1 = points.getFirst();
        Position n2 = points.getSecond();
        double dist = n1.getDistance(n2);
        int dx1 = (int) ((n2.x - n1.x) * length / dist);
        int dy1 = (int) ((n2.y - n1.y) * length / dist);
        int dx2 = (int) ((n1.x - n2.x) * length / dist);
        int dy2 = (int) ((n1.y - n2.y) * length / dist);
        int x1 = (int) ((n1.x + n2.x)/2);
        int y1 = (int) ((n1.y + n2.y)/2);
        Position direction1 = new Position(-dy1 + x1, dx1 + y1);
        Position direction2 = new Position(-dy2 + x1, dx2 + y1);
        return new Pair<>(direction1, direction2);
    }

    /// Returns the closest buildable BWAPI::TilePosition for any type of structure.
    /// <param name="type"> The BWAPI::UnitType of the structure you want to build.
    /// <param name="tile"> The BWAPI::TilePosition you want to build closest to.
    public static TilePosition getBuildPosition(UnitType type, TilePosition searchCenter) {
        double distBest = Double.MAX_VALUE;
        TilePosition tileBest = TilePosition.Invalid;

        // Search through each block to find the closest block and valid position
        for (Block block : Blocks.getBlocks()) {
            List<TilePosition> placements;

            if (type.tileWidth() == 4) {
                placements = block.getLargeTiles();
            } else if (type.tileWidth() == 3) {
                placements = block.getMediumTiles();
            } else {
                placements = block.getSmallTiles();
            }

            for (TilePosition tile : placements) {
                double dist = tile.getDistance(searchCenter);
                if (dist < distBest && isPlaceable(type, tile)) {
                    distBest = dist;
                    tileBest = tile;
                }
            }
        }
        return tileBest;
    }

    /// Returns the closest buildable BWAPI::TilePosition for a defensive structure.
    /// <param name="type"> The BWAPI::UnitType of the structure you want to build.
    /// <param name="tile"> The BWAPI::TilePosition you want to build closest to.
    public static TilePosition getDefBuildPosition(UnitType type, TilePosition searchCenter) {
        double distBest = Double.MAX_VALUE;
        TilePosition tileBest = TilePosition.Invalid;

        // Search through each wall to find the closest valid TilePosition
        for (ChokePoint chokePoint : Walls.getWalls().keySet()) {
            Wall wall = Walls.getWalls().get(chokePoint);
            for (TilePosition tile : wall.getDefenses()) {
                double dist = tile.getDistance(searchCenter);
                if (dist < distBest && isPlaceable(type, tile)) {
                    distBest = dist;
                    tileBest = tile;
                }
            }
        }

        // Search through each station to find the closest valid TilePosition
        for (Station station : Stations.getStations()) {
            for (TilePosition tile : station.getDefenseLocations()) {
                double dist = tile.getDistance(searchCenter);
                if (dist < distBest && isPlaceable(type, tile)) {
                    distBest = dist;
                    tileBest = tile;
                }
            }
        }
        return tileBest;
    }

    /// Returns the BWEM::Area of the natural expansion.
    public static Area getNaturalArea() {
        return naturalArea;
    }

    /// Returns the BWEM::Area of the main.
    public static Area getMainArea() {
        return mainArea;
    }

    /// Returns the BWEM::Chokepoint of the natural.
    public static ChokePoint getNaturalChoke() {
        return naturalChoke;
    }

    /// Returns the BWEM::Chokepoint of the main.
    public static ChokePoint getMainChoke() {
        return mainChoke;
    }

    /// Returns the BWAPI::TilePosition of the natural expansion.
    public static TilePosition getNaturalTile() {
        return naturalTile;
    }

    /// Returns the BWAPI::Position of the natural expansion.
    public static Position getNaturalPosition() {
        return naturalPosition;
    }

    /// Returns the BWAPI::TilePosition of the main.
    public static TilePosition getMainTile() {
        return mainTile;
    }

    /// Returns the BWAPI::Position of the main.
    public static Position getMainPosition() {
        return mainPosition;
    }
}
