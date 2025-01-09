package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.avoid.WantsToAvoid;
import atlantis.units.AUnit;
import atlantis.game.player.Enemy;

public class ScoutSafetyAvoidTooCloseEnemies extends Manager {
    private AUnit enemy;

    public ScoutSafetyAvoidTooCloseEnemies(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        enemy = defineEnemyToRunFrom();
        if (enemy != null) return true;

        return unit.enemiesNear().buildings().inRadius(9, unit).empty()
            && unit.enemiesNear().combatUnits().canAttack(unit, safetyMargin()).notEmpty()
            && (!unit.isMoving() && unit.friendsNear().specialAction().inRadius(11, unit).empty());
    }

    private AUnit defineEnemyToRunFrom() {
        AUnit enemy = (new AvoidEnemies(unit)).enemiesDangerouslyClose().first();
        if (enemy != null) return enemy;

        return unit.enemiesNear().combatUnits().inRadius(9, unit).nearestTo(unit);
    }

    private double safetyMargin() {
        return 3.1
            + (Enemy.zerg() ? 2.3 : 0)
            + (unit.lastUnderAttackLessThanAgo(30 * 8) ? 1.5 : 0)
            + unit.woundPercent() / 22.0;
    }

    @Override
    public Manager handle() {
        Manager manager = (new WantsToAvoid(unit)).forceHandle();
        if (manager != null) return usedManager(this);

        return null;

//        if (enemy != null) {
//            Manager manager = (new AvoidSingleEnemy(unit, enemy)).forceHandle();
//            if (manager != null) return usedManager(manager);
//        }
//
//        if (
//            (unit.isHealthy() || unit.enemiesNearInRadius(2.6) == 0)
//                && unit.distToBase() >= 40
//                && unit.moveToMain(Actions.MOVE_AVOID)
//        ) return usedManager(this);
//
//        return null;
    }
}
