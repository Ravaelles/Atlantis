package atlantis.strategy;

import atlantis.util.Enemy;
import atlantis.util.Us;

public class OurStrategyManager {

    /**
     * Choose initial strategy and therefore the Build Order.
     */
    public static void initialize() {
        AStrategy strategy;

        if (Us.isProtoss()) {
            strategy = initForProtoss();
        } else if (Us.isTerran()) {
            strategy = initForTerran();
        } else {
            strategy = initForZerg();
        }

        OurStrategy.setTo(strategy);
    }

    // =========================================================

    private static AStrategy initForProtoss() {
        if (Enemy.zerg()) {
            return ProtossStrategies.PROTOSS_2_Gate_Zealot;
        }

        return ProtossStrategies.PROTOSS_2_Gate_Range_Expand;
    }

    private static AStrategy initForTerran() {
//        return TerranStrategies.TERRAN_3_Rax_MnM;
        return TerranStrategies.TERRAN_Nada_2_Factory;
    }

    private static AStrategy initForZerg() {
        return ZergStrategies.ZERG_13_Pool_Muta;
    }

}
