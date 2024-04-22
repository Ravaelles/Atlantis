package atlantis.combat.generic.under_attack;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.game.A;
import atlantis.protoss.ProtossFlags;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class TerranUnitUnderAttack extends Manager {
    public TerranUnitUnderAttack(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false; // @Check

        if (!unit.lastUnderAttackLessThanAgo(70)) return false;
        if (!unit.lastAttackFrameMoreThanAgo(30 * 6)) return false;

        if (preventForTerran()) return false;

        return !unit.isAttacking()
            && unit.isGroundUnit()
            && unit.noCooldown()
            && unit.lastActionMoreThanAgo(30, Actions.MOVE_DANCE_AWAY)
            && (unit.hp() >= 61 || !unit.isTank())
            && unit.hasAnyWeapon()
            && (
            unit.hp() >= 18
                || !unit.recentlyMoved(30)
                || shouldAttackBackBecauseOverstackedAndCantRun()
        )
            && unit.enemiesNear().canBeAttackedBy(unit, 1).notEmpty();
    }

    private boolean preventForTerran() {
        if (A.seconds() <= 350) return true;
        if (unit.hp() <= 29) return false;

        return false;
    }

    private boolean shouldAttackBackBecauseOverstackedAndCantRun() {
//        if (!unit.isRunning()) return false;
        if (unit.allUnitsNear().groundUnits().inRadius(0.7, unit).atMost(2)) return false;
        if (unit.friendsNear().groundUnits().combatUnits().inRadius(3, unit).atMost(4)) return false;

        return true;
    }

    @Override
    public Manager handle() {
        if ((new AttackNearbyEnemies(unit)).invoked(this)) {
            return usedManager(this, "FightBack");
        }

        return null;
    }
}
