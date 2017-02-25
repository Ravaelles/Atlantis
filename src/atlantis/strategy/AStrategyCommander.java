package atlantis.strategy;

import atlantis.AGame;

public class AStrategyCommander {

    /**
     * Detect enemy strategy and use our strategy accordingly.
     */
    public static void update() {
        
        // If we don't know enemy strategy, try to define it based on enemy buildings/units we know
        if (AGame.getTimeFrames() < 350 && AGame.getTimeFrames() % 10 == 0) {
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
        AEnemyStrategy detectedStrategy = AEnemyProtossStrategy.detectStrategy();
        if (detectedStrategy != null) {
            changeEnemyStrategyTo(detectedStrategy);
        }
    }

    private static void defineEnemyStrategyWhenEnemyIsTerran() {
        AEnemyStrategy detectedStrategy = AEnemyTerranStrategy.detectStrategy();
        if (detectedStrategy != null) {
            changeEnemyStrategyTo(detectedStrategy);
        }
    }

    private static void defineEnemyStrategyWhenEnemyIsZerg() {
        AEnemyStrategy detectedStrategy = AEnemyZergStrategy.detectStrategy();
        if (detectedStrategy != null) {
            changeEnemyStrategyTo(detectedStrategy);
        }
    }
    
    // =========================================================

    private static void changeEnemyStrategyTo(AEnemyStrategy strategy) {
        AGame.sendMessage("Enemy strategy: " + strategy);
        AEnemyStrategy.setEnemyStrategy(strategy);
    }
    
}
