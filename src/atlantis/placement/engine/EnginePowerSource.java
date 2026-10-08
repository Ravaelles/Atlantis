package atlantis.placement.engine;

import atlantis.map.position.APosition;
import atlantis.placement.core.PsiGating;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

import java.util.ArrayList;
import java.util.List;

/**
 * The engine-backed {@link PsiGating.PowerSource}: the placement core's only
 * window into Psi power.
 *
 * <p>
 * "Is this tile powered" is answered by the engine itself
 * ({@code Game.hasPowerPrecise}), which already knows the completed-Pylon rule;
 * "when will an incoming Pylon cover this tile" is answered from our own
 * under-construction Pylons, using the same radius the engine applies to a
 * finished one. Both stay here so the gating logic itself is pure.
 * </p>
 */
public final class EnginePowerSource implements PsiGating.PowerSource {

    /**
     * Pylon power radius in build tiles. The engine's own value for a finished
     * Pylon is 6 tiles from its centre; a 2x2 Pylon's top-left therefore reaches
     * 6 + 1 tiles away, which is what {@link #withinPowerRadius} checks.
     */
    private static final int PYLON_RADIUS_TILES = 6;

    @Override
    public boolean isPowered(int tx, int ty) {
        AUnitType probe = AUnitType.Protoss_Gateway;
        return atlantis.Atlantis.game().hasPowerPrecise(tx, ty, probe.ut());
    }

    @Override
    public List<Integer> incomingPowerFrames(int tx, int ty) {
        List<Integer> frames = new ArrayList<>();

        for (AUnit pylon : Select.ourWithUnfinished(AUnitType.Protoss_Pylon).list()) {
            if (pylon.isCompleted()) continue;
            if (!withinPowerRadius(pylon, tx, ty)) continue;

            frames.add(framesUntilComplete(pylon));
        }

        return frames;
    }

    /**
     * Could a Pylon we may place cover this tile? True when any free 2x2 spot
     * exists within the Pylon radius - the question the pull-forward verdict turns
     * into "go build a Pylon" instead of "this spot is useless".
     *
     * <p>
     * Deliberately a coarse check (free terrain in range), not a full placement
     * decision: the real Pylon placement happens through this same planner on the
     * next pass, so pretending to decide it here would duplicate that logic.
     * </p>
     */
    @Override
    public boolean canBeCoveredByNewPylon(int tx, int ty) {
        int reach = PYLON_RADIUS_TILES + 1;

        for (int x = tx - reach; x <= tx + reach; x += 2) {
            for (int y = ty - reach; y <= ty + reach; y += 2) {
                if (x < 0 || y < 0) continue;
                if (isPylonSpotFree(x, y) && tileWithinRadius(x, y, tx, ty)) return true;
            }
        }

        return false;
    }

    private static boolean isPylonSpotFree(int x, int y) {
        for (int dx = 0; dx < 2; dx++) {
            for (int dy = 0; dy < 2; dy++) {
                APosition tile = APosition.create(x + dx, y + dy);
                if (tile.isOutOfBounds() || !tile.isWalkable()) return false;
                if (!tile.isBuildableIncludeBuildings()) return false;
            }
        }
        return true;
    }

    private static boolean tileWithinRadius(int pylonX, int pylonY, int tx, int ty) {
        return Math.max(Math.abs(tx - pylonX), Math.abs(ty - pylonY)) <= PYLON_RADIUS_TILES + 1;
    }

    /** Would a Pylon at this unit's position power the tile once finished? */
    private static boolean withinPowerRadius(AUnit pylon, int tx, int ty) {
        // Distance from the tile to the Pylon's footprint, in tiles.
        int nearestX = clamp(tx, pylon.tx(), pylon.tx() + pylon.type().getTilesWidth() - 1);
        int nearestY = clamp(ty, pylon.ty(), pylon.ty() + pylon.type().getTilesHeights() - 1);

        int dx = Math.abs(tx - nearestX);
        int dy = Math.abs(ty - nearestY);
        return Math.max(dx, dy) <= PYLON_RADIUS_TILES;
    }

    /**
     * Frames until this building finishes, estimated from its health fraction -
     * the engine does not expose remaining build time through this binding. The
     * estimate is an upper bound (a fresh Pylon reports its full duration), which
     * is the safe direction for a time shift.
     */
    private static int framesUntilComplete(AUnit unit) {
        int hpPercent = unit.hpPercent();
        if (hpPercent >= 100) return 0;

        int total = unit.type().totalTrainTime();
        int remaining = (int) Math.ceil(total * (100 - hpPercent) / 100.0);
        return Math.max(0, remaining);
    }

    private static int clamp(int value, int min, int max) {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }
}
