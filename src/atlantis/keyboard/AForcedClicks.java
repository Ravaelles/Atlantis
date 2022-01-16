package atlantis.keyboard;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class AForcedClicks {

    /**
     * Alt+F9 makes ChaosLauncher double the size, it's simply bigger.
     */
    public static void clickAltF9() {
        Robot robot = null;
        try {
            robot = new Robot();

            // Simulate a key press
            robot.keyPress(KeyEvent.VK_ALT);
            robot.keyPress(KeyEvent.VK_F9);
            robot.keyRelease(KeyEvent.VK_F9);
            robot.keyRelease(KeyEvent.VK_ALT);
        } catch (AWTException e) { }

        System.out.println("Info: I clicked Alt+F9 to make ChaosLauncher twice the size (double the failure)");
    }

}
