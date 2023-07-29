package atlantis.config;

import atlantis.Atlantis;
import atlantis.architecture.Commander;
import atlantis.combat.missions.defend.MissionDefendFocusPoint;
import atlantis.config.env.Env;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.game.CameraCommander;
import atlantis.game.GameSpeed;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

import java.util.Arrays;

public class MapSpecificCommander extends Commander {

    @Override
    public void handle() {
        if (Env.isParamTweaker() || !Env.isLocal()) {
            return;
        }

        // =========================================================
        // Marines & Medics v. Zealots

        if (MapAndRace.isMap("minimaps/")) {
            if (A.now() <= 1) {
                GameSpeed.changeSpeedTo(30);
                GameSpeed.changeFrameSkipTo(0);
                CameraCommander.centerCameraNowOnSquadCenter();
            }
        }

        // =========================================================

        else if (
            MapAndRace.isMap("rav/Dragoon_v_Zealot")
                || MapAndRace.isMap("rav/Dragoons_v_Zealots")
        ) {
            if (A.now() <= 1) {
                GameSpeed.changeSpeedTo(50);
                GameSpeed.changeFrameSkipTo(0);
            }
        }

        // =========================================================
        // v. ZERG / PROTOSS / Gosu maps

        else if (
            MapAndRace.isMap("vsGosuComputer")
                || MapAndRace.isMap("vsGosuRav")
                || MapAndRace.isMap("exp_skilltest")
                || MapAndRace.isMap("7th")
        ) {
            int initFrameSkip = 30;

            if (A.seconds() <= 1) {
                GameSpeed.changeSpeedTo(0);
                GameSpeed.changeFrameSkipTo(initFrameSkip);

                if (MapAndRace.isMap("vsGosuRav")) {
                    CameraCommander.centerCameraOn((new MissionDefendFocusPoint()).focusPoint());
                }
            }
        }

        // =========================================================
        // Marines v. ZERGLINGS

        else if (MapAndRace.isMap("marines_v_zerglings")) {
            if (A.now() <= 1) {
                GameSpeed.changeSpeedTo(20);
                GameSpeed.changeFrameSkipTo(0);
            }
            if (Atlantis.KILLED >= 32) {
                Atlantis.getInstance().onEnd(true);
            }
        }

        // =========================================================
        // Marines v. ZERGLINGS

        else if (MapAndRace.isMap("Jim_v_Lurker")) {
            if (A.now() <= 1) {
                GameSpeed.changeSpeedTo(1);
                GameSpeed.changeFrameSkipTo(0);
            }
            CameraCommander.centerCameraNowOnSquadCenter();
        }

        // =========================================================
        // Marines v. ZEALOTS

        else if (MapAndRace.isMap("mar_v_zea")) {
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
                CameraCommander.centerCameraNowOnSquadCenter();
//                GameSpeed.unpauseGame();
            }
        }

        // =========================================================
        // Rebasing

        else if (MapAndRace.isMap("rav/T_rebasing")) {
            CameraCommander.centerCameraOn(Select.ourBases().first());
        }
    }

    public static boolean shouldTreatAsNormalMap() {
        String[] umsMapsTreatedAsNormal = new String[] {
            "ums/vsGosuComputer.scx",
            "ums/rav/vsGosuRav.scx"
        };

        return Arrays.asList(umsMapsTreatedAsNormal).contains(MapAndRace.activeMap());
    }
}