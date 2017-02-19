package atlantis.strategy;

import atlantis.AGame;

public class AStrategyCommander {

    /**
     * Detect enemy strategy and use our strategy accordingly.
     */
    public static void update() {
        if (!AEnemyStrategy.isEnemyStrategyKnown() && AGame.getTimeFrames() % 10 == 0) {
            if (AGame.isEnemyProtoss()) {
                defineEnemyStrategyWhenEnemyIsProtoss();
            }
            else if (AGame.isEnemyTerran()) {
                defineEnemyStrategyWhenEnemyIsTerran();
            }
            else if (AGame.isEnemyZerg()) {
                defineEnemyStrategyWhenEnemyIsZerg();
            }
        }
    }
    
    // =========================================================

    private static void defineEnemyStrategyWhenEnemyIsProtoss() {
        
    }

    private static void defineEnemyStrategyWhenEnemyIsTerran() {
        AEnemyStrategy detectedStrategy = TerranStrategyDetector.detectStrategy();
        if (detectedStrategy != null) {
            changeEnemyStrategyTo(detectedStrategy);
        }
    }

    private static void defineEnemyStrategyWhenEnemyIsZerg() {
        
    }
    
    // =========================================================

    private static void changeEnemyStrategyTo(AEnemyStrategy strategy) {
        AGame.sendMessage("Enemy strategy: " + strategy);
        AEnemyStrategy.setEnemyStrategy(strategy);
    }
    
}
