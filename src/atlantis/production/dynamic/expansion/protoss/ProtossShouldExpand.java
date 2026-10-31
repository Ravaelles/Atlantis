package atlantis.production.dynamic.expansion.protoss;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.production.dynamic.expansion.decision.CancelNotStartedBases;
import atlantis.production.dynamic.expansion.decision.ShouldExpand;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.units.select.Count;
import atlantis.util.cache.Cache;

public class ProtossShouldExpand {
    private static final Cache<Boolean> cache = new Cache<>();

    protected static int bases;
    protected static int basesInProduction;

    public static boolean shouldExpand() {
        // A reason, not a bare false: this is the first gate a Protoss passes and it is
        // the one a game sits behind for minutes while it probes (GAME_631F4FE6 queued
        // its first Nexus at 4:30 with 778 minerals - the line in the log said
        // "LimitedBases", and a "no" here would have left that string standing for
        // minutes beforehand). Set directly, not through no(reason): that one also
        // cancels not-started bases, and saving minerals is the opposite of that.
        if (A.minerals() < 300) {
            ShouldExpand.reason = "NotEnoughMinerals";

            return false;
        }

        bases = Count.existingOrInProductionOrInQueue(AtlantisRaceConfig.BASE);
//        basesInProduction = Count.inProductionOrInQueue(AtlantisRaceConfig.BASE);
        basesInProduction = Count.inProductionOrInQueue(AtlantisRaceConfig.BASE);

        // === One doctrine, three readers (owner 2026-11-01) =====================
        // The top-level matrix lives in ExpansionTiming; this method only maps
        // live game onto its snapshot and keeps the legacy side-effects (yes/no
        // set the reason, no() cancels not-started bases when poor). The thicket
        // below is untouched: second base and third+ still run their files.
        atlantis.production.demand.ExpansionTiming.Snapshot in = readTimingSnapshot();
        atlantis.production.demand.ExpansionTiming.Verdict verdict =
            atlantis.production.demand.ExpansionTiming.decide(in);
        if (verdict == atlantis.production.demand.ExpansionTiming.Verdict.WANT_BANK)
            return yes(atlantis.production.demand.ExpansionTiming.reason);
        if (verdict == atlantis.production.demand.ExpansionTiming.Verdict.HOLD)
            return no(atlantis.production.demand.ExpansionTiming.reason);
        // THICKET falls through to the files below.
        // =========================================================

        // === Second base ===========================================

        if (bases <= 1 && basesInProduction == 0) return ProtossShouldExpandToNaturalBase.forSecondBase();

        // =========================================================

        if (bases >= 7) return no("TooManyBases");

        // =========================================================

        return ProtossShouldExpandToThirdAndLaterBase.forThirdAndLaterBases();
    }

    /**
     * Live game onto the doctrine snapshot. Same reads the matrix always
     * used - Count/Enemy/Army/strategy numbers, never re-tuned here.
     */
    private static atlantis.production.demand.ExpansionTiming.Snapshot readTimingSnapshot() {
        atlantis.production.demand.ExpansionTiming.Snapshot in =
            new atlantis.production.demand.ExpansionTiming.Snapshot();
        in.minerals = A.minerals();
        in.basesTotal = bases;
        in.basesInProduction = basesInProduction;
        in.basesPlanned = Count.basesWithPlanned();
        in.basesUnfinished = Count.basesWithUnfinished();
        in.enemyZerg = Enemy.zerg();
        in.enemyProtoss = Enemy.protoss();
        in.enemyTerran = Enemy.terran();
        in.enemyMutalisks = EnemyUnits.mutas();
        in.cannons = Count.cannons();
        in.armyStrength = Army.strength();
        in.seconds = A.s;
        return in;
    }

    protected static boolean yes(String reason) {
        ShouldExpand.reason = reason;

        return true;
    }

    protected static boolean no(String reason) {
        ShouldExpand.reason = reason;

        if (!reason.contains("InProgress") && !A.hasMinerals(550)) {
            CancelNotStartedBases.cancelNotStartedOrEarlyBases(null, reason);
        }

        return false;
    }

    public static boolean needToSaveMineralsForExpansion() {
//        boolean needToSave = !A.hasMinerals(530) && ReservedResources.minerals() >= 400;
        boolean needToSave = A.minerals() < 100 + ReservedResources.minerals();

//        if (needToSave) System.err.println("@ " + A.now() + " - needToSave (" + A.minerals());

        return needToSave;
    }
}
