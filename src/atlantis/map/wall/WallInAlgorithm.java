package atlantis.map.wall;


import atlantis.map.AMap;
import atlantis.map.position.APosition;

import java.util.*;

public class WallInAlgorithm {
    private static Set<Structure> structureSet = new HashSet<>();
    private static boolean[][] visited = new boolean[16][16];

    public static Set<Structure> wallIn(
        List<Structure> structures,
        List<Structure> placedStructures,
        APosition choke,
        APosition sTile,
        APosition eTile,
        EnemyUnitToWallFrom enemySize
    ) {
        structurePlacement(0, structures, placedStructures, choke, sTile, eTile, enemySize);

        return structureSet;
    }

    private static void structurePlacement(int depth, List<Structure> structures, List<Structure> placedStructures,
                                           APosition choke, APosition sTile, APosition eTile, EnemyUnitToWallFrom enemySize) {
        if (structures.isEmpty()) {
            // All structures placed
            checkWall(placedStructures, choke, sTile, eTile, enemySize);
            return;
        }

        for (int tx = choke.tx() - 8; tx <= choke.tx() + 7; tx++) {
            for (int ty = choke.ty() - 8; ty <= choke.ty() + 7; ty++) {
                clearVisited(visited, tx, ty);
            }
        }

        boolean isWall = checkWall(placedStructures, choke, sTile, eTile, enemySize);

        if (!isWall) {
            // Not a wall-in, generate a new structure location
            return;
        }

        for (int tx = choke.tx() - 8; tx <= choke.tx() + 7; tx++) {
            for (int ty = choke.ty() - 8; ty <= choke.ty() + 7; ty++) {
                Structure tempStructure = new Structure(tx, ty, structures.get(0).getWidth(),
                    structures.get(0).getHeight());
                if (LocationValidator.isValidLocation(tx, ty, tempStructure, depth)) {
                    List<Structure> newPlacedStructures = new ArrayList<>(placedStructures);
                    newPlacedStructures.add(tempStructure);

                    List<Structure> newStructures = new ArrayList<>(structures);
                    newStructures.remove(0);

                    structurePlacement(depth + 1, newStructures, newPlacedStructures,
                        choke, sTile, eTile, enemySize);
                }
            }
        }
    }

    private static boolean checkWall(List<Structure> placedStructures, APosition choke, APosition sTile, APosition eTile,
                                     EnemyUnitToWallFrom enemySize) {
        // Implement wall validation using either A* or flood fill
        // ...

        // Example:
        boolean isWall = isWallUsingFloodFill(placedStructures, choke, sTile, eTile);
        if (isWall) {
            structureSet.addAll(placedStructures);
            return true;
        }

        return false;
    }

    private static void clearVisited(boolean[][] visited, int tx, int ty) {
        // Assuming visited is a 2D boolean array
        // Set all elements in the specified row to false
        for (int i = 0; i < visited.length; i++) {
            visited[i][ty] = false;
        }

        // Set all elements in the specified column to false
        for (int j = 0; j < visited[0].length; j++) {
            visited[tx][j] = false;
        }
    }

    // Assuming these methods are defined elsewhere
//    private static boolean isWallUsingFloodFill(List<Structure> placedStructures, APosition choke, APosition sTile, APosition eTile) {
//        // Your implementation of flood fill wall validation
//        return false;
//    }

    // =========================================================

    //    private static boolean isWallUsingFloodFill(List<Structure> placedStructures, Tile choke, Tile sTile, Tile eTile) {
    private static boolean isWallUsingFloodFill(List<Structure> placedStructures, APosition choke, APosition sTile, APosition eTile) {
        boolean[][] visited = new boolean[AMap.getMapWidthInTiles()][AMap.getMapHeightInTiles()];

        // Mark obstacles and structures as visited
        for (Structure structure : placedStructures) {
            for (int x = structure.tx(); x < structure.tx() + structure.getWidth(); x++) {
                for (int y = structure.ty(); y < structure.ty() + structure.getHeight(); y++) {
                    visited[x][y] = true;
                }
            }
        }

//        Queue<ATile> queue = new LinkedList<>();
        Queue<APosition> queue = new LinkedList<>();
        queue.add(sTile);

        while (!queue.isEmpty()) {
//            ATile current = queue.poll();
            APosition current = queue.poll();

            if (current.equals(eTile)) {
                // End tile reached, it's a valid wall
                return true;
            }

            // Check adjacent tiles
//            for (ATile neighbor : AdjacentTiles.to(current)) {
            for (APosition neighbor : AdjacentTiles.to(current)) {
                int tx = neighbor.tx();
                int ty = neighbor.ty();

                if (isValidTile(tx, ty, AMap.getMapWidthInTiles(), AMap.getMapHeightInTiles()) && !visited[tx][ty]) {
                    visited[tx][ty] = true;
                    queue.add(neighbor);
                }
            }
        }

        // If flood fill completes without reaching the end tile, it's not a valid wall
        return false;
    }

    private static boolean isValidTile(int tx, int ty, int mapWidth, int mapHeight) {
        // Check if the tile coordinates are within the boundaries of the map
        return tx >= 0 && tx < mapWidth && ty >= 0 && ty < mapHeight;
    }
}

//    public static void main(String[] args) {
// Example usage
//        Structure structure = new Structure(3, 3);
//        ChokePoint chokePoint = new ChokePoint(); // Replace with actual implementation
//        APosition startAPosition = new Tile(); // Replace with actual implementation
//        APosition endAPosition = new Tile(); // Replace with actual implementation
//
//        wallIn(structure, chokePoint, startTile, endTile);
//
//        // Access the result from structureSet
//        for (Structure placedStructure : structureSet) {
//            System.out.println("Placed structure at (" + placedStructure.x + ", " + placedStructure.y + ")");
//        }
//    }
