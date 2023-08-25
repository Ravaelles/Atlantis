package atlantis.information.strategy;

import atlantis.production.orders.production.ProductionQueue;

import static org.junit.Assert.assertNotNull;

public class OurStrategy {

    private static AStrategy ourStrategy = null;

    // =========================================================

    public static AStrategy get() {
        if (ourStrategy == null) {
            throw new RuntimeException("Strategy was not properly initialized.");
        }

        return ourStrategy;
    }

//    private static AStrategy initWithGeneric() {
//        if (We.isProtoss()) {
//            return ProtossStrategies.PROTOSS_2_Gate_Range_Expand;
//        }
//        if (We.isTerran()) {
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
        assertNotNull(strategy);

        ourStrategy = strategy;



//        for (ProductionOrder po : ourStrategy.buildOrder().getProductionOrders()) {

//        }


        ProductionQueue.useBuildOrderFrom(strategy);
    }

}
