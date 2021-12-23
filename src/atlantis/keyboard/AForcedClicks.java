package atlantis.keyboard;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class AForcedClicks {

    public static void clickAltF9() {
        Robot robot = null;
        try {
            robot = new Robot();

            // Simulate a key press
            robot.keyPress(KeyEvent.VK_ALT);
            robot.keyPress(KeyEvent.VK_F9);
//            System.out.println("Fire Ctrl+F9 click - Double window size");
            robot.keyRelease(KeyEvent.VK_F9);
            robot.keyRelease(KeyEvent.VK_ALT);
        } catch (AWTException e) { }
    }

}
