package atlantis.production.dynamic.terran.buildings;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import static atlantis.units.AUnitType.Terran_Factory;

public class BuildFactory {
    /**
     * If all factories are busy (training units) request new ones.
     */
    public static boolean factories() {
        if (!Have.barracks()) return false;

        int existing = Count.existing(Terran_Factory);

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
