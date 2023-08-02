package atlantis.config;

public class MapAndRace {

    /**
     * Race used by the Atlantis.
     */
//    public static final String OUR_RACE = "Protoss";
    public static final String OUR_RACE = "Terran";
//    public static final String OUR_RACE = "Zerg";

    /**
     * Single player enemy race.
     */
//    public static final String ENEMY_RACE = "Protoss";
//    public static final String ENEMY_RACE = "Terran";
    public static final String ENEMY_RACE = "Zerg";

    /**
     * Will modify bwapi.ini to use this map.
     */
    public static final String MAP = activeMapPath();

    // =========================================================

    public static String activeMap() {
//        return "sscai/(?)*.sc?"; // Default map-pack for SSCAIT

        // === Popular SSCAIT maps =================================

//        return "sscai/(2)HeartbreakRidge.scx";
//        return "sscai/(2)Destination.scx";
//        return "sscai/(4)Roadrunner.scx";

        // === Gosu bots - advanced single player cheating bots ====

        // vs PROTOSS cheat-bots
//        return "ums/7th.scx"; // v. AI Protoss player, that can kill CSv constructing
//        return "ums/exp_as_protoss.scx"; // Protoss v. AI Protoss player
//        return "ums/exp_skilltest.scx"; // Terran v. AI Protoss player
//        return "ums/member_test.scx"; // v. AI 2x Protoss players, massive Zealot rush
//        return "ums/LostTemple.scm"; // v. 3x Protoss players, again Zealot rush
//@        return "ums/exp_as_zerg.scx"; // Zerg v. AI Protoss player

        // vs TERRAN cheat-bots
//        return "ums/lt-terran1j.scm"; // Zerg v. Terran

        // vs ZERG cheat-bots
//        return "ums/vsGosuComputer.scx"; // v. AI Zerg Player - cheating as fuck
//        return "ums/rav/vsGosuRav.scx"; // Like above, but starting at middle game

        // === Terran ==============================================

        // vs Zerg
//        return "ums/rav/M&M_v_Hydras.scx"; // M&M v Hydras
//        return "ums/rav/T_v_Sunkens.scx"; // M&M + Tank + Wraith v Sunkens
//        return "ums/rav/M_v_1Ling.scm"; // 1 Marine v 1 Zergling
//        return "ums/rav/Wraiths_v_Base.scx"; // Wraiths v CC & Turret & Workers
//        return "ums/rav/Wraiths_v_Base2.scm"; // Wraiths v Hatchery & Spore & Workers
        return "ums/rav/TBall_v_HydraLings.scx"; // Tanks & Marines v Hydras & Lings

        // vs Protoss
//        return "ums/rav/minimaps/M&M_v_Zealots.scx"; // Marines & Medics v Zealots
//        return "ums/rav/minimaps/M_v_Zealots.scx"; // Marines v Zealots
//        return "ums/rav/minimaps/2M_v_1Zealot.scm";
//        return "ums/rav/minimaps/3M_v_2Zealots.scx";
//        return "ums/rav/minimaps/4M_v_2Zealots.scx";
//        return "ums/rav/minimaps/4M_v_1Zealot.scm";
//        return "ums/rav/M_v_Zealots.scx"; // Many Zealots v Many Marines in a Terran base
//        return "ums/rav/Wraiths_v_Protoss.scm"; // Wraiths v Reavers & HT & Observer
//        return "ums/rav/Bunker_v_Zealots.scx";
//@        return "ums/rav/Ghosts_v_P.scx";
//        return "ums/rav/M&M_v_Dragoons_A.scx";
//        return "ums/rav/M&M_v_Dragoons_B.scx";
//@        return "ums/rav/M&M_v_M&M.scx"; // Yours in bad line formation, ~10 away from enemies
//@        return "ums/rav/M&M_v_M&M_2.scx"; // Standing in lines, shooting at another
//@        return "ums/rav/M&M_v_M&M_3.scx"; // You attacking behind the corner
//@        return "ums/rav/M&M_v_T.scx"; // Terran bio engaging with units that keep on adding
//        return "ums/rav/T_rebasing.scm"; // CC that mines out, tests proper rebasing process
//@        return "ums/rav/Tanks_v_DT.scx";
//@        return "ums/rav/Tanks_v_DT_2.scx";
//        return "ums/rav/Tanks_v_Lurkers.scx"; // Tanks & Marines vs Lurkers
//@        return "ums/rav/TanksM&M_v_ZealDrag.scx";
//        return "ums/rav/Vultures_v_Dragoons.scm";
//@        return "ums/rav/Vultures_v_Marines.scm";
//        return "ums/rav/Vultures_v_Zealots.scm";
//        return "ums/rav/Wraiths_v_Cannons.scm";
//        return "ums/rav/Wraiths_v_Probes.scm"; // Wraiths v Probes & Cannons
//        return "ums/Jim_v_Lurker.scx"; // Marine vs Lurker
//        return "ums/InfantryControl.scx"; // Terran Infantry units vs enemies
//        return "ums/gol_v_zeals.scx"; // Goliath & Dropship v Zerglings
//        return "ums/marines/m (5).scx"; // Nice map to test different terran infantry in rounds
//        return "ums/marines/m (8).scx"; // Hmm
//        return "ums/rav/M&M_v_Cannons.scx";

        // vs Terran
//        return "ums/rav/Wraiths_v_Base.scx";
//@        return "ums/rav/Bunker_v_M&M.scx";

        // === Zerg ================================================

//        return "ums/rav/Wraiths_v_Zerg.scm"; // Wraiths v Scourge + Overlord + Guardian
//        return "ums/Muta Micro_ Python.scm"; // Need good squad management
//        return "ums/vulture_v_zerglings.scx"; // 1 Vulture v entire map of Lings
//@        return "ums/rav/Z+H_v_Zealots+Dragoons.scm";

        // === Protoss ============================================

//        return "ums/rav/3Drag_v_1Drag.scm";
//        return "ums/rav/3Drag_v_4Drag.scm";
//        return "ums/rav/3Zeal_v_1Zeal.scm";
//        return "ums/rav/4Drag_v_4Drag_withBase.scm";
//        return "ums/rav/4Drag_v_4Drag.scm";
//        return "ums/rav/4Drag_v_5Drag.scm";
//        return "ums/rav/4Drag_v_Zeal.scm";
//        return "ums/rav/Drag&Zeal_v_M&M.scx";
//        return "ums/rav/Dragoon_v_Zealot.scm";
//        return "ums/rav/DragoonsRange_v_Marines.scm";
//        return "ums/rav/Dragoons_v_Marines.scm";
//        return "ums/rav/Dragoons_v_Zealots.scm";
//        return "ums/rav/Dragoons_v_Zerglings.scm";
//        return "ums/rav/P2_v_Buildings.scx";  // Dragoons vs Units+Buildings
//        return "ums/rav/P_v_Buildings.scx"; // Zealots+Dragoons vs Units+Buildings
//        return "ums/rav/Zeal_v_Zeal.scm";
//        return "ums/rav/ZealDrag_v_LingsHydra.scm";
//        return "ums/rav/ZealDrag_v_ZealDrag.scm";
//        return "ums/wraiths_v_carriers_obs.scx"; // Wraiths & Valkyries v. Carriers & Observers

        // === Generic ======================================================

//        return "ums/dragoons_v_map.scx"; // 4 Dragoons attacking Zealots
//        return "ums/dragoon_sweeping_mines.scm"; // 5 dragoons v. mines
//        return "ums/mar_v_zea.scx"; // Marines & Medics v. Zealots on quite small map
//        return "ums/NeWconTrol.scx"; // Cool minigames, starting with 2 drones v. 2 drones, lings v. goons etc
//        return "ums/training-PvT.scx"; // Dragoons & Zealots v. Vultures & Tanks + slowly Hi-Templars & Archons
//        return "ums/marines_v_zerglings.scm"; // 12 marines v. 24 zerglings
//        return "ums/ConTrol2.scx"; // More minigames
//        return "ums/micro challenge.scx"; // Even more minigames
//        return "ums/tank-dropship.scm"; // 2 Tanks & 2 Dropships v. Dragoons
//        return "ums/trainzvreaver.scm"; // Zerglings & Hydras v. 2 Reavers & Shuttle
//        return "ums/trening nr 2.scx";
//        return "ums/micro tvp 1.00.scx"; // Huge Terran army (tanks & vultures) v. Zealots & Hi-Templars & Dragoons
//        return "ums/micro3.scx";
//        return "ums/(1)micro3_007.scx";
//        return "ums/vulture_control.scx"; // Vulture v. Broodlings
//        return "ums/MultiTask PvT.scx"; // Weird - ums but starts with bases
//        return "ums/ControlFighterTZ-Easy.scx"; // Tanks & Marines v. Zerg
//        return "ums/protoss_micro.scx"; // Huge parallel map for Protosss

//        return "sscai/(?)*.sc?";
    }

    // =========================================================

    public static String activeMapPath() {
        return "maps/BroodWar/" + activeMap();
    }

    public static boolean isMap(String mapPartialName) {
        return activeMap().contains(mapPartialName);
    }
}
