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
 * <b>Power is computed from our own Pylons, not asked of the engine.</b>
 * {@code Game.hasPowerPrecise} is the obvious call and it is not usable here: it
 * returned false for tiles a finished Pylon clearly covered, so Forge and
 * Cybernetics Core were placed in unpowered spots while the Gateways beside them
 * were fine (owner report, 2026-10-08). The JBWAPI binding has a history of this
 * call being wrong in the same way.
 * </p>
 *
 * <p>
 * So the answer is derived: a tile is powered when a <b>completed</b> Pylon is
 * within the engine's Pylon power radius of it. Same radius the engine uses, read
 * from our own unit list, which is the one source that has been reliable
 * throughout this work.
 * </p>
 */
public final class EnginePowerSource implements PsiGating.PowerSource {

    /**
     * Pylon power radius in build tiles, as the engine applies it to a finished
     * Pylon: 6 tiles from the Pylon's own tiles.
     */
    private static final int PYLON_RADIUS_TILES = 6;

    @Override
    public boolean isPowered(int tx, int ty) {
        // No game attached (a unit test): there are no Pylons, so nothing is
        // powered. Answering false here keeps the rule "power comes from our own
        // Pylons" true everywhere, instead of throwing from a cache inside Select.
        if (atlantis.Atlantis.game() == null) return false;

        return Select.ourOfType(AUnitType.Protoss_Pylon)
                .inRadius(PYLON_RADIUS_TILES, APosition.create(tx, ty))
                .notEmpty();
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
