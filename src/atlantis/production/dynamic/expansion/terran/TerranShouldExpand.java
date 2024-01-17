package atlantis.production.dynamic.expansion.terran;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.map.base.BaseLocations;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.cache.Cache;
import atlantis.util.log.ErrorLog;

public class TerranShouldExpand {
    private static Cache<Boolean> cache = new Cache<>();

    public static boolean shouldExpand() {
        return cache.get(
            "shouldExpand",
            63,
            () -> {
                int bases = Count.bases();
                int basesInProduction = Count.inProductionOrInQueue(AtlantisRaceConfig.BASE);

                if (basesInProduction > 0) return false;
                if (bases <= 1) return forSecond();

                return forThirdAndLater();
            }
        );
    }

    private static boolean forThirdAndLater() {
        return false;
    }

    private static boolean forSecond() {
        if (TerranShouldExpandToNatural.shouldExpandToNatural()) return decisionTrue("Expand to natural");

        return false;
    }

    private static boolean decisionTrue(String reason) {
        ErrorLog.printMaxOncePerMinute("Expand: " + reason);
        return true;
    }
}
