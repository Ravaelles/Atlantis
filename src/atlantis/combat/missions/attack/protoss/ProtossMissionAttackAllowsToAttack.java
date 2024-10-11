package atlantis.combat.missions.attack.protoss;

import atlantis.combat.missions.attack.MissionAttackAllowsToAttack;
import atlantis.units.AUnit;
import atlantis.util.Enemy;

public class ProtossMissionAttackAllowsToAttack extends MissionAttackAllowsToAttack {
    public ProtossMissionAttackAllowsToAttack(AUnit unit) {
        super(unit);
    }

    public boolean allowsToAttackEnemyUnit(AUnit enemy) {
        if (Enemy.zerg() || Enemy.terran()) {
//            boolean lowCooldown = unit.cooldown() <= 4;
            double distToEnemy = unit.distTo(enemy);

            if (Enemy.zerg()) {
                if (unit.isMelee() && distToEnemy <= 1.4) {
                    if (unit.shotSecondsAgo() >= 6 && distToEnemy <= 1) return true;
                }
            }
        }

        return super.allowsToAttackEnemyUnit(enemy);
    }
}
