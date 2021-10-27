package atlantis.strategy;

import atlantis.AGame;
import atlantis.production.ProductionOrder;
import atlantis.production.orders.AProductionQueueManager;
import atlantis.util.Us;

public class OurStrategy {

    private static AStrategy ourStrategy = null;

    // =========================================================

    public static AStrategy ourStrategy() {
        if (ourStrategy == null) {
            throw new RuntimeException("Strategy was not properly initialized.");
        }

        return ourStrategy;
    }

//    private static AStrategy initWithGeneric() {
//        if (Us.isProtoss()) {
//            return ProtossStrategies.PROTOSS_2_Gate_Range_Expand;
//        }
//        if (Us.isTerran()) {
//            return TerranStrategies.TERRAN_Three_Factory_Vultures;
//        }
//        else {
//            return ZergStrategies.ZERG_9_Pool;
//        }
//    }

    /**
     * Use this strategy and build order.
     */
    public static void setTo(AStrategy strategy) {
        System.out.println("### Use strategy `" + strategy + "` ###");

        ourStrategy = strategy;

//        System.out.println("--------------");
//        for (ProductionOrder po : ourStrategy.buildOrder().getProductionOrders()) {
//            System.out.println(po);
//        }
//        System.out.println("--------------");

        AProductionQueueManager.switchToBuildOrder(strategy.buildOrder());
    }

}
