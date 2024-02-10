package atlantis.production.dynamic.protoss.units;

import atlantis.game.A;
import atlantis.information.enemy.EnemyFlags;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;

import static atlantis.production.AbstractDynamicUnits.buildToHave;

public class ProduceObservers {
    public static void observers() {
        if (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT) {
            if (Have.notEvenPlanned(AUnitType.Protoss_Robotics_Facility)) {
                AddToQueue.withTopPriority(AUnitType.Protoss_Robotics_Facility);
                if (Have.notEvenPlanned(AUnitType.Protoss_Observatory)) {
                    AddToQueue.toHave(AUnitType.Protoss_Observatory, 1, ProductionOrderPriority.HIGH);
                }
                return;
            }

            else if (Have.notEvenPlanned(AUnitType.Protoss_Observatory)) {
                AddToQueue.withTopPriority(AUnitType.Protoss_Observatory);
                if (Have.notEvenPlanned(AUnitType.Protoss_Observer)) {
                    AddToQueue.toHave(AUnitType.Protoss_Observer, 1, ProductionOrderPriority.HIGH);
                }
                return;
            }

            else if (Have.notEvenPlanned(AUnitType.Protoss_Observer)) {
                if (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT) {
                    AddToQueue.toHave(AUnitType.Protoss_Observer, 1, ProductionOrderPriority.TOP);
                }
                return;
            }
        }

        int limit = Math.max(
            1 + (EnemyFlags.HAS_HIDDEN_COMBAT_UNIT ? 2 : 0),
            A.supplyTotal() / 30
        );
        buildToHave(AUnitType.Protoss_Observer, limit);
    }
}
