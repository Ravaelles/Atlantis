package atlantis.production.dynamic.terran.buildings;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import static atlantis.units.AUnitType.Terran_Factory;

public class ProduceFactory {
    /**
     * If all factories are busy (training units) request new ones.
     */
    public static boolean factories() {
        if (!Have.barracks()) return false;
        if (OurStrategy.get().goingBio()) return false; // See: ProduceFactoryWhenBioOnly
        if (!Have.academy() && !A.canAfford(350, 100)) return false;
        if (Count.inProductionOrInQueue(Terran_Factory) >= 1) return false;
        if (CountInQueue.count(Terran_Factory) >= 1) return false;

        int existing = Count.existing(Terran_Factory);
        int existingAndPlanned = Count.withPlanned(Terran_Factory);

        if (ProduceFactory.secondFactory()) return true;

        if (!A.canAfford(350, 200) && existingAndPlanned >= 1) return false;

        if (existingAndPlanned >= 2 && !Have.starport()) return false;

        if (
            existing >= 1 && !A.canAfford(300, 175)
                && Count.vultures() <= 1 && Count.tanks() <= 1
        ) return false;

        int inProgress = Count.inProductionOrInQueue(Terran_Factory);

        if (inProgress == 0 && !Have.factory() && A.canAfford(230, 115)) {
            produce();
            return true;
        }

        if (AGame.canAffordWithReserved(160, 120)) {
            Selection factories = Select.ourOfType(Terran_Factory);

            if (inProgress >= A.inRange(1, AGame.gas() / 150, 3)) return false;

            int numberOfFactories = factories.size() + inProgress;

            // Proceed only if all factories are busy
            if (numberOfFactories >= 1 && factories.areAllBusy()) {

                if (inProgress == 0) {
                    produce();
                    return true;
                }
                else if (inProgress >= 1 && AGame.canAfford(
                    100 + 200 * inProgress, 100 + 100 * inProgress
                )) {
                    produce();
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean secondFactory() {
        if (!A.hasGas(180)) return false;
        if (CountInQueue.count(Terran_Factory) >= (A.hasGas(400) ? 2 : 1)) return false;

        if (
            Select.ourFree(Terran_Factory).isEmpty()
                && Count.factories() == 1
                && Count.inQueue(Terran_Factory) == 0
        ) {
            if (
                A.canAfford(370, 160)
                    || (A.canAfford(270, 140) && CountInQueue.bases() == 0)
            ) return produce() != null;
        }

        return false;
    }

    private static ProductionOrder produce() {
        return AddToQueue.withHighPriority(Terran_Factory);
    }
}
