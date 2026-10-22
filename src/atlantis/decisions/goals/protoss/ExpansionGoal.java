package atlantis.decisions.goals.protoss;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;
import atlantis.units.AUnitType;

/**
 * Second base (and windfalls) on the board: the top-level WHEN of
 * {@code ProtossShouldExpand.shouldExpand}, row for row. Below 300
 * minerals there is nothing to discuss; past 777 with at most three
 * bases planned the Nexus goes regardless; mutas, rush defense and
 * unfinished-base saturation veto; seven bases cap the game. The
 * second base itself is a yes once nothing vetoes (which natural is
 * the siting half, owned by ProtossExpandNow, not this predicate).
 * Third and later bases stay silent here: their thicket (worker and
 * gateway ratios, pressure reads, observer tech) migrates only with
 * a game proof each, never by mirroring. Affordability is the
 * wallet's job, not this predicate's.
 */
public final class ExpansionGoal implements Goal {

    public static final int EXPANSION_MIN_WORKERS = 12;

    @Override
    public String id() {
        return "expansion";
    }

    @Override
    public boolean active(BoardState state) {
        return wanted(state);
    }

    @Override
    public int rank() {
        return GoalRanks.EXPANSION;
    }

    @Override
    public int priceMinerals() {
        return AUnitType.Protoss_Nexus.mineralPrice();
    }

    @Override
    public int priceGas() {
        return AUnitType.Protoss_Nexus.gasPrice();
    }

    /**
     * The legacy top-level matrix, in legacy order. Stub states decide
     * in milliseconds; the numbers are the legacy's, never re-tuned.
     */
    public static boolean wanted(BoardState state) {
        if (state.minerals < 300) return false;
        if (state.workerCount < EXPANSION_MIN_WORKERS) return false;
        if (state.baseUnderAttack) return false;

        if (state.minerals >= 777 && state.basesPlanned <= 3) return true;

        if (veto(state) != null) return false;

        if (state.baseCount >= 7) return false;

        if (state.baseCount <= 1 && state.basesUnfinished == 0) return true;

        return false;
    }

    /**
     * Names the veto, or null when no veto fires. Public as a seam: the
     * saturation veto never flips wanted() on its own today (an
     * unfinished base also closes the second-base row and the third+
     * thicket is still legacy's), so it pins here instead - and a named
     * veto is what a twin will read when the thicket lands.
     */
    public static String veto(BoardState state) {
        if (dontExpandAgainstMutas(state)) return "DontExpandAgainstMutas";
        if (defendRush(state)) return "DefendRush";
        if (tooManyInProgress(state)) return "HasInProgress";

        return null;
    }

    private static boolean dontExpandAgainstMutas(BoardState state) {
        if (!"Zerg".equals(state.enemyRace)) return false;
        if (state.enemyMutalisks == 0) return false;

        return state.minerals >= 800 || state.basesUnfinished * 3 >= state.cannonCount;
    }

    private static boolean defendRush(BoardState state) {
        if (state.minerals >= 384) return false;
        if (state.secondsElapsed >= 9 * 60) return false;

        return state.armyStrength <= 115;
    }

    private static boolean tooManyInProgress(BoardState state) {
        if (state.basesUnfinished == 0) return false;

        return (double) (state.minerals / (400 * state.basesUnfinished)) <= 1.15;
    }
}
