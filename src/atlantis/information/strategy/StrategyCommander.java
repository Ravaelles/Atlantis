package atlantis.information.strategy;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.response.AStrategyResponseFactory;
import atlantis.util.CodeProfiler;
import atlantis.util.Enemy;

public class StrategyCommander extends Commander {
    
    /**
     * Detect enemy strategy and use our strategy accordingly.
     */
    protected void handle() {
        CodeProfiler.startMeasuring(this);

        // If we don't know enemy strategy, try to guess it based on enemy buildings/units we know
        if (GamePhase.isEarlyGame() && A.everyNthGameFrame(13)) {
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

        AStrategyResponseFactory.forOurRace().update();

        CodeProfiler.endMeasuring(this);
    }
    
    // =========================================================

    private void guessEnemyStrategyWhenEnemyIsProtoss() {
        AStrategy detectedStrategy = ProtossStrategies.detectStrategy();
        if (detectedStrategy != null) {
            changeEnemyStrategyTo(detectedStrategy);
        }
    }

    private void guessEnemyStrategyWhenEnemyIsTerran() {
        AStrategy detectedStrategy = TerranStrategies.detectStrategy();
        if (detectedStrategy != null) {
            changeEnemyStrategyTo(detectedStrategy);
        }
    }

    private void guessEnemyStrategyWhenEnemyIsZerg() {
        AStrategy detectedStrategy = ZergStrategies.detectStrategy();
        if (detectedStrategy != null) {
            changeEnemyStrategyTo(detectedStrategy);
        }
    }
    
    // =========================================================

    private void changeEnemyStrategyTo(AStrategy strategy) {
        if (!EnemyStrategy.get().equals(strategy)) {
            AGame.sendMessage("Enemy strategy detected at " + A.seconds() + "s: " + strategy);
        }
        EnemyStrategy.setEnemyStrategy(strategy);
        AStrategyResponseFactory.forOurRace().updateEnemyStrategyChanged();
    }
    
}
