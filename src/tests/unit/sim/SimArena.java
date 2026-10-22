package tests.unit.sim;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * A flat, fully controlled battlefield for behavior scenarios (NEXT #161).
 *
 * <p>Pixels, like the engine. The arena is a bounded rectangle with optional
 * impassable obstacle blocks the test places (the user's obstacle course).
 * Walkability here is geometric, not map data - it answers "can a unit stand
 * here" so make-way/retreat steps have somewhere legal to go, nothing more.
 */
public class SimArena {

    private final int widthPx;
    private final int heightPx;
    private final List<Rectangle> obstacles = new ArrayList<>();

    public SimArena(int widthPx, int heightPx) {
        this.widthPx = widthPx;
        this.heightPx = heightPx;
    }

    /** Adds an impassable block, in pixels. Returns this for chaining. */
    public SimArena block(int x, int y, int w, int h) {
        obstacles.add(new Rectangle(x, y, w, h));

        return this;
    }

    public boolean isWalkablePx(int x, int y) {
        if (x < 0 || y < 0 || x >= widthPx || y >= heightPx) return false;

        for (Rectangle obstacle : obstacles) {
            if (obstacle.contains(x, y)) return false;
        }
        return true;
    }

    public int clampX(int x) {
        return Math.max(0, Math.min(widthPx - 1, x));
    }

    public int clampY(int y) {
        return Math.max(0, Math.min(heightPx - 1, y));
    }

    public double clampXd(double x) {
        return Math.max(0.0, Math.min((double) widthPx - 1, x));
    }

    public double clampYd(double y) {
        return Math.max(0.0, Math.min((double) heightPx - 1, y));
    }
}
