package atlantis.production;

import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.A;
import atlantis.wrappers.ATech;
import bwapi.TechType;
import bwapi.UpgradeType;

public class Requirements {

    public static boolean hasRequirements(ProductionOrder order) {
        if (order.unit() != null) {
            return !order.unit().hasRequiredUnit() || hasRequirements(order.unit());
        }
        else if (order.tech() != null) {
            return hasRequirements(order.tech());
        }
        else if (order.upgrade() != null) {
            return hasRequirements(order.upgrade());
        }
        else if (order.mission() != null) {
            return A.supplyUsed() >= order.minSupply();
        }
        System.err.println(order);
        throw new RuntimeException("Shouldn't reach here");
    }

    public static boolean hasRequirements(AUnitType type) {
        if (type == null) {
            return true;
        }

        if (type.hasRequiredUnit() && Count.ofType(type.getWhatIsRequired()) == 0) {
            return false;
        }

        TechType techType = type.getRequiredTech();
        return techType == null || techType == TechType.None || ATech.isResearched(techType);
    }

    // =========================================================

    private static boolean hasRequirements(TechType tech) {

        if (Count.ofType(AUnitType.createFrom(tech.requiredUnit())) == 0) {
            return false;
        }
        return true;
    }

    private static boolean hasRequirements(UpgradeType upgrade) {
        if (Count.ofType(AUnitType.createFrom(upgrade.whatsRequired())) == 0) {
            return false;
        }
        return true;
    }

    /**
     * Returns true if it's possible to produce unit (or building) of given type.
     */
//    public static boolean hasTechAndBuildingsToProduce(AUnitType unitType) {
//        return hasTechToProduce(unitType) && Requirements.hasRequirements(unitType);
//    }

    /**
     * Returns true if we have all techs needed for given unit (but we may NOT have some of the buildings!).
     */
//    public static boolean hasTechToProduce(AUnitType unitType) {
//
//        // Needs to have tech
//        TechType techType = unitType.getRequiredTech();
//        return techType == null || techType == TechType.None || ATech.isResearched(techType);
//    }

    /**
     * Returns true if we have all buildings needed for given unit.
     *
     * @param countUnfinished if true, then if it will require required units to be finished to return
     * true e.g. to produce Zealot you need at least one finished Gateway
     */
//    public static boolean hasBuildingsToProduce(AUnitType unitType, boolean countUnfinished) {
//
//        // Need to have every prerequisite building
//        for (AUnitType requiredType : unitType.getRequiredUnits().keySet()) {
//            if (requiredType.equals(AUnitType.Zerg_Larva)) {
//                continue;
//            }
//
//            int requiredAmount = unitType.getRequiredUnits().get(requiredType);
//            int weHaveAmount = requiredType.equals(AUnitType.Zerg_Larva)
//                    ? Select.ourLarva().count()
//                    : (countUnfinished ? Select.ourIncludingUnfinished() : Select.our()).ofType(requiredType).count();
//            if (weHaveAmount < requiredAmount) {
//                return false;
//            }
//        }
//        return true;
//    }
}
