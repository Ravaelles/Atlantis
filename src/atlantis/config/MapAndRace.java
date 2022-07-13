package atlantis.config;

import atlantis.Atlantis;
import atlantis.config.env.Env;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.game.CameraManager;
import atlantis.game.GameSpeed;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

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
    public static final String ENEMY_RACE = "Terran";
//    public static final String ENEMY_RACE = "Zerg";

    /**
     * Will modify bwapi.ini to use this map.
     */
    public static final String MAP = activeMapPath();

    // =========================================================

    public static String activeMap() {
//        if (true) return "sscai/(?)*.sc?"; // Default map-pack for SSCAIT

//        if (true) return "sscai/(2)Heartbreak Ridge.scx";
        if (true) return "sscai/(2)Destination.scx";
//        if (true) return "sscai/(4)Roadrunner.scx";

        // === Zerg ================================================

//        if (true) return "ums/Muta Micro_ Python.scm"; // Need good squad management
//        if (true) return "ums/rav/Z+H_v_Zealots+Dragoons.scm";

        // === Protoss ============================================

//        if (true) return "ums/rav/3Drag_v_1Drag.scm";
//        if (true) return "ums/rav/3Drag_v_4Drag.scm";
//        if (true) return "ums/rav/3Zeal_v_1Zeal.scm";
//        if (true) return "ums/rav/4Drag_v_4Drag_withBase.scm";
//        if (true) return "ums/rav/4Drag_v_4Drag.scm";
//        if (true) return "ums/rav/4Drag_v_5Drag.scm";
//        if (true) return "ums/rav/4Drag_v_Zeal.scm";
//        if (true) return "ums/rav/Drag&Zeal_v_M&M.scx";
//        if (true) return "ums/rav/Dragoon_v_Zealot.scm";
//        if (true) return "ums/rav/DragoonsRange_v_Marines.scm";
//        if (true) return "ums/rav/Dragoons_v_Marines.scm";
//        if (true) return "ums/rav/Dragoons_v_Zealots.scm";
//        if (true) return "ums/rav/Dragoons_v_Zerglings.scm";
//        if (true) return "ums/rav/P2_v_Buildings.scx";  // Dragoons vs Units+Buildings
//        if (true) return "ums/rav/P_v_Buildings.scx"; // Zealots+Dragoons vs Units+Buildings
//        if (true) return "ums/rav/Zeal_v_Zeal.scm";
//        if (true) return "ums/rav/ZealDrag_v_LingsHydra.scm";
//        if (true) return "ums/rav/ZealDrag_v_ZealDrag.scm";

        // === Terran ==============================================

//        if (true) return "ums/rav/4M_v_2Zealots.scm";
//        if (true) return "ums/rav/Bunker_v_M&M.scx";
//        if (true) return "ums/rav/Ghosts_v_P.scx";
//        if (true) return "ums/rav/M&M_v_Dragoons_A.scx";
//        if (true) return "ums/rav/M&M_v_Dragoons_B.scx";
//        if (true) return "ums/rav/M&M_v_M&M.scx"; // Yours in bad line formation, ~10 away from enemies
//        if (true) return "ums/rav/M&M_v_M&M_2.scx"; // Standing in lines, shooting at another
//        if (true) return "ums/rav/M&M_v_M&M_3.scx"; // You attacking behind the corner
//        if (true) return "ums/rav/M&M_v_T.scx"; // Terran bio engaging with units that keep on adding
//        if (true) return "ums/rav/M_v_Zealots.scx";
//        if (true) return "ums/rav/minimaps/3M_v_2Zealots.scx";
//        if (true) return "ums/rav/minimaps/4M_v_2Zealots.scx";
//        if (true) return "ums/rav/minimaps/M&M_v_Zealots.scx";
//        if (true) return "ums/rav/minimaps/M_v_Zealots.scx";
//        if (true) return "ums/rav/T_rebasing.scm";
        if (true) return "ums/rav/T_v_Sunkens.scx";
//        if (true) return "ums/rav/T_v_Sunkens2.scx";
//        if (true) return "ums/rav/T_v_Sunkens3.scx";
//        if (true) return "ums/rav/Tanks_v_DT.scx";
//        if (true) return "ums/rav/Tanks_v_DT_2.scx";
//        if (true) return "ums/rav/TanksM&M_v_ZealDrag.scx";
//        if (true) return "ums/rav/Vultures_v_Dragoons.scm";
//        if (true) return "ums/rav/Vultures_v_Marines.scm";
//        if (true) return "ums/rav/Vultures_v_Zealots.scm";
//        if (true) return "ums/rav/Wraiths_v_Cannons.scm";
//        if (true) return "ums/rav/Wraiths_v_Probes.scm";

        // === Gosu bots - advanced single player cheating bots ====

        if (true) return "ums/7th.scx"; // v. AI Protoss player, that can kill CSv constructing
//        if (true) return "ums/exp_as_protoss.scx"; // Protoss v. AI Protoss player
//        if (true) return "ums/exp_skilltest.scx"; // Terran v. AI Protoss player
//        if (true) return "ums/exp_as_zerg.scx"; // Zerg v. AI Protoss player
//        if (true) return "ums/vsGosuComputer.scx"; // v. AI Zerg Player - cheating as fuck
//        if (true) return "ums/lt-terran1j.scm"; // Zerg v. Terran
//        if (true) return "ums/member_test.scx"; // v. AI 2x Protoss players, massive Zealot rush
//        if (true) return "ums/LostTemple.scm"; // v. 3x Protoss players, again Zealot rush

        // =========================================================

//        if (true) return "ums/";
//        if (true) return "ums/";
//        if (true) return "ums/(1)Aracahn's Call A.scx"; // RPG
//        if (true) return "ums/gol_v_zeals.scx";
//        if (true) return "ums/aaa (1).scx";
//        if (true) return "ums/aaa (2).scx";
//        if (true) return "ums/aaa (3).scx";
//        if (true) return "ums/aaa (4).scx";
//        if (true) return "ums/aaa (5).scx";
//        if (true) return "ums/aaa (6).scx";

//        if (true) return "ums/marines/m (5).scx"; // Nice map to test different terran infantry in rounds
//        if (true) return "ums/marines/m (8).scx"; // Hmm

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
//        if (true) return "ums/wraiths_v_carriers_obs.scx"; // Wraiths & Valkyries v. Carriers & Observers
//        if (true) return "ums/(1)micro3_007.scx";
//        if (true) return "ums/vulture_control.scx"; // Vulture v. Broodlings
//        if (true) return "ums/MultiTask PvT.scx"; // Weird - ums but starts with bases
//        if (true) return "ums/ControlFighterTZ-Easy.scx"; // Tanks & Marines v. Zerg
//        if (true) return "ums/protoss_micro.scx"; // Huge parallel map, bad performance wise

        return "sscai/(?)*.sc?";
    }

    // =========================================================

    public static String activeMapPath() {
        return "maps/BroodWar/" + activeMap();
    }

    // =========================================================

    public static void updateMapSpecific() {
        if (Env.isParamTweaker() || !Env.isLocal()) {
            return;
        }

        // =========================================================
        // Marines & Medics v. Zealots

        if (
                activeMap().startsWith("ums/rav/")
                || activeMap().startsWith("ums/rav/minimaps/")
        ) {
            if (A.now() <= 1) {
                GameSpeed.changeSpeedTo(30);
                GameSpeed.changeFrameSkipTo(0);
            }
        }

        // =========================================================

        else if (
            activeMap().equals("ums/rav/Dragoon_v_Zealot.scm")
            || activeMap().equals("ums/rav/Dragoons_v_Zealots.scm")
        ) {
            if (A.now() <= 1) {
                GameSpeed.changeSpeedTo(50);
                GameSpeed.changeFrameSkipTo(0);
            }
        }

        // =========================================================
        // v. ZERG / PROTOSS / Gosu maps

        else if (
                activeMap().equals("ums/vsGosuComputer.scx")
                || activeMap().equals("ums/exp_skilltest.scx")
                || activeMap().equals("ums/7th.scx")
        ) {
            int initFrameSkip = 0;

            if (A.now() <= 1) {
                GameSpeed.changeSpeedTo(0);
                GameSpeed.changeFrameSkipTo(initFrameSkip);

//                CameraManager.centerCameraOn((new MissionDefendFocusPoint()).focusPoint());
            }

//            if (
//                GameSpeed.frameSkip == initFrameSkip
//                    && GameSpeed.gameSpeed == 0
//                    && A.everyNthGameFrame(51)
//            ) {
//                if (
//                        EnemyUnits.discovered().atLeast(4)
//                        && (Have.dragoon() || Have.cannon())
//                ) {
//                    GameSpeed.changeSpeedTo(10);
//                    GameSpeed.changeFrameSkipTo(0);
//                }
//            }
        }

        // =========================================================
        // Marines v. ZERGLINGS

        else if (activeMap().equals("ums/marines_v_zerglings.scm")) {
            if (A.now() <= 1) {
                GameSpeed.changeSpeedTo(20);
                GameSpeed.changeFrameSkipTo(0);
            }
            if (Atlantis.KILLED >= 32) {
                Atlantis.getInstance().onEnd(true);
            }
        }

        // =========================================================
        // Marines v. ZEALOTS

        else if (activeMap().equals("ums/mar_v_zea.scx")) {
            if (Select.enemyCombatUnits().isEmpty()) {
                GameSpeed.changeSpeedTo(0);
                GameSpeed.changeFrameSkipTo(70);
            }

            if (
                    A.now() > 60
                    && GameSpeed.frameSkip >= 30
                    && Count.ourCombatUnits() >= 5
//                        && Select.ourOfType(AUnitType.Terran_Science_Vessel).atLeast(1)
                    && Select.enemyCombatUnits().atLeast(2)
            ) {
//                GameSpeed.pauseGame();
                APainter.disablePainting();
                GameSpeed.changeSpeedTo(30);
                GameSpeed.changeFrameSkipTo(0);
                CameraManager.centerCameraNowOnSquadCenter();
//                GameSpeed.unpauseGame();
            }
        }

        // =========================================================
        // Rebasing

        else if (activeMap().equals("ums/rav/T_rebasing.scm")) {
            CameraManager.centerCameraOn(Select.ourBases().first());
        }
    }
}
