package atlantis.production.dynamic.terran;

import atlantis.game.A;
import atlantis.information.strategy.OurStrategy;
import atlantis.information.strategy.services.AreWeGoingBio;
import atlantis.production.dynamic.DynamicBuildingsCommander;
import atlantis.production.dynamic.reinforce.ReinforceBasesWithCombatBuildings;
import atlantis.production.dynamic.reinforce.terran.turrets.ReinforceBunkersWithTurrets;
import atlantis.production.dynamic.reinforce.terran.turrets.TurretNeededHere;
import atlantis.production.dynamic.terran.buildings.BuildFactory;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

import static atlantis.units.AUnitType.*;

public class TerranDynamicBuildingsCommander extends DynamicBuildingsCommander {
    @Override
    protected void handle() {
        ReinforceBasesWithCombatBuildings.get().invoke();
        (new ReinforceBunkersWithTurrets()).invoke();
        (new TurretNeededHere()).invoke();

        if (A.everyNthGameFrame(13)) {
            comsats();
            barracks();
            factoryIfBioOnly();
        }

        if (A.everyNthGameFrame(33)) {
            armory();
            academy();
        }

        if (A.everyNthGameFrame(37)) {
            machineShop();
            BuildFactory.factories();
        }

        if (A.everyNthGameFrame(67)) {
            engBay();
            starport();
            scienceFacilities();
        }
    }

    // =========================================================

    private static void scienceFacilities() {
        if (
            Have.a(Terran_Science_Facility)
                || !Have.a(Terran_Starport)
                || Count.withPlanned(Terran_Science_Facility) > 0
        ) {
            return;
        }

        if (A.supplyUsed() >= (Enemy.terran() ? 90 : 50) && enemyStrategy().isGoingHiddenUnits()) {
            if (haveNoExistingOrPlanned(Terran_Starport)) {
                AddToQueue.withHighPriority(Terran_Starport);
                return;
            }
            if (haveNoExistingOrPlanned(Terran_Science_Facility)) {
                AddToQueue.withHighPriority(Terran_Science_Facility);
                return;
            }
            if (haveNoExistingOrPlanned(Terran_Control_Tower)) {
                AddToQueue.withHighPriority(Terran_Control_Tower);
                return;
            }
        }

        int scienceFacilities = Count.existingOrInProductionOrInQueue(Terran_Science_Facility);
        if (A.supplyUsed() >= 60) {
            if (scienceFacilities == 0) {
                AddToQueue.withHighPriority(Terran_Science_Facility);
                return;
            }
        }

        if (A.supplyUsed() >= 120 && scienceFacilities > 0) {
            int covertOps = Count.existingOrInProductionOrInQueue(Terran_Covert_Ops);
            if (covertOps == 0) {
                AddToQueue.withHighPriority(Terran_Covert_Ops);
                return;
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

    private static void academy() {
        if (!A.supplyUsed(50) || Have.academy() || Count.withPlanned(Terran_Academy) > 0) return;

        if (Count.marines() >= 3 && A.hasMinerals(350)) {
            AddToQueue.withStandardPriority(Terran_Academy);
        }
    }

    private static void engBay() {
        if (Have.engBay() || Count.withPlanned(Terran_Engineering_Bay) > 0) return;

        if (A.supplyUsed(60) || A.seconds() >= 500) {
            AddToQueue.withStandardPriority(Terran_Engineering_Bay);
        }
    }

    private static boolean factoryIfBioOnly() {
        if (!Have.barracks()) return false;

        if (A.supplyUsed() <= 30 || !A.hasGas(90) || Have.factory()) return false;

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
            AddToQueue.withHighPriority(Terran_Factory);
            return true;
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

        if (AreWeGoingBio.check() && AreWeGoingBio.doNotFocusOnTanksForNow()) {
            return;
        }

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

        if (barracks >= 10) return false;

        if (barracks >= 2 && Enemy.terran()) return false;

        if (!A.hasMinerals(630)) {
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

            if (barracks >= 4 && A.supplyUsed() <= 70) {
                return false;
            }
        }

        if (A.canAffordWithReserved(150, 0) || A.hasMinerals(650)) {
            return buildIfAllBusyButCanAfford(Terran_Barracks, 0, 0);
        }

        return false;
    }

}
