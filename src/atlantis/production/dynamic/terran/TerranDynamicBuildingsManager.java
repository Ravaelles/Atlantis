package atlantis.production.dynamic.terran;

import atlantis.AGame;
import atlantis.combat.micro.terran.TerranBunker;
import atlantis.combat.micro.terran.TerranMissileTurret;
import atlantis.production.constructing.AConstructionRequests;
import atlantis.production.dynamic.ADynamicBuildingsManager;
import atlantis.production.orders.AddToQueue;
import atlantis.strategy.EnemyStrategy;
import atlantis.strategy.OurStrategy;
import atlantis.strategy.decisions.OurDecisions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;


public class TerranDynamicBuildingsManager extends ADynamicBuildingsManager {

    public static void update() {
        offensiveBunkers();
        offensiveMissileTurrets();

        factoryIfBioOnly();

        armory();
        factory();

        comsat();
        machineShop();
    }

    private static void offensiveBunkers() {
        TerranBunker.handleOffensiveBunkers();
    }

    // =========================================================

    private static boolean offensiveMissileTurrets() {
        return TerranMissileTurret.handleOffensiveMissileTurrets();
    }

    private static boolean armory() {
        if (Count.includingPlanned(AUnitType.Terran_Armory) == 0) {
            return false;
        }

        if (EnemyStrategy.get().isAirUnits()) {
            return AddToQueue.withTopPriority(AUnitType.Terran_Armory);
        }

        return false;
    }

    private static boolean factoryIfBioOnly() {
//        if (OurDecisions.haveFactories() && Count.factories() < 2) {
//            AddToQueue.withHighPriority(AUnitType.Terran_Factory);
//        }
        if (
                OurStrategy.get().goingBio()
                        && OurDecisions.wantsToBeAbleToProduceTanksSoon()
                        && Count.includingPlanned(AUnitType.Terran_Factory) == 0
        ) {
//            System.err.println("Change from BIO to TANKS (" + Count.includingPlanned(AUnitType.Terran_Factory) + ")");
//            System.err.println("A = " + Count.inProduction(AUnitType.Terran_Factory));
//            System.err.println("B = " + Count.inQueue(AUnitType.Terran_Factory, 5));
            return AddToQueue.withHighPriority(AUnitType.Terran_Factory);
        }

        return false;
    }

    /**
     * If all factories are busy (training units) request new ones.
     */
    private static boolean factory() {
        if (AGame.canAffordWithReserved(280, 180)) {
            Selection factories = Select.ourOfType(AUnitType.Terran_Factory);
            
            int unfinishedFactories = 
                    AConstructionRequests.countNotFinishedConstructionsOfType(AUnitType.Terran_Factory);
            int numberOfFactories = factories.size() + unfinishedFactories;
            
            // Proceed only if all factories are busy
            if (numberOfFactories >= 1 && factories.areAllBusy()) {
                
                if (unfinishedFactories == 0) {
                    return AddToQueue.withHighPriority(AUnitType.Terran_Factory);
                }
                else if (unfinishedFactories >= 1 && AGame.canAfford(
                        100 + 200 * unfinishedFactories, 100 + 100 * unfinishedFactories
                )) {
                    return AddToQueue.withHighPriority(AUnitType.Terran_Factory);
                }
            }
        }

        return false;
    }

    private static void comsat() {
        if (Count.bases() > Count.includingPlanned(AUnitType.Terran_Comsat_Station)) {
            AddToQueue.withStandardPriority(AUnitType.Terran_Comsat_Station);
        }
    }

    /**
     * If there are buildings without addons, build them.
     */
    private static void machineShop() {
        if (OurDecisions.wantsToBeAbleToProduceTanksSoon() || AGame.canAffordWithReserved(150, 150)) {

            for (AUnit building : Select.ourBuildings().list()) {
                if (building.type().isFactory() && !building.hasAddon()) {
                    AUnitType addonType = building.type().getRelatedAddon();
                    if (addonType != null) {

                        if (AGame.canAfford(addonType)) {
//                            AddToQueue.withHighPriority(addonType);
                            building.buildAddon(addonType);
                            return;
                        }
                    }
                }
            }
        }
    }

    
}
