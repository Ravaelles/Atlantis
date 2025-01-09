package bweb;

import java.util.*;

public class Blocks {
    private static List<Block> allBlocks = new ArrayList<>();

    public static List<Block> getBlocks() { return allBlocks; }

    public static Block getClosestBlock(BWAPI.TilePosition here) {
        double distBest = Double.MAX_VALUE;
        Block bestBlock = null;
        for (Block block : allBlocks) {
            BWAPI.TilePosition tile = new BWAPI.TilePosition(block.getTilePosition().x + block.width() / 2, block.getTilePosition().y + block.height() / 2);
            int dist = here.getDistance(tile);
            if (dist < distBest) {
                distBest = dist;
                bestBlock = block;
            }
        }
        return bestBlock;
    }

    public static void eraseBlock(BWAPI.TilePosition here) {
        Iterator<Block> it = allBlocks.iterator();
        while (it.hasNext()) {
            Block block = it.next();
            if (here.x >= block.getTilePosition().x && here.x < block.getTilePosition().x + block.width() && here.y >= block.getTilePosition().y && here.y < block.getTilePosition().y + block.height()) {
                it.remove();
                return;
            }
        }
    }

    public static void draw() {
        for (Block block : allBlocks) block.draw();
    }

    // Stub methods for findBlocks, etc. Add logic as needed for your use case.
    public static void findBlocks() {
        // Placeholder: implement real block finding logic if available
        allBlocks.clear();
        // Example: add a sample block at (10,10) of size 4x4
        Map<BWAPI.TilePosition, Block.Piece> pieces = new HashMap<>();
        for (int x = 10; x < 14; x++) {
            for (int y = 10; y < 14; y++) {
                pieces.put(new BWAPI.TilePosition(x, y), Block.Piece.Small);
            }
        }
        allBlocks.add(new Block(new BWAPI.TilePosition(10, 10), pieces, 4, 4, Block.BlockType.Production));
    }
} 