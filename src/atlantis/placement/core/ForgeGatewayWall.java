package atlantis.placement.core;

import java.util.ArrayList;
import java.util.List;

/**
 * A Forge/Gateway Pylon wall at a choke (`_AI/redesign/03_PLACEMENT.md` §2
 * Phase 4,
 * §5.4 S4).
 *
 * <p>
 * Stardust computes this with a choke-walking algorithm that measures the gap
 * the
 * wall leaves and which tiles end up inside or outside it. That machinery is
 * elaborate; S4 here does the part the game actually needs - <b>pick the three
 * tiles a Forge, a Gateway and a Pylon occupy to seal a choke</b> - and is
 * explicit
 * about what it does not do (see {@link #limitations()}).
 * </p>
 *
 * <p>
 * Pure: it is handed a grid, the free tiles and the choke's tiles, and it
 * answers
 * a wall or null. No pathing, no gap measurement, no game objects.
 * </p>
 */
public final class ForgeGatewayWall {

    /** The three buildings a wall is made of. */
    public static final class Tiles {
        private final int forgeX;
        private final int forgeY;
        private final int gatewayX;
        private final int gatewayY;
        private final int pylonX;
        private final int pylonY;

        Tiles(int forgeX, int forgeY, int gatewayX, int gatewayY, int pylonX, int pylonY) {
            this.forgeX = forgeX;
            this.forgeY = forgeY;
            this.gatewayX = gatewayX;
            this.gatewayY = gatewayY;
            this.pylonX = pylonX;
            this.pylonY = pylonY;
        }

        public int forgeX() {
            return forgeX;
        }

        public int forgeY() {
            return forgeY;
        }

        public int gatewayX() {
            return gatewayX;
        }

        public int gatewayY() {
            return gatewayY;
        }

        public int pylonX() {
            return pylonX;
        }

        public int pylonY() {
            return pylonY;
        }

        @Override
        public String toString() {
            return "wall[forge(" + forgeX + "," + forgeY + ") "
                    + "gateway(" + gatewayX + "," + gatewayY + ") "
                    + "pylon(" + pylonX + "," + pylonY + ")]";
        }
    }

    /** The choke centre and the tiles it spans, as the wall strategy needs them. */
    public static final class Choke {
        private final int centerX;
        private final int centerY;
        private final List<int[]> tiles;

        public Choke(int centerX, int centerY, List<int[]> tiles) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.tiles = tiles;
        }

        public int centerX() {
            return centerX;
        }

        public int centerY() {
            return centerY;
        }

        /** Every tile of the choke, nearest-relevant first is not required. */
        public List<int[]> tiles() {
            return tiles;
        }
    }

    private final TileAvailabilityGrid grid;

    public ForgeGatewayWall(TileAvailabilityGrid grid) {
        this.grid = grid;
    }

    /**
     * The best wall for this choke, or null when no three free tiles near it can
     * carry a Forge, a Gateway and a Pylon.
     *
     * <p>
     * Every candidate triple must fit the grid (so the wall never overlaps terrain
     * or an existing building) and the Pylon must power the Forge and Gateway
     * tiles, or the wall would not work once built.
     * </p>
     */
    public Tiles find(Choke choke) {
        if (choke == null)
            return null;

        List<int[]> near = freeTilesWithin(choke, 4);
        if (near.size() < 3)
            return null;

        // Deterministic, choke-first ordering: try the tiles closest to the choke
        // centre as the Forge, then the Gateway, then the Pylon.
        sortByDistanceTo(near, choke.centerX(), choke.centerY());

        for (int[] forge : near) {
            if (!grid.isFreeFor(forge[0], forge[1], 3, 2))
                continue;

            for (int[] gateway : near) {
                if (overlaps(forge, 3, 2, gateway, 3, 2))
                    continue;
                if (!grid.isFreeFor(gateway[0], gateway[1], 3, 2))
                    continue;

                for (int[] pylon : near) {
                    if (overlaps(pylon, 2, 2, forge, 3, 2))
                        continue;
                    if (overlaps(pylon, 2, 2, gateway, 3, 2))
                        continue;
                    if (!grid.isFreeFor(pylon[0], pylon[1], 2, 2))
                        continue;
                    if (!pylonPowers(pylon, forge) || !pylonPowers(pylon, gateway))
                        continue;

                    return new Tiles(
                            forge[0], forge[1], gateway[0], gateway[1], pylon[0], pylon[1]);
                }
            }
        }

        return null;
    }

    /**
     * What this wall does NOT model, stated here so nobody assumes more than it
     * does: the gap the wall leaves is not measured, so a wall that leaves a
     * one-tile hole a unit can walk through is still returned; tiles inside vs
     * outside are not classified; and probe-blocking positions are not computed.
     * Those are Stardust's choke-walking results and would be a follow-up stage.
     */
    public static String limitations() {
        return "no gap measurement, no inside/outside classification, no probe-blocking positions";
    }

    private static boolean pylonPowers(int[] pylon, int[] building) {
        // The same 6-tile radius the engine applies to a finished Pylon.
        int dx = Math.abs(pylon[0] - building[0]);
        int dy = Math.abs(pylon[1] - building[1]);
        return Math.max(dx, dy) <= 6;
    }

    private static boolean overlaps(int[] a, int aw, int ah, int[] b, int bw, int bh) {
        return a[0] < b[0] + bw && a[0] + aw > b[0]
                && a[1] < b[1] + bh && a[1] + ah > b[1];
    }

    private List<int[]> freeTilesWithin(Choke choke, int radius) {
        List<int[]> free = new ArrayList<>();

        for (int x = choke.centerX() - radius; x <= choke.centerX() + radius; x++) {
            for (int y = choke.centerY() - radius; y <= choke.centerY() + radius; y++) {
                if (x < 0 || y < 0 || x >= grid.width() || y >= grid.height())
                    continue;
                if (grid.flagsAt(x, y) != 0)
                    continue;
                free.add(new int[] { x, y });
            }
        }

        return free;
    }

    private static void sortByDistanceTo(List<int[]> tiles, final int cx, final int cy) {
        java.util.Collections.sort(tiles, new java.util.Comparator<int[]>() {
            @Override
            public int compare(int[] a, int[] b) {
                return Integer.compare(dist(a, cx, cy), dist(b, cx, cy));
            }
        });
    }

    private static int dist(int[] tile, int cx, int cy) {
        return Math.max(Math.abs(tile[0] - cx), Math.abs(tile[1] - cy));
    }
}
