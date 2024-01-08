package atlantis.information.strategy.terran;

import atlantis.game.AGame;
import atlantis.information.strategy.AStrategy;
import atlantis.units.AUnitType;

public class TerranStrategies extends AStrategy {

    // Cheese
    public static final AStrategy TERRAN_BBS = new AStrategy();

    // Rush
    public static final AStrategy TERRAN_Sparks = new Sparks();
    public static final AStrategy TERRAN_Shallow_Two_vP = new AStrategy(); // Marine Medic Ghost (with Lockdown)
    public static final AStrategy TERRAN_2_Rax_MnM = new AStrategy();
    public static final AStrategy TERRAN_3_Rax_MnM = new AStrategy();
    public static final AStrategy TERRAN_2_Rax_Academy_vZ = new AStrategy();
    public static final AStrategy TERRAN_2_Rax_Academy_vP = new AStrategy();
    public static final AStrategy TERRAN_2_Rax_Academy_vT = new AStrategy();
    public static final AStrategy TERRAN_3_Rax_Academy_vP = new AStrategy();

    // Expansion
    public static final AStrategy TERRAN_1_Rax_FE = new AStrategy();

    // Tech
    public static final AStrategy TERRAN_Mech = new AStrategy();
    public static final AStrategy TERRAN_Nada_2_Fac = new AStrategy();
    public static final AStrategy TERRAN_Three_Factory_Vultures = new AStrategy();
    public static final AStrategy TERRAN_Tests = new AStrategy(); // Marine Medic Ghost (with Lockdown)

    // =========================================================

    public static void loadAll() {

        // === Rushes ========================================

//        TERRAN_Sparks.setTerran().setName("Sparks").setGoingBio().setGoingRush();

        TERRAN_Shallow_Two_vP.setTerran().setName("Shallow Two vP").setGoingBio();

        TERRAN_2_Rax_MnM.setTerran().setName("Double Rax MnM").setGoingRush().setGoingBio();

        TERRAN_2_Rax_Academy_vZ.setTerran().setName("2 Rax Academy vZ").setGoingRush().setGoingBio();

        TERRAN_2_Rax_Academy_vP.setTerran().setName("2 Rax Academy vP").setGoingRush().setGoingBio();

        TERRAN_2_Rax_Academy_vT.setTerran().setName("2 Rax Academy vT").setGoingRush().setGoingBio();

        TERRAN_3_Rax_Academy_vP.setTerran().setName("3 Rax Academy vP").setGoingRush().setGoingBio();

        // === Cheese =================================

        TERRAN_BBS.setTerran().setName("BBS").setGoingRush().setGoingCheese();

        TERRAN_3_Rax_MnM.setTerran().setName("Tri-Rax MnM Rush").setGoingRush();

        // === Expansion =====================================

        TERRAN_1_Rax_FE.setTerran().setName("1 Rax FE").setGoingExpansion().setGoingTech();

        // === Tech ==========================================

        TERRAN_Mech.setTerran().setName("Mech").setGoingTech();

        TERRAN_Nada_2_Fac.setTerran().setName("Nada 2 Fac").setGoingTech();

        TERRAN_Three_Factory_Vultures.setTerran().setName("Three Factory Vultures").setGoingTech();

        TERRAN_Tests.setTerran().setName("Terran strategy for Tests").setGoingTech();
    }

    // =========================================================

    public static AStrategy detectStrategy() {
        int seconds = AGame.timeSeconds();
        int barracks = count(AUnitType.Terran_Barracks);
        int bases = count(AUnitType.Terran_Command_Center);
        int factories = count(AUnitType.Terran_Factory);
        int bunkers = count(AUnitType.Terran_Bunker);
        int marines = count(AUnitType.Terran_Marine);
        int medics = count(AUnitType.Terran_Medic);

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
