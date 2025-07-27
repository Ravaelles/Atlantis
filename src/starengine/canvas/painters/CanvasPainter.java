package starengine.canvas.painters;

import starengine.canvas.EngineCanvas;
import starengine.assets.Images;
import starengine.StarEngine;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CanvasPainter {
    protected final EngineCanvas canvas;
    protected final StarEngine engine;
    private final UnitPainter unitPainter;

    public CanvasPainter(EngineCanvas canvas) {
        this.canvas = canvas;
        this.engine = canvas.engine;
        this.unitPainter = new UnitPainter(this, engine);
    }

    public void paint(Graphics g) {
        paintBackground(g);
//        paintNonWalkable(g);
        unitPainter.paintUnits(g);
    }

    private void paintBackground(Graphics g) {
        // Draw background image
        g.drawImage(Images.groundImage, 0, 0, canvas.getWidth(), canvas.getHeight(), canvas);
    }

    private void paintNonWalkable(Graphics g) {
//        // Draw non-walkable areas
//        g.setColor(Color.RED);
//        for (Rectangle nonWalkableArea : engine.map.nonWalkableAreas) {
//            g.fillRect(nonWalkableArea.x, nonWalkableArea.y, nonWalkableArea.width, nonWalkableArea.height);
//        }

        BufferedImage nonWalkable = Images.nonWalkable;
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setPaint(new TexturePaint(
            nonWalkable,
            new Rectangle(0, 0, nonWalkable.getWidth(), nonWalkable.getHeight())
        ));

        for (Rectangle nonWalkableArea : engine.map.nonWalkableAreas) {
            g2d.fill(nonWalkableArea);
        }

        g2d.dispose();
    }
}
