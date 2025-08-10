package atlantis.combat.micro.early.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;
import atlantis.util.We;

public class ZealotAvoidEarlyGameLings extends Manager {
    private Selection enemies;

    public ZealotAvoidEarlyGameLings(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss()
            && Enemy.zerg()
            && Count.ourCombatUnits() <= 3
            && unit.isMelee()
//            && unit.cooldown() >= 4
            && (Enemy.zerg() || Enemy.protoss())
            && unit.shieldWound() >= 4
            && unit.eval() <= 1.1
            && (unit.hp() <= 60 || unit.lastAttackFrameLessThanAgo(40))
            && (enemies = unit.enemiesNear().groundUnits().melee()).notEmpty()
            && enemies.inRadius(4, unit).notEmpty()
            && unit.friendsNear().workers().inRadius(2, unit).empty()
            && unit.friendsNear().buildings().inRadius(2, unit).empty();
    }

    @Override
    public Manager handle() {
        AUnit enemy = enemies.havingAtLeastHp(1).first();
        if (enemy == null) return null;

//        if (enemy.enemiesNear().workers().inRadius(2, unit).notEmpty()) return null;

        if (unit.distToMain() >= 15 && unit.moveToSafety(Actions.MOVE_AVOID)) {
            return usedManager(this);
        }

        if (unit.runOrMoveAway(enemy, 3)) {
            return usedManager(this);
        }

//        if ((new AttackNearbyEnemies(unit)).forceHandle() != null) {
//            return usedManager(this, "MoRtAlKoMbAt");
//        }

        return null;
    }
}

