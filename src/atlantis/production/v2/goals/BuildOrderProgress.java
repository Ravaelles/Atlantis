package atlantis.production.v2.goals;

import atlantis.production.v2.Producible;

/**
 * What the game already has towards the build order: the port that keeps
 * {@link BuildOrderGoals} stateless without re-emitting finished rows.
 *
 * <p>
 * The build order is re-read every frame. A row is the N-th occurrence of its
 * item in the file, so it is satisfied once {@link #produced} reaches N.
 * </p>
 */
public interface BuildOrderProgress {

    /** Supply in use now, as BWAPI reports it (queued units are already in it). */
    int supplyUsed();

    /**
     * How many of this item count towards the build order: completed, in
     * production and pending construction, minus what the game started with
     * (the first base, the starting workers). For tech/upgrades: levels
     * researched or in research.
     */
    int produced(Producible item);
}
