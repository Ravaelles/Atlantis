package atlantis.production.dynamic.terran;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.OurStrategy;
import atlantis.production.dynamic.expansion.ShouldExpand;
import atlantis.production.dynamic.terran.abundance.TerranAbundance;
import atlantis.production.dynamic.terran.units.*;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.units.select.Count;
import atlantis.util.Enemy;
import atlantis.util.We;

public class TerranDynamicUnitsCommander extends Commander {

    private int dynamicOrders;

    @Override
    public boolean applies() {
        if (!We.terran()) return false;
        if (OurStrategy.get().isRushOrCheese()) return true;
        if (Count.basesWithUnfinished() >= 2) return true;

        if (shouldProduceUnitsBeforeSecondBase()) return true;

//        System.err.println("ReservedResources.minerals() = " + ReservedResources.minerals());
//        System.err.println("(150 + A.minerals()) = " + (150 + A.minerals()));
//        System.err.println("CountInQueue.countDynamicUnitsOrders() = " + CountInQueue.countDynamicUnitsOrders());
//        System.err.println("Queue.get().forCurrentSupply().size() = " + Queue.get().forCurrentSupply().size());

        return !saveForBase()
            && ReservedResources.minerals() <= (150 + A.minerals())
            && (dynamicOrders = CountInQueue.countDynamicUnitsOrders()) <= 10;
//            && (ReservedResources.minerals() <= 500 || A.hasMinerals(650));
//            && Queue.get().forCurrentSupply().nonCompleted().size() <= 10;
    }

    private boolean shouldProduceUnitsBeforeSecondBase() {
        return Count.marines() <= 3 || Count.medics() <= 3 || ArmyStrength.weAreWeaker();
    }

    private static boolean saveForBase() {
        return !A.hasMinerals(520) && ShouldExpand.shouldExpand();
    }

    @Override
    protected void handle() {
        ProduceScienceVessels.scienceVessels();

        if (dynamicOrders <= 3 || (dynamicOrders <= 10 && A.hasMinerals(700))) {
            if (
                ProduceWraiths.wraiths()
//                    || TerranDynamicFactoryUnits.handleFactoryProduction()
            ) return;
        }

        if (dynamicOrders <= 8 || (dynamicOrders <= 10 && A.hasMinerals(700))) {
            if (Count.infantry() <= 14 || (Enemy.protoss() && Count.tanks() >= 4) || A.hasMinerals(500)) {
                ProduceGhosts.ghosts();
                ProduceMedicsAndFirebats.medics();
                ProduceMarines.marines();
            }
        }

        (new TerranAbundance()).invokeCommander();
    }

    // =========================================================

}
