package atlantis.production.dynamic.terran;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.dynamic.DynamicBuildingsCommander;
import atlantis.production.dynamic.expansion.TerranReinforceBaseWithCombatBuildings;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.*;

public class TerranDynamicBuildingsCommander extends DynamicBuildingsCommander {
    @Override
    public void handle() {
        super.handle();

        TerranReinforceBaseWithCombatBuildings.get().handle();

        if (A.everyNthGameFrame(7)) {
            comsats();
            scienceFacilities();

            factoryIfBioOnly();
        }

        if (A.everyNthGameFrame(9)) {
            armory();
            machineShop();
            factories();
            starport();

            barracks();
        }
    }

    // =========================================================

    private static void scienceFacilities() {
//        if (!ATech.isResearched(Tank_Siege_Mode)) {
//            return;
//        }

        if (A.supplyUsed() >= (Enemy.terran() ? 90 : 50) && enemyStrategy().isGoingHiddenUnits()) {
            if (haveNoExistingOrPlanned(Terran_Starport)) {
                AddToQueue.withHighPriority(Terran_Starport);
            }
            if (haveNoExistingOrPlanned(Terran_Science_Facility)) {
                AddToQueue.withHighPriority(Terran_Science_Facility);
            }
            if (haveNoExistingOrPlanned(Terran_Control_Tower)) {
                AddToQueue.withHighPriority(Terran_Control_Tower);
            }
        }

        if (Have.a(Terran_Science_Facility)) {
            return;
        }

        int scienceFacilities = Count.existingOrInProductionOrInQueue(Terran_Science_Facility);
        if (A.supplyUsed() >= 60) {
            if (scienceFacilities == 0) {
                AddToQueue.withHighPriority(Terran_Science_Facility);
            }
        }

        if (A.supplyUsed() >= 120 && scienceFacilities > 0) {
            int covertOps = Count.existingOrInProductionOrInQueue(Terran_Covert_Ops);
            if (covertOps == 0) {
                AddToQueue.withHighPriority(Terran_Covert_Ops);
            }
        }
    }

    private static boolean armory() {
        if (!Have.factory()) return false;

        if (enemyStrategy().isAirUnits()) {
            if (haveNoExistingOrPlanned(Terran_Armory)) {
                return addWithTopPriorityThisOrItsRequirement(Terran_Armory, Terran_Factory);
            }
        }

        return false;
    }

    private static void starport() {
//        if (!ATech.isResearched(Tank_Siege_Mode)) {
//            return;
//        }

        int minSupply = Enemy.zerg() ? 60 : 90;

        if (A.supplyUsed() >= minSupply && Have.factory() && Have.notEvenPlanned(Terran_Starport)) {
            AddToQueue.maxAtATime(Terran_Starport, A.hasMinerals(800) ? 2 : 1);
        }
    }

    private static boolean factoryIfBioOnly() {
        if (!Have.barracks()) return false;

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
        if (!Have.barracks()) return false;

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
                && Count.inQueueOrUnfinished(Terran_Comsat_Station, 10) <= 0
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

//        if (
//            GamePhase.isEarlyGame()
//                && Count.vultures() <= 3
//                && EnemyUnits.discovered().ofType(Protoss_Zealot).atLeast(5)
//        ) {
//            return;
//        }

        if (Count.factories() > Count.ofType(Terran_Machine_Shop)) {
            if (Count.inProductionOrInQueue(Terran_Machine_Shop) <= 1) {
                AddToQueue.withHighPriority(Terran_Machine_Shop);
                return;
            }
        }

//        if (
//                Decisions.wantsToBeAbleToProduceTanksSoon()
//                        || (A.supplyUsed(45) && !Have.machineShop())
//                        || AGame.canAffordWithReserved(150, 150)
//                        || A.supplyUsed(70)
//        ) {
//
//            for (AUnit building : Select.ourBuildings().list()) {
//                if (building.type().isFactory() && !building.hasAddon()) {
//                    AUnitType addonType = building.type().getRelatedAddon();
//                    if (addonType != null) {
//
//                        if (AGame.canAfford(addonType) && Count.inQueueOrUnfinished(addonType, 3) <= 1) {
////                            AddToQueue.withHighPriority(addonType);
//                            building.buildAddon(addonType);
//                            return;
//                        }
//                    }
//                }
//            }
//        }
    }

    private static boolean barracks() {
        int barracks = Count.withPlanned(Terran_Barracks);

        if (barracks >= 1 && Enemy.terran()) return false;

        if (!A.hasMinerals(650)) {
//            if (barracks >= 3) {
//                return false;
//            }

            //        if (!Have.academy() && Count.existingOrInProductionOrInQueue(Terran_Barracks) >= 2) {
            if (!Have.academy() && Count.existingOrInProductionOrInQueue(Terran_Barracks) >= 2) {
                return false;
            }

            if (barracks >= 3 && A.supplyUsed() <= 40) {
                return false;
            }

            if (barracks >= 3 && A.supplyUsed() <= 70) {
                return false;
            }
        }

        if (barracks >= 10) {
            return false;
        }

        if (A.canAffordWithReserved(150, 0) || A.hasMinerals(650)) {
            return buildIfAllBusyButCanAfford(Terran_Barracks, 0, 0);
        }

        return false;
    }

}
