package atlantis.production.constructing.position;

import atlantis.AGame;
import atlantis.Atlantis;
import atlantis.map.AChoke;
import atlantis.map.Bases;
import atlantis.map.Chokes;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.ConstructionOrder;
import atlantis.map.ABaseLocation;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.position.PositionUtil;
import atlantis.units.select.Select;
import atlantis.util.We;
import bwapi.Position;

public abstract class AbstractPositionFinder {
    
    public static String _CONDITION_THAT_FAILED = null;

    // =========================================================
    // Hi-level methods
    
    /**
     * Returns true if game says it's possible to build given building at this position.
     */
    public static boolean isForbiddenByStreetGrid(AUnit builder, AUnitType building, APosition position) {
        
        // Special buildings can be build anywhere
        if (building.isBase() || building.isGasBuilding() || building.isCombatBuilding()) {
            return false;
        }
        
        // =========================================================

        // Leave entire vertical (same tileX) corridor free for units
        if (
                position.tx() % 7 <= 1
                || (position.tx() + building.dimensionRight() / 32) % 7 <= 1
        ) {
//        System.out.println(building.shortName() + "   " + position.getTileX() + " // (" + position.getTileX() % 7 + ") // "
//                + (position.getTileX() + building.getDimensionRight() / 32) + " // (" +
//                (position.getTileX() + building.getDimensionRight() / 32) % 7 + ")");
            _CONDITION_THAT_FAILED = "LEAVE_PLACE_VERTICALLY";
            return true;
        }

        // Leave entire horizontal (same tileY) corridor free for units
        if (
                position.ty() % 7 <= 1
                || (position.ty() + building.dimensionDown() / 32) % 7 <= 0
        ) {
            _CONDITION_THAT_FAILED = "LEAVE_PLACE_HORIZONTALLY";
            return true;
        }
        
        return false;
    }
    
    /**
     * Returns true if game says it's possible to build given building at this position.
     */
    public static boolean canPhysicallyBuildHere(AUnit builder, AUnitType building, APosition position) {
        if (position == null) {
            _CONDITION_THAT_FAILED = "POSITION IS NULL";
            return false;
        }
        if (builder == null) {
            _CONDITION_THAT_FAILED = "BUILDER IS NULL";
            return false;
        }

        if (!We.zerg() && Atlantis.game().hasCreep(position.toTilePosition())) {
            _CONDITION_THAT_FAILED = "Ugly creep on it";
            return false;
        }

        _CONDITION_THAT_FAILED = "Can't physically build here";
        return Atlantis.game().canBuildHere(position.toTilePosition(), building.ut(), builder.u());
    }

    /**
     * Returns true if any other building is too close to this building or if two buildings would overlap
     * add-on place of another. Buildings can be stacked, but it needs to be done properly e.g. Supply Depots
     * could be stacked.
     */
    protected static boolean isOtherConstructionTooClose(AUnit builder, AUnitType building, Position position) {
        
        // Compare against planned construction places
        for (ConstructionOrder order : ConstructionRequests.getAllConstructionOrders()) {
            if (
                    order.notStarted()
                    && !builder.equals(order.builder())
                    && order.positionToBuild() != null
            ) {
                double distance = PositionUtil.distanceTo(order.positionToBuild(), position);
                boolean areBasesTooCloseOneToAnother = (distance <= 5 && !AGame.isPlayingAsZerg()
                        && building.isBase() && order.buildingType().isBase());

                // Look for two bases that would be built too close one to another
                if (distance <= 4 || areBasesTooCloseOneToAnother) {
                    _CONDITION_THAT_FAILED = "PLANNED BUILDING TOO CLOSE ("
                            + order.buildingType() + ", DIST: " + distance + ")";
                    return true;
                }
            }
        }

        // No collisions detected
        return false;
    }

    protected static boolean isTooCloseToMainBase(AUnitType building, APosition position) {
        if (building.isCombatBuilding()) {
            return false;
        }

        AUnit base = Select.main();

//        APainter.paintCircle(position, 10, Color.Green);
        if (base != null && base.translateByTiles(We.terran() ? 3 : 0, 0).distTo(position) <= 3.5) {
//            APainter.paintCircle(position, 10, Color.Red);
            _CONDITION_THAT_FAILED = "Too close to main base";
            return true;
        }

        return false;
    }

    protected static boolean isOverlappingBaseLocation(AUnitType building, APosition position) {
        if (building.isBase()) {
            if (Select.ourBuildingsIncludingUnfinished().bases().inRadius(10, position).isNotEmpty()) {
                _CONDITION_THAT_FAILED = "Base already exists here";
                return true;
            }

            return false;
        }

        for (ABaseLocation base : Bases.baseLocations()) {
            if (
                    !base.isStartLocation()
                    && base.translateByTiles(We.terran() ? 3 : 0, 0).distTo(position) <= 3.5
            ) {
                _CONDITION_THAT_FAILED = "Overlaps base location";
                return true;
            }
        }

        return false;
    }

    protected static boolean isTooCloseToChoke(AUnitType building, APosition position) {
        if (building.isBase() || building.isCombatBuilding()) {
            return false;
        }

        for (AChoke choke : Chokes.chokes()) {
            if (choke.width() >= 7) {
                continue;
            }

            double distToChoke = choke.center().distTo(position) - choke.width();
            if (distToChoke <= 3.3) {
                _CONDITION_THAT_FAILED = "Overlaps choke (" + distToChoke + ")";
                return true;
            }
        }

        return false;
    }

    protected static boolean isTooCloseToMineralsOrGeyser(AUnitType building, APosition position) {
        if (building.isCombatBuilding()) {
            return false;
        }

        if (Select.main() == null) {
            return false;
        }

        // We have problem only if building is both close to base and to minerals or to geyser
        AUnit nearestBase = Select.ourBases().nearestTo(position);

        if (nearestBase == null) {
            return false;
        }

        double distToBase = nearestBase.translateByTiles(2, 0).distTo(position);
        if (distToBase <= 10) {
            AUnit mineral = Select.minerals().nearestTo(position);
            if (mineral != null && mineral.distTo(position) <= 4 && distToBase <= 7.5) {
                _CONDITION_THAT_FAILED = "Too close to mineral";
                return true;
            }

            AUnit geyser = Select.geysers().nearestTo(position);
//            System.out.println("Select.geysers() = " + Select.geysers().count());
            if (geyser != null && geyser.distTo(position) <= (building.isPylon() ? 7 : (building.isSupplyUnit() ? 10 : 6))) {
                _CONDITION_THAT_FAILED = "Too close to geyser";
                return true;
            }

            AUnit gasBuilding = Select.geyserBuildings().nearestTo(position);
//            System.out.println("Select.geyserBuildings() = " + Select.geyserBuildings().count());
            if (gasBuilding != null && gasBuilding.distTo(position) <= 4 && distToBase <= 7.5) {
                _CONDITION_THAT_FAILED = "Too close to gas building";
                return true;
            }
        }

        return false;
    }

}
