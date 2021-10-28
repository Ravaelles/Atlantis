package atlantis.strategy;

import atlantis.AGame;
import atlantis.strategy.response.AStrategyResponse;
import atlantis.strategy.response.AStrategyResponseFactory;

public class AStrategyCommander {
    
    private static final boolean hasBeenInitialized = false;
    
    // =========================================================

    /**
     * Detect enemy strategy and use our strategy accordingly.
     */
    public static void update() {

        // If we don't know enemy strategy, try to guess it based on enemy buildings/units we know
        if (AGame.timeSeconds() < 500 && AGame.getTimeFrames() % 12 == 0) {
            if (AGame.isEnemyProtoss()) {
                guessEnemyStrategyWhenEnemyIsProtoss();
            }
            else if (AGame.isEnemyTerran()) {
                guessEnemyStrategyWhenEnemyIsTerran();
            }
            else if (AGame.isEnemyZerg()) {
                guessEnemyStrategyWhenEnemyIsZerg();
            }
        }

        // =========================================================
        
        AStrategyResponseFactory.forOurRace().update();
    }
    
    // =========================================================

    private static void guessEnemyStrategyWhenEnemyIsProtoss() {
        AStrategy detectedStrategy = ProtossStrategies.detectStrategy();
        if (detectedStrategy != null) {
            changeEnemyStrategyTo(detectedStrategy);
        }
    }

    private static void guessEnemyStrategyWhenEnemyIsTerran() {
        AStrategy detectedStrategy = TerranStrategies.detectStrategy();
        if (detectedStrategy != null) {
            changeEnemyStrategyTo(detectedStrategy);
        }
    }

    private static void guessEnemyStrategyWhenEnemyIsZerg() {
        AStrategy detectedStrategy = ZergStrategies.detectStrategy();
        if (detectedStrategy != null) {
            changeEnemyStrategyTo(detectedStrategy);
        }
    }
    
    // =========================================================

    private static void changeEnemyStrategyTo(AStrategy strategy) {
        if (!EnemyStrategy.isEnemyStrategyKnown()) {
            AGame.sendMessage("Enemy strategy: " + strategy);
        }
        EnemyStrategy.setEnemyStrategy(strategy);
        AStrategyResponseFactory.forOurRace().updateEnemyStrategyChanged();
    }
    
}
