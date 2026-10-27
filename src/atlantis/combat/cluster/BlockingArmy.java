package atlantis.combat.cluster;

import atlantis.battle.cluster.Alpha;

import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

/**
 * Stardust rule, adapted (owner 2026-10-23): never run
 * through a blocking army. An armed enemy force sitting between the
 * cluster and home is fought through instead of fled into - a retreat
 * destination past them is death, not safety.
 *
 * <p>Only visible enemies count (last-known ghosts must not pin the
 * cluster in place), and only groups ({@code MIN_BLOCKING_ENEMIES}+):
 * a single stray is dodged around, not fought. The corridor test is
 * pure geometry so it pins in a unit test without a game.</p>
 */
public final class BlockingArmy {

    private static final int MIN_BLOCKING_ENEMIES = 2;
    private static final double BLOCK_CORRIDOR_TILES = 4.0;

    private BlockingArmy() {
    }

    public static boolean blocksWayHome() {
        AUnit main = HomeGround.resolve().main();
        if (main == null || main.position() == null) return false;

        HasPosition center = Alpha.alphaCenter();
        if (center == null || center.position() == null) return false;
        if (center.distTo(main.position()) < 1) return false;

        Selection armed = Select.enemyCombatUnits().havingWeapon();
        if (armed.count() < MIN_BLOCKING_ENEMIES) return false;

        int blocking = 0;
        for (AUnit enemy : armed.list()) {
            if (enemy == null || !enemy.hasPosition()) continue;
            if (blocksPath(
                center.tx(), center.ty(), main.tx(), main.ty(),
                enemy.tx(), enemy.ty(), BLOCK_CORRIDOR_TILES
            )) {
                blocking++;
            }
        }

        return blocking >= MIN_BLOCKING_ENEMIES;
    }

    /**
     * True when (ex, ey) sits within {@code corridor} tiles of the
     * center→home segment, strictly between the endpoints. Pure.
     */
    public static boolean blocksPath(
        double cx, double cy, double hx, double hy, double ex, double ey, double corridor
    ) {
        double dx = hx - cx;
        double dy = hy - cy;
        double len2 = dx * dx + dy * dy;
        if (len2 < 1) return false;

        double t = ((ex - cx) * dx + (ey - cy) * dy) / len2;
        if (t <= 0 || t >= 1) return false;

        double px = cx + t * dx - ex;
        double py = cy + t * dy - ey;

        return px * px + py * py <= corridor * corridor;
    }
}
