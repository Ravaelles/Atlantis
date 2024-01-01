package main;

import atlantis.Atlantis;
import atlantis.config.AtlantisIgniter;
import atlantis.config.ActiveMap;
import atlantis.config.env.Env;
import atlantis.keyboard.AKeyboard;
import atlantis.util.ProcessHelper;

/**
 * This is the main class of the bot. Here everything starts.
 * <p>
 * "A journey of a thousand miles begins with a single step." - Lao Tse
 */
public class Main {
    public static final String OUR_RACE = "Protoss";
//    public static final String OUR_RACE = "Terran";
//    public static final String OUR_RACE = "Zerg";

    public static final String ENEMY_RACE = "Protoss";
//    public static final String ENEMY_RACE = "Terran";
//    public static final String ENEMY_RACE = "Zerg";

    /**
     * Will modify bwapi.ini to use this map.
     */
    public static final String MAP = ActiveMap.activeMapPath();

    /**
     * Sets up Atlantis config and runs the bot.
     */
    public static void main(String[] args) {
        Env.readEnvFile(args);

        // If run locally (not in tournament) auto-start Starcraft.exe and Chaoslauncher, modify bwapi.ini etc
        if (Env.isLocal()) {
            ProcessHelper.killStarcraftProcess();
            ProcessHelper.killChaosLauncherProcess();

            // Dynamically modify bwapi.ini file, change race and enemy race.
            // If you want to change your/enemy race, edit AtlantisRaceConfig constants.
            AtlantisIgniter.modifyBwapiFileIfNeeded();

            // Listen for keyboard events
            AKeyboard.listenForKeyEvents();

            // IMPORTANT: Make sure Chaoslauncher -> Settings -> "Run Starcraft on Startup" is checked
            ProcessHelper.startChaosLauncherProcess();
        }

        // =============================================================
        // =============================================================
        // ==== See AtlantisRaceConfig class to customize execution ====
        // =============================================================
        // =============================================================

        // Create Atlantis object, it is a wrapper around JBWAPI
        Atlantis atlantis = new Atlantis();

        // Start the bot
        atlantis.run();
    }

