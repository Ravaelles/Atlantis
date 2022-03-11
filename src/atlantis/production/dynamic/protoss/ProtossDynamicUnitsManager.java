
package atlantis.production.dynamic.protoss;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.information.enemy.EnemyFlags;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ProtossArmyComposition;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.production.AbstractDynamicUnits;
import atlantis.production.orders.build.AddToQueue;
import atlantis.production.orders.build.BuildOrderSettings;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

import java.util.List;


public class ProtossDynamicUnitsManager extends AbstractDynamicUnits {

    public static void update() {
        if (AGame.notNthGameFrame(3)) {
            return ;
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
                Have.no(AUnitType.Protoss_Robotics_Facility)
                || Count.ofType(AUnitType.Protoss_Reaver) >= Count.ofType(AUnitType.Protoss_Shuttle)
        ) {
            return;
        }

        buildToHave(AUnitType.Protoss_Shuttle, 1);
    }

    private static void observers() {
        if (Have.no(AUnitType.Protoss_Observatory)) {
            if (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT) {
                AddToQueue.withTopPriority(AUnitType.Protoss_Observatory);
                AddToQueue.withTopPriority(AUnitType.Protoss_Observer);
            }
            return;
        }

        if (Have.no(AUnitType.Protoss_Observer)) {
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
        if (Have.no(AUnitType.Protoss_Stargate)) {
            return;
        }

        int mutas = EnemyUnits.count(AUnitType.Zerg_Mutalisk);
        if (mutas >= 1) {
            buildToHave(AUnitType.Protoss_Corsair, (int) (mutas / 2) + 1);
        }
    }

    private static void reavers() {
        if (Have.no(AUnitType.Protoss_Robotics_Facility) || Have.no(AUnitType.Protoss_Robotics_Support_Bay)) {
            return;
        }

        int maxReavers = Decisions.isEnemyGoingAirAndWeAreNotPreparedEnough() ? 0 : 5;

        buildToHave(AUnitType.Protoss_Reaver, maxReavers);
    }

    private static void dragoons() {
        if (Have.no(AUnitType.Protoss_Gateway) || Have.no(AUnitType.Protoss_Cybernetics_Core)) {
            return;
        }

        if (
            GamePhase.isEarlyGame()
                && EnemyStrategy.get().isRushOrCheese()
                && !A.hasGas(70)
                && !A.hasMinerals(175)
                && Count.zealots() < minZealotsAganstEnemyRush()
        ) {
                return;
        }

        if ((A.supplyUsed() <= 38 || Count.observers() >= 1) && A.hasGas(50) && A.hasMinerals(175)) {
            trainIfPossible(AUnitType.Protoss_Dragoon, false, 125, 50);
            return;
        }

//        if (ProtossArmyComposition.zealotsToDragoonsRatioTooLow()) {
//            return;
//        }

        trainIfPossible(AUnitType.Protoss_Dragoon);
    }

    private static int minZealotsAganstEnemyRush() {
        return 4;
    }

    private static void zealots() {
        if (Have.no(AUnitType.Protoss_Gateway)) {
            return;
        }

//        if (!AGame.canAffordWithReserved(125, 0)) {
//            return;
//        }

        if (
                GamePhase.isEarlyGame()
                    && EnemyStrategy.get().isRushOrCheese()
                    && Count.existingOrInProductionOrInQueue(AUnitType.Protoss_Zealot) < minZealotsAganstEnemyRush()
        ) {
            trainIfPossible(AUnitType.Protoss_Zealot);
            return;
        }

        if (dragoonInsteadOfZealot()) {
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

        if (A.hasGas(50) && !A.hasMinerals(225) && Have.cyberneticsCore() && Count.dragoons() <= 2 && Count.zealots() >= 1) {
            return true;
        }

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
