package atlantis.production.dynamic.terran;

import atlantis.combat.micro.terran.TerranBunker;
import atlantis.combat.micro.terran.TerranMissileTurretsForMain;
import atlantis.combat.micro.terran.TerranMissileTurretsForNonMain;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.decisions.Decisions;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.dynamic.ADynamicBuildingsManager;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import static atlantis.units.AUnitType.Terran_Barracks;


public class TerranDynamicBuildingsManager extends ADynamicBuildingsManager {

    public static void update() {
        if (A.everyNthGameFrame(60)) {
            TerranMissileTurretsForMain.buildIfNeeded();
            TerranMissileTurretsForNonMain.buildIfNeeded();
//            OffensiveTerranMissileTurrets.buildIfNeeded();
//            TerranBunker.handleOffensiveBunkers();
            TerranBunker.handleDefensiveBunkers();
        }

        factoryIfBioOnly();

        armory();
        machineShop();
        factories();
        starport();

        comsats();

        barracks();
    }

    // =========================================================

    private static boolean armory() {
        if (Have.no(AUnitType.Terran_Armory)) {
            return false;
        }

        if (EnemyStrategy.get().isAirUnits()) {
            AddToQueue.withTopPriority(AUnitType.Terran_Armory);
            return true;
        }

        return false;
    }

    private static void starport() {
        if (A.supplyUsed() >= 90 && Have.factory() && Have.no(AUnitType.Terran_Starport)) {
            AddToQueue.withStandardPriority(AUnitType.Terran_Starport);
        }
    }

    private static boolean factoryIfBioOnly() {
        if (A.supplyUsed() <= 30 || !A.hasGas(90) || Have.factory()) {
            return false;
        }

//        if (OurDecisions.haveFactories() && Count.factories() < 2) {
//            AddToQueue.withHighPriority(AUnitType.Terran_Factory);
//        }
        if (
                OurStrategy.get().goingBio()
                && (
//                        (Decisions.wantsToBeAbleToProduceTanksSoon() && Count.includingPlanned(AUnitType.Terran_Factory) == 0)
                        (Count.includingPlanned(AUnitType.Terran_Factory) == 0)
                        || (A.supplyUsed() >= 30 && Count.includingPlanned(AUnitType.Terran_Factory) == 0)
                )
        ) {
//            System.err.println("Change from BIO to TANKS (" + Count.includingPlanned(AUnitType.Terran_Factory) + ")");
//            System.err.println("A = " + Count.inProduction(AUnitType.Terran_Factory));
//            System.err.println("B = " + Count.inQueue(AUnitType.Terran_Factory, 5));
            AddToQueue.withHighPriority(AUnitType.Terran_Factory);
            return true;
        }

        return false;
    }

    /**
     * If all factories are busy (training units) request new ones.
     */
    private static boolean factories() {
        if (AGame.canAffordWithReserved(160, 120)) {
            Selection factories = Select.ourOfType(AUnitType.Terran_Factory);
            
            int unfinishedFactories = 
                    ConstructionRequests.countNotFinishedOfType(AUnitType.Terran_Factory);
            int numberOfFactories = factories.size() + unfinishedFactories;
            
            // Proceed only if all factories are busy
            if (numberOfFactories >= 1 && factories.areAllBusy()) {
                
                if (unfinishedFactories == 0) {
                    AddToQueue.withHighPriority(AUnitType.Terran_Factory);
                    return true;
                }
                else if (unfinishedFactories >= 1 && AGame.canAfford(
                        100 + 200 * unfinishedFactories, 100 + 100 * unfinishedFactories
                )) {
                    AddToQueue.withHighPriority(AUnitType.Terran_Factory);
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
                Count.bases() > Count.includingPlanned(AUnitType.Terran_Comsat_Station)
                && Count.inQueueOrUnfinished(AUnitType.Terran_Comsat_Station, 5) <= 0
        ) {
            AddToQueue.withStandardPriority(AUnitType.Terran_Comsat_Station);
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
        if (!Have.academy() && Count.existingOrInProductionOrInQueue(Terran_Barracks) >= 2) {
            return false;
        }

        return requestMoreIfAllBusy(Terran_Barracks, 200, 0);
    }

}
