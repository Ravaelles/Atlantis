package atlantis;

import atlantis.debug.APainter;
import atlantis.env.Env;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.A;

public class UseMap {

    public static String activeMap() {
//        return "sscai/(2)Destination.scx";
//        return "sscai/(2)Heartbreak Ridge.scx";
//        return "sscai/(4)Roadrunner.scx";
//        return "sscai/(?)*.sc?"; // Default map-pack for SSCAIT

        // === UMS maps - great for bot development ============

        return "ums/rav/Dragoons_v_Zerglings.scm";

//        return "ums/rav/T_v_Sunkens.scx";
//        return "ums/rav/T_v_Sunkens2.scx";
//        return "ums/rav/T_v_Sunkens3.scx";
//        return "ums/rav/Wraiths_v_Probes.scm";
//        return "ums/rav/Wraiths_v_Cannons.scm";
//        return "ums/rav/M&M_v_Dragoons_A.scx";
//        return "ums/rav/M&M_v_Dragoons_B.scx";
//        return "ums/rav/M_v_Zealots.scx";
//        return "ums/rav/Vultures_v_Zealots.scm";
//        return "ums/rav/Vultures_v_Dragoons.scm";

//        return "ums/rav/minimaps/M_v_Zealots.scx";
//        return "ums/rav/minimaps/M&M_v_Zealots.scx";
//        return "ums/rav/minimaps/3M_v_2Zealots.scx";
//        return "ums/rav/minimaps/4M_v_2Zealots.scx";
//        return "ums/";
//        return "ums/";
//        return "ums/";
//        return "ums/";
//        return "ums/gol_v_zeals.scx";
//        return "ums/aaa (1).scx"; // Nice mini maps for terran
//        return "ums/aaa (2).scx";
//        return "ums/aaa (3).scx";
//        return "ums/aaa (4).scx";
//        return "ums/aaa (5).scx";
//        return "ums/aaa (6).scx";

//        return "ums/marines/m (5).scx"; // Nice map to test different terran infantry in rounds
//        return "ums/marines/m (8).scx"; // Hmm

        // ### Gosu bots ###
//        return "ums/7th.scx"; // v. AI Protoss player, that can kill CSv constructing
//        return "ums/exp_skilltest.scx"; // v. AI Protoss player
//        return "ums/vGosuComputer.scx"; // v. AI Zerg Player - cheating as fuck
//        return "ums/lt-terran1j.scm"; // Zerg v. Terran
//        return "ums/member_test.scx"; // v. AI 2x Protoss players, massive Zealot rush
//        return "ums/LostTemple.scm"; // v. 2x Protoss players, again Zealot rush

//        return "ums/mar_v_zea.scx"; // Marines & Medics v. Zealots on quite small map
//        return "ums/marines_v_zerglings.scm"; // 12 marines v. 24 zerglings
//        return "ums/dragoons_v_map.scx"; // 4 Dragoons attacking Zealots
//        return "ums/ConTrol2.scx"; // More minigames
//        return "ums/micro challenge.scx"; // Even more minigames
//        return "ums/NeWconTrol.scx"; // Cool minigames, starting with 2 drones v. 2 drones, lings v. goons etc
//        return "ums/tank-dropship.scm"; // 2 Tanks & 2 Dropships v. Dragoons
//        return "ums/trainzvreaver.scm"; // Zerglings & Hydras v. 2 Reavers & Shuttle
//        return "ums/training-PvT.scx"; // Dragoons & Zealots v. Vultures & Tanks + slowly Hi-Templars & Archons
//        return "ums/trening nr 2.scx";
//        return "ums/micro tvp 1.00.scx"; // Huge Terran army (tanks & vultures) v. Zealots & Hi-Templars & Dragoons
//        return "ums/micro3.scx";
//        return "ums/wraiths_v_carriers_obs.scx"; // Wraiths & Valkyries v. Carriers & Observers
//        return "ums/(1)micro3_007.scx";
//        return "ums/dragoon_sweeping_mines.scm"; // 5 dragoons v. mines
//        return "ums/vulture_control.scx"; // Vulture v. Broodlings
//        return "ums/MultiTask PvT.scx"; // Weird - ums but starts with bases
//        return "ums/ControlFighterTZ-Easy.scx"; // Tanks & Marines v. Zerg
//        return "ums/protoss_micro.scx"; // Huge parallel map, bad performance wise
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
        // v. ZERG / PROTOSS / Gosu maps

        if (
                activeMap().equals("ums/vGosuComputer.scx")
                || activeMap().equals("ums/exp_skilltest.scx")
        ) {
            int FAST = 90;

            if (A.now() <= 1) {
                GameSpeed.changeSpeedTo(0);
                GameSpeed.changeFrameSkipTo(FAST);
            }

            if (A.everyNthGameFrame(50)) {
                if (GameSpeed.frameSkip == FAST && Select.enemyCombatUnits().atLeast(3)) {
                    GameSpeed.changeFrameSkipTo(0);
                }
            }
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
    }
}
