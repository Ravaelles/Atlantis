package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ProtossArmyComposition;
import atlantis.information.strategy.GamePhase;
import atlantis.production.orders.build.BuildOrderSettings;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.*;

public class ProduceZealot {
    public static boolean produce() {
        if (Have.notEvenPlanned(AUnitType.Protoss_Gateway)) return false;
        if (Select.ourFree(Protoss_Gateway).isEmpty()) return false;

        if (dragoonInsteadOfZealot()) {
            return true;
        }

        if (Count.ourWithUnfinished(Protoss_Zealot) >= 1 && !A.hasMinerals(250)) return false;

        if (A.hasGas(100) && Have.cyberneticsCore() && !A.hasMinerals(500) && Count.freeGateways() >= 2) {
            return produceZealot();
        }

        if (Decisions.needToProduceZealotsNow()) {
            return produceZealot();
        }

        if (BuildOrderSettings.autoProduceZealots()) {
            return produceZealot();
        }

        if (ProtossArmyComposition.zealotsToDragoonsRatioTooLow()) {
            return produceZealot();
        }

        if (AGame.isEnemyZerg() && Count.ofType(AUnitType.Protoss_Zealot) <= 0) {
            return produceZealot();
        }

        return false;
    }

    private static boolean produceZealot() {
        return Select.ourFree(Protoss_Gateway).random().train(
            Protoss_Zealot, ForcedDirectProductionOrder.create(Protoss_Zealot)
        );
    }

    private static boolean dragoonInsteadOfZealot() {
        if (!A.hasGas(50) || !Have.cyberneticsCore()) return false;

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
}
