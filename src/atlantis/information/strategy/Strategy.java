package atlantis.information.strategy;

import atlantis.game.A;
import atlantis.production.orders.production.queue.QueueInitializer;
import atlantis.util.log.ErrorLog;

public class Strategy {
    private static AStrategy ourStrategy = null;

    // =========================================================

    public static AStrategy get() {
//        if (ourStrategy == null && Env.isTesting()) {
//            Strategy.setTo(TerranStrategies.TERRAN_MMG_vP);
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
        if (strategy == null) {
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Strategy.setTo() was called with null");
            A.quit();
        }

        if (ourStrategy == strategy) return;

        ourStrategy = strategy;
        ourStrategy.applyDecisions();

        QueueInitializer.initializeProductionQueue();
    }

    public static boolean is(AStrategy strategy) {
        return strategy.equals(get());
    }
}
