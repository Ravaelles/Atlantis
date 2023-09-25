package atlantis.information.strategy;


import atlantis.config.env.Env;
import atlantis.production.orders.production.queue.QueueInitializer;

import static org.junit.Assert.assertNotNull;

public class OurStrategy {

    private static AStrategy ourStrategy = null;

    // =========================================================

    public static AStrategy get() {
//        if (ourStrategy == null && Env.isTesting()) {
//            OurStrategy.setTo(TerranStrategies.TERRAN_MMG_vP);
//        }

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

        if (ourStrategy == strategy) return;

        ourStrategy = strategy;

        QueueInitializer.initializeProductionQueue();
    }
}
