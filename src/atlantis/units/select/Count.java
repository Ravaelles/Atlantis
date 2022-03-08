package atlantis.units.select;

import atlantis.map.position.APosition;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.production.ProductionQueue;
import atlantis.units.AUnitType;
import atlantis.util.cache.Cache;
import atlantis.util.We;

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
        else if (type.equals(AUnitType.Zerg_Creep_Colony)) {
            return Select.ourIncludingUnfinished().ofType(type).count()
                    + Select.ourIncludingUnfinished().ofType(AUnitType.Zerg_Spore_Colony).count()
                    + Select.ourIncludingUnfinished().ofType(AUnitType.Zerg_Sunken_Colony).count();
        }
        else if (type.isPrimaryBase()) {
            return Select.ourUnfinished().bases().count()
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
        if (type.equals(AUnitType.Zerg_Creep_Colony)) {
            return Select.countOurOfType(AUnitType.Zerg_Sunken_Colony)
                    + Select.countOurOfType(AUnitType.Zerg_Creep_Colony)
                    + Select.countOurOfType(AUnitType.Zerg_Spore_Colony);
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

    public static int zerglings() {
        return ofType(AUnitType.Zerg_Zergling);
    }

    public static int hydralisks() {
        return ofType(AUnitType.Zerg_Hydralisk);
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
        return cache.get(
            "tanks",
            1,
            () -> ofType(AUnitType.Terran_Siege_Tank_Siege_Mode)
                + ofType(AUnitType.Terran_Siege_Tank_Tank_Mode)
        );
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

    public static int ourStrictlyAntiAir() {
        if (We.protoss()) {
            return Select.countOurOfTypeIncludingUnfinished(AUnitType.Protoss_Corsair);
        }
        else if (We.terran()) {
            return Select.countOurOfTypesIncludingUnfinished(AUnitType.Terran_Goliath, AUnitType.Terran_Valkyrie);
        }
        else {
            return Select.countOurOfTypesIncludingUnfinished(AUnitType.Zerg_Scourge);
        }
    }

    public static int infantry() {
        if (We.terran()) {
            return Select.ourTerranInfantry().count();
        }
        else if (We.protoss()) {
            return Select.ourOfType(
                AUnitType.Protoss_Zealot, AUnitType.Protoss_Dragoon, AUnitType.Protoss_Dark_Templar
            ).count();
        }
        else {
            return Select.ourOfType(
                AUnitType.Zerg_Zergling, AUnitType.Zerg_Hydralisk
            ).count();
        }
    }

    public static int bunkers() {
        return Select.countOurOfType(AUnitType.Terran_Bunker);
    }

    public static int cannons() {
        return Select.countOurOfType(AUnitType.Protoss_Photon_Cannon);
    }

    public static int barracks() {
        return Select.countOurOfType(AUnitType.Terran_Barracks);
    }

    public static int ghosts() {
        return Count.ofType(AUnitType.Terran_Ghost);
    }

    public static int observers() {
        return Count.ofType(AUnitType.Protoss_Observer);
    }

//    public static int () {
//        return ofType(AUnitType.);
//    }
}
