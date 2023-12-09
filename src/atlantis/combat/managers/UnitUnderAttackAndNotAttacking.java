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
        return !unit.isAttacking()
            && unit.lastUnderAttackLessThanAgo(40)
            && unit.hasAnyWeapon()
            && shouldAttackBackBecauseOverstackedAndCantRun();
    }

    private boolean shouldAttackBackBecauseOverstackedAndCantRun() {
        if (!unit.isRunning()) return false;
        if (unit.hasCooldown()) return false;
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

