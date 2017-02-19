package atlantis.strategy;

import atlantis.AGame;
import atlantis.units.AUnitType;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class TerranStrategyDetector {

    public static AEnemyStrategy detectStrategy() {
        int barracks = Select.enemy().countUnitsOfType(AUnitType.Terran_Barracks);

        // === Double Rax MnM ========================================
        
        if (barracks == 2 && AGame.getTimeSeconds() < 290) {
            return AEnemyStrategy.TERRAN_Double_Rax_MnM;
        }
        
        // =========================================================
        
        return null;
    }
    
}
