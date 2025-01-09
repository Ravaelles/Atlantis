package starengine.canvas;

import starengine.StarEngineConfig;
import starengine.assets.Map;
import starengine.StarEngine;
import starengine.canvas.painters.CanvasPainter;

import javax.swing.*;
import java.awt.*;

public class EngineCanvas extends JPanel {
    public final StarEngine engine;
    public final CanvasPainter painter;

    public EngineCanvas(StarEngine engine) {
        this.engine = engine;
        this.painter = new CanvasPainter(this);

        setBackground(Color.DARK_GRAY);
        startPainter();
    }

    public void updateScale(int monitorHeight) {
        // Calculate scale factor based on monitor height
        int originalHeight = Map.SPACE_HEIGHT; // The original height of the map
        StarEngineConfig.SCALE_MAP = (double) monitorHeight / originalHeight;

        // Update preferred size based on the new scale factor
        int newWidth = (int) (Map.SPACE_WIDTH * StarEngineConfig.SCALE_MAP);
        int newHeight = (int) (Map.SPACE_HEIGHT * StarEngineConfig.SCALE_MAP);
        setPreferredSize(new Dimension(newWidth, newHeight));
        revalidate();
    }

    public void updateOnFrameEnd() {
        if (engine.game().isGameEnd()) return;

        repaint();
    }

    private void startPainter() {
        Timer timer = new Timer(1, e -> updateOnFrameEnd());
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Apply scaling
        Graphics2D g2d = (Graphics2D) g;
        g2d.scale(StarEngineConfig.SCALE_MAP, StarEngineConfig.SCALE_MAP);

        // Delegate actual painting to the painter
        painter.paint(g2d);
    }
}
