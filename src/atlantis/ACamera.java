package atlantis;

import atlantis.position.HasPosition;

public class ACamera {
    
    protected static int SCREEN_WIDTH = 640;
    protected static int SCREEN_HEIGHT = 480;
    protected static boolean focusCameraOnFirstCombatUnit = true;

    // =========================================================
    
    public static void centerCameraOn(HasPosition position) {
        Atlantis.game().setScreenPosition(
                position.getPosition().translateByPixels(-SCREEN_WIDTH / 2, -SCREEN_HEIGHT * 3 / 7)
        );
    }

    public static void toggleFocusCameraOnFirstCombatUnit() {
        focusCameraOnFirstCombatUnit = !focusCameraOnFirstCombatUnit;
    }

    public static boolean isFocusCameraOnFirstCombatUnit() {
        return focusCameraOnFirstCombatUnit;
    }

}
