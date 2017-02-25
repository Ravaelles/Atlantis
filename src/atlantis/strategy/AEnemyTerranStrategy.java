package atlantis.strategy;

import atlantis.AGame;
import atlantis.units.AUnitType;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AEnemyTerranStrategy extends AEnemyStrategy {
    
    // Rush
    public static final AEnemyStrategy TERRAN_Double_Rax_MnM = new AEnemyStrategy();
    public static final AEnemyStrategy TERRAN_Tri_Rax_MnM = new AEnemyStrategy();
    
    // Cheese
    public static final AEnemyStrategy TERRAN_BBS = new AEnemyStrategy();
    
    // Expansion
    public static final AEnemyStrategy TERRAN_1_Rax_FE = new AEnemyStrategy();
    
    // Tech
    public static final AEnemyStrategy TERRAN_Three_Factory_Vultures = new AEnemyStrategy();
    
    // =========================================================

    protected static void initialize() {

        // === Rushes ========================================
        
        TERRAN_Double_Rax_MnM.setTerran().setName("Double Rax MnM")
                .setGoingRush()
                .setUrl("http://strategywiki.org/wiki/StarCraft/Terran_strategies#Terran_Double_Rax_MnM");

        // === Cheese =================================
        
        TERRAN_BBS.setTerran().setName("BBS")
                .setGoingRush().setGoingCheese()
                .setUrl("http://wiki.teamliquid.net/starcraft/Barracks_Barracks_Supply_(vs._Terran)");
        TERRAN_Tri_Rax_MnM.setTerran().setName("Tri-Rax MnM Rush")
                .setGoingRush()
                .setUrl("http://strategywiki.org/wiki/StarCraft/Terran_strategies#Terran_Tri-Rax_MnM_Rush");

        // === Expansion =====================================
        
        TERRAN_1_Rax_FE.setTerran().setName("1 Rax FE")
                .setGoingExpansion().setGoingTech()
                .setUrl("http://wiki.teamliquid.net/starcraft/1_Rax_FE_(vs._Terran)");

        // === Tech ==========================================
        
        TERRAN_Three_Factory_Vultures.setTerran().setName("Three Factory Vultures")
                .setGoingTech()
                .setUrl("http://wiki.teamliquid.net/starcraft/Three_Factory_Vultures");
    }
    
    // =========================================================
    
    public static AEnemyStrategy detectStrategy() {
        int seconds = AGame.getTimeSeconds();
        int barracks = Select.enemy().countUnitsOfType(AUnitType.Terran_Barracks);

        // === Double Rax MnM ========================================
        
        if (barracks == 2 && seconds < 290) {
            return AEnemyTerranStrategy.TERRAN_Double_Rax_MnM;
        }
        
        // =========================================================
        
        return null;
    }
    
}
