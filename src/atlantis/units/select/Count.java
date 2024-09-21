package atlantis.units.select;

import atlantis.config.AtlantisRaceConfig;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.ConstructionRequests;

import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;
import atlantis.util.cache.Cache;
import bwapi.TechType;
import bwapi.UpgradeType;

import static atlantis.units.AUnitType.Terran_Refinery;
import static atlantis.units.AUnitType.Zerg_Extractor;

/**
 * Quick auxiliary class for counting our units.
 */
public class Count {
    private static Cache<Integer> cache = new Cache<>();

    public static boolean clearCache() {
        cache.clear();
        return true;
    }

    public static int ourCombatUnits() {
        return cache.get(
            "ourCombatUnits",
            1,
            () -> Select.ourCombatUnits().count()
        );
    }

    public static int ofType(AUnitType type) {
        return Select.countOurOfType(type);
    }

    public static int withPlanned(AUnitType type) {
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
        return inProduction(type) + inQueue(type);
    }

    public static int inQueue(AUnitType type) {
        return CountInQueue.count(type);
    }

    public static int inQueue(AUnitType type, int amongNTop) {
        return CountInQueue.count(type, amongNTop);
    }

    public static int inQueue(TechType tech, int amongNTop) {
        return CountInQueue.count(tech, amongNTop);
    }

    public static int inQueue(UpgradeType upgrade, int amongNTop) {
        return CountInQueue.count(upgrade, amongNTop);
    }

    public static int inQueueOrUnfinished(AUnitType type, int amongNTop) {
        return inQueue(type, amongNTop) + inProduction(type);
    }

    public static int inQueueOrUnfinished(TechType tech, int amongNTop) {
        return inQueue(tech, amongNTop) + inProduction(tech);
    }

    public static int inQueueOrUnfinished(UpgradeType upgrade, int amongNTop) {
        return inQueue(upgrade, amongNTop) + inProduction(upgrade);
    }

    private static int inProduction(TechType tech) {
        for (AUnit building : Select.ourOfType(AUnitType.from(tech.whatResearches())).list()) {
            if (tech.equals(building.whatIsResearching())) return 1;
        }
        return 0;
    }

    private static int inProduction(UpgradeType type) {
        for (AUnit building : Select.ourOfType(AUnitType.from(type.whatUpgrades())).list()) {
            if (type.equals(building.whatIsResearching())) {
                return 1;
            }
        }
        return 0;
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
            return Select.ourWithUnfinished().ofType(type).count()
                + Select.ourWithUnfinished().ofType(AUnitType.Zerg_Spore_Colony).count()
                + Select.ourWithUnfinished().ofType(AUnitType.Zerg_Sunken_Colony).count();
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

    public static int existingOrPlannedBuildingsNear(AUnitType type, double radius, HasPosition position) {
        assert type.isABuilding();

        return ourWithUnfinished(type, radius, position) + plannedBuildingsNear(type, radius, position);
    }

    public static int existingOrUnfinishedBuildingsNear(AUnitType type, double radius, HasPosition position) {
        assert type.isABuilding();

        return ourWithUnfinished(type, radius, position);
    }

    public static int plannedBuildingsNear(AUnitType type, double radius, HasPosition position) {
        assert type.isABuilding();

        return ConstructionRequests.countNotStartedOfTypeInRadius(type, radius, position);
    }

//    private static int countExistingOrPlanned(AUnitType type) {
//        return Select.ourOfType(type).count() + ProductionQueue.countInQueue(type, 6);
//    }

    public static int ourWithUnfinished(AUnitType type, double radius, HasPosition near) {
        return Select.ourWithUnfinishedOfType(type).inRadius(radius, near).count();
    }

    public static int ourWithUnfinished(AUnitType type) {
        return Select.countOurOfTypeWithUnfinished(type);
    }

    public static int ourUnfinishedOfType(AUnitType type) {
        return Select.countOurUnfinishedOfType(type);
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

    public static int zealotsAndDragoons() {
        return ofType(AUnitType.Protoss_Zealot) + ofType(AUnitType.Protoss_Dragoon);
    }

    public static int zerglings() {
        return ofType(AUnitType.Zerg_Zergling);
    }

    public static int hydralisks() {
        return ofType(AUnitType.Zerg_Hydralisk);
    }

    public static int mutas() {
        return ofType(AUnitType.Zerg_Mutalisk);
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

    public static int bunkersWithUnfinished() {
        return Select.ourWithUnfinished().bunkers().count();
    }

    public static int basesWithUnfinished() {
        return Select.ourWithUnfinished().bases().count();
    }

    public static int basesWithPlanned() {
        return basesWithUnfinished() + withPlanned(AtlantisRaceConfig.BASE);
    }

    public static int tanks() {
        return cache.get(
            "tanks",
            7,
//            () -> ofType(AUnitType.Terran_Siege_Tank_Siege_Mode) + ofType(AUnitType.Terran_Siege_Tank_Tank_Mode)
            () -> Select.ourTanks().size()
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

    public static boolean larvas(int minLarvas) {
        return larvas() >= minLarvas;
    }

    public static int turrets() {
        return ofType(AUnitType.Terran_Missile_Turret);
    }

    public static int wraiths() {
        return ofType(AUnitType.Terran_Wraith);
    }

    public static int ourStrictlyAntiAir() {
        if (We.protoss()) {
            return Select.countOurOfTypeWithUnfinished(AUnitType.Protoss_Corsair);
        }
        else if (We.terran()) {
            return Select.countOurOfTypesWithUnfinished(AUnitType.Terran_Goliath, AUnitType.Terran_Valkyrie);
        }
        else {
            return Select.countOurOfTypesWithUnfinished(AUnitType.Zerg_Scourge);
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

    public static int cannonsWithUnfinished() {
        return Select.ourWithUnfinished(AUnitType.Protoss_Photon_Cannon).count();
    }

    public static int sunkens() {
        return Select.countOurOfType(AUnitType.Zerg_Sunken_Colony);
    }

    public static int scienceVessels() {
        return Select.countOurOfType(AUnitType.Terran_Science_Vessel);
    }

    public static int creepColonies() {
        return Select.countOurOfType(AUnitType.Zerg_Creep_Colony);
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

    public static boolean notBeingProduced(AUnitType type) {
        return inProductionOrInQueue(type) == 0;
    }

    public static boolean beingProduced(AUnitType type) {
        return inProductionOrInQueue(type) > 0;
    }

    public static int freeFactories() {
        return ofTypeFree(AUnitType.Terran_Factory);
    }

    public static int freeStarports() {
        return ofTypeFree(AUnitType.Terran_Starport);
    }

    public static int freeBarracks() {
        return ofTypeFree(AUnitType.Terran_Barracks);
    }

    public static int freeGateways() {
        return ofTypeFree(AUnitType.Protoss_Gateway);
    }

    public static int gateways() {
        return ofType(AUnitType.Protoss_Gateway);
    }

    public static int gatewaysWithUnfinished() {
        return ourWithUnfinished(AUnitType.Protoss_Gateway);
    }

    public static int zealotsWithUnfinished() {
        return ourWithUnfinished(AUnitType.Protoss_Zealot);
    }

    public static int gasBuildings() {
        return ofType(AUnitType.Protoss_Assimilator)
            + ofType(Zerg_Extractor)
            + ofType(Terran_Refinery);
    }

    public static int gasBuildingsWithUnfinished() {
        return ourWithUnfinished(AUnitType.Protoss_Assimilator)
            + ourWithUnfinished(Zerg_Extractor)
            + ourWithUnfinished(Terran_Refinery);
    }
}
