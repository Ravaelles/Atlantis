package atlantis.units.select;

import atlantis.position.APosition;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.ProductionQueue;
import atlantis.units.AUnitType;
import atlantis.util.Cache;

/**
 * Quick auxiliary class for counting our units.
 */
public class Count {

    private static Cache<Integer> cache = new Cache<>();

    public static int ourCombatUnits() {
        return cache.get(
                "ourCombatUnits",
                0,
                () -> Select.ourCombatUnits().count()
        );
    }

    public static int ofType(AUnitType type) {
        return Select.countOurOfType(type);
    }

    public static int includingPlanned(AUnitType type) {
        return existingOrInProductionOrInQueue(type);
    }

    public static int ofTypeFree(AUnitType type) {
        return Select.ourOfType(type).free().count();
    }

    /**
     * Some buildings like Sunken Colony are morphed into from Creep Colony. When counting Creep Colonies, we
     * need to count sunkens as well.
     */
    public static int existingOrInProduction(AUnitType type) {
        return existing(type) + inProduction(type);
    }

    public static int existingOrInProductionOrInQueue(AUnitType type) {
        return existing(type) + inProductionOrInQueue(type);
    }

    public static int inProductionOrInQueue(AUnitType type) {
        return inProduction(type) + inQueue(type, 5);
    }

    public static int inQueue(AUnitType type, int amongNTop) {
        return ProductionQueue.countInQueue(type, amongNTop);
    }

    public static int inQueueOrUnfinished(AUnitType type, int amongNTop) {
        return inQueue(type, amongNTop) + inProduction(type);
    }

    public static int inProduction(AUnitType type) {
        if (type.equals(AUnitType.Zerg_Sunken_Colony)) {
            return Select.ourUnfinished().ofType(AUnitType.Zerg_Creep_Colony).count()
                    + Select.ourUnfinished().ofType(AUnitType.Zerg_Sunken_Colony).count()
                    + ConstructionRequests.countNotStartedOfType(AUnitType.Zerg_Creep_Colony)
                    + ConstructionRequests.countNotStartedOfType(AUnitType.Zerg_Sunken_Colony);
        }
        else if (type.equals(AUnitType.Zerg_Spore_Colony)) {
            return Select.ourUnfinished().ofType(AUnitType.Zerg_Creep_Colony).count()
                    + Select.ourUnfinished().ofType(AUnitType.Zerg_Spore_Colony).count()
                    + ConstructionRequests.countNotStartedOfType(AUnitType.Zerg_Creep_Colony)
                    + ConstructionRequests.countNotStartedOfType(AUnitType.Zerg_Spore_Colony);
        }
//        if (type.equals(AUnitType.Zerg_Creep_Colony)) {
//            return Select.ourIncludingUnfinished().ofType(type).count()
//                    + Select.ourIncludingUnfinished().ofType(AUnitType.Zerg_Spore_Colony).count()
//                    + Select.ourIncludingUnfinished().ofType(AUnitType.Zerg_Sunken_Colony).count();
//        }
        else if (type.isPrimaryBase()) {
            return Select.ourIncludingUnfinished().bases().count()
                    + ConstructionRequests.countNotStartedOfType(type)
                    + ConstructionRequests.countNotStartedOfType(AUnitType.Zerg_Lair)
                    + ConstructionRequests.countNotStartedOfType(AUnitType.Zerg_Hive);
        }
        else if (type.isBase() && !type.isPrimaryBase()) {
            return Select.ourUnfinished().ofType(type).count()
                    + ConstructionRequests.countNotStartedOfType(type);
        }
        else {
            return Select.ourUnfinished().ofType(type).count()
                    + ConstructionRequests.countNotStartedOfType(type);
        }
    }

    public static int existing(AUnitType type) {
        if (type.equals(AUnitType.Zerg_Sunken_Colony)) {
            return Select.countOurOfType(AUnitType.Zerg_Sunken_Colony)
                    + Select.countOurOfType(AUnitType.Zerg_Creep_Colony);
        }
        else if (type.isPrimaryBase()) {
            return Select.ourOfType(AUnitType.Zerg_Hatchery, AUnitType.Zerg_Lair, AUnitType.Zerg_Hive).count();
        }
        else if (type.isBase() && !type.isPrimaryBase()) {
            return Select.countOurOfType(type);
        }
        else {
            return Select.countOurOfType(type);
        }
    }

    public static int existingOrPlannedBuildingsNear(AUnitType type, double radius, APosition position) {
        assert type.isBuilding();

        return ourOfTypeIncludingUnfinished(type, position, radius) + plannedBuildingsNear(type, radius, position);
    }

    public static int plannedBuildingsNear(AUnitType type, double radius, APosition position) {
        assert type.isBuilding();

        return ConstructionRequests.countNotStartedOfTypeInRadius(type, radius, position);
    }

//    private static int countExistingOrPlanned(AUnitType type) {
//        return Select.ourOfType(type).count() + ProductionQueue.countInQueue(type, 6);
//    }

    public static int ourOfTypeIncludingUnfinished(AUnitType type, APosition near, double radius) {
        return Select.ourIncludingUnfinishedOfType(type).inRadius(radius, near).count();
    }

    public static int ourOfTypeIncludingUnfinished(AUnitType type) {
        return Select.countOurOfTypeIncludingUnfinished(type);
    }

    public static int ourOfTypeUnfinished(AUnitType type) {
        return Select.ourUnfinished().ofType(type).count();
    }

    public static int workers() {
        return cache.get(
                "workers",
                5,
                () -> Select.ourWorkers().count()
        );
    }

    public static int dragoons() {
        return ofType(AUnitType.Protoss_Dragoon);
    }

    public static int zealots() {
        return ofType(AUnitType.Protoss_Zealot);
    }

    public static int pylons() {
        return Select.countOurOfType(AUnitType.Protoss_Pylon);
    }

    public static int bases() {
        return cache.get(
                "bases",
                0,
                () -> Select.ourBases().count()
        );
    }

    public static int basesWithUnfinished() {
        return Select.ourIncludingUnfinished().bases().count();
    }

    public static int tanks() {
        return ofType(AUnitType.Terran_Siege_Tank_Siege_Mode)
                + ofType(AUnitType.Terran_Siege_Tank_Tank_Mode);
    }

    public static int vultures() {
        return Select.countOurOfType(AUnitType.Terran_Vulture);
    }

    public static int marines() {
        return ofType(AUnitType.Terran_Marine);
    }

    public static int medics() {
        return ofType(AUnitType.Terran_Medic);
    }

    public static int factories() {
        return ofType(AUnitType.Terran_Factory);
    }

    public static int firebats() {
        return ofType(AUnitType.Terran_Firebat);
    }

    public static int larvas() {
        return ofType(AUnitType.Zerg_Larva);
    }

    public static int turrets() {
        return ofType(AUnitType.Terran_Missile_Turret);
    }

//    public static int () {
//        return ofType(AUnitType.);
//    }
}
