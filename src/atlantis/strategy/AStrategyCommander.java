package atlantis.strategy;

import atlantis.AGame;

public class AStrategyCommander {
    
    private static boolean hasBeenInitialized = false;
    
    // =========================================================

    /**
     * Detect enemy strategy and use our strategy accordingly.
     */
    public static void update() {
        if (!hasBeenInitialized) {
            autoInitialize();
        }
        
        // If we don't know enemy strategy, try to define it based on enemy buildings/units we know
        if (AGame.getTimeSeconds() < 500 && AGame.getTimeFrames() % 12 == 0) {
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

        // =========================================================
        
        AStrategyResponse.update();
    }
    
    // =========================================================
    
    /**
     * Executed on class load.
     */
    private static void autoInitialize() {
        AEnemyTerranStrategy.initialize();
        AEnemyProtossStrategy.initialize();
        AEnemyZergStrategy.initialize();
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
        if (!AEnemyStrategy.isEnemyStrategyKnown()) {
            AGame.sendMessage("Enemy strategy: " + strategy);
        }
        AEnemyStrategy.setEnemyStrategy(strategy);
        AStrategyResponse.updateEnemyStrategyChanged();
    }
    
}
