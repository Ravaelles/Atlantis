package atlantis.units.special.idle.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.interrupt.protoss.ProtossContinueAttack;

public class FixIdleUnitsPostAttack extends Manager {
    public FixIdleUnitsPostAttack(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.lastPositionChangedAgo() <= 20) return false;
        if (unit.hasCooldown()) return false;
        if (!unit.isAction(Actions.ATTACK_UNIT) && !unit.isActiveManager(ProtossContinueAttack.class)) return false;
        if (unit.lastCommandIssuedAgo() <= 2) return false;
        if (unit.isMelee() && unit.enemiesNear().inRadius(3, unit).notEmpty()) return false;
        if (unit.lastActionLessThanAgo(20, Actions.ATTACK_UNIT)) return false;

        if (unit.isStopped()) return true;
        if (!unit.hasValidTarget()) return true;
        if (unit.lastCommandIssuedAgo() >= 40) return true;

        return false;
    }

    @Override
    public Manager handle() {
//        unit.commandHistory().print("Command history PostAttack");

        if (FixActions.attackEnemies(unit, this, 0.5)) return yesUsedManager("IdleAttack-Fire");
//        if (FixActions.moveToLeader(unit)) return yesUsedManager("IdleAttack-2Leader");
//        if (FixActions.movedSlightlyOrToFocusPoint(unit)) return yesUsedManager("IdleAttack-2Leader");

        return null;
    }

    private Manager yesUsedManager(String message) {
//        System.err.println(
//            "Tlt:" + unit.tooltip() + " / "
//                + "Man:" + unit.manager() + " / "
//                + "Act:" + unit.action() + " / "
//                + "Comm:" + unit.lastCommandName() + " / "
//                + "PosChng:" + unit.lastPositionChangedAgo() + " / "
//        );

        return usedManager(this, message);
    }
}
