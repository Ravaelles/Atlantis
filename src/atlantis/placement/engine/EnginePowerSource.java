package atlantis.placement.engine;

import atlantis.map.position.APosition;
import atlantis.map.position.PsiField;
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

    /**
     * Frame-keyed memo, same pattern as the terrain source: rank asks every
     * candidate for every building goal in one update, but pylons do not move
     * mid-update. One Select scan per frame, pure rectangle checks after.
     */
    private static int memoFrame = Integer.MIN_VALUE;
    private static Object memoGame = null;
    private static java.util.List<AUnit> memoPylons = java.util.Collections.emptyList();
    private static java.util.List<AUnit> memoUnfinished = java.util.Collections.emptyList();
    private static final java.util.Map<Long, Boolean> memoPowered = new java.util.HashMap<>();
    private static final java.util.Map<Long, java.util.List<Integer>> memoIncoming = new java.util.HashMap<>();
    private static final java.util.Map<Long, Boolean> memoCoverable = new java.util.HashMap<>();

    private static long key(int tx, int ty) {
        return (((long) tx) << 32) | (ty & 0xffffffffL);
    }

    /**
     * Test seam: the memo is keyed by (frame, game), but every stub
     * world plays frames 0-5 on a re-mocked game the key cannot tell
     * apart - a powered tile from one test reads unpowered (or the
     * reverse) in the next. The harness clears this per test, next to
     * the other static resets.
     */
    public static void clearForTests() {
        memoFrame = Integer.MIN_VALUE;
        memoGame = null;
        memoPylons = java.util.Collections.emptyList();
        memoUnfinished = java.util.Collections.emptyList();
        memoPowered.clear();
        memoIncoming.clear();
        memoCoverable.clear();
    }

    private static void memo(int frame) {
        Object game = atlantis.Atlantis.game();
        if (frame == memoFrame && game == memoGame) return;
        memoFrame = frame;
        memoGame = game;
        memoPylons = new java.util.ArrayList<>(Select.ourOfType(AUnitType.Protoss_Pylon).list());
        memoUnfinished = new java.util.ArrayList<>(Select.ourWithUnfinished(AUnitType.Protoss_Pylon).list());
        memoPowered.clear();
        memoIncoming.clear();
        memoCoverable.clear();
    }

    @Override
    public boolean isPowered(int tx, int ty) {
        // No game attached (a unit test): there are no Pylons, so nothing is
        // powered. Answering false here keeps the rule "power comes from our own
        // Pylons" true everywhere, instead of throwing from a cache inside Select.
        if (atlantis.Atlantis.game() == null) return false;

        memo(atlantis.game.A.now());
        Boolean hit = memoPowered.get(key(tx, ty));
        if (hit != null) return hit;

        boolean powered = anyCovers(memoPylons, tx, ty, 1, 1);

        memoPowered.put(key(tx, ty), powered);
        return powered;
    }

    /**
     * One PsiField sweep over a pylon list, at the given footprint, so the
     * engine rule lives once. Result memos stay tile-keyed (the footprint
     * answers deliberately skip them - one key cannot name all footprints).
     * Tile-point callers pass 1x1: an approximation the legacy path (exact
     * center) agrees with everywhere except sub-tile razor boundaries, and
     * the verdict layer degrades a miss to NEEDS_NEW_PYLON, not to a deadlock.
     */
    private boolean anyCovers(java.util.List<AUnit> pylons, int tx, int ty, int w, int h) {
        for (AUnit pylon : pylons) {
            int[] origin = pylonOrigin(pylon);
            if (PsiField.covers(origin[0], origin[1], tx, ty, w, h)) return true;
        }

        return false;
    }

    /**
     * The Pylon's origin tile - the address {@link PsiField} reads power
     * from. {@code tx()/ty()} are center tiles on a live engine (a 2x2
     * Pylon at origin [15,49] centers on (16,50)) but origin tiles in
     * stubs, so feeding them in double-centers the field live and
     * phantom-powers the rim (measured 2026-11-07 vs Steamhammer:
     * Gateway [12,54] served all game on a field dy=176px out). The
     * engine's own top-left tile is right in both worlds; fail-open to
     * the old read when it is missing.
     */
    private static int[] pylonOrigin(AUnit pylon) {
        try {
            bwapi.TilePosition tile = pylon.tilePosition();
            if (tile != null) return new int[]{tile.getX(), tile.getY()};
        } catch (RuntimeException ignored) {
        }

        return new int[]{pylon.tx(), pylon.ty()};
    }

    private List<Integer> incomingFrames(java.util.List<AUnit> unfinished, int tx, int ty, int w, int h) {
        List<Integer> frames = new ArrayList<>();
        for (AUnit pylon : unfinished) {
            if (pylon.isCompleted()) continue;
            int[] origin = pylonOrigin(pylon);
            if (!PsiField.covers(origin[0], origin[1], tx, ty, w, h)) continue;

            frames.add(framesUntilComplete(pylon));
        }

        return frames;
    }

    @Override
    public List<Integer> incomingPowerFrames(int tx, int ty) {
        if (atlantis.Atlantis.game() == null) return java.util.Collections.emptyList();

        memo(atlantis.game.A.now());
        long k = key(tx, ty);
        List<Integer> hit = memoIncoming.get(k);
        if (hit != null) return hit;

        List<Integer> frames = incomingFrames(memoUnfinished, tx, ty, 1, 1);

        memoIncoming.put(k, frames);
        return frames;
    }

    /**
     * Footprint-aware answers: the engine enforces power at the footprint
     * center, so a building whose origin tile reads powered but whose
     * center does not must not be served (live 2026-11-03 vs Steamhammer:
     * 4x3 Gateway at [123,26] with warping Pylon at [121,22] - tile-point
     * dy=144 reads in, center dy=176 reads out, engine refused every
     * Build and the row strangled on timeouts). Same PsiField rule as the
     * tile-point answers, evaluated at the center instead of the origin.
     */
    @Override
    public boolean isPowered(int tx, int ty, int w, int h) {
        if (atlantis.Atlantis.game() == null) return false;

        memo(atlantis.game.A.now());

        return anyCovers(memoPylons, tx, ty, w, h);
    }

    @Override
    public List<Integer> incomingPowerFrames(int tx, int ty, int w, int h) {
        if (atlantis.Atlantis.game() == null) return java.util.Collections.emptyList();

        memo(atlantis.game.A.now());

        return incomingFrames(memoUnfinished, tx, ty, w, h);
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
        if (atlantis.Atlantis.game() == null) return false;

        memo(atlantis.game.A.now());
        long k = key(tx, ty);
        Boolean hit = memoCoverable.get(k);
        if (hit != null) return hit;

        boolean coverable = canBeCoveredByNewPylonRaw(tx, ty);
        memoCoverable.put(k, coverable);
        return coverable;
    }

    private static boolean canBeCoveredByNewPylonRaw(int tx, int ty) {
        int reach = PYLON_RADIUS_TILES + 1;

        for (int x = tx - reach; x <= tx + reach; x += 2) {
            for (int y = ty - reach; y <= ty + reach; y += 2) {
                if (x < 0 || y < 0) continue;
                if (isPylonSpotFree(x, y) && tileWithinRadius(x, y, tx, ty)) return true;
            }
        }

        return false;
    }
}
