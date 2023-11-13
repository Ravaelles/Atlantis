package atlantis.production.dynamic.expansion.terran;

import atlantis.game.A;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.units.select.Count;
import atlantis.util.We;

public class TerranEarlyExpansion {
    public static boolean shouldExpandEarly() {
        if (!We.terran()) return false;
        if (!GamePhase.isEarlyGame()) return false;
        if (Count.basesWithUnfinished() >= 2) return false;

        return A.hasMinerals(minMinerals());
    }

    private static int minMinerals() {
        return EnemyStrategy.isUnknownOrRush() ? 450 : 320;
    }
}
