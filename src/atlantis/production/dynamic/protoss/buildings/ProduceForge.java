package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.game.player.Enemy;
import atlantis.util.log.ErrorLog;

import static atlantis.units.AUnitType.Protoss_Forge;

public class ProduceForge {
    public static boolean produce() {
        if (Have.forge()) return false;

        if (predictEnemyDT()) return produce(40);

        if (A.supplyUsed() <= 60 && (Count.ourCombatUnits() <= 11 || Army.strengthWithoutCB() <= 180)) return false;

        return produce(buildAtSupply());
    }

    private static boolean predictEnemyDT() {
        if (EnemyInfo.hasHiddenUnits()) return yes("HasHiddenUnits");

        if (Count.ourCombatUnits() <= 9) return false;
        if (Army.strengthWithoutCB() <= 130 && !A.supplyUsed(60)) return false;
        if (Count.gatewaysWithUnfinished() <= 3) return false;

        if (
            (A.hasMinerals(250) || Army.strength() >= 150) && EnemyInfo.goesOrHasHiddenUnits()
        ) return yes("goesOrHasHiddenUnits");
//        if (Army.strength() >= 120 && EnemyUnits.zealots() >= 4 && EnemyUnits.dragoons() <= 2) return true;

        return false;
    }

    private static boolean yes(String reason) {
        ErrorLog.printErrorOnce("@@@@@@@@@@ Make FORGE: " + reason);
        return true;
    }

    private static boolean produce(int buildAtSupply) {
        if (!A.supplyUsed(buildAtSupply)) return false;

//        if (Have.notEvenPlanned(Protoss_Forge)) A.printStackTrace("Why Forge? @ " + A.minSec());

        return DynamicCommanderHelpers.buildToHaveOne(buildAtSupply, Protoss_Forge)
            && yes("buildAtSupply: " + buildAtSupply);
    }

    private static int buildAtSupply() {
        if (EnemyStrategy.get().isGoingHiddenUnits()) return 30;

        if (Enemy.terran() && !EnemyInfo.hasHiddenUnits()) return 93;
        if (Enemy.protoss() && !EnemyInfo.hasRanged()) return 73;
        if (Enemy.zerg()) return 46 + (Army.strength() <= 150 ? 10 : 0);

//        if (EnemyStrategy.get().isRushOrCheese()) return 10;

        return 62;
    }
}
