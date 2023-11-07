package atlantis.production.dynamic.terran.buildings;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.OurStrategy;
import atlantis.production.orders.production.queue.add.AddToQueue;
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

        int existing = Count.existing(Terran_Factory);
        int existingAndPlanned = Count.withPlanned(Terran_Factory);

        if (!A.canAfford(350, 200) && existingAndPlanned >= 1) return false;

        if (existingAndPlanned >= 2 && !Have.starport()) return false;

        if (
            existing >= 1 && !A.canAfford(300, 175)
                && Count.vultures() <= 1 && Count.tanks() <= 1
        ) return false;

        int inProgress = Count.inProductionOrInQueue(Terran_Factory);

        if (inProgress == 0 && !Have.factory() && A.canAfford(230, 115)) {
            AddToQueue.withHighPriority(Terran_Factory);
            return true;
        }

        if (AGame.canAffordWithReserved(160, 120)) {
            Selection factories = Select.ourOfType(Terran_Factory);

            if (inProgress >= A.inRange(1, AGame.gas() / 150, 3)) return false;

            int numberOfFactories = factories.size() + inProgress;

            // Proceed only if all factories are busy
            if (numberOfFactories >= 1 && factories.areAllBusy()) {

                if (inProgress == 0) {
                    AddToQueue.withHighPriority(Terran_Factory);
                    return true;
                }
                else if (inProgress >= 1 && AGame.canAfford(
                    100 + 200 * inProgress, 100 + 100 * inProgress
                )) {
                    AddToQueue.withHighPriority(Terran_Factory);
                    return true;
                }
            }
        }

        return false;
    }
}
