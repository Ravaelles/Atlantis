package atlantis.production.orders.production;

import atlantis.game.A;
import atlantis.information.tech.ATech;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.MappingCounter;
import bwapi.TechType;
import bwapi.UpgradeType;

public class Requirements {

    public static boolean hasRequirements(ProductionOrder order) {
        if (order.unitType() != null) {
//            System.out.println("-----");
//            System.out.println("order.unitType() = " + order.unitType());
//            System.out.println("order.unitType().getWhatIsRequired() = " + order.unitType().whatIsRequired());
//            System.out.println("order.unitType().hasRequiredUnit() = " + order.unitType().hasRequiredUnit());
//            System.out.println("hasRequirements(order.unitType()) = " + hasRequirements(order.unitType()));
//            order.unitType().requiredUnits().print("Required units");
//            return !order.unitType().hasRequiredUnit() || hasRequirements(order.unitType());
            return !order.unitType().hasRequiredUnit() || hasRequirements(order.unitType());
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

        if (type.hasRequiredUnit() && Count.ofType(type.whatIsRequired()) == 0) {
            return false;
        }

        MappingCounter<AUnitType> requiredUnits = type.requiredUnits();
        for (AUnitType requiredType : requiredUnits.keys()) {
            if (!requiredType.isLarva() && !Have.a(requiredType)) {
//                System.err.println("Dont have " + requiredType + " for " + type);
                return false;
            }
        }

        if (type.getGasPrice() > 0) {
            if (!A.hasGas((int) (type.getGasPrice() * 0.7))) {
                return false;
            }
        }

        TechType techType = type.getRequiredTech();
        return techType == null || techType == TechType.None || ATech.isResearched(techType);
    }

    // =========================================================

    private static boolean hasRequirements(TechType tech) {
        if (tech.gasPrice() > 0) {
            if (!A.hasGas((int) (tech.gasPrice() * 0.4))) {
                return false;
            }
        }

        if (TechType.Tank_Siege_Mode.equals(tech)) {
            return Have.machineShop();
        }

        AUnitType required = AUnitType.from(tech.requiredUnit());
        if (required != null && Count.ofType(AUnitType.from(tech.requiredUnit())) == 0) {
            return false;
        }
        return true;
    }

    private static boolean hasRequirements(UpgradeType upgrade) {
        if (upgrade.gasPrice() > 0) {
            if (!A.hasGas((int) (upgrade.gasPrice() * 0.4))) {
                return false;
            }
        }

        AUnitType required = AUnitType.from(upgrade.whatsRequired());
        if (required != null && Count.ofType(required) == 0) {
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
//                    : (countUnfinished ? Select.ourWithUnfinished() : Select.our()).ofType(requiredType).count();
//            if (weHaveAmount < requiredAmount) {
//                return false;
//            }
//        }
//        return true;
//    }
}
