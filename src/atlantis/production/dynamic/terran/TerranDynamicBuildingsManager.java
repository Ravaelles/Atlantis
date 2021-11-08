package atlantis.production.dynamic.terran;

import atlantis.AGame;
import atlantis.production.constructing.AConstructionRequests;
import atlantis.production.dynamic.ADynamicBuildingsManager;
import atlantis.production.orders.AddToQueue;
import atlantis.strategy.EnemyStrategy;
import atlantis.strategy.OurStrategy;
import atlantis.strategy.decisions.OurDecisions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;


public class TerranDynamicBuildingsManager extends ADynamicBuildingsManager {

    public static void update() {
        factoryIfBioOnly();

        armory();
        factory();
        addonIfNeeded();
    }

    // =========================================================

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
                        && OurDecisions.beAbleToProduceTanks()
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

        if (AGame.canAffordWithReserved(250, 100)) {
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

    /**
     * If there are buildings without addons, build them.
     */
    private static void addonIfNeeded() {
        if (OurDecisions.beAbleToProduceTanks()) {

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
