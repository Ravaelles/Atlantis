package atlantis.units.attacked_by;

import atlantis.map.bullets.ABullet;
import atlantis.units.AUnit;
import atlantis.util.log.ErrorLog;

import java.util.ArrayList;
import java.util.Collection;

public class PendingAttacksAgainstEnemyUnit {
    public static Collection<ABullet> against(AUnit enemy) {
        if (!enemy.isEnemy()) {
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace(
                "PendingAttacksAgainstEnemyUnit only works for enemies, passed: "
                    + enemy.type() + " (our:" + enemy.isOur() + ")"
            );
            return java.util.Collections.emptyList();
        }

        Collection<ABullet> pendingBullets = new ArrayList<>();
        for (AUnit attacker : enemy.enemiesNear().havingTargeted(enemy).attackStatePending().list()) {
            ABullet bullet = ABullet.fromPendingAttack(attacker, enemy);
            pendingBullets.add(bullet);
        }

        return pendingBullets;
    }
}
