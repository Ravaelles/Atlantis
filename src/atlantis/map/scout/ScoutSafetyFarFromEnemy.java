package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.micro.avoid.WantsToAvoid;
import atlantis.units.AUnit;

public class ScoutSafetyFarFromEnemy extends Manager {
    private AUnit enemy;

    public ScoutSafetyFarFromEnemy(AUnit unit) {
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

        return unit.enemiesNear().combatUnits().inRadius(7, unit).nearestTo(unit);
    }

    private double safetyMargin() {
        return 3.1 + unit.woundPercent() / 30.0;
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
