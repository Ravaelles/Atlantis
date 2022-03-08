package atlantis.information.strategy;

import atlantis.game.AGame;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;


public class TerranStrategies {
    
    // Rush
    public static final AStrategy TERRAN_2_Rax_MnM = new AStrategy();
    public static final AStrategy TERRAN_3_Rax_MnM = new AStrategy();
    public static final AStrategy TERRAN_2_Rax_Academy_vZ = new AStrategy();
    public static final AStrategy TERRAN_2_Rax_Academy_vP = new AStrategy();
    public static final AStrategy TERRAN_2_Rax_Academy_vT = new AStrategy();
    public static final AStrategy TERRAN_3_Rax_Academy_vP = new AStrategy();

    // Cheese
    public static final AStrategy TERRAN_BBS = new AStrategy();

    // Expansion
    public static final AStrategy TERRAN_1_Rax_FE = new AStrategy();

    // Tech
    public static final AStrategy TERRAN_Mech = new AStrategy();
    public static final AStrategy TERRAN_Nada_2_Fac = new AStrategy();
    public static final AStrategy TERRAN_Three_Factory_Vultures = new AStrategy();

    // =========================================================

    public static void initialize() {

        // === Rushes ========================================
        
        TERRAN_2_Rax_MnM.setTerran().setName("Double Rax MnM")
                .setGoingRush()
                .setGoingBio()
                .setUrl("http://strategywiki.org/wiki/StarCraft/Terran_strategies#Terran_Double_Rax_MnM");

        TERRAN_2_Rax_Academy_vZ.setTerran().setName("2 Rax Academy vZ")
                .setGoingRush()
                .setGoingBio()
                .setUrl("https://liquipedia.net/starcraft/2_Rax_Academy_(vs._Zerg)");
        TERRAN_2_Rax_Academy_vP.setTerran().setName("2 Rax Academy vP")
                .setGoingRush()
                .setGoingBio()
                .setUrl("https://liquipedia.net/starcraft/2_Rax_Academy_(vs._Zerg)");
        TERRAN_2_Rax_Academy_vT.setTerran().setName("2 Rax Academy vT")
                .setGoingRush()
                .setGoingBio();

        TERRAN_3_Rax_Academy_vP.setTerran().setName("3 Rax Academy vP")
                .setGoingRush()
                .setGoingBio();

        // === Cheese =================================
        
        TERRAN_BBS.setTerran().setName("BBS")
                .setGoingRush().setGoingCheese()
                .setUrl("http://wiki.teamliquid.net/starcraft/Barracks_Barracks_Supply_(vs._Terran)");
        TERRAN_3_Rax_MnM.setTerran().setName("Tri-Rax MnM Rush")
                .setGoingRush()
                .setUrl("http://strategywiki.org/wiki/StarCraft/Terran_strategies#Terran_Tri-Rax_MnM_Rush");

        // === Expansion =====================================
        
        TERRAN_1_Rax_FE.setTerran().setName("1 Rax FE")
                .setGoingExpansion().setGoingTech()
                .setUrl("http://wiki.teamliquid.net/starcraft/1_Rax_FE_(vs._Terran)");

        // === Tech ==========================================

        TERRAN_Mech.setTerran().setName("Mech")
                .setGoingTech()
                .setUrl("Improvised");
        TERRAN_Nada_2_Fac.setTerran().setName("Nada 2 Fac")
                .setGoingTech()
                .setUrl("https://liquipedia.net/starcraft/Nada_2_Fac");
        TERRAN_Three_Factory_Vultures.setTerran().setName("Three Factory Vultures")
                .setGoingTech()
                .setUrl("http://wiki.teamliquid.net/starcraft/Three_Factory_Vultures");
    }
    
    // =========================================================
    
    public static AStrategy detectStrategy() {
        int seconds = AGame.timeSeconds();
        int barracks = Select.enemy().countOfType(AUnitType.Terran_Barracks);
        int bases = Select.enemy().countOfType(AUnitType.Terran_Command_Center);
        int factories = Select.enemy().countOfType(AUnitType.Terran_Factory);
        int bunkers = Select.enemy().countOfType(AUnitType.Terran_Bunker);
        int marines = Select.enemy().countOfType(AUnitType.Terran_Marine);
        int medics = Select.enemy().countOfType(AUnitType.Terran_Medic);
        
        // === Cheese ==============================================
        
        if (barracks >= 3 && seconds < 350) {
            return TerranStrategies.TERRAN_3_Rax_MnM;
        }
        
        if (barracks >= 2 && seconds < 200) {
            return TerranStrategies.TERRAN_BBS;
        }

        // === Expansion ===========================================
        
        if (bases >= 2 && factories >= 1 && seconds < 300) {
            return TerranStrategies.TERRAN_1_Rax_FE;
        }

        // === Rush ================================================
        
        if (barracks >= 2 && seconds < 350) {
            return TerranStrategies.TERRAN_2_Rax_MnM;
        }
        
        // =========================================================
        
        return null;
    }
    
}
