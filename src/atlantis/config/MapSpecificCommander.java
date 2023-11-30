package atlantis.config;

import atlantis.Atlantis;
import atlantis.architecture.Commander;
import atlantis.combat.missions.defend.focus.MissionDefendFocusPoint;
import atlantis.config.env.Env;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.game.CameraCommander;
import atlantis.game.GameSpeed;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

import java.util.Arrays;

public class MapSpecificCommander extends Commander {
    public static boolean initialSpeed = true;

    @Override
    protected void handle() {
        if (Env.isParamTweaker() || !Env.isLocal()) return;
//        if (!A.isUms()) return;

        // =========================================================
        // Marines & Medics v. Zealots

        if (ActiveMap.isMap("minimaps/")) {
            if (A.now() <= 1) {
                GameSpeed.changeSpeedTo(30);
                GameSpeed.changeFrameSkipTo(0);
                CameraCommander.centerCameraNowOnSquadCenter();
            }
        }

        // =========================================================

        else if (ActiveMap.isMap("Wraith_v_Wraith")) {
            if (A.now() <= 1) GameSpeed.changeSpeedToNormal();
            if (A.seconds() <= 3) CameraCommander.centerCameraOn(Select.ourCombatUnits().first());
        }

        // =========================================================

        else if (ActiveMap.isMap("Lings_v_Zealots")) {
            if (A.now() <= 1) GameSpeed.changeSpeedTo(30);
            if (A.seconds() <= 3) CameraCommander.centerCameraOn(ourUnitNearestToEnemy());
        }

        // =========================================================

        else if (
            ActiveMap.isMap("rav/Dragoon_v_Zealot")
                || ActiveMap.isMap("rav/Dragoons_v_Zealots")
        ) {
            if (A.now() <= 1) {
                GameSpeed.changeSpeedTo(50);
                GameSpeed.changeFrameSkipTo(0);
            }
        }

        // =========================================================
        // v. ZERG / PROTOSS / Gosu maps

        else if (
            ActiveMap.isMap("vsGosuComputer")
                || ActiveMap.isMap("vsGosuRav")
                || ActiveMap.isMap("exp_skilltest")
                || ActiveMap.isMap("7th")
        ) {
            int initFrameSkip = 80;

            if (A.now() <= 0) {
                AAdvancedPainter.disablePainting();
                GameSpeed.changeSpeedTo(0);
                GameSpeed.changeFrameSkipTo(initFrameSkip);

                if (ActiveMap.isMap("vsGosuRav")) {
                    CameraCommander.centerCameraOn((new MissionDefendFocusPoint()).focusPoint());
                }
            }

//            if (ActiveMap.isMap("exp_skilltest") && !Have.controlTower()) {
//                GameSpeed.changeFrameSkipTo(2000);
//            }
        }

        // =========================================================
        // Marines v. ZERGLINGS

        else if (ActiveMap.isMap("marines_v_zerglings")) {
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

        else if (ActiveMap.isMap("Jim_v_Lurker")) {
            if (A.now() <= 1) {
                GameSpeed.changeSpeedTo(1);
                GameSpeed.changeFrameSkipTo(0);
            }
            CameraCommander.centerCameraNowOnSquadCenter();
        }

        // =========================================================
        // Marines v. ZEALOTS

        else if (ActiveMap.isMap("mar_v_zea")) {
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

        else if (ActiveMap.isMap("T_rebasing")) {
            if (A.now() <= 3) {
                CameraCommander.centerCameraOn(Select.ourBases().first());
                GameSpeed.changeSpeedTo(0);
                GameSpeed.changeFrameSkipTo(0);
            }
        }

        else if (ActiveMap.isMap("TBall_v_HydraLings")) {
            if (A.now() <= 3) {
                CameraCommander.centerCameraOn(Select.ourBases().first());
                GameSpeed.changeSpeedTo(1);
                GameSpeed.changeFrameSkipTo(0);
            }
        }
    }

    private static AUnit ourUnitNearestToEnemy() {
        return Select.ourCombatUnits().nearestTo(Select.enemy().first());
    }

    public static boolean shouldTreatAsNormalMap() {
        String[] umsMapsTreatedAsNormal = new String[]{
            "ums/vsGosuComputer.scx",
            "ums/rav/vsGosuRav.scx"
        };

        return Arrays.asList(umsMapsTreatedAsNormal).contains(ActiveMap.activeMap());
    }
}
