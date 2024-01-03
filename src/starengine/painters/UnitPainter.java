package starengine.painters;

import atlantis.units.AUnit;
import starengine.units.Unit;
import starengine.units.Units;

import java.awt.*;
import java.awt.image.BufferedImage;

public class UnitPainter {
    private final CanvasPainter canvasPainter;
    private AUnit au;
    private Graphics g;

    public UnitPainter(CanvasPainter canvasPainter) {
        this.canvasPainter = canvasPainter;
    }

    public void paintUnits(Graphics g) {
        this.g = g;

        // Draw units
        for (Unit unit : canvasPainter.engine.units.allUnits()) {
            paintUnit(unit);
        }
    }

    private void paintUnit(Unit unit) {
        au = unit.aUnit();

        paintTarget(unit);
        paintBackground(unit);
//        paintBorder(unit);
        paintLifeBar(unit);
        paintTextsOverUnit(unit);
    }

    // =========================================================

    private void paintTarget(Unit unit) {
        if (au.targetPosition() != null) {
            g.setColor(targetLineColor());
            g.drawLine(
                unit.x(),
                unit.y(),
                au.targetPosition().x,
                au.targetPosition().y
            );
        }
    }

    private Color targetLineColor() {
        if (au.isAttacking()) return Color.ORANGE;
        if (au.isRunning()) return Color.BLUE;
        if (au.isMoving()) return Color.GRAY;
        return Color.WHITE;
    }

    private void paintTextsOverUnit(Unit unit) {
        paintText(unit, au.typeWithUnitId(), -20);
        paintText(unit, au.manager().toString(), 9);
    }

    private void paintLifeBar(Unit unit) {
        int width = unitWidth() / 2;
        int height = 2;

        g.setColor(Color.GREEN);
        g.fillRect(
            unit.x() - unitWidth() / 6,
            unit.y() + unitHeight() / 2 + 3,
            width,
            height
        );

        g.setColor(au.isOur() ? Color.YELLOW : Color.RED);
        g.fillRect(
            unit.x() - unitWidth() / 6,
            unit.y() + unitHeight() / 2 + 3,
            (int) (width * au.woundPercent() / 100),
            height
        );
    }

    private void paintText(Unit unit, String text, int dy) {
        g.setColor(Color.WHITE);
        g.drawString(text, (int) (unit.x() + text.length() * -2.5), unit.y() + dy);
    }

    private void paintText(Unit unit, String text, int dx, int dy) {
        g.setColor(Color.WHITE);
        g.drawString(text, unit.x() + dx, unit.y() + dy);
    }

    private void paintBorder(Unit unit) {
        g.setColor(Color.BLACK);
        g.drawOval(
            unit.x() - unitWidth() / 2,
            unit.y() - unitHeight() / 2,
            unitWidth(),
            unitHeight()
        );
    }

    private static int unitWidth() {
        return Units.UNIT_WIDTH;
    }

    private static int unitHeight() {
        return Units.UNIT_HEIGHT;
    }

    private void paintBackground(Unit unit) {
//        g.setColor(unit.getColor());
//        g.fillOval(
//            unit.x() - unitSize() / 2,
//            unit.y() - unitSize() / 2,
//            unitSize(),
//            unitSize()
//        );

        BufferedImage image = unit.image();

        g.drawImage(
            image,
            unit.x() - unitWidth() / 2,
            unit.y() - unitHeight() / 2,
            unitWidth(),
            unitHeight(),
            canvasPainter.canvas
        );

//        Graphics2D g2d = (Graphics2D) g.create();
//        g2d.setPaint(new TexturePaint(
//            image,
//            new Rectangle(
//                unit.x() - unitSize() / 2,
//                unit.y() - unitSize() / 2,
//                unitSize(),
//                unitSize()
//            )
//        ));
//        g2d.dispose();
    }
}
