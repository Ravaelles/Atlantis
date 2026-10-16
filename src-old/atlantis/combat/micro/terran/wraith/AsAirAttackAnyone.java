package atlantis.combat.micro.terran.wraith;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.AMap;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class AsAirAttackAnyone extends Manager {
    public AsAirAttackAnyone(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isAir()
            && unit.hasAnyWeapon()
            && unit.cooldown() <= 8
            && unit.looksIdle()
            && !otherAirUnitsNear();
    }

    private boolean otherAirUnitsNear() {
        return unit.enemiesNear()
            .air()
            .havingAntiAirWeapon()
            .canAttack(unit, 3.5)
            .notEmpty();
    }

    @Override
    public Manager handle() {
        if (unit.isAttacking()) {
            if (!A.everyNthGameFrame(51)) return usedManager(this);
        }

        AUnit enemy = getEnemy();
        if (enemy == null) {
            if (!unit.isMoving()) return whenNoEnemyKnown();

            return null;
        }

        if (enemy.isVisibleUnitOnMap()) {
            if (unit.attackUnit(enemy)) return usedManager(this);
        }

        if (enemy.hasPosition()) {
            if (unit.move(enemy, Actions.MOVE_ENGAGE)) return usedManager(this);
        }

        return whenNoEnemyKnown();
    }

    private AUnit getEnemy() {
        Selection enemies = EnemyUnits.discovered();

        if (unit.isCorsair()) enemies = enemies.air();

        return enemies.canBeAttackedBy(unit, 500).nearestTo(unit);
    }

    private Manager whenNoEnemyKnown() {
        APosition goTo = AMap.randomUnexploredPosition(unit);
        if (goTo != null && unit.move(goTo, Actions.MOVE_SCOUT, "AttackAnyoneExploreA")) {
            return usedManager(this, "ExploreA");
        }

        goTo = AMap.randomInvisiblePosition(unit);
        if (goTo != null && unit.move(goTo, Actions.MOVE_SCOUT, "AttackAnyoneExploreB")) {
            return usedManager(this, "ExploreB");
        }

        return null;
    }
}
