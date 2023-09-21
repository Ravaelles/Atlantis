
package atlantis.production.dynamic.protoss;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.information.enemy.EnemyFlags;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ProtossArmyComposition;
import atlantis.information.strategy.GamePhase;
import atlantis.production.orders.build.BuildOrderSettings;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.We;

import java.util.List;

import static atlantis.production.AbstractDynamicUnits.*;
import static atlantis.units.AUnitType.Protoss_Dragoon;
import static atlantis.units.AUnitType.Protoss_Gateway;

public class ProtossDynamicUnitsCommander extends Commander {
    @Override
    public boolean applies() {
        return We.protoss();
    }

    protected void handle() {
        if (AGame.notNthGameFrame(3)) {
            return;
        }

        scarabs();
        observers();
        arbiters();
        corsairs();
        shuttles();
        reavers();

        dragoons();
        zealots();
    }

    // =========================================================

    private static void shuttles() {
        if (
            Have.notEvenPlanned(AUnitType.Protoss_Robotics_Facility)
                || Count.ofType(AUnitType.Protoss_Reaver) >= Count.ofType(AUnitType.Protoss_Shuttle)
        ) {
            return;
        }

        buildToHave(AUnitType.Protoss_Shuttle, 1);
    }

    private static void observers() {
        if (Have.notEvenPlanned(AUnitType.Protoss_Observatory)) {
            if (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT) {
                AddToQueue.withTopPriority(AUnitType.Protoss_Observatory);
                AddToQueue.withTopPriority(AUnitType.Protoss_Observer);
            }
            return;
        }

        if (Have.notEvenPlanned(AUnitType.Protoss_Observer)) {
            if (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT) {
                AddToQueue.withTopPriority(AUnitType.Protoss_Observer);
                return;
            }
        }

        int limit = Math.max(
            1 + (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT ? 2 : 0),
            A.supplyTotal() / 30
        );
        buildToHave(AUnitType.Protoss_Observer, limit);
    }

    private static void corsairs() {
        if (Have.notEvenPlanned(AUnitType.Protoss_Stargate)) {
            return;
        }

        int mutas = EnemyUnits.count(AUnitType.Zerg_Mutalisk);
        if (mutas >= 1) {
            buildToHave(AUnitType.Protoss_Corsair, (int) (mutas / 2) + 1);
        }
    }

    private static void reavers() {
        if (Have.notEvenPlanned(AUnitType.Protoss_Robotics_Facility) || Have.notEvenPlanned(AUnitType.Protoss_Robotics_Support_Bay)) {
            return;
        }

        int maxReavers = Decisions.isEnemyGoingAirAndWeAreNotPreparedEnough() ? 0 : 5;

        buildToHave(AUnitType.Protoss_Reaver, maxReavers);
    }

    private static boolean dragoons() {
        if (Have.notEvenPlanned(AUnitType.Protoss_Gateway) || Have.notEvenPlanned(AUnitType.Protoss_Cybernetics_Core))
            return false;

//        if (!A.hasGas(50) && !A.hasMinerals(125)) {
//            return;
//        }

        if (
            Decisions.needToProduceZealotsNow()
                && !A.hasGas(50)
//                && !A.hasMinerals(225)
        ) return false;

        if (!A.hasMineralsAndGas(700, 250) && !A.canAffordWithReserved(125, 50)) return false;

        if ((A.supplyUsed() <= 38 || Count.observers() >= 1)) {
//            trainIfPossible(AUnitType.Protoss_Dragoon, false, 125, 50);
            return produceDragoon();
        }

        if (A.hasGas(100) && A.supplyUsed() <= 38) {
            return produceDragoon();
        }

//        if (ProtossArmyComposition.zealotsToDragoonsRatioTooLow()) {
//            return;
//        }

        return trainIfPossible(Protoss_Dragoon);
    }

    private static boolean produceDragoon() {
        return AddToQueue.maxAtATime(Protoss_Dragoon, freeGateways());
    }

    private static int freeGateways() {
        return Select.ourFree(Protoss_Gateway).count();
    }

    private static void zealots() {
        if (Have.notEvenPlanned(AUnitType.Protoss_Gateway)) {
            return;
        }

//        if (!AGame.canAffordWithReserved(125, 0)) {
//            return;
//        }

        if (dragoonInsteadOfZealot()) {
            return;
        }

        if (Decisions.needToProduceZealotsNow()) {
            trainIfPossible(AUnitType.Protoss_Zealot);
            return;
        }

        if (BuildOrderSettings.autoProduceZealots()) {
            trainIfPossible(AUnitType.Protoss_Zealot);
            return;
        }

        if (ProtossArmyComposition.zealotsToDragoonsRatioTooLow()) {
            trainIfPossible(AUnitType.Protoss_Zealot);
            return;
        }

        if (AGame.isEnemyZerg() && Count.ofType(AUnitType.Protoss_Zealot) <= 0) {
            trainIfPossible(AUnitType.Protoss_Zealot);
            return;
        }
    }

    private static boolean dragoonInsteadOfZealot() {
        int mutas = EnemyUnits.count(AUnitType.Zerg_Mutalisk);
        if (mutas >= 3) {
            if (GamePhase.isEarlyGame()) {
                return true;
            }

            if (mutas >= 8) {
                return true;
            }
        }

        if (A.hasGas(50) && !A.hasMinerals(225) && Have.cyberneticsCore() && Count.dragoons() <= 2 && Count.zealots() >= 1)
            return true;

        return false;
    }

    private static void scarabs() {
        List<AUnit> reavers = Select.ourOfType(AUnitType.Protoss_Reaver).list();
        for (AUnit reaver : reavers) {
            if (reaver.scarabCount() <= 3 && !reaver.isTrainingAnyUnit()) {
                reaver.train(AUnitType.Protoss_Scarab);
            }
        }
    }

    private static void arbiters() {
        if (Count.ofType(AUnitType.Protoss_Arbiter_Tribunal) == 0) {
            return;
        }

        trainNowIfHaveWhatsRequired(AUnitType.Protoss_Arbiter, true);
    }

}
