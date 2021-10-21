package atlantis;

import atlantis.position.APosition;
import atlantis.position.HasPosition;
import atlantis.units.AUnit;

public class ACamera {
    
    protected static int SCREEN_WIDTH = 640;
    protected static int SCREEN_HEIGHT = 480;
    
    // =========================================================
    
    public static void centerCameraOn(HasPosition position) {
        Atlantis.game().setScreenPosition(
                position.getPosition().translateByPixels(-SCREEN_WIDTH / 2, -SCREEN_HEIGHT * 3 / 7)
        );
    }
    
}
