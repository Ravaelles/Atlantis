package atlantis.debug.tweaker;

import atlantis.debug.tweaker.TunerObjective.Row;

import java.util.List;

/**
 * Promotion bar for tuner candidates (NEXT #160, step 3).
 *
 * <p>Duels are noisy - first blood on instant-contact maps is spawn
 * luck (measured 97 vs 163 frames on identical code) - so a vector is
 * promoted only after it wins K-of-N replicates, never on one lucky
 * game. Pure tooling, no game-loop callers; the replicate runner that
 * feeds it is batch orchestration and lands with the wiring.
 */
public final class TunerPromotion {

    private TunerPromotion() { }

    /** Wins among the replicates. */
    public static int wins(List<Row> replicates) {
        if (replicates == null) return 0;

        int wins = 0;
        for (Row row : replicates) {
            if (row != null && row.win()) wins++;
        }
        return wins;
    }

    /**
     * True when the candidate earned promotion: at least
     * {@code minGames} replicates played and at least
     * {@code winsRequired} of them won. A short or losing record
     * never promotes - the hall of fame keeps the old champion.
     */
    public static boolean isPromoted(List<Row> replicates, int winsRequired, int minGames) {
        if (replicates == null || replicates.size() < minGames) return false;

        return wins(replicates) >= winsRequired;
    }
}
