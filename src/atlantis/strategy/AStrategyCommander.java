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
        changeEnemyStrategyTo(AEnemyStrategy.TERRAN_Double_Rax_MnM);
    }

    private static void defineEnemyStrategyWhenEnemyIsZerg() {
        
    }
    
    // =========================================================

    private static void changeEnemyStrategyTo(AEnemyStrategy TERRAN_Double_Rax_MnM) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
