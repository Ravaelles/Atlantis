package starengine;

import javax.swing.*;

public class StarEngineLauncher {
    public static void launchStarEngine() {
        StarEngine engine = new StarEngine();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("StarEngine");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setIconImage(Images.atlantisIcon.getImage());

            frame.getContentPane().add(new EngineCanvas(engine));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
