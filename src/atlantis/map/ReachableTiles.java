package atlantis.map;

import atlantis.Atlantis;
import atlantis.placement.core.Reachability;
import atlantis.placement.engine.EngineReachabilityProbe;
import atlantis.placement.engine.RegionReachability;
import atlantis.placement.engine.EngineWalkabilityGrid;

/**
 * The single place the game asks "can a ground unit get from A to B?".
 *
 * <p>
 * <b>Why this class exists.</b> Before it, that question was asked with
 * {@code Atlantis.game().hasPath(...)} from at least six different callers, and
 * every one
 * of them was wrong on OpenBW in the same way: the engine's query is
 * implemented over
 * BWAPI <b>regions</b>, the server publishes {@code regionCount = 0}, and so it
 * answers
 * {@code false} for every pair of tiles - a tile cannot even reach itself
 * (measured
 * 2026-10-10, {@code _AI/CHALLENGES/OpenBW.md}). One caller grew an
 * engine-then-map
 * fallback ({@code MapTiles.hasPathBetween}), one was switched to that fallback
 * ({@code IsProbablyInAnotherRegion}), and the rest kept asking the broken
 * question - which
 * is the recurring shape of this whole investigation: the same question,
 * several answers,
 * quietly disagreeing.
 * </p>
 *
 * <p>
 * <b>The design.</b> Which implementation is in charge is decided once, at
 * startup, by
 * {@link EngineReachabilityProbe}, which <em>measures</em> whether the engine
 * can answer
 * (does a walkable tile reach itself?) rather than assuming it from a version
 * or a flag.
 * A healthy engine keeps the fast engine answer; a region-less one is answered
 * from
 * terrain by {@link RegionReachability}, which cannot have this failure because
 * it never
 * asks the engine anything.
 * </p>
 *
 * <p>
 * <b>Failure mode, stated plainly:</b> this port decides reachability for
 * <em>our</em>
 * decisions. It does not make the engine's own builder-reachability check work
 * - that one
 * lives inside the library's {@code canBuild} and is what still refuses
 * {@code Unit.build(...)}. See {@code _AI/PLAN-REGION-DETECTION.md}, stage 4.
 * </p>
 */
public final class ReachableTiles {

    /**
     * Cache: the probe's answer cannot change within a game (regions are per-game
     * map data).
     */
    private static Reachability chosen = null;
    private static String chosenReason = "not resolved yet";

    /** A seam for tests: use this implementation instead of resolving one. */
    public static void useReachability(Reachability reachability, String reason) {
        chosen = reachability;
        chosenReason = reason;
    }

    public static void reset() {
        chosen = null;
        chosenReason = "not resolved yet";
    }

    /** True when a ground unit can travel between the two positions. */
    public static boolean between(atlantis.map.position.HasPosition from, atlantis.map.position.HasPosition to) {
        if (from == null || to == null)
            return false;

        return from(from.tx(), from.ty()).canReach(from.tx(), from.ty(), to.tx(), to.ty());
    }

    /** True when a ground unit can travel between the two tiles. */
    public static boolean between(int fromTx, int fromTy, int toTx, int toTy) {
        return from(fromTx, fromTy).canReach(fromTx, fromTy, toTx, toTy);
    }

    /**
     * The implementation in charge, resolved on first use.
     *
     * <p>
     * <b>Why the probe and not a flag.</b> A flag would be one more thing a run can
     * get
     * wrong, and it would not answer the question that actually matters ("is the
     * engine's
     * answer sane right now?"). The probe asks the engine whether a tile reaches
     * itself:
     * one query, no version sniffing, and it keeps working if a future engine
     * starts
     * publishing regions correctly.
     * </p>
     */
    private static Reachability from(int tx, int ty) {
        if (chosen != null)
            return chosen;

        // The tile asked about is walkable by construction at most call sites; if it is
        // not, the probe is meaningless, so any tile will do for the capability
        // question.
        EngineReachabilityProbe.Verdict verdict = EngineReachabilityProbe.verdictFor(
                (x, y) -> Atlantis.game() != null && Atlantis.game().hasPath(
                        new bwapi.Position(x * 32, y * 32), new bwapi.Position(x * 32, y * 32)),
                tx, ty);

        if (verdict == EngineReachabilityProbe.Verdict.ENGINE_USABLE) {
            chosen = new EngineBackedReachability();
            chosenReason = "engine regions are usable";
        } else {
            chosen = new RegionReachability(new EngineWalkabilityGrid());
            chosenReason = "engine publishes no regions - answered from terrain";
            System.err.println("ReachableTiles: " + chosenReason);
        }

        return chosen;
    }

    /** Why this implementation - for diagnostics and for tests to assert. */
    public static String chosenReason() {
        return chosenReason;
    }

    /**
     * The engine's own answer, used only when the probe says it is sane. Kept as a
     * named
     * class rather than an inline lambda so "which answer is this" is visible in a
     * stack
     * trace, which is exactly what was missing when six call sites disagreed.
     */
    static final class EngineBackedReachability implements Reachability {
        @Override
        public boolean canReach(int fromTx, int fromTy, int toTx, int toTy) {
            return Atlantis.game().hasPath(
                    new bwapi.Position(fromTx * 32, fromTy * 32),
                    new bwapi.Position(toTx * 32, toTy * 32));
        }
    }
}
