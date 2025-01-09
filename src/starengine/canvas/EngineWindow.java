package starengine.canvas;

import starengine.assets.Images;
import starengine.StarEngine;

import javax.swing.*;

public class EngineWindow {
    public static final int TOOLBAR_HEIGHT_AKA_SCREEN_HEIGHT_MARGIN = 150;
    private static JFrame window;

    public static void createWindow(StarEngine engine) {
        window = new JFrame("StarEngine");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setIconImage(Images.atlantisIcon.getImage());

        // Create the canvas
        EngineCanvas canvas = new EngineCanvas(engine);

        // Get monitor height and update canvas scaling
        canvas.updateScale(monitorHeight());

        // Add the canvas to the window
        window.getContentPane().add(canvas);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        engine.setWindow(window);
    }

    private static int monitorHeight() {
        return java.awt.Toolkit.getDefaultToolkit().getScreenSize().height - TOOLBAR_HEIGHT_AKA_SCREEN_HEIGHT_MARGIN;
    }
}