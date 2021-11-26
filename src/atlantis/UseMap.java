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

//        return "ums/rav/Wraiths_v_Probes.scm";
        return "ums/rav/M&M_v_Dragoons.scx";
//        return "ums/rav/M_v_Zealots.scx";

//        return "ums/rav/minimaps/M_vs_Zealots.scx";
//        return "ums/rav/minimaps/M&M_vs_Zealots.scx";

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
//        return "ums/7th.scx"; // vs. AI Protoss player, that can kill CSVs constructing
//        return "ums/exp_skilltest.scx"; // vs. AI Protoss player
//        return "ums/vsGosuComputer.scx"; // vs. AI Zerg Player - cheating as fuck
//        return "ums/lt-terran1j.scm"; // Zerg vs. Terran
//        return "ums/member_test.scx"; // vs. AI 2x Protoss players, massive Zealot rush
//        return "ums/LostTemple.scm"; // vs. 2x Protoss players, again Zealot rush

//        return "ums/mar_vs_zea.scx"; // Marines & Medics vs. Zealots on quite small map
//        return "ums/marines_vs_zerglings.scm"; // 12 marines vs. 24 zerglings
//        return "ums/dragoons_vs_map.scx"; // 4 Dragoons attacking Zealots
//        return "ums/ConTrol2.scx"; // More minigames
//        return "ums/micro challenge.scx"; //8 Even more minigames
//        return "ums/NeWconTrol.scx"; // Cool minigames, starting with 2 drones vs. 2 drones, lings vs. goons etc
//        return "ums/tank-dropship.scm"; // 2 Tanks & 2 Dropships vs. Dragoons
//        return "ums/trainzvreaver.scm"; // Zerglings & Hydras vs. 2 Reavers & Shuttle
//        return "ums/training-PvT.scx"; // Dragoons & Zealots vs. Vultures & Tanks + slowly Hi-Templars & Archons
//        return "ums/trening nr 2.scx";
//        return "ums/micro tvp 1.00.scx"; // Huge Terran army (tanks & vultures) vs. Zealots & Hi-Templars & Dragoons
//        return "ums/micro3.scx";
//        return "ums/wraiths_vs_carriers_obs.scx"; // Wraiths & Valkyries vs. Carriers & Observers
//        return "ums/(1)micro3_007.scx";
//        return "ums/dragoon_sweeping_mines.scm"; // 5 dragoons vs. mines
//        return "ums/vulture_control.scx"; // Vulture vs. Broodlings
//        return "ums/MultiTask PvT.scx"; // Weird - ums but starts with bases
//        return "ums/ControlFighterTZ-Easy.scx"; // Tanks & Marines vs. Zerg
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
        // Marines & Medics vs. Zealots

//        else if (activeMap().equals("ums/rav/Rav_MM_Zea.scx")) {
        if (activeMap().startsWith("ums/rav/M_v_Zealots.scx")) {
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
        // vs. ZERG / PROTOSS / Gosu maps

        else if (
                activeMap().equals("ums/vsGosuComputer.scx")
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
        // Marines vs. ZERGLINGS

        else if (activeMap().equals("ums/marines_vs_zerglings.scm")) {
            if (A.now() <= 1) {
                GameSpeed.changeSpeedTo(0);
                GameSpeed.changeFrameSkipTo(40);
            }
            if (Atlantis.KILLED >= 32) {
                Atlantis.getInstance().onEnd(true);
            }
        }

        // =========================================================
        // Marines vs. ZEALOTS

        else if (activeMap().equals("ums/mar_vs_zea.scx")) {
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
                GameSpeed.changeSpeedTo(0);
                GameSpeed.changeFrameSkipTo(60);
                CameraManager.centerCameraNowOnSquadCenter();
//                GameSpeed.unpauseGame();
            }
        }
    }
}
