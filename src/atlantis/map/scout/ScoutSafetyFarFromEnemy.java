package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemiesIfNeeded;
import atlantis.combat.micro.avoid.AvoidSingleEnemy;
import atlantis.combat.micro.avoid.DoAvoidEnemies;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

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
        AUnit enemy = (new AvoidEnemiesIfNeeded(unit)).enemiesDangerouslyClose().first();
        if (enemy != null) return enemy;

        return unit.enemiesNear().combatUnits().inRadius(7, unit).nearestTo(unit);
    }

    private double safetyMargin() {
        return 3.1 + unit.woundPercent() / 30.0;
    }

    @Override
    public Manager handle() {
        if (enemy != null) {
            Manager manager = (new AvoidSingleEnemy(unit, enemy)).forceHandle();
            if (manager != null) return usedManager(manager);
        }

        if (
            (unit.isHealthy() || unit.enemiesNearInRadius(2.6) == 0)
                && unit.distToBase() >= 40
                && unit.moveToMain(Actions.MOVE_AVOID)
        ) return usedManager(this);

        return null;
    }
}
