package atlantis.map.wall;

import atlantis.map.AMap;
import atlantis.units.select.Select;
import bwapi.*;
import bwem.BWEM;
import bwem.ChokePoint;

public class BuildingPlacementLogic {

//    private Game game;
//    private BWEM bwem;
//    private Player self;
//    private TilePosition wallInPosition;
//    private Unit builder;
//
//    public BuildingPlacementLogic(Game game, BWEM bwem, Player self) {
//        this.game = game;
//        this.bwem = bwem;
//        this.self = self;
//
//        builder = Select.ourWorkers().first().u();
//    }
//
//    public void findWallInPosition() {
//        // Get the chokepoints on the map
//        for (ChokePoint chokePoint : AMap.getMap().chokes()) {
//            // Check if the chokepoint is accessible by ground
//            if (chokePoint.isBlocked() || !chokePoint.getBlockingNeutral().isBlocking()) {
//                continue;
//            }
//
//            // Find a suitable location for the Barracks
//            TilePosition barracksTile = findSuitableTile(chokePoint, UnitType.Terran_Barracks);
//            if (barracksTile != null) {
//                // Check if we can place two Supply Depots adjacent to the Barracks
//                TilePosition supply1Tile = new TilePosition(barracksTile.getX() - 3, barracksTile.getY());
//                TilePosition supply2Tile = new TilePosition(barracksTile.getX() + 3, barracksTile.getY());
//
//                if (
//                    game.canBuildHere(supply1Tile, UnitType.Terran_Supply_Depot, builder, false) &&
//                        game.canBuildHere(supply2Tile, UnitType.Terran_Supply_Depot, builder, false)
//                ) {
//                    // Set the wall-in position
//                    wallInPosition = barracksTile;
//                    break;
//                }
//            }
//        }
//    }
//
//    public TilePosition getWallInPosition() {
//        return wallInPosition;
//    }
//
//    // Helper function to find a suitable tile for a building near a chokepoint
//    private TilePosition findSuitableTile(ChokePoint chokePoint, UnitType buildingType) {
//        for (int x = chokePoint.getX() - 5; x <= chokePoint.getX() + 5; x++) {
//            for (int y = chokePoint.getY() - 5; y <= chokePoint.getY() + 5; y++) {
//                TilePosition tile = new TilePosition(x, y);
//                if (game.canBuildHere(tile, buildingType, builder, false)) {
//                    return tile;
//                }
//            }
//        }
//        return null;
//    }
}