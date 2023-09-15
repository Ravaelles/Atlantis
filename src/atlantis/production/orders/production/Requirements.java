package atlantis.production.orders.production;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.tech.ATech;
import atlantis.production.dynamic.terran.tech.U238;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Counter;
import atlantis.util.log.ErrorLog;
import bwapi.TechType;
import bwapi.UpgradeType;

public class Requirements {

    public static boolean hasRequirements(ProductionOrder order) {
        AUnitType type = order.unitType();

        if (type != null) {
//            if (AUnitType.Terran_Medic.equals(type)) {
////                order.type().requiredUnits().print("Required units");
//                System.err.println("hasRequiredTechForUnit(type) = " + hasRequiredTechForUnit(type));
//                System.err.println("dontHaveEnoughGasAsRequirement(type) = " + dontHaveEnoughGasAsRequirement(type));
//            }

            return hasRequirements(type);
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

        ErrorLog.printMaxOncePerMinute("Shouldn't reach order here: " + order);
        return false;
    }

    public static boolean hasRequirements(AUnitType type) {
        if (type == null) return true;

        Counter<AUnitType> requiredUnits = type.requiredUnits();
        for (AUnitType requiredType : requiredUnits.keys()) {
//            A.errPrintln(requiredType + " required for " + type + " - Have:" + A.trueFalse(Have.a(requiredType)));
            if (!requiredType.isLarva() && !requiredType.isWorker() && !Have.a(requiredType)) {
//                A.errPrintln("--------------------------");
//                A.errPrintln("DONT have " + requiredType + " for " + type);
//                A.errPrintln("Have.a(requiredType) = " + Have.a(requiredType));
//                A.errPrintln(Select.ourOfType(requiredType).count());
//                Select.clearCache();
//                A.errPrintln(Select.ourOfType(requiredType).count());
//                Select.ourOfType(requiredType).print();
//                Select.our().print();
//                A.errPrintln("--------------------------");
                return false;
            }
        }

        if (dontHaveEnoughGasAsRequirement(type)) return false;

        return hasRequiredTechForUnit(type);
    }

    private static boolean dontHaveEnoughGasAsRequirement(AUnitType type) {
        if (type.getGasPrice() == 0) return false;

        return AGame.gas() < (int) (type.getGasPrice() * 0.7);
    }

    private static boolean hasRequiredTechForUnit(AUnitType type) {
        TechType techType = type.getRequiredTech();
        return techType == null || techType == TechType.None || ATech.isResearched(techType);
    }

    // =========================================================

    private static boolean hasRequirements(TechType tech) {
        if (tech.gasPrice() > 0) {
            if (AGame.gas() < ((int) (tech.gasPrice() * 0.4))) return false;
        }

        if (TechType.Tank_Siege_Mode.equals(tech)) {
            return Have.machineShop();
        }

        AUnitType required = AUnitType.from(tech.requiredUnit());
//        if (TechType.Stim_Packs.equals(tech)) {
//            System.out.println("for Stim_Packs required = " + required);
//            System.out.println("for Stim_Packs whatResearches = " + tech.whatResearches());
//        }
        if (required != null && Count.ofType(required) == 0) return false;

        AUnitType whatResearches = AUnitType.from(tech.whatResearches());
        if (whatResearches != null && Count.ofType(whatResearches) == 0) return false;

        return true;
    }

    private static boolean hasRequirements(UpgradeType upgrade) {
        if (upgrade.gasPrice() > 0) {
            if (AGame.gas() < (upgrade.gasPrice() * 0.4)) return false;
        }

        if (upgrade.whatsRequired() != null) {
            AUnitType required = AUnitType.from(upgrade.whatsRequired());
//            if (U238.upgradeType().equals(upgrade)) {
//                A.errPrintln("for U238 required = " + required);
//            }
            if (required != null && Count.ofType(required) == 0) return false;
        }

        if (upgrade.whatUpgrades() != null) {
            AUnitType whatUpgrades = AUnitType.from(upgrade.whatUpgrades());
//            if (U238.upgradeType().equals(upgrade)) {
//                A.errPrintln("for U238 whatResearches = " + whatUpgrades);
//            }
            if (whatUpgrades != null && Count.ofType(whatUpgrades) == 0) return false;
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
