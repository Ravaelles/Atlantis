package atlantis.production.constructions.position.conditions.can_build_here;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

/**
 * Is this tile inside a finished Pylon's power field?
 *
 * <p>
 * <b>The answer is derived from our own Pylons, never asked of the engine.</b>
 * {@code Game.hasPowerPrecise} is not usable: it returned false for tiles a
 * finished Pylon clearly covered, so Forge and Cybernetics Core were placed in
 * unpowered spots while the Gateways beside them were fine (measured 2026-10-08,
 * see {@code _AI/POSITION-FINDER.md} point 4). That is why
 * {@code EnginePowerSource} derives it too, and why this class must agree with it
 * rather than keep its own idea of range.
 * </p>
 *
 * <p>
 * <b>This used to answer with a 3.2-tile radius measured from the position's
 * centre, which was wrong in two ways at once</b> - the engine's Pylon radius is
 * <b>6</b> tiles, measured from the Pylon's <b>footprint</b> (a 2x2 Pylon powers
 * from any of its tiles, so a tile diagonally off a corner is 6+1 away, not 6). So
 * the old check refused tiles that were genuinely powered, which is how a building
 * could look "unpowered" while sitting next to a Pylon.
 * </p>
 *
 * <p>
 * The radius and the footprint arithmetic are kept here in step with
 * {@code atlantis.placement.engine.EnginePowerSource}; if the two ever disagree
 * again, this comment is the place that says which one is authoritative (the
 * engine) and why.
 * </p>
 */
public class IsPoweredByAPylon {

    /** The engine's Pylon power radius in build tiles, from the Pylon's own tiles. */
    public static final int PYLON_POWER_RADIUS_TILES = 6;

    public static boolean check(APosition position) {
        if (position == null) return false;

        for (AUnit pylon : Select.ourOfType(AUnitType.Protoss_Pylon).list()) {
            if (withinPowerRadius(pylon, position)) return true;
        }

        return false;
    }

    /**
     * Distance is measured from the tile to the Pylon's <b>footprint</b> (nearest
     * tile of it), Chebyshev - same rule as {@code EnginePowerSource}.
     */
    private static boolean withinPowerRadius(AUnit pylon, APosition position) {
        int nearestX = clamp(position.tx(), pylon.tx(), pylon.tx() + pylon.type().getTilesWidth() - 1);
        int nearestY = clamp(position.ty(), pylon.ty(), pylon.ty() + pylon.type().getTilesHeights() - 1);

        int dx = Math.abs(position.tx() - nearestX);
        int dy = Math.abs(position.ty() - nearestY);
        return Math.max(dx, dy) <= PYLON_POWER_RADIUS_TILES;
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}
