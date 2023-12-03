package atlantis.production.dynamic.terran.buildings;

import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.generic.OurArmyStrength;
import atlantis.information.strategy.OurStrategy;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import static atlantis.units.AUnitType.Terran_Factory;

public class ProduceFactoryWhenBioOnly {
    public static boolean factoryIfBioOnly() {
        if (!Have.barracks()) return false;
        if (!OurStrategy.get().goingBio()) return false; // See: ProduceFactory
        if (A.supplyUsed() <= 25 || !A.hasGas(90)) return false;
        if (Count.inProductionOrInQueue(Terran_Factory) >= 1) return false;
        if (CountInQueue.count(Terran_Factory) >= 1) return false;

        Selection freeFactories = Select.ourFree(Terran_Factory);

        if (freeFactories.notEmpty()) return false;

        if (ProduceFactory.secondFactory()) return true;

        if (freeFactories.size() <= 0 && A.canAfford(300, 200)) {
            produce();
            return true;
        }

        if (
            A.canAfford(160, 80)
                && CountInQueue.bases() == 0
                && ArmyStrength.weAreStronger()
        ) return produce();

        if (
            Count.ourCombatUnits() >= 3
                && ArmyStrength.weAreStronger()
                &&
                (
                    (Count.withPlanned(Terran_Factory) == 0)
                        || (A.supplyUsed() >= 32 && Count.withPlanned(Terran_Factory) == 0)
                )
        ) {
            return produce();
        }

        return false;
    }

    private static boolean produce() {
        return AddToQueue.withHighPriority(Terran_Factory) != null;
    }
}
