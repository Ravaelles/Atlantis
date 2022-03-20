package atlantis.production.dynamic.terran;

import atlantis.combat.micro.terran.TerranBunker;
import atlantis.combat.micro.terran.TerranMissileTurretsForMain;
import atlantis.combat.micro.terran.TerranMissileTurretsForNonMain;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.decisions.Decisions;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.dynamic.ADynamicBuildingsManager;
import atlantis.production.orders.build.AddToQueue;
import atlantis.production.requests.AntiLandBuildingManager;
import atlantis.production.requests.protoss.ProtossPhotonCannonAntiLand;
import atlantis.production.requests.zerg.ZergSunkenColony;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;

import static atlantis.units.AUnitType.*;

public class TerranDynamicBuildingsManager extends ADynamicBuildingsManager {

    public static void update() {
        if (A.everyNthGameFrame(61)) {
            (new TerranMissileTurretsForMain()).buildIfNeeded();
            (new TerranMissileTurretsForNonMain()).buildIfNeeded();
//            OffensiveTerranMissileTurrets.buildIfNeeded();
//            TerranBunker.handleOffensiveBunkers();
            TerranBunker.get().handleDefensiveBunkers();
        }

        comsats();
        scienceFacilities();

        factoryIfBioOnly();

        armory();
        machineShop();
        factories();
        starport();

        barracks();
    }

    // =========================================================

    private static void scienceFacilities() {
        if (Have.a(Terran_Science_Facility)) {
            return;
        }

        if (A.supplyUsed() >= 50 || enemyStrategy().goingHiddenUnits()) {
            if (haveNotExistingOrPlanned(Terran_Starport)) {
                AddToQueue.withHighPriority(Terran_Starport);
            }
            if (haveNotExistingOrPlanned(Terran_Science_Facility)) {
                AddToQueue.withHighPriority(Terran_Science_Facility);
            }
            if (haveNotExistingOrPlanned(Terran_Control_Tower)) {
                AddToQueue.withHighPriority(Terran_Control_Tower);
            }
        }
    }

    private static boolean haveNotExistingOrPlanned(AUnitType type) {
        if (Count.ofType(type) > 0) {
            return false;
        }

        return Count.inQueueOrUnfinished(type, 4) == 0;
    }

    private static boolean armory() {

        if (enemyStrategy().isAirUnits()) {
            AddToQueue.withTopPriority(Terran_Armory);
            return true;
        }

        return false;
    }

    private static void starport() {
        if (A.supplyUsed() >= 90 && Have.factory() && Have.notEvenPlanned(Terran_Starport)) {
            AddToQueue.withStandardPriority(Terran_Starport);
        }
    }

    private static boolean factoryIfBioOnly() {
        if (A.supplyUsed() <= 30 || !A.hasGas(90) || Have.factory()) {
            return false;
        }

//        if (OurDecisions.haveFactories() && Count.factories() < 2) {
//            AddToQueue.withHighPriority(Terran_Factory);
//        }
        if (
                OurStrategy.get().goingBio()
                && (
//                        (Decisions.wantsToBeAbleToProduceTanksSoon() && Count.WithPlanned(Terran_Factory) == 0)
                        (Count.withPlanned(Terran_Factory) == 0)
                        || (A.supplyUsed() >= 30 && Count.withPlanned(Terran_Factory) == 0)
                )
        ) {
//            System.err.println("Change from BIO to TANKS (" + Count.WithPlanned(Terran_Factory) + ")");
//            System.err.println("A = " + Count.inProduction(Terran_Factory));
//            System.err.println("B = " + Count.inQueue(Terran_Factory, 5));
            AddToQueue.withHighPriority(Terran_Factory);
            return true;
        }

        return false;
    }

    /**
     * If all factories are busy (training units) request new ones.
     */
    private static boolean factories() {
        if (AGame.canAffordWithReserved(160, 120)) {
            Selection factories = Select.ourOfType(Terran_Factory);
            
            int unfinishedFactories = 
                    ConstructionRequests.countNotFinishedOfType(Terran_Factory);
            int numberOfFactories = factories.size() + unfinishedFactories;
            
            // Proceed only if all factories are busy
            if (numberOfFactories >= 1 && factories.areAllBusy()) {
                
                if (unfinishedFactories == 0) {
                    AddToQueue.withHighPriority(Terran_Factory);
                    return true;
                }
                else if (unfinishedFactories >= 1 && AGame.canAfford(
                        100 + 200 * unfinishedFactories, 100 + 100 * unfinishedFactories
                )) {
                    AddToQueue.withHighPriority(Terran_Factory);
                    return true;
                }
            }
        }

        return false;
    }

    private static void comsats() {
        if (!Have.academy()) {
            return;
        }

        if (
                Count.bases() > Count.withPlanned(Terran_Comsat_Station)
                && Count.inQueueOrUnfinished(Terran_Comsat_Station, 5) <= 0
        ) {
            AddToQueue.withStandardPriority(Terran_Comsat_Station);
        }
    }

    /**
     * If there are buildings without addons, build them.
     */
    private static void machineShop() {
        if (!Have.factory()) {
            return;
        }

        if (
            GamePhase.isEarlyGame()
                && Count.vultures() <= 3
                && EnemyUnits.discovered().ofType(Protoss_Zealot).atLeast(5)
        ) {
            return;
        }

        if (
                Decisions.wantsToBeAbleToProduceTanksSoon()
                        || (A.supplyUsed(45) && !Have.machineShop())
                        || AGame.canAffordWithReserved(150, 150)
                        || A.supplyUsed(70)
        ) {

            for (AUnit building : Select.ourBuildings().list()) {
                if (building.type().isFactory() && !building.hasAddon()) {
                    AUnitType addonType = building.type().getRelatedAddon();
                    if (addonType != null) {

                        if (AGame.canAfford(addonType) && Count.inQueueOrUnfinished(addonType, 3) <= 1) {
//                            AddToQueue.withHighPriority(addonType);
                            building.buildAddon(addonType);
                            return;
                        }
                    }
                }
            }
        }
    }

    private static boolean barracks() {
        int barracks = Count.barracks();

        if (barracks >= 3) {
            return false;
        }

//        if (!Have.academy() && Count.existingOrInProductionOrInQueue(Terran_Barracks) >= 2) {
        if (!Have.academy() && Count.existingOrInProductionOrInQueue(Terran_Barracks) >= 4) {
            return false;
        }

        if (barracks >= 3 && A.supplyUsed() <= 40) {
            return false;
        }

        if (barracks >= 3 && A.supplyUsed() <= 70) {
            return false;
        }

        return requestMoreIfAllBusy(Terran_Barracks, 200, 0);
    }

}
