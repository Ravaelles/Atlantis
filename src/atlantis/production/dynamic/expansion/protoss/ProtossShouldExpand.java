package atlantis.production.dynamic.expansion.protoss;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.production.dynamic.expansion.decision.CancelNotStartedBases;
import atlantis.production.dynamic.expansion.decision.ShouldExpand;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.units.select.Count;
import atlantis.game.player.Enemy;
import atlantis.util.cache.Cache;

public class ProtossShouldExpand {
    private static final Cache<Boolean> cache = new Cache<>();

    protected static int bases;
    protected static int basesInProduction;

    public static boolean shouldExpand() {
        if (A.minerals() < 300) return false;

        bases = Count.existingOrInProductionOrInQueue(AtlantisRaceConfig.BASE);
//        basesInProduction = Count.inProductionOrInQueue(AtlantisRaceConfig.BASE);
        basesInProduction = Count.inProductionOrInQueue(AtlantisRaceConfig.BASE);

        // =========================================================

        if (A.hasMinerals(777) && Count.basesWithPlanned() <= 3) return yes("LimitedBases");

        if (!A.hasMinerals(384) && defendRush()) return no("DefendRush");
        if (tooManyInProgress()) return no("HasInProgress(" + basesInProduction + ")");

        // === Second base ===========================================

        if (bases <= 1 && basesInProduction == 0) return ProtossShouldExpandToNaturalBase.forSecondBase();

        // =========================================================

        if (bases >= 7) return no("TooManyBases");

        // =========================================================

        return ProtossShouldExpandToThirdAndLaterBase.forThirdAndLaterBases();
    }

    // =========================================================

    private static boolean defendRush() {
        if (defendVsProtossRush()) return true;
        if (defendVsTerranRush()) return true;
        return defendVsZergRush();
    }

    private static boolean defendVsTerranRush() {
        if (!Enemy.terran()) return false;
        if (A.s >= 9 * 60) return false;
        return Army.strength() <= 115;
    }

    private static boolean defendVsProtossRush() {
        if (!Enemy.protoss()) return false;
        if (A.s >= 9 * 60) return false;
        return Army.strength() <= 115;
    }

    private static boolean defendVsZergRush() {
        if (!Enemy.zerg()) return false;
        if (A.s >= 9 * 60) return false;

        return Army.strength() <= 115;
    }

    private static boolean tooManyInProgress() {
        if (basesInProduction == 0) return false;

        int minerals = A.minerals();

        return (double) (minerals / (400 * basesInProduction)) <= 1.15;
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
