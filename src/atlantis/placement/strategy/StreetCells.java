package atlantis.placement.strategy;

import atlantis.map.position.APosition;
import atlantis.units.AUnitType;

/**
 * The street grid as pure geometry (owner 2026-11-09, extracted from
 * {@code ProtossForbiddenByStreetGrid}): corridors every 11 tiles in X
 * and every 7 in Y, buildings live on the cells between them. No supply,
 * no bank, no game reads - the preferences that used to hide inside
 * supply branches are separate tiers here, so a blocked game degrades
 * to relaxed cells instead of dying on a veto.
 *
 * <p>Modulo addresses count from the map origin, exactly like the legacy
 * file ({@code tx % 11}, {@code ty % 7}). Only the origin tile is
 * classified - the footprint may overhang a street, same as before;
 * the physics veto downstream owns the footprint.</p>
 */
public final class StreetCells {

    public static final int GRID_X = 11;
    public static final int GRID_Y = 7;

    private StreetCells() {
    }

    public static int modX(int tx) {
        int m = tx % GRID_X;

        return m < 0 ? m + GRID_X : m;
    }

    public static int modY(int ty) {
        int m = ty % GRID_Y;

        return m < 0 ? m + GRID_Y : m;
    }

    /**
     * A corridor tile itself: nothing but bases and gas ever sits here.
     */
    public static boolean isCorridor(int tx, int ty) {
        return modX(tx) == 0 || modY(ty) == 0;
    }

    /**
     * Pylon cells, strict tier (legacy {@code forPylon}): MX == 2,
     * MY in {2, 4}.
     */
    public static boolean isPylonCell(int tx, int ty) {
        if (isCorridor(tx, ty)) return false;

        return modX(tx) == 2 && (modY(ty) == 2 || modY(ty) == 4);
    }

    /**
     * Pylon cells, relaxed tier (legacy even-MX escape): any even MX on
     * a Pylon row. A blocked game degrades here instead of dying.
     */
    public static boolean isRelaxedPylonCell(int tx, int ty) {
        if (isCorridor(tx, ty)) return false;

        return modX(tx) % 2 == 0 && (modY(ty) == 2 || modY(ty) == 4);
    }

    /**
     * Gateway cells (legacy {@code forStandardBuilding}): MX in
     * {2, 4, 6}, MY in {2, 5}.
     */
    public static boolean isGatewayCell(int tx, int ty) {
        if (isCorridor(tx, ty)) return false;

        int mx = modX(tx);
        int my = modY(ty);

        return (mx == 2 || mx == 4 || mx == 6) && (my == 2 || my == 5);
    }

    /**
     * Standard cells for everything else (legacy standard shape).
     */
    public static boolean isStandardCell(int tx, int ty) {
        if (isCorridor(tx, ty)) return false;

        int mx = modX(tx);
        int my = modY(ty);

        return (mx == 2 || mx == 4 || mx == 6) && (my == 2 || my == 5);
    }

    /**
     * Ring enumeration around an anchor, nearest first - the old spiral
     * as a generator. Stops after {@code budget} candidates so an
     * unbounded search cannot scan the map every frame per demand.
     */
    public interface CellVisitor {
        /**
         * @return false to stop the walk early.
         */
        boolean visit(int tx, int ty);
    }

    public static void spiral(APosition centre, int maxRadius, int budget, CellVisitor visitor) {
        if (centre == null || visitor == null) return;
        if (maxRadius <= 0 || budget <= 0) return;

        int cx = centre.tx();
        int cy = centre.ty();
        int seen = 0;

        for (int r = 0; r <= maxRadius && seen < budget; r++) {
            for (int dx = -r; dx <= r && seen < budget; dx++) {
                int dy = r - Math.abs(dx);
                int[][] steps = dy == 0
                    ? new int[][]{{dx, 0}}
                    : new int[][]{{dx, dy}, {dx, -dy}};
                for (int[] step : steps) {
                    if (seen >= budget) return;
                    seen++;
                    if (!visitor.visit(cx + step[0], cy + step[1])) return;
                }
            }
        }
    }

    /**
     * Combat buildings keep off the choke (legacy {@code forCombatBuilding}):
     * nothing warps within 4.1 tiles of the nearest choke by ground.
     */
    public static boolean tooCloseToChoke(APosition tile) {
        if (tile == null) return true;
        try {
            return tile.distToNearestChokeLessThan(4.1);
        } catch (Exception | Error ignored) {
            return true;
        }
    }

    public static boolean isPylonGroup(AUnitType type) {
        return type != null && type.isPylon();
    }

    public static boolean isGatewayGroup(AUnitType type) {
        return type != null && type.isGateway();
    }
}
