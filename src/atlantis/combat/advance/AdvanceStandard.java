package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.tank.TerranTank;
import atlantis.combat.missions.MissionManager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;

public class AdvanceStandard extends MissionManager {
    public AdvanceStandard(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isCombatUnit() && !squad.isLeader(unit) && unit.combatEvalRelative() > 1.5;
    }

    protected Manager handle() {
        if (unit.isGroundUnit() && !unit.isTank() && Count.tanks() >= 2) {
            if (advanceAsNonTank()) return usedManager(this);
        }

        if (unit.isTankSieged()) {
            if (TerranTank.wantsToUnsiege(unit)) return null;
        }

        if (A.seconds() % 6 <= 3 && (!unit.isMoving() && !unit.isAttacking())) {
            return usedManager(this, "AdvanceContinue");
        }

        if (focusPoint != null) {
            unit.move(focusPoint, Actions.MOVE_FOCUS, "Advance");
            return usedManager(this);
        }

        return null;
    }

    private boolean advanceAsNonTank() {
        AUnit target = unit.nearestOurTank();
        if (target != null && unit.distTo(target) >= 5) {
            unit.move(target, Actions.MOVE_FORMATION, "RollWithTank");
            return true;
        }

        return false;
    }
}
