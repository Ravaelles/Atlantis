package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class SkipCombatManager extends Manager {

    public SkipCombatManager(AUnit unit) {
        super(unit);
    }

    public Manager handle() {
        if (preActions()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean preActions() {
        if (unit.lastActionLessThanAgo(15, Actions.RIGHT_CLICK)) {
            unit.setTooltip("Manual", true);
            return true;
        }

//        if (
//                A.seconds() >= 1
//                && GameSpeed.isDynamicSlowdownAllowed()
//                && !GameSpeed.isDynamicSlowdownActive()
//                && (unit.lastActionLessThanAgo(2, UnitActions.ATTACK_UNIT) || unit.isUnderAttack(3)))
//        {
//            GameSpeed.activateDynamicSlowdown();
//        }

        if (!unit.isRealUnit()) {
            System.err.println("Not real unit: " + unit.name());
            return true;
        }

        if (unit.isWorker() && unit.squad() == null) {
            System.err.println("Worker being ACUM");
            return true;
        }

        if (unit.lastActionLessThanAgo(120, Actions.PATROL) || unit.isPatrolling()) {
            if (A.now() > 90) {
                unit.setTooltipTactical("#Manual");
                return true;
            }
        }

        return false;
    }
}

