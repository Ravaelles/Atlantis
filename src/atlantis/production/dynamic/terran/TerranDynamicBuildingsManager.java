package atlantis.production.dynamic.terran;

import atlantis.AGame;
import atlantis.production.constructing.AConstructionRequests;
import atlantis.production.dynamic.ADynamicBuildingsManager;
import atlantis.production.orders.AddToQueue;
import atlantis.strategy.decisions.OurDecisions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;


public class TerranDynamicBuildingsManager extends ADynamicBuildingsManager {

    public static void update() {
        factoryIfBioOnly();

        factoryIfNeeded();
        addonIfNeeded();
    }

    // =========================================================

    private static void factoryIfBioOnly() {
        if (OurDecisions.haveFactories() && Count.factories() < 2) {
            AddToQueue.withHighPriority(AUnitType.Terran_Factory);
        }
    }

    /**
     * If all factories are busy (training units) request new ones.
     */
    private static void factoryIfNeeded() {
        if (AGame.canAffordWithReserved(250, 100)) {
            Selection factories = Select.ourOfType(AUnitType.Terran_Factory);
            
            int unfinishedFactories = 
                    AConstructionRequests.countNotFinishedConstructionsOfType(AUnitType.Terran_Factory);
            int numberOfFactories = factories.size() + unfinishedFactories;
            
            // Proceed only if all factories are busy
            if (numberOfFactories >= 1 && factories.areAllBusy()) {
                
                if (unfinishedFactories == 0) {
                    AddToQueue.withHighPriority(AUnitType.Terran_Factory);
                }
                else if (unfinishedFactories >= 1 && AGame.canAfford(
                        100 + 200 * unfinishedFactories, 100 + 100 * unfinishedFactories
                )) {
                    AddToQueue.withHighPriority(AUnitType.Terran_Factory);
                }
            }
        }
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

                        if (AGame.canAffordWithReserved(addonType)) {
                            AddToQueue.withHighPriority(addonType);
//                            building.buildAddon(addonType);
//                            return;
                        }
                    }
                }
            }
        }
    }

    
}
