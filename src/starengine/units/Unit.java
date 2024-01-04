package starengine.units;

import starengine.Images;
import tests.unit.FakeUnit;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Unit {
    private int x, y, dx, dy;
    private Owner owner;
    private Color color;
    private FakeUnit unit;

    public Unit(int x, int y, int dx, int dy, Owner owner) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.owner = owner;
        this.color = defineColor();
    }

    private Color defineColor() {
        if (isOur()) {
            return Color.BLUE;
        } else if (isEnemy()) {
            return Color.RED;
        } else {
            return Color.GRAY;
        }
//        return new Color(new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat());
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public Color getColor() {
        return color;
    }

    public boolean isOur() {
        return owner == Owner.PLAYER;
    }

    public boolean isEnemy() {
        return owner == Owner.ENEMY;
    }

    public boolean isNeutral() {
        return owner == Owner.NEUTRAL;
    }

    public void move() {
        x += dx;
        y += dy;
    }

    public void reverseDirection() {
        reverseXDirection();
        reverseYDirection();
    }

    public void reverseXDirection() {
        dx = -dx;
    }

    public void reverseYDirection() {
        dy = -dy;
    }

    public FakeUnit FakeUnit() {
        return unit;
    }

    public Unit setFakeUnit(FakeUnit unit) {
        this.unit = unit;
        return this;
    }

    public BufferedImage image() {
        return isOur() ? Images.dragoonOur : Images.dragoonEnemy;
    }
}
