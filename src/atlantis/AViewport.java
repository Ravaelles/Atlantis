package atlantis;

import atlantis.position.APosition;
import atlantis.units.AUnit;

public class AViewport {
    
    protected static int SCREEN_WIDTH = 640;
    protected static int SCREEN_HEIGHT = 480;
    
    // =========================================================
    
    public static void centerScreenOn(APosition position) {
        Atlantis.game().setScreenPosition(
                position.translateByPixels(-SCREEN_WIDTH / 2, -SCREEN_HEIGHT * 3 / 7)
        );
    }
    
}
