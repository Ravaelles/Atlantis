package starengine;

import starengine.painters.CanvasPainter;

import javax.swing.*;
import java.awt.*;

public class EngineCanvas extends JPanel {
    public final StarEngine engine;
    public final CanvasPainter painter;

    public EngineCanvas(StarEngine engine) {
        this.engine = engine;
        this.painter = new CanvasPainter(this);

        setBackground(Color.DARK_GRAY);
        setPreferredSize(new Dimension(Map.SPACE_WIDTH, Map.SPACE_HEIGHT));
        startPainter();
    }

    public void updateOnFrameEnd() {
//        EngineUpdater.update(engine);
        repaint();
    }

    private void startPainter() {
        // Timer for animation
        Timer timer = new Timer(20, e -> updateOnFrameEnd());
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        painter.paint(g);
    }
}
