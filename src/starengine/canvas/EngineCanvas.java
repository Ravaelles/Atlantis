package starengine.canvas;

import starengine.assets.Map;
import starengine.StarEngine;
import starengine.canvas.painters.CanvasPainter;

import javax.swing.*;
import java.awt.*;

public class EngineCanvas extends JPanel {
    public final StarEngine engine;
    public final CanvasPainter painter;
    private double scaleFactor = 1.0;

    public EngineCanvas(StarEngine engine) {
        this.engine = engine;
        this.painter = new CanvasPainter(this);

        setBackground(Color.DARK_GRAY);
        startPainter();
    }

    public void updateScale(int monitorHeight) {
        // Calculate scale factor based on monitor height
        int originalHeight = Map.SPACE_HEIGHT; // The original height of the map
        scaleFactor = (double) monitorHeight / originalHeight;

        // Update preferred size based on the new scale factor
        int newWidth = (int) (Map.SPACE_WIDTH * scaleFactor);
        int newHeight = (int) (Map.SPACE_HEIGHT * scaleFactor);
        setPreferredSize(new Dimension(newWidth, newHeight));
        revalidate();
    }

    public void updateOnFrameEnd() {
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
        g2d.scale(scaleFactor, scaleFactor);

        // Delegate actual painting to the painter
        painter.paint(g2d);
    }
}
