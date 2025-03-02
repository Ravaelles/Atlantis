package atlantis.combat.missions.attack.protoss;

import atlantis.combat.missions.attack.MissionAttackAllowsToAttack;
import atlantis.units.AUnit;
import atlantis.game.player.Enemy;

public class ProtossMissionAttackAllowsToAttack extends MissionAttackAllowsToAttack {
    public ProtossMissionAttackAllowsToAttack(AUnit unit) {
        super(unit);
    }

    public boolean allowsToAttackEnemyUnit(AUnit enemy) {
        if (unit.leaderIsRetreating()) return forbidden("LeaderRetreating");

        if (Enemy.zerg()) {
            if (preventProtossFromChasingScatteredLings(enemy)) return forbidden("ChasingLings");
            if (preventLonelyMelee(enemy)) return forbidden("LonelyMelee");
        }

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

    private boolean preventLonelyMelee(AUnit enemy) {
        if (!unit.isMelee()) return false;

        return unit.eval() <= 3 && unit.friendsInRadius(0.6).count() <= 0;
    }
}
