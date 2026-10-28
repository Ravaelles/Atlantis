package atlantis.production.dynamic.protoss.prioritize;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.generic.Army;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public class PrioritizeGatewaysVsProtoss {
    public static boolean shouldPrioritizeOverExpanding() {
        if (!Enemy.protoss()) return false;
        if (EnemyInfo.combatBuildingsAntiLand() > 0) return false;

        if (Count.withPlanned(AUnitType.Protoss_Gateway) <= 6) return true;

        return (!A.hasMinerals(460) || Army.strength() < 170);
    }
}
