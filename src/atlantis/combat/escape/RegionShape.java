package atlantis.combat.escape;

import atlantis.map.position.APosition;
import atlantis.map.region.ARegion;

/**
 * How much room a region has: bounding-box width/height and the smaller
 * of the two. Coarse by design - a winding peninsula reads wide here,
 * which is exactly why the 8-direction ray probe (not this number)
 * decides real narrowness. The box only answers "definitely cramped".
 */
public final class RegionShape {

    public final double widthTiles;
    public final double heightTiles;
    public final double minDimTiles;
    public final APosition center;

    private RegionShape(double widthTiles, double heightTiles, APosition center) {
        this.widthTiles = widthTiles;
        this.heightTiles = heightTiles;
        this.minDimTiles = Math.min(widthTiles, heightTiles);
        this.center = center;
    }

    public static RegionShape of(double widthTiles, double heightTiles, APosition center) {
        return new RegionShape(widthTiles, heightTiles, center);
    }

    /**
     * Guarded adapter: null without map data, never an exception.
     */
    public static RegionShape of(ARegion region) {
        try {
            if (region == null) return null;

            double[] box = region.boundingBoxTiles();
            if (box == null) return null;

            return new RegionShape(box[0], box[1], region.center());
        } catch (Exception ignored) {
            return null;
        }
    }

    public boolean isNarrow(double thresholdTiles) {
        return minDimTiles < thresholdTiles;
    }

    @Override
    public String toString() {
        return "RegionShape(" + widthTiles + "x" + heightTiles + ", min=" + minDimTiles + ")";
    }
}
