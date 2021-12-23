package atlantis.strategy;

import atlantis.AGame;
import atlantis.strategy.response.AStrategyResponse;
import atlantis.strategy.response.AStrategyResponseFactory;
import atlantis.util.A;
import atlantis.util.Enemy;

public class AStrategyCommander {
    
    private static final boolean hasBeenInitialized = false;
    
    // =========================================================

    /**
     * Detect enemy strategy and use our strategy accordingly.
     */
    public static void update() {

        // If we don't know enemy strategy, try to guess it based on enemy buildings/units we know
        if (GamePhase.isEarlyGame() && A.everyNthGameFrame(12)) {
            if (Enemy.protoss()) {
                guessEnemyStrategyWhenEnemyIsProtoss();
            }
            else if (Enemy.terran()) {
                guessEnemyStrategyWhenEnemyIsTerran();
            }
            else if (Enemy.zerg()) {
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
        if (!EnemyStrategy.get().equals(strategy)) {
            AGame.sendMessage("Enemy strategy detected: " + strategy);
        }
        EnemyStrategy.setEnemyStrategy(strategy);
        AStrategyResponseFactory.forOurRace().updateEnemyStrategyChanged();
    }
    
}
