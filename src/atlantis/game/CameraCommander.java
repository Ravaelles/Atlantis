package atlantis.game;

import atlantis.Atlantis;
import atlantis.architecture.Commander;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class CameraCommander extends Commander {

    protected static int SCREEN_WIDTH = 640;
    protected static int SCREEN_HEIGHT = 480;
    //    protected static boolean focusCameraOnFirstCombatUnit = true;
    protected static boolean focusCameraOnFirstCombatUnit = false;

    // =========================================================


    @Override
    protected void handle() {
        AUnit cameraUnit = centerCameraOnUnit();
        if (A.now() <= 1 || CameraCommander.isFocusCameraOnUnit()) {
            CameraCommander.centerCameraOn(cameraUnit);
        }
    }

    // =========================================================

    private static AUnit centerCameraOnUnit() {
//        return null;atlantis.combat.missions.Missions.globalMission
        AUnit cameraUnit;

//        cameraUnit = Select.ourOfType(AUnitType.Protoss_High_Templar).groundUnits().first();
//        if (cameraUnit != null) {
////            if (GameSpeed.gameSpeed <= 0 && GameSpeed.frameSkip >= 1) {
////                GameSpeed.changeSpeedTo(2);
////            }
//            return cameraUnit;
//        }

        cameraUnit = Select.ourOfType(AUnitType.Terran_Wraith).first();
        if (cameraUnit != null) {
            return cameraUnit;
        }

//        cameraUnit = Select.ourOfType(AUnitType.Terran_Bunker).first();
//        if (cameraUnit != null) {
//            return cameraUnit;
//        }

        cameraUnit = Alpha.get().leader();
        if (cameraUnit != null) return cameraUnit;

//        return null;
        return Select.ourCombatUnits().nonBuildings().excludeTypes(AUnitType.Terran_Medic).groundUnits().first();
    }

//    public static void centerCameraNowOnFirstCombatUnit() {
//        AUnit unit = Select.ourCombatUnits().groundUnits().nonBuildings().first();
//        if (unit != null) {
//            centerCameraOn(unit);
//        }
//    }

    public static void centerCameraOn(HasPosition position) {
        if (position == null) return;

        Atlantis.game().setScreenPosition(
            position.translateByPixels(-SCREEN_WIDTH / 2, -SCREEN_HEIGHT * 3 / 7).p()
        );
    }

    public static void toggleFocusCameraOnInterestingCombatUnit() {
        focusCameraOnFirstCombatUnit = !focusCameraOnFirstCombatUnit;
    }

    public static void focusCameraOnInterestingCombatUnit() {
        focusCameraOnFirstCombatUnit = true;
    }

    public static boolean isFocusCameraOnUnit() {
        return focusCameraOnFirstCombatUnit;
    }

    public static void centerCameraNowOnSquadCenter() {
        centerCameraOn(Squad.alphaCenter());
    }
}