    public static String activeMap() {
//        if (true) return "sscai/(?)*.sc?"; // Default map-pack for SSCAIT

        // === Popular SSCAIT maps =================================

//        if (true) return "sscai/(2)HeartbreakRidge.scx";
        if (true) return "sscai/(2)Destination.scx";
//        if (true) return "sscai/(4)Roa.scx";

        // === Gosu bots - advanced single player cheating bots ====

        // vs PROTOSS cheat-bots
        /**
         * vs AI Protoss player - ♥‿♥ - My two favorite maps for testing,
         * features ugly Reaver+Archon spawn in the main at around 85 supply that is hard to stop
         */
//        if (true) return "ums/7th.scx";
//        if (true) return "ums/exp_skilltest.scx"; // Terran v. AI Protoss player

//        if (true) return "ums/member_test.scx"; // v. AI 2x Protoss players, massive Zealot rush
//        if (true) return "ums/LostTemple.scm"; // v. 3x Protoss players, again Zealot rush

        // vs TERRAN cheat-bots
//        if (true) return "ums/lt-terran1j.scm"; // Zerg v. Terran

        // vs ZERG cheat-bots
//        if (true) return "ums/vsGosuComputer.scx"; // v. AI Zerg Player - cheating as fuck
//        if (true) return "ums/rav/vsGosuRav.scx"; // Like above, but starting at middle game

        // === Maps for testing as Terran ==========================

        // vs Zerg
//        if (true) return "ums/rav/TankBall_v_Zerg.scx"; // Tanks & Marines v Zerg Base
//        if (true) return "ums/rav/TBall_v_HydraLings.scx"; // Tanks & Marines v Hydras & Lings
//        if (true) return "ums/rav/M&M_v_Hydras.scx"; // M&M v Hydras
//        if (true) return "ums/rav/T_v_Sunkens.scx"; // M&M + Tank + Wraith v Sunkens
//        if (true) return "ums/rav/M_v_1Ling.scm"; // 1 Marine v 1 Zergling
//        if (true) return "ums/rav/Wraiths_v_Base.scx"; // Wraiths v CC & Turret & Workers
//        if (true) return "ums/rav/Wraiths_v_Base2.scm"; // Wraiths v Hatchery & Spore & Workers

        // vs Terran
//        if (true) return "ums/rav/Wraith_v_Wraith.scm";
//        if (true) return "ums/rav/Wraiths_v_Base.scx";
//@        if (true) return "ums/rav/Bunker_v_M&M.scx";
//        if (true) return "ums/rav/Tanks_v_Tanks_in_line.scx";
//        if (true) return "ums/rav/Tanks_v_Tanks_with_choke.scx";
//        if (true) return "ums/rav/Tanks_v_Wraiths.scm"; // Make sure that SCVs can construct turrets ad hoc
//        if (true) return "ums/rav/Wraiths_v_Zerg.scm"; // Wraiths v Scourge + Overlord + Guardian

        // vs Protoss
        if (true) return "ums/rav/minimaps/M&M_v_Zealots.scx"; // Marines & Medics v Zealots
//        if (true) return "ums/rav/Bunker_v_Zealots.scx";
//        if (true) return "ums/rav/M_v_Zealots_map.scx"; // Marines running from Zealots on big map
//        if (true) return "ums/rav/minimaps/M_v_Zealots.scx"; // Marines v Zealots
//        if (true) return "ums/rav/minimaps/2M_v_1Zealot.scm";
//        if (true) return "ums/rav/minimaps/3M_v_2Zealots.scx";
        if (true) return "ums/rav/minimaps/4M_v_2Zealots.scx";
//        if (true) return "ums/rav/minimaps/4M_v_1Zealot.scm";
//        if (true) return "ums/rav/M_v_Zealots.scx"; // Many Zealots v Many Marines in a Terran base
//        if (true) return "ums/rav/Wraiths_v_Protoss.scm"; // Wraiths v Reavers & HT & Observer
//@        if (true) return "ums/rav/Ghosts_v_P.scx";
//        if (true) return "ums/rav/M&M_v_Dragoons_A.scx";
//        if (true) return "ums/rav/M&M_v_Dragoons_B.scx";
//@        if (true) return "ums/rav/M&M_v_M&M.scx"; // Yours in bad line formation, ~10 away from enemies
//@        if (true) return "ums/rav/M&M_v_M&M_2.scx"; // Standing in lines, shooting at another
//@        if (true) return "ums/rav/M&M_v_M&M_3.scx"; // You attacking behind the corner
//@        if (true) return "ums/rav/M&M_v_T.scx"; // Terran bio engaging with units that keep on adding
//        if (true) return "ums/rav/T_rebasing.scm"; // CC that mines out, tests the rebasing process
//@        if (true) return "ums/rav/Tanks_v_DT.scx";
//@        if (true) return "ums/rav/Tanks_v_DT_2.scx";
//        if (true) return "ums/rav/Tanks_v_Lurkers.scx"; // Tanks & Marines vs Lurkers
//@        if (true) return "ums/rav/TanksM&M_v_ZealDrag.scx";
//        if (true) return "ums/rav/Vultures_v_Dragoons.scm";
//@        if (true) return "ums/rav/Vultures_v_Marines.scm";
//        if (true) return "ums/rav/Vultures_v_Zealots.scm";
//        if (true) return "ums/rav/Wraiths_v_Cannons.scm";
//        if (true) return "ums/rav/Wraiths_v_Probes.scm"; // Wraiths v Probes & Cannons
//        if (true) return "ums/Jim_v_Lurker.scx"; // Marine vs Lurker
//        if (true) return "ums/InfantryControl.scx"; // Terran Infantry units vs enemies
//        if (true) return "ums/gol_v_zeals.scx"; // Goliath & Dropship v Zerglings
//        if (true) return "ums/marines/m (5).scx"; // Nice map to test different terran infantry in rounds
//        if (true) return "ums/marines/m (8).scx"; // Hmm
//        if (true) return "ums/rav/M&M_v_Cannons.scx";

        // === Maps for testing as Zerg ============================

//        if (true) return "ums/rav/Lings_v_Zealots.scm";
//        if (true) return "ums/Muta Micro_ Python.scm"; // Need crazy good squad management
//@        if (true) return "ums/rav/Z+H_v_Zealots+Dragoons.scm";

        // === Maps for testing as Protoss ========================

//        if (true) return "ums/rav/1Drag_v_1Drag.scm";
//        if (true) return "ums/rav/2Drag_v_2Drag.scm";
//        if (true) return "ums/rav/3Drag_v_1Drag.scm";
//        if (true) return "ums/rav/3Drag_v_4Drag.scm";
        if (true) return "ums/rav/3Drag_v_3Drag.scm";
//        if (true) return "ums/rav/3Zeal_v_1Zeal.scm";
//        if (true) return "ums/rav/4Drag_v_4Drag_withBase.scm";
//        if (true) return "ums/rav/4Drag_v_4Drag.scm";
//        if (true) return "ums/rav/4Drag_v_5Drag.scm";
//        if (true) return "ums/rav/4Drag_v_Zeal.scm";
//@        if (true) return "ums/rav/Drag&Zeal_v_M&M.scx";
//@        if (true) return "ums/rav/Dragoon_v_Zealot.scm";
//        if (true) return "ums/rav/DragoonsRange_v_Marines.scm";
//        if (true) return "ums/rav/Dragoons_v_Marines.scm";
//        if (true) return "ums/rav/Dragoons_v_Zealots.scm";
//        if (true) return "ums/rav/Dragoons_v_Zerglings.scm";
//        if (true) return "ums/rav/P2_v_Buildings.scx";  // Dragoons vs Units+Buildings
//        if (true) return "ums/rav/P_v_Buildings.scx"; // Zealots+Dragoons vs Units+Buildings
//@        if (true) return "ums/rav/Zeal_v_Zeal.scm";
//        if (true) return "ums/rav/ZealDrag_v_LingsHydra.scm";
//        if (true) return "ums/rav/ZealDrag_v_ZealDrag.scm";
//        if (true) return "ums/wraiths_v_carriers_obs.scx"; // Wraiths & Valkyries v. Carriers & Observers

        // === Generic ======================================================

//        if (true) return "ums/dragoons_v_map.scx"; // 4 Dragoons attacking Zealots
//        if (true) return "ums/dragoon_sweeping_mines.scm"; // 5 dragoons v. mines
//        if (true) return "ums/mar_v_zea.scx"; // Marines & Medics v. Zealots on quite small map
//        if (true) return "ums/NeWconTrol.scx"; // Cool minigames, starting with 2 drones v. 2 drones, lings v. goons etc
//        if (true) return "ums/training-PvT.scx"; // Dragoons & Zealots v. Vultures & Tanks + slowly Hi-Templars & Archons
//        if (true) return "ums/marines_v_zerglings.scm"; // 12 marines v. 24 zerglings
//        if (true) return "ums/ConTrol2.scx"; // More minigames
//        if (true) return "ums/micro challenge.scx"; // Even more minigames
//        if (true) return "ums/tank-dropship.scm"; // 2 Tanks & 2 Dropships v. Dragoons
//        if (true) return "ums/trainzvreaver.scm"; // Zerglings & Hydras v. 2 Reavers & Shuttle
//        if (true) return "ums/trening nr 2.scx";
//        if (true) return "ums/micro tvp 1.00.scx"; // Huge Terran army (tanks & vultures) v. Zealots & Hi-Templars & Dragoons
//        if (true) return "ums/micro3.scx";
//        if (true) return "ums/(1)micro3_007.scx";
//        if (true) return "ums/vulture_control.scx"; // Vulture v. Broodlings
//        if (true) return "ums/MultiTask PvT.scx"; // Weird - ums but starts with bases
//        if (true) return "ums/ControlFighterTZ-Easy.scx"; // Tanks & Marines v. Zerg
//        if (true) return "ums/protoss_micro.scx"; // Huge parallel map for Protosss

        return "sscai/(?)*.sc?";
    }
}
