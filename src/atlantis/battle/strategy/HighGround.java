package atlantis.battle.strategy;

import atlantis.map.position.APosition;

/**
 * High-ground choke scoring (NEXT #171/#181): which side of a choke to
 * hold. Height first - the low side cannot see up while the high side
 * sees down, and shooting uphill misses - then funnel width (a wide
 * mouth needs more guns to cover). Pure numbers in, one number out;
 * the map-height reader plugs in where a live game exists.
 */
public final class HighGround {

    private HighGround() {
    }

    /** How much one tile of height advantage is worth, in score units. */
    public static final double HEIGHT_WEIGHT = 10;

    /** How much one tile of funnel width costs, in the same units. */
    public static final double WIDTH_PENALTY = 1;

    /**
     * Defensive value of holding one side: height advantage over the
     * other side minus the width you must cover. Higher wins.
     */
    public static double defensiveValue(double ourHeightTiles, double enemyHeightTiles, double widthTiles) {
        return (ourHeightTiles - enemyHeightTiles) * HEIGHT_WEIGHT - widthTiles * WIDTH_PENALTY;
    }

    /**
     * Which lip to hold: the foe side when both heights are known and
     * theirs is higher, ours otherwise (unknown or flat keeps today).
     * Pure seam: the live heights never reach a test, so the choice
     * pins here while the reader stays guarded.
     */
    public static boolean preferFoeSide(int ourHeight, int foeHeight) {
        if (ourHeight < 0 || foeHeight < 0) return false;
        return foeHeight > ourHeight;
    }

    /** Ground height at a tile, or -1 without a live map. Guarded. */
    public static int groundHeightOf(APosition tile) {
        try {
            if (tile == null) return -1;
            return atlantis.game.A.game().getGroundHeight(tile.tx(), tile.ty());
        } catch (Exception | Error ignored) {
            return -1;
        }
    }
}
