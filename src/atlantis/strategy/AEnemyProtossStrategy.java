package atlantis.strategy;

import atlantis.AGame;
import atlantis.units.AUnitType;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AEnemyProtossStrategy extends AEnemyStrategy {
    
    // Rush
    public static final AEnemyStrategy PROTOSS_2_Gate = new AEnemyStrategy();
    
    // Cheese
    public static final AEnemyStrategy PROTOSS_3_Gate = new AEnemyStrategy();
    
    // Expansion
    public static final AEnemyStrategy PROTOSS_12_Nexus = new AEnemyStrategy();
    
    // Tech
    public static final AEnemyStrategy PROTOSS_2_Gate_DT = new AEnemyStrategy();
    
    // =========================================================

    protected static void initialize() {
        
        // === Rushes ========================================
        
        PROTOSS_2_Gate.setProtoss().setName("2 Gate")
                .setGoingRush()
                .setUrl("http://wiki.teamliquid.net/starcraft/2_Gate_Zealot_(vs._Terran)");

        // === Cheese ========================================
        
        PROTOSS_3_Gate.setProtoss().setName("3 Gate")
                .setGoingRush().setGoingCheese()
                .setUrl("---");

        // === Expansion =====================================
        
        PROTOSS_12_Nexus.setProtoss().setName("12 Nexus")
                .setGoingExpansion()
                .setUrl("http://wiki.teamliquid.net/starcraft/Barracks_Barracks_Supply_(vs._Terran)");

        // === Tech ==========================================
        
        PROTOSS_2_Gate_DT.setProtoss().setName("2 Gate DT")
                .setGoingTech()
                .setUrl("http://wiki.teamliquid.net/starcraft/2_Gate_DT");
        
    }
    
    // =========================================================
    
    public static AEnemyStrategy detectStrategy() {
        int seconds = AGame.getTimeSeconds();
        int gateways = Select.enemy().countUnitsOfType(AUnitType.Protoss_Gateway);
        int nexus = Select.enemy().countUnitsOfType(AUnitType.Protoss_Nexus);
        int citadel = Select.enemy().countUnitsOfType(AUnitType.Protoss_Citadel_of_Adun);

        // === Dark Templar ========================================
        
        if (citadel >= 1 && seconds < 320) {
            return AEnemyProtossStrategy.PROTOSS_2_Gate_DT;
        }

        // === Three Gateway ========================================
        
        if (gateways >= 3 && seconds < 300) {
            return AEnemyProtossStrategy.PROTOSS_3_Gate;
        }

        // === Two Gateway ========================================
        
        if (gateways == 2 && seconds < 290) {
            return AEnemyProtossStrategy.PROTOSS_2_Gate;
        }

        // === 12 Nexus ========================================
        
        if (nexus == 2 && seconds < 290) {
            return AEnemyProtossStrategy.PROTOSS_12_Nexus;
        }
        
        // =========================================================
        
        return null;
    }
    
}
