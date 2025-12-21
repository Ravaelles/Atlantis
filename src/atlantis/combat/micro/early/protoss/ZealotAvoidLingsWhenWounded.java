package atlantis.combat.micro.early.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;
import atlantis.util.We;

public class ZealotAvoidLingsWhenWounded extends Manager {
    private Selection enemies;

    public ZealotAvoidLingsWhenWounded(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss()
            && Enemy.zerg()
//            && Count.ourCombatUnits() <= 10
            && unit.isMelee()
//            && unit.cooldown() >= 4
            && (Enemy.zerg() || Enemy.protoss())
            && unit.shieldWound() >= 14
            && unit.cooldown() >= 4
            && (enemies = unit.enemiesNear().melee().inRadius(2.5, unit)).notEmpty()
            && unit.eval() <= 2
            && (unit.cooldown() >= 5 || unit.hp() <= 18)
            && (unit.shotSecondsAgo(1.5) || unit.hp() <= 18)
            && (unit.cooldown() >= 5 || unit.eval() <= 0.8 || unit.hp() <= 40)
//            && (unit.hp() <= 18 || unit.shotSecondsAgo(2))
//            && (unit.hp() <= 60 || unit.lastAttackFrameLessThanAgo(40))
//            && enemies.inRadius(4, unit).notEmpty()
            && unit.friendsNear().workers().inRadius(2, unit).empty()
            && unit.friendsNear().cannons().inRadius(2, unit).empty();
    }

    @Override
    public Manager handle() {
//        if (unit.hp() >= 20 && unit.cooldown() <= 3 && unit.eval() >= 1.1) return null;

        AUnit enemy = enemies.havingAtLeastHp(1).first();
        if (enemy == null) return null;

//        if (enemy.enemiesNear().workers().inRadius(2, unit).notEmpty()) return null;

        if (unit.distToMain() >= 5) {
            if (unit.moveToSafety(Actions.MOVE_AVOID)) {
                return usedManager(this);
            }

            if (unit.moveToMain(Actions.MOVE_AVOID)) {
                return usedManager(this);
            }
        }

        if (unit.runOrMoveAway(enemy, 2)) {
            return usedManager(this);
        }

        return null;
    }
}

