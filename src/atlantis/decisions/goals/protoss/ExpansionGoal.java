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

    /**
     * The legacy top-level matrix, in legacy order. Delegates to
     * {@code ExpansionTiming} (the one doctrine all three authors read);
     * THICKET keeps the board's standing simplification (second base yes
     * when nothing is unfinished, third+ silent here). The worker and
     * under-attack gates are the board's own, kept as they were.
     */
    public static boolean wanted(BoardState state) {
        if (state.workerCount < EXPANSION_MIN_WORKERS) return false;
        if (state.baseUnderAttack) return false;

        atlantis.production.demand.ExpansionTiming.Snapshot in = snapshot(state);
        atlantis.production.demand.ExpansionTiming.Verdict verdict =
            atlantis.production.demand.ExpansionTiming.decide(in);
        if (verdict == atlantis.production.demand.ExpansionTiming.Verdict.WANT_BANK) return true;
        if (verdict == atlantis.production.demand.ExpansionTiming.Verdict.HOLD) return false;

        return state.baseCount <= 1 && state.basesUnfinished == 0;
    }

    /**
     * Names the veto, or null when no veto fires. The board twin reads
     * the short name (no parenthetical detail); the legacy log reads the
     * full reason from the doctrine.
     */
    public static String veto(BoardState state) {
        atlantis.production.demand.ExpansionTiming.Snapshot in = snapshot(state);
        atlantis.production.demand.ExpansionTiming.Verdict verdict =
            atlantis.production.demand.ExpansionTiming.decide(in);
        String veto = atlantis.production.demand.ExpansionTiming.vetoOf(in);
        if (veto == null) return null;

        int open = veto.indexOf('(');
        return open < 0 ? veto : veto.substring(0, open);
    }

    private static atlantis.production.demand.ExpansionTiming.Snapshot snapshot(BoardState state) {
        atlantis.production.demand.ExpansionTiming.Snapshot in =
            new atlantis.production.demand.ExpansionTiming.Snapshot();
        in.minerals = state.minerals;
        in.basesTotal = Math.max(state.baseCount, state.basesPlanned);
        in.basesInProduction = state.basesUnfinished;
        in.basesPlanned = state.basesPlanned;
        in.basesUnfinished = state.basesUnfinished;
        in.enemyZerg = "Zerg".equals(state.enemyRace);
        in.enemyProtoss = "Protoss".equals(state.enemyRace);
        in.enemyTerran = "Terran".equals(state.enemyRace);
        in.enemyMutalisks = state.enemyMutalisks;
        in.cannons = state.cannonCount;
        in.armyStrength = state.armyStrength;
        in.seconds = state.secondsElapsed;
        return in;
    }

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
}
