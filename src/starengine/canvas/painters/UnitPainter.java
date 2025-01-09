package starengine.canvas.painters;


import atlantis.units.AUnit;
import atlantis.units.select.Select;
import starengine.StarEngine;
import starengine.units.Units;
import tests.fakes.FakeUnit;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

import static starengine.StarEngineConfig.SCALE_UNITS;

public class UnitPainter {
    private final CanvasPainter canvasPainter;
    private final StarEngine engine;
    private FakeUnit au;
    private Graphics g;

    public UnitPainter(CanvasPainter canvasPainter, StarEngine engine) {
        this.canvasPainter = canvasPainter;
        this.engine = engine;
    }

    public void paintUnits(Graphics g) {
        this.g = g;

        if (engine.game().isGameEnd()) return;

        // Draw units
        List<AUnit> units = Select.all().list();
        for (AUnit au : units) {
            FakeUnit unit = (FakeUnit) au;
            paintUnit(unit);
        }
    }

    private void paintUnit(FakeUnit unit) {
//        au = unit.FakeUnit();
        au = unit;

        if (au.isDead() || !au.isCompleted()) return;

        paintTarget(unit);
        paintUnitImage(unit);
//        paintBorder(unit);
        paintLifeBar(unit);
        paintTextsOverUnit(unit);
    }

    // =========================================================

    private void paintTarget(FakeUnit unit) {
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

    private void paintTextsOverUnit(FakeUnit unit) {
        paintText(unit, au.typeWithUnitId(), -22);
        paintText(unit, au.manager().toString(), 25);
    }

    private void paintLifeBar(FakeUnit unit) {
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

    private void paintText(FakeUnit unit, String text, int dy) {
        g.setColor(Color.GRAY);
        g.drawString(text, (int) (unit.x() + text.length() * -2.5), unit.y() + dy);
    }

    private void paintText(FakeUnit unit, String text, int dx, int dy) {
        g.setColor(Color.WHITE);
        g.drawString(text, unit.x() + dx, unit.y() + dy);
    }

    private void paintBorder(FakeUnit unit) {
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

    private void paintUnitImage(FakeUnit unit) {
//        g.setColor(unit.getColor());
//        g.fillOval(
//            unit.x() - unitSize() / 2,
//            unit.y() - unitSize() / 2,
//            unitSize(),
//            unitSize()
//        );

        BufferedImage image = unit.image();
        int unitSize = Math.max(unitWidth(), unitHeight());

        g.drawImage(
            image,
            unit.x() - unitWidth() / 2,
            unit.y() - unitHeight() / 2,
            (int) (unitWidth() * SCALE_UNITS),
            (int) (unitHeight() * SCALE_UNITS),
//            (int) (unitSize * SCALE_UNITS),
//            (int) (unitSize * SCALE_UNITS),
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
