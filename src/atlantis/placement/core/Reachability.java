package atlantis.placement.core;

/**
 * "Can a ground unit get from here to there?" - the question this project kept
 * asking
 * the engine and getting a wrong answer for.
 *
 * <p>
 * <b>Why this is a port and not a helper.</b> The engine's answer
 * ({@code Game.hasPath}) is implemented over BWAPI regions, and the OpenBW
 * server
 * publishes none, so it returns {@code false} for every pair of tiles - a tile
 * cannot
 * reach itself (measured 2026-10-10, {@code _AI/CHALLENGES/OpenBW.md}). That is
 * not a
 * bug we can fix in application code, and it is not a bug in one caller: the
 * same
 * question is asked from at least three places, each of which grew its own
 * workaround or
 * its own wrong answer. A port gives the question one implementation, chosen at
 * startup,
 * and makes "which one is in charge here" a decision instead of an accident.
 * </p>
 *
 * <p>
 * Implementations are expected to be three, in ascending order of independence
 * from the
 * engine:
 * </p>
 * <ul>
 * <li>an engine-backed one, which is the fastest and correct on a real
 * StarCraft;</li>
 * <li>a map-data one built on {@link ConnectivityRegions}, which works headless
 * and is
 * the point of this exercise;</li>
 * <li>a test one, where the harness decides.</li>
 * </ul>
 *
 * <p>
 * <b>The contract callers may rely on:</b>
 * {@link #canReach(int, int, int, int)} is
 * <b>reflexive</b> - a tile always reaches itself. Every implementation must
 * honour that,
 * because it is the property the engine violates and the reason a builder could
 * not
 * build on the tile it was standing on.
 * </p>
 */
public interface Reachability {

    /**
     * True when a ground unit can travel between these two map tiles.
     *
     * <p>
     * Implementations must return {@code true} when the two coordinates are the
     * same
     * walkable tile. Returning {@code false} there is the exact failure this port
     * exists
     * to make impossible.
     * </p>
     */
    boolean canReach(int fromTx, int fromTy, int toTx, int toTy);
}
