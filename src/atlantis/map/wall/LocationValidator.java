package atlantis.map.wall;

import atlantis.Atlantis;
import atlantis.game.AGame;
import atlantis.map.AMap;
import atlantis.map.position.APosition;
import atlantis.units.select.Select;
import bwapi.Position;

public class LocationValidator {
    public static boolean isValidLocation(int x, int y, Structure struct, int depth) {
        boolean adjacent = false;

        for (int xTile = x; xTile < x + struct.getWidth(); xTile++) {
            for (int yTile = y; yTile < y + struct.getHeight(); yTile++) {
                // Assuming Occupied function is defined elsewhere
                if (isOccupied(xTile, yTile)) {
                    return false;
                }

                // Assuming there is a function to check if a tile is adjacent to another structure
                if (isTileAdjacentToStructure(xTile, yTile)) {
                    adjacent = true;
                }
            }
        }

        if (!adjacent && depth != 0) {
            return false;
        }

        return true;
    }

    private static boolean isOccupied(int xTile, int yTile) {
        return !Atlantis.game().isBuildable(xTile, yTile, true);
    }

    private static boolean isTileAdjacentToStructure(int xTile, int yTile) {
        APosition tile = APosition.create(xTile * 32, yTile * 32);

        if (
            !tile.left().isWalkable()
                || !tile.right().isWalkable()
                || !tile.top().isWalkable()
                || !tile.bottom().isWalkable()
        ) return true;

        return Select.all().inRadius(1, tile).notEmpty();
    }
}

//    public static void main(String[] args) {
//        // Example usage
//        Structure structure = new Structure(3, 3);
//        int x = 5;
//        int y = 5;
//        int depth = 2;
//
//        boolean isValid = isValidLocation(x, y, structure, depth);
//        System.out.println("Is the location valid? " + isValid);
//    }
