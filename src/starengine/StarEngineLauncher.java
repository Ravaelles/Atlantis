package starengine;

import starengine.canvas.EngineWindow;
import tests.acceptance.AbstractWorldCreatingTest;

import javax.swing.*;

public class StarEngineLauncher {
    public static void launchStarEngine(AbstractWorldCreatingTest testClass) {
        StarEngine engine = new StarEngine(testClass);

        SwingUtilities.invokeLater(() -> {
            EngineWindow.createWindow(engine);
        });
    }
}
