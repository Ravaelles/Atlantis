package atlantis.units.special.ums;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.squad.positioning.Cohesion;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class FixInvalidTargets extends Manager {
    public FixInvalidTargets(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isCombatUnit()) return false;

        if (unit.action().isAttacking() && unit.target() == null) {
//            A.errPrintln("FixStoppedUnits: " + unit + " is attacking but target is null");
            return true;
        }

        return false;
    }

    @Override
    public Manager handle() {
//        unit.paintCircleFilled(16, Color.Teal);

        if ((new AttackNearbyEnemies(unit)).invokeFrom(this) != null) return usedManager(this);

//        if ((new Cohesion(unit)).invokeFrom(this) != null) return usedManager(this);
//
//        if (unit.lastActionMoreThanAgo(11, Actions.HOLD_POSITION)) {
//            unit.holdPosition("FixStopped&Hold");
//            return usedManager(this);
//        }

        return null;
    }
}
