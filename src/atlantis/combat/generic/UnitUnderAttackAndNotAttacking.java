package atlantis.combat.generic;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.game.A;
import atlantis.production.dynamic.protoss.units.ProduceDragoon;
import atlantis.protoss.ProtossFlags;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class UnitUnderAttackAndNotAttacking extends Manager {
    public UnitUnderAttackAndNotAttacking(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false; // @Check

        if (!unit.lastUnderAttackLessThanAgo(70)) return false;
        if (!unit.lastAttackFrameMoreThanAgo(30 * 6)) return false;

        if (preventForTerran()) return false;
        if (preventForDragoon()) return false;

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
        if (!We.terran()) return false;

        if (A.seconds() <= 350) return true;
        if (unit.hp() <= 29) return false;

        return false;
    }

    private boolean preventForDragoon() {
        if (!unit.isDragoon()) return false;

        return unit.hp() <= 40
//            && unit.lastStartedAttackLessThanAgo(30 * 5)
            && !ProtossFlags.dragoonBeBrave()
            && (
            unit.lastUnderAttackLessThanAgo(30 * 4)
                || unit.enemiesNear(4.5).ranged().notEmpty()
        );
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
