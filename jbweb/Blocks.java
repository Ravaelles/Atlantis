package jbweb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import bwapi.*;
import bwem.*;

public class Blocks {
    private static List<Block> allBlocks = new ArrayList<>();
    private static HashMap<Area, Integer> typePerArea = new HashMap<>();
    private static HashMap<Piece, Integer> mainPieces = new HashMap<>();

    private static int countPieces(List<Piece> pieces, Piece type) {
        int count = 0;
        for (Piece piece : pieces) {
        if (piece == type)
            count++;
        }
        return count;
    }

    private static List<Piece> whichPieces(int width, int height, boolean faceUp, boolean faceLeft) {
        List<Piece> pieces = new ArrayList<>();

        // Zerg Block pieces
        if (JBWEB.game.self().getRace() == Race.Zerg) {
            if (height == 2) {
                if (width == 2) {
                    pieces.add(Piece.Small);
                }
                if (width == 3) {
                    pieces.add(Piece.Medium);
                }
                if (width == 5) {
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Medium);
                }
            } else if (height == 3) {
                if (width == 4) {
                    pieces.add(Piece.Large);
                }
            } else if (height == 4) {
                if (width == 3) {
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Medium);
                }
                if (width == 5) {
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Medium);
                }
            } else if (height == 6) {
                if (width == 5) {
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Medium);
                }
            }
        }

        // Protoss Block pieces
        if (JBWEB.game.self().getRace() == Race.Protoss) {
            if (height == 2) {
                if (width == 5) {
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Medium);
                }
            } else if (height == 4) {
                if (width == 5) {
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Medium);
                }
            } else if (height == 5) {
                if (width == 4) {
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Small);
                }
                if (width == 8) {
                    if (faceLeft) {
                        if (faceUp) {
                            pieces.add(Piece.Large);
                            pieces.add(Piece.Large);
                            pieces.add(Piece.Row);
                            pieces.add(Piece.Medium);
                            pieces.add(Piece.Medium);
                            pieces.add(Piece.Small);
                        } else {
                            pieces.add(Piece.Medium);
                            pieces.add(Piece.Medium);
                            pieces.add(Piece.Small);
                            pieces.add(Piece.Row);
                            pieces.add(Piece.Large);
                            pieces.add(Piece.Large);
                        }
                    } else {
                        if (faceUp) {
                            pieces.add(Piece.Large);
                            pieces.add(Piece.Large);
                            pieces.add(Piece.Row);
                            pieces.add(Piece.Small);
                            pieces.add(Piece.Medium);
                            pieces.add(Piece.Medium);
                        } else {
                            pieces.add(Piece.Small);
                            pieces.add(Piece.Medium);
                            pieces.add(Piece.Medium);
                            pieces.add(Piece.Row);
                            pieces.add(Piece.Large);
                            pieces.add(Piece.Large);
                        }
                    }
                }
            } else if (height == 6) {
                if (width == 10) {
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Addon);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Large);
                }
                if (width == 18) {
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Addon);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Large);
                }
            } else if (height == 8) {
                if (width == 8) {
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Large);
                }
                if (width == 5) {
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Small);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Large);
                }
            }
        }

        // Terran Block pieces
        if (JBWEB.game.self().getRace() == Race.Terran) {
            if (height == 2) {
                if (width == 3) {
                    pieces.add(Piece.Medium);
                }
                if (width == 6) {
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Medium);
                }
            } else if (height == 4) {
                if (width == 3) {
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Medium);
                }
            } else if (height == 6) {
                if (width == 3) {
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Medium);
                }
            } else if (height == 3) {
                if (width == 6) {
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Addon);
                }
            } else if (height == 4) {
                if (width == 6) {
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Medium);
                }
                if (width == 9) {
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Medium);
                }
            } else if (height == 5) {
                if (width == 6) {
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Addon);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Medium);
                    pieces.add(Piece.Medium);
                }
            } else if (height == 6) {
                if (width == 6) {
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Addon);
                    pieces.add(Piece.Row);
                    pieces.add(Piece.Large);
                    pieces.add(Piece.Addon);
                }
            }
        }
        return pieces;
    }

    private static boolean canAddBlock(TilePosition here, int width, int height) {
        // Check if a block of specified size would overlap any bases, resources or other blocks
        for (int x = here.x - 1; x < here.x + width + 1; x++) {
            for (int y = here.y - 1; y < here.y + height + 1; y++) {
                TilePosition t = new TilePosition(x, y);
                if (!t.isValid(JBWEB.game) || !JBWEB.mapBWEM.getMap().getTile(t).isBuildable() || JBWEB.isReserved(t, 1, 1)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean canAddProxyBlock(TilePosition here, int width, int height) {
        // Check if a proxy block of specified size is not buildable here
        for (int x = here.x - 1; x < here.x + width + 1; x++) {
            for (int y = here.y - 1; y < here.y + height + 1; y++) {
                TilePosition t = new TilePosition(x, y);
                if (!t.isValid(JBWEB.game) || !JBWEB.mapBWEM.getMap().getTile(t).isBuildable() || !JBWEB.game.isWalkable(new WalkPosition(t))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void insertBlock(TilePosition here, List<Piece> pieces) {
        Block newBlock = new Block(here, pieces, false, false);
        allBlocks.add(newBlock);
        JBWEB.addReserve(here, newBlock.width(), newBlock.height());
    }

    private static void insertProxyBlock(TilePosition here, List<Piece> pieces) {
        Block newBlock = new Block(here, pieces, true, false);
        allBlocks.add(newBlock);
        JBWEB.addReserve(here, newBlock.width(), newBlock.height());
    }

    private static void insertDefensiveBlock(TilePosition here, List<Piece> pieces) {
        Block newBlock = new Block(here, pieces, false, true);
        allBlocks.add(newBlock);
        JBWEB.addReserve(here, newBlock.width(), newBlock.height());
    }

    private static boolean creepOnCorners(TilePosition here, int width, int height) {
        boolean b1 = JBWEB.game.hasCreep(here);
        boolean b2 = JBWEB.game.hasCreep(new TilePosition(here.x + width - 1, here.y));
        boolean b3 = JBWEB.game.hasCreep(new TilePosition(here.x, here.y + height - 1));
        boolean b4 = JBWEB.game.hasCreep(new TilePosition(here.x + width - 1, here.y + height - 1));
        return b1 && b2 && b3 && b4;
    }

    private static void searchStart(Position start) {
        TilePosition tileStart = new TilePosition(start);
        TilePosition tileBest = TilePosition.Invalid;
        double distBest = Double.MAX_VALUE;
        List<Piece> piecesBest = new ArrayList<>();

        for (int i = 10; i > 0; i--) {
            for (int j = 10; j > 0; j--) {
                // Try to find a block near our starting location
                for (int x = tileStart.x - 15; x <= tileStart.x + 15; x++) {
                    for (int y = tileStart.y - 15; y <= tileStart.y + 15; y++) {
                        TilePosition tile = new TilePosition(x, y);
                        Position blockCenter = new Position(tile.x + i*16, tile.y + j*16);
                        double dist = blockCenter.getDistance(start);
                        boolean blockFacesLeft = (blockCenter.x < JBWEB.getMainPosition().x);
                        boolean blockFacesUp = (blockCenter.y < JBWEB.getMainPosition().y);

                        // Check if we have pieces to use
                        List<Piece> pieces = whichPieces(i, j, blockFacesUp, blockFacesLeft);
                        if (pieces.isEmpty()) {
                            continue;
                        }

                        // Check if we have creep as Zerg
                        Race race = JBWEB.game.self().getRace();
                        if (race == Race.Zerg && !creepOnCorners(tile, i, j)) {
                            continue;
                        }

                        int smallCount = countPieces(pieces, Piece.Small);
                        int mediumCount = countPieces(pieces, Piece.Medium);
                        int largeCount = countPieces(pieces, Piece.Large);

                        if (!tile.isValid(JBWEB.game)
                                || mediumCount < 1
                                || (race == Race.Zerg && smallCount == 0 && mediumCount == 0)
                                || (race == Race.Protoss && largeCount < 2)
                                || (race == Race.Terran && largeCount < 1)) {
                            continue;
                        }

                        if (dist < distBest && canAddBlock(tile, i, j)) {
                            piecesBest = pieces;
                            distBest = dist;
                            tileBest = tile;
                        }
                    }
                }

                if (tileBest.isValid(JBWEB.game) && canAddBlock(tileBest, i, j)) {
                    if (JBWEB.mapBWEM.getMap().getArea(tileBest) == JBWEB.getMainArea()) {
                        for (Piece piece : piecesBest) {
                            if (mainPieces.get(piece) == null) {
                                mainPieces.put(piece, 1);
                            } else {
                                int tmp = mainPieces.get(piece) + 1;
                                mainPieces.put(piece, tmp);
                            }
                        }
                    }
                    insertBlock(tileBest, piecesBest);
                }
            }
        }
    }

    private static void findMainStartBlocks() {
        Race race = JBWEB.game.self().getRace();
        Position firstStart = JBWEB.getMainPosition();
        Position secondStart = race != Race.Zerg ? (new Position(JBWEB.getMainChoke().getCenter().x + JBWEB.getMainPosition().x/2,
                JBWEB.getMainChoke().getCenter().y + JBWEB.getMainPosition().y/2)) : JBWEB.getMainPosition();

        searchStart(firstStart);
        searchStart(secondStart);
    }

    private static void findMainDefenseBlock() {
        if (JBWEB.game.self().getRace() == Race.Zerg) {
            return;
        }

        // Added a block that allows a good shield battery placement or bunker placement
        TilePosition tileBest = TilePosition.Invalid;
        TilePosition start = new TilePosition(JBWEB.getMainChoke().getCenter());
        double distBest = Double.MAX_VALUE;
        for (int x = start.x - 12; x <= start.x + 16; x++) {
            for (int y = start.y - 12; y <= start.y + 16; y++) {
                TilePosition tile = new TilePosition(x, y);
                Position blockCenter = new Position(tile.toPosition().x + 80, tile.toPosition().y + 32);
                double dist = (blockCenter.getDistance(JBWEB.getMainChoke().getCenter().toPosition()));

                if (!tile.isValid(JBWEB.game)
                        || JBWEB.mapBWEM.getMap().getArea(tile) != JBWEB.getMainArea()
                        || dist < 96.0){
                    continue;
                }

                if (dist < distBest && canAddBlock(tile, 5, 2)) {
                    tileBest = tile;
                    distBest = dist;
                }
            }
        }

        if (tileBest.isValid(JBWEB.game)) {
            List<Piece> p = new ArrayList<>();
            p.add(Piece.Small);
            p.add(Piece.Medium);
            insertDefensiveBlock(tileBest, p);
        }
    }

    private static void findProductionBlocks() {
        // Calculate distance for each tile to our natural choke, we want to place bigger blocks closer to the chokes
        HashMap<Double, TilePosition> tilesByPathDist = new HashMap<>();
        for (int y = 0; y < JBWEB.game.mapHeight(); y++) {
            for (int x = 0; x < JBWEB.game.mapWidth(); x++) {
                TilePosition t = new TilePosition(x, y);
                if (t.isValid(JBWEB.game) && JBWEB.game.isBuildable(t)) {
                    Position p = new Position(x * 32, y * 32);
                    double dist = (JBWEB.getNaturalChoke() != null && JBWEB.game.self().getRace() != Race.Zerg) ?
                            p.getDistance(new Position(JBWEB.getNaturalChoke().getCenter())) : p.getDistance(JBWEB.getMainPosition());
                    tilesByPathDist.put(dist, t);
                }
            }
        }

        // Iterate every tile
        for (int i = 20; i > 0; i--) {
            for (int j = 20; j > 0; j--) {
                // Check if we have pieces to use
                List<Piece> pieces = whichPieces(i, j, false, false);
                if (pieces.isEmpty()) {
                    continue;
                }

                int smallCount = countPieces(pieces, Piece.Small);
                int mediumCount = countPieces(pieces, Piece.Medium);
                int largeCount = countPieces(pieces, Piece.Large);

                for (Double key : tilesByPathDist.keySet()) {
                    TilePosition tile = tilesByPathDist.get(key);

                    // Protoss caps large pieces in the main at 12 if we don't have necessary medium pieces
                    if (JBWEB.game.self().getRace() == Race.Protoss) {
                        if (mainPieces.get(Piece.Large) != null && mainPieces.get(Piece.Medium) != null) {
                            if (largeCount > 0 && JBWEB.mapBWEM.getMap().getArea(tile) == JBWEB.getMainArea() &&
                                    mainPieces.get(Piece.Large) >= 12 && mainPieces.get(Piece.Medium) < 10) {
                                continue;
                            }
                        }
                    }

                    // Zerg only need 4 medium pieces and 2 small piece
                    if (JBWEB.game.self().getRace() == Race.Zerg) {
                        if (mainPieces.get(Piece.Medium) != null && mainPieces.get(Piece.Small) != null) {
                            if ((mediumCount > 0 && mainPieces.get(Piece.Medium) >= 4) ||
                                    (smallCount > 0 && mainPieces.get(Piece.Small) >= 2)) {
                                continue;
                            }
                        }
                    }

                    // Terran only need about 20 depot spots
                    if (JBWEB.game.self().getRace() == Race.Terran) {
                        if (mainPieces.get(Piece.Medium) != null) {
                            if (mediumCount > 0 && mainPieces.get(Piece.Medium) >= 20) {
                                continue;
                            }
                        }
                    }

                    if (canAddBlock(tile, i, j)) {
                        insertBlock(tile, pieces);

                        if (JBWEB.mapBWEM.getMap().getArea(tile) == JBWEB.getMainArea()) {
                            for (Piece piece : pieces) {
                                if (mainPieces.get(piece) == null) {
                                    mainPieces.put(piece, 1);
                                } else {
                                    int tmp = mainPieces.get(piece) + 1;
                                    mainPieces.put(piece, tmp);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Check if this block is in a good area
    private static boolean goodArea(TilePosition t, List<TilePosition> enemyStartLocations, HashSet<Area> areasToAvoid) {
        for (TilePosition start : enemyStartLocations) {
            if (JBWEB.mapBWEM.getMap().getArea(t) == JBWEB.mapBWEM.getMap().getArea(start)) {
                return false;
            }
        }
        for (Area area : areasToAvoid) {
            if (JBWEB.mapBWEM.getMap().getArea(t) == area) {
                return false;
            }
        }
        return true;
    }

    // Check if there's a blocking neutral between the positions to prevent bad pathing
    private static boolean blockedPath(Position source, Position target) {
        for (ChokePoint choke : JBWEB.mapBWEM.getMap().getPath(source, target)) {
            if (JBWEB.isUsed(new TilePosition(choke.getCenter()), 1, 1) != UnitType.None){
                return true;
            }
        }
        return false;
    }

    private static void findProxyBlock() {
        // For base-specific locations, avoid all areas likely to be traversed by worker scouts
        HashSet<Area> areasToAvoid = new HashSet<>();
        for (TilePosition first : JBWEB.mapBWEM.getMap().getStartingLocations()) {
            for (TilePosition second : JBWEB.mapBWEM.getMap().getStartingLocations()) {
                if (first == second) {
                    continue;
                }

                for (ChokePoint choke : JBWEB.mapBWEM.getMap().getPath(new Position(first), new Position(second))) {
                    areasToAvoid.add(choke.getAreas().getFirst());
                    areasToAvoid.add(choke.getAreas().getSecond());
                }
            }

            // Also add any areas that neighbour each start location
            Area baseArea = JBWEB.mapBWEM.getMap().getNearestArea(first);
            areasToAvoid.addAll(baseArea.getAccessibleNeighbors());
        }

        // Gather the possible enemy start locations
        List<TilePosition> enemyStartLocations = new ArrayList<>();
        for (TilePosition start : JBWEB.mapBWEM.getMap().getStartingLocations()) {
            if (JBWEB.mapBWEM.getMap().getArea(start) != JBWEB.getMainArea()){
                enemyStartLocations.add(start);
            }
        }

        // Find the best locations
        TilePosition tileBest = TilePosition.Invalid;
        double distBest = Double.MAX_VALUE;
        for (int x = 0; x < JBWEB.game.mapWidth(); x++) {
            for (int y = 0; y < JBWEB.game.mapHeight(); y++) {
                TilePosition topLeft = new TilePosition(x, y);
                TilePosition botRight = new TilePosition(x + 8, y + 5);

                if (!topLeft.isValid(JBWEB.game)
                        || !botRight.isValid(JBWEB.game)
                        || !canAddProxyBlock(topLeft, 8, 5)) {
                    continue;
                }

                Position blockCenter = new Position(topLeft.toPosition().x + 160, topLeft.toPosition().y + 96);

                // Consider each start location
                double dist = 0.0;
                for (TilePosition base : enemyStartLocations) {
                    Position baseCenter = new Position(base.toPosition().x + 64, base.toPosition().y + 48);
                    dist += JBWEB.getGroundDistance(blockCenter, baseCenter);
                    if (blockedPath(blockCenter, baseCenter)) {
                        dist = Double.MAX_VALUE;
                        break;
                    }
                }

                // Bonus for placing in a good area
                if (goodArea(topLeft, enemyStartLocations, areasToAvoid) &&
                        goodArea(botRight, enemyStartLocations, areasToAvoid)) {
                    dist = Math.log(dist);
                }

                if (dist < distBest) {
                    distBest = dist;
                    tileBest = topLeft;
                }
            }
        }

        // Add the blocks
        if (canAddProxyBlock(tileBest, 8, 5)) {
            List<Piece> p = new ArrayList<>();
            p.add(Piece.Large);
            p.add(Piece.Large);
            p.add(Piece.Row);
            p.add(Piece.Small);
            p.add(Piece.Small);
            p.add(Piece.Small);
            p.add(Piece.Small);
            insertProxyBlock(tileBest, p);
        }
    }

    /// Erases any blocks at the specified TilePosition.
    /// <param name="here"> The TilePosition that you want to delete any BWEB::Block that exists here.
    public void eraseBlock(TilePosition here) {
        List<Block> blocksToRemove = new ArrayList<>();
        for (Block block : allBlocks) {
            if (here.x >= block.getTilePosition().x && here.x < block.getTilePosition().x + block.width() &&
                    here.y >= block.getTilePosition().y && here.y < block.getTilePosition().y + block.height()) {
                blocksToRemove.add(block);
            }
        }
        for (Block block : blocksToRemove) {
            allBlocks.remove(block);
        }
    }

    /// Initializes the building of every BWEB::Block on the map, call it only once per game.
    public static void findBlocks() {
        findMainDefenseBlock();
        findMainStartBlocks();
        findProxyBlock();
        findProductionBlocks();
    }

    /// Calls the draw function for each Block that exists.
    public static void draw() {
        for (Block block : allBlocks) {
            block.draw();
        }
    }

    /// Returns a List containing every Block.
    public static List<Block> getBlocks() {
        return allBlocks;
    }

    /// Returns the closest BWEB::Block to the given TilePosition.
    public Block getClosestBlock(TilePosition here) {
        double distBest = Double.MAX_VALUE;
        Block bestBlock = null;
        for (Block block : allBlocks) {
            TilePosition tile = new TilePosition(block.getTilePosition().x + block.width()/2, block.getTilePosition().y + block.height()/2);
            double dist = here.getDistance(tile);

            if (dist < distBest) {
                distBest = dist;
                bestBlock = block;
            }
        }
        return bestBlock;
    }
}
