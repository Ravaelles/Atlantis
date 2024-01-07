package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.game.A;
import atlantis.units.AUnit;

public class UnitUnderAttackAndNotAttacking extends Manager {
    public UnitUnderAttackAndNotAttacking(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false; // @Check

        return !unit.isAttacking()
            && unit.isGroundUnit()
            && unit.noCooldown()
            && (unit.hp() >= 61 || !unit.isTank())
            && unit.lastUnderAttackLessThanAgo(70)
            && unit.hasAnyWeapon()
            && (
            unit.hp() >= 18
                || !unit.recentlyMoved(30)
                || unit.lastAttackFrameMoreThanAgo(30 * 5)
                || shouldAttackBackBecauseOverstackedAndCantRun()
        )
            && unit.enemiesNear().canBeAttackedBy(unit, 1).notEmpty();
    }

    private boolean shouldAttackBackBecauseOverstackedAndCantRun() {
//        if (!unit.isRunning()) return false;
        if (unit.allUnitsNear().groundUnits().inRadius(0.7, unit).atMost(2)) return false;
        if (unit.friendsNear().groundUnits().combatUnits().inRadius(3, unit).atMost(4)) return false;

        return true;
    }

    @Override
    public Manager handle() {
        (new AttackNearbyEnemies(unit)).forceHandle();
        return usedManager(this, "FightBack");
    }
}

