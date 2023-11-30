package atlantis.production.dynamic.terran.buildings;

import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.generic.OurArmyStrength;
import atlantis.information.strategy.OurStrategy;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.Terran_Factory;

public class ProduceFactoryWhenBioOnly {
    public static boolean factoryIfBioOnly() {
        if (!Have.barracks()) return false;
        if (!OurStrategy.get().goingBio()) return false;
        if (A.supplyUsed() <= 30 || !A.hasGas(90) || Have.factory()) return false;
        if (Select.ourFree(Terran_Factory).notEmpty()) return false;
        if (Count.inProductionOrInQueue(Terran_Factory) >= 1) return false;

        if (
            Count.ourCombatUnits() >= 3
                && ArmyStrength.weAreStronger()
                &&
                (
                    (Count.withPlanned(Terran_Factory) == 0)
                        || (A.supplyUsed() >= 32 && Count.withPlanned(Terran_Factory) == 0)
                )
        ) {
            return AddToQueue.withHighPriority(Terran_Factory) != null;
        }

        return false;
    }
}
