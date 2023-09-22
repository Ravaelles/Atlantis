package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.tank.TerranTank;
import atlantis.combat.missions.MissionManager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class AdvanceStandard extends MissionManager {
    public AdvanceStandard(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isCombatUnit() && !squad.isLeader(unit) && unit.combatEvalRelative() > 1.5;
    }

    protected Manager handle() {
        if (unit.isTankSieged()) {
            TerranTank.wantsToUnsiege(unit);
        }

        if (A.now() % 10 <= 8 && (!unit.isMoving() && !unit.isAttacking())) {
            return usedManager(this, "AdvanceContinue");
        }

        if (focusPoint != null) {
            unit.move(focusPoint, Actions.MOVE_FOCUS, "Advance");
            return usedManager(this);
        }

        return null;
    }
}
