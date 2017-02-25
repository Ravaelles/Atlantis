package atlantis.strategy;

import atlantis.AGame;
import static atlantis.strategy.AEnemyProtossStrategy.PROTOSS_3_Gate;
import static atlantis.strategy.AEnemyStrategy.TERRAN_1_Rax_FE;
import static atlantis.strategy.AEnemyStrategy.TERRAN_BBS;
import static atlantis.strategy.AEnemyStrategy.TERRAN_Double_Rax_MnM;
import static atlantis.strategy.AEnemyStrategy.TERRAN_Three_Factory_Vultures;
import static atlantis.strategy.AEnemyStrategy.TERRAN_Tri_Rax_MnM;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AEnemyZergStrategy extends AEnemyStrategy {
    
    // Rush
    public static final AEnemyStrategy ZERG_ = new AEnemyStrategy();
    
    // Cheese
    public static final AEnemyStrategy ZERG_4_Pool = new AEnemyStrategy();
    public static final AEnemyStrategy ZERG_5_Pool = new AEnemyStrategy();
    public static final AEnemyStrategy ZERG_6_Pool = new AEnemyStrategy();
    
    // Expansion
    public static final AEnemyStrategy ZERG_ = new AEnemyStrategy();
    
    // Tech
    public static final AEnemyStrategy ZERG_ = new AEnemyStrategy();
    
    // =========================================================

    protected static void initialize() {
        
        // === Rushes ========================================
        

        // === Cheese ========================================
        
        ZERG_4_Pool.setProtoss().setName("4 Pool")
                .setGoingRush().setGoingCheese()
                .setUrl("http://wiki.teamliquid.net/starcraft/4/5_Pool");
        
        ZERG_5_Pool.setProtoss().setName("5 Pool")
                .setGoingRush().setGoingCheese()
                .setUrl("http://wiki.teamliquid.net/starcraft/5_Pool_(vs._Terran)");
        
        ZERG_6_Pool.setProtoss().setName("4 Pool")
                .setGoingRush().setGoingCheese()
                .setUrl("---");

        // === Expansion =====================================
        

        // === Tech ==========================================
        
    }
    
    public static AEnemyStrategy detectStrategy() {
        int pool = Select.enemy().countUnitsOfType(AUnitType.Zerg_Spawning_Pool);
        int drones = Select.enemy().countUnitsOfType(AUnitType.Zerg_Drone);
        int lings = Select.enemy().countUnitsOfType(AUnitType.Zerg_Zergling);

        // === Detect N-pools ========================================
        
        if (pool == 1 && drones <= 4 && AGame.getTimeSeconds() < 220) {
            return AEnemyZergStrategy.ZERG_4_Pool;
        }
        
        if (pool == 1 && drones <= 5 && AGame.getTimeSeconds() < 220) {
            return AEnemyZergStrategy.ZERG_5_Pool;
        }
        
        if (pool == 1 && drones <= 6 && AGame.getTimeSeconds() < 220) {
            return AEnemyZergStrategy.ZERG_6_Pool;
        }
        
        // =========================================================
        
        return null;
    }
    
}
