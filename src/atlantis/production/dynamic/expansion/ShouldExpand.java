package atlantis.production.dynamic.expansion;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.map.base.BaseLocations;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.dynamic.expansion.protoss.ProtossShouldExpand;
import atlantis.production.dynamic.expansion.terran.TerranShouldExpand;
import atlantis.production.dynamic.expansion.terran.TerranShouldExpandToNatural;
import atlantis.production.dynamic.expansion.zerg.ZergShouldExpand;
import atlantis.production.dynamic.zerg.ZergExpansionCommander;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.We;
import org.junit.Assert;

public class ShouldExpand {
    public static String reason = "---";

    public static boolean shouldExpand() {
        if (A.isUms() && !Have.base()) return false;

        if (We.terran()) return TerranShouldExpand.shouldExpand();
        if (We.protoss()) return ProtossShouldExpand.shouldExpand();
        if (We.zerg()) return ZergShouldExpand.shouldExpand();

        return false;
    }
}
