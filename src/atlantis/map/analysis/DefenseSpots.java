package atlantis.map.analysis;

import atlantis.map.position.APosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Defensive spots for one choke: ranked cannon/tank tiles covering the
 * choke line and, behind it, the anchor (mineral line, base).
 *
 * <p>
 * Pure: choke ends + anchor + weapon range + a tile grid in, ranked spots
 * out. No game reads, no statics. The legacy cannon search
 * ({@code FindPositionForCannon}) answered "near the choke" by ring search
 * with no coverage math at all; this states coverage as the ranking so a
 * different weapon (cannon vs tank) is a parameter, not a new finder.
 * Footprint, range and scan radius are parameters for the same reason.
 * </p>
 */
public final class DefenseSpots {

    /** Tile grid the scan reads: free means a footprint may stand here. */
    public interface TileGrid {
        int width();

        int height();

        boolean isFree(int tx, int ty);
    }

    /** One ranked candidate tile (top-left) with what it covers. */
    public static final class Spot {
        private final int tx;
        private final int ty;
        private final boolean coversChoke;
        private final boolean coversAnchor;
        private final double distToChokePx;

        Spot(int tx, int ty, boolean coversChoke, boolean coversAnchor, double distToChokePx) {
            this.tx = tx;
            this.ty = ty;
            this.coversChoke = coversChoke;
            this.coversAnchor = coversAnchor;
            this.distToChokePx = distToChokePx;
        }

        public int tx() {
            return tx;
        }

        public int ty() {
            return ty;
        }

        public boolean coversChoke() {
            return coversChoke;
        }

        public boolean coversAnchor() {
            return coversAnchor;
        }
    }

    private DefenseSpots() {
    }

    /** Center of a footprint in pixels: the point coverage is measured from. */
    public static APosition tileCenter(int tx, int ty, int footW, int footH) {
        return APosition.createFromPixels(tx * 32 + footW * 16, ty * 32 + footH * 16);
    }

    /**
     * Every free footprint within {@code scanRadiusTiles} of the choke
     * center, ranked: covering the choke line first, then covering the
     * anchor, then nearer the choke. Coverage means the tile CENTER is
     * within {@code rangeTiles} of the choke segment / the anchor.
     */
    public static List<Spot> ranked(
        APosition chokeEndA, APosition chokeEndB, APosition anchor,
        int rangeTiles, int footW, int footH, int scanRadiusTiles, TileGrid grid
    ) {
        List<Spot> spots = new ArrayList<>();
        if (chokeEndA == null || chokeEndB == null || grid == null) return spots;

        double rangePx = rangeTiles * 32.0;
        APosition center = APosition.createFromPixels(
            (chokeEndA.getX() + chokeEndB.getX()) / 2,
            (chokeEndA.getY() + chokeEndB.getY()) / 2);

        for (int tx = 0; tx + footW <= grid.width(); tx++) {
            for (int ty = 0; ty + footH <= grid.height(); ty++) {
                if (!footprintFree(tx, ty, footW, footH, grid)) continue;
                APosition middle = tileCenter(tx, ty, footW, footH);
                if (middle.getX() < 0 || middle.getY() < 0) continue;
                double distToCenter = Math.hypot(
                    middle.getX() - center.getX(), middle.getY() - center.getY());
                if (distToCenter > scanRadiusTiles * 32.0) continue;

                double distToChoke = distPointToSegmentPx(middle, chokeEndA, chokeEndB);
                boolean coversChoke = distToChoke <= rangePx;
                boolean coversAnchor = anchor != null
                    && Math.hypot(middle.getX() - anchor.getX(), middle.getY() - anchor.getY()) <= rangePx;
                spots.add(new Spot(tx, ty, coversChoke, coversAnchor, distToChoke));
            }
        }

        Collections.sort(spots, new Comparator<Spot>() {
            @Override
            public int compare(Spot a, Spot b) {
                if (a.coversChoke != b.coversChoke) return a.coversChoke ? -1 : 1;
                if (a.coversAnchor != b.coversAnchor) return a.coversAnchor ? -1 : 1;
                return Double.compare(a.distToChokePx, b.distToChokePx);
            }
        });
        return spots;
    }

    private static boolean footprintFree(int tx, int ty, int footW, int footH, TileGrid grid) {
        for (int x = tx; x < tx + footW; x++) {
            for (int y = ty; y < ty + footH; y++) {
                if (!grid.isFree(x, y)) return false;
            }
        }
        return true;
    }

    /** Distance from a point to a segment, pixels. Public as a seam: rankers score coverage with it. */
    public static double distPointToSegmentPx(APosition p, APosition a, APosition b) {
        double ax = a.getX();
        double ay = a.getY();
        double bx = b.getX();
        double by = b.getY();
        double dx = bx - ax;
        double dy = by - ay;
        double lenSq = dx * dx + dy * dy;
        if (lenSq == 0) return Math.hypot(p.getX() - ax, p.getY() - ay);
        double t = ((p.getX() - ax) * dx + (p.getY() - ay) * dy) / lenSq;
        t = Math.max(0, Math.min(1, t));
        return Math.hypot(p.getX() - (ax + t * dx), p.getY() - (ay + t * dy));
    }
}
