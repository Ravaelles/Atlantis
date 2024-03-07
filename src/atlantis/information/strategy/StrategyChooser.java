package atlantis.information.strategy;

import atlantis.information.strategy.terran.TerranStrategies;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class StrategyChooser {

    /**
     * Choose initial strategy and therefore the Build Order.
     */
    public static void initializeStrategy() {
        AStrategy strategy;

        if (We.protoss()) {
            strategy = ProtossStrategies.chooseStrategyForProtoss();
        }
        else if (We.terran()) {
            strategy = TerranStrategies.chooseStrategyForTerran();
        }
        else if (We.zerg()) {
            strategy = ZergStrategies.chooseStrategyForZerg();
        }
        else {
            ErrorLog.printErrorOnce("Unhandled race in StrategyChooser::initialize()");
            strategy = null;
        }

        OurStrategy.setTo(strategy);
    }
}
