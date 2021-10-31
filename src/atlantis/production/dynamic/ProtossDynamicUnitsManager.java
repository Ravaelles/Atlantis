
package atlantis.production.dynamic;

import atlantis.AGame;
import atlantis.production.AbstractDynamicUnits;
import atlantis.strategy.EnemyStrategy;
import atlantis.strategy.GamePhase;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import java.util.List;


public class ProtossDynamicUnitsManager extends AbstractDynamicUnits {

    public static void update() {
        if (AGame.notNthGameFrame(3)) {
            return ;
        }

        scarabs();
        zealots();
        dragoons();
        arbiters();
    }

    // =========================================================

    private static void dragoons() {
        if (GamePhase.isEarlyGame() && EnemyStrategy.get().isRushOrCheese() && Count.zealots() > minZealotsInRush()) {
            return;
        }

        trainIfPossible(17, AUnitType.Protoss_Dragoon, false);
    }

    private static int minZealotsInRush() {
        return 3;
    }

    private static void zealots() {
        if (GamePhase.isEarlyGame() && EnemyStrategy.get().isRushOrCheese() && Count.zealots() <= minZealotsInRush()) {
            trainNow(AUnitType.Protoss_Zealot, false);
            return;
        }

        if (AGame.isEnemyZerg() && Count.ofType(AUnitType.Protoss_Zealot) <= 0) {
            trainIfPossible(8, AUnitType.Protoss_Zealot, true);
        }
    }

    private static void scarabs() {
        List<AUnit> reavers = Select.ourOfType(AUnitType.Protoss_Reaver).listUnits();
        for (AUnit reaver : reavers) {
            if (reaver.scarabCount() <= 2 && !reaver.isTrainingAnyUnit()) {
                reaver.train(AUnitType.Protoss_Scarab);
            }
        }
    }

    private static void arbiters() {
        trainNowIfHaveWhatsRequired(AUnitType.Protoss_Arbiter, true);
    }

}