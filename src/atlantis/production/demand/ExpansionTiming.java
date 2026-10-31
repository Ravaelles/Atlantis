package atlantis.production.demand;

/**
 * One class answering: build a Nexus now? (owner 2026-11-01: expansion is
 * one timing decision, not a thicket spread over three authors).
 *
 * <p>The Protoss doctrine, rule for rule in legacy order with legacy
 * numbers - {@code ProtossShouldExpand} (live game), {@code ExpansionGoal}
 * (board) and {@code DynamicGoals} (V2) all read this, so the three
 * authors cannot drift. The second-base thicket and the third-base
 * file stay where they are: this predicate only owns the top-level
 * matrix, and routes the rest to them.</p>
 *
 * <p>Pure: snapshot in, verdict out. The verdict names its reason, the
 * way {@code ShouldExpand.reason} always did - a silent no is a bug
 * factory.</p>
 */
public final class ExpansionTiming {

    /** Last verdict's reason (LimitedBases, DefendRush, ...). Read, never set. */
    public static String reason = "_NO_EXPAND_REASON_";

    /** The doctrine's inputs, all plain numbers - no live reads in here. */
    public static final class Snapshot {
        public int minerals;
        /** Finished, building and queued - every base that counts as ours. */
        public int basesTotal;
        /** Under construction or queued. */
        public int basesInProduction;
        /** Finished, building, queued and planned. */
        public int basesPlanned;
        public int basesUnfinished;
        public boolean enemyZerg;
        public boolean enemyProtoss;
        public boolean enemyTerran;
        public int enemyMutalisks;
        public int cannons;
        public int armyStrength;
        public int seconds;
    }

    public enum Verdict {
        /** Bank rule: the Nexus goes now, no thicket consulted. */
        WANT_BANK,
        /** A veto fired: do not expand, reason named. */
        HOLD,
        /** Second-base thicket or third-base file owns this frame. */
        THICKET
    }

    /**
     * Names the mid-game veto (mutas, rush, saturation), or null. Blind to
     * the bank windfall and the caps on purpose: the board names pressure
     * even when the windfall overrules it, exactly like the legacy gate did.
     */
    public static String vetoOf(Snapshot in) {
        if (dontExpandAgainstMutas(in)) return "DontExpandAgainstMutas";
        if (in.minerals < 384 && defendRush(in)) return "DefendRush";
        if (tooManyInProgress(in)) return "HasInProgress(" + in.basesInProduction + ")";
        return null;
    }

    public static Verdict decide(Snapshot in) {
        if (in.minerals < 300) return hold("NotEnoughMinerals");

        if (in.minerals >= 777 && in.basesPlanned <= 3) return want("LimitedBases");

        if (dontExpandAgainstMutas(in)) return hold("DontExpandAgainstMutas");

        if (in.minerals < 384 && defendRush(in)) return hold("DefendRush");

        if (tooManyInProgress(in)) return hold("HasInProgress(" + in.basesInProduction + ")");

        if (in.basesTotal >= 7) return hold("TooManyBases");

        return Verdict.THICKET;
    }

    private static boolean dontExpandAgainstMutas(Snapshot in) {
        if (!in.enemyZerg) return false;
        if (in.enemyMutalisks == 0) return false;

        return in.minerals >= 800 || in.basesInProduction * 3 >= in.cannons;
    }

    private static boolean defendRush(Snapshot in) {
        if (in.seconds >= 9 * 60) return false;

        if (!in.enemyProtoss && !in.enemyTerran && !in.enemyZerg) return false;

        return in.armyStrength <= 115;
    }

    private static boolean tooManyInProgress(Snapshot in) {
        if (in.basesInProduction == 0) return false;

        return (double) (in.minerals / (400 * in.basesInProduction)) <= 1.15;
    }

    private static Verdict want(String why) {
        reason = why;
        return Verdict.WANT_BANK;
    }

    private static Verdict hold(String why) {
        reason = why;
        return Verdict.HOLD;
    }
}
