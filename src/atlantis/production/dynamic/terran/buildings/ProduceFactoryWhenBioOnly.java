package atlantis.production.dynamic.terran.buildings;

import atlantis.game.A;
import atlantis.information.strategy.OurStrategy;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.Terran_Factory;

public class ProduceFactoryWhenBioOnly {
    public static boolean factoryIfBioOnly() {
        if (!Have.barracks()) return false;

        if (A.supplyUsed() <= 30 || !A.hasGas(90) || Have.factory()) return false;

        if (Select.free(Terran_Factory).notEmpty()) return false;

//        if (OurDecisions.haveFactories() && Count.factories() < 2) {
//            AddToQueue.withHighPriority(Terran_Factory);
//        }
        if (
            OurStrategy.get().goingBio()
                && (
                (Count.withPlanned(Terran_Factory) == 0)
                    || (A.supplyUsed() >= 30 && Count.withPlanned(Terran_Factory) == 0)
            )
        ) {
            AddToQueue.withHighPriority(Terran_Factory);
            return true;
        }

        return false;
    }
}
