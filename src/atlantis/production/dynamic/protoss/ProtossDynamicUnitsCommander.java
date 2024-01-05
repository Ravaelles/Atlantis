
package atlantis.production.dynamic.protoss;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.information.enemy.EnemyFlags;
import atlantis.information.enemy.EnemyUnits;
import atlantis.production.dynamic.protoss.units.ProduceDragoon;
import atlantis.production.dynamic.protoss.units.ProduceZealot;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.We;

import java.util.List;

import static atlantis.production.AbstractDynamicUnits.*;
import static atlantis.units.AUnitType.*;

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

        ProduceDragoon.dragoon();
        ProduceZealot.produce();
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

    public static int freeGateways() {
        return Select.ourFree(Protoss_Gateway).count();
    }

    private static void scarabs() {
        List<AUnit> reavers = Select.ourOfType(AUnitType.Protoss_Reaver).list();
        for (AUnit reaver : reavers) {
            if (reaver.scarabCount() <= 3 && !reaver.isTrainingAnyUnit()) {
                reaver.trainForced(AUnitType.Protoss_Scarab);
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
