package atlantis.production.dynamic.expansion.terran;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.information.generic.Army;
import atlantis.production.dynamic.expansion.decision.ShouldExpand;
import atlantis.units.select.Count;
import atlantis.util.cache.Cache;

public class TerranShouldExpand {
    private static Cache<Boolean> cache = new Cache<>();
    private static int bases;
    private static int basesInProduction;

    public static boolean shouldExpand() {
        return cache.get(
            "shouldExpand",
            47,
            () -> {
                bases = Count.bases();
                basesInProduction = Count.inProductionOrInQueue(AtlantisRaceConfig.BASE);
                if (basesInProduction > 0) return false;

                // === Second base ===========================================

                if (bases <= 1) return forSecondBase();

                // === Third and later =======================================

                return forThirdAndLater();
            }
        );
    }

    private static boolean forThirdAndLater() {
        return A.hasMinerals(700) || (A.hasMinerals(600) && Army.strength() >= 115);
    }

    private static boolean forSecondBase() {
        if (TerranShouldExpandToNatural.shouldExpandToNatural()) return true;

        return false;
    }

    protected static boolean returnYes(String reason) {
//        ErrorLog.printMaxOncePerMinute("Expand: " + reason);
        ShouldExpand.reason = reason;
        return true;
    }

    public static boolean returnNo(String reason) {
        ShouldExpand.reason = reason;
        return false;
    }
}
