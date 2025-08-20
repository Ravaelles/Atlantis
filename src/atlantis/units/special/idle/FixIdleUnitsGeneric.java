package atlantis.units.special.idle;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class FixIdleUnitsGeneric extends Manager {
    public FixIdleUnitsGeneric(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.lastPositionChangedAgo() <= 2) return false;

        if (
            unit.isStopped()
                && unit.noCooldown()
        ) return true;

        return false;
//        return unit.lastActionMoreThanAgo(60);
    }

    @Override
    public Manager handle() {
//        unit.commandHistory().print("Command history GENERIC");

//        unit.paintCircleFilled(15, Color.White);

//        if (!unit.isAttacking() && unit.woundPercent() <= 70 && unit.combatEvalRelative() >= 1.3) {
//            if (attackEnemies()) return usedManager(this, "FixIdleByAttack");
//        }

//        if ((new HandleUnitPositioningOnMap(unit)).invokeFrom(this) != null) return usedManager(this);

//        moveToLeader(); // Move, but don't return that we used this manager.

        if (unit.isStopped() && FixActions.attackEnemies(unit, this, 0.7)) {
            return usedManager(this, "FixIdleByAttack");
        }
        if (FixActions.moveToLeader(unit)) return usedManager(this, "FixIdleGoToLeader");
        if (FixActions.movedSlightly(unit)) return usedManager(this, "FixIdle2FP");
//        if ((new HandleUnitPositioningOnMap(unit)).invokeFrom(this) != null) return usedManager(this);
//        if (movedToFocusPoint()) return usedManager(this);

        return null;
    }
}
