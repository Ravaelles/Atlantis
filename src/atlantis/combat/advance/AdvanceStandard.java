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
        if (A.now() % 10 <= 8 && !unit.looksIdle()) {
            return usedManager(this, "AdvanceContinue");
        }

        if (focusPoint != null) {
            if (unit.isTankSieged()) {
                TerranTank.wantsToUnsiege(unit);
            }
            else {
                unit.move(focusPoint, Actions.MOVE_FOCUS, "Advance");
            }
            return usedManager(this);
        }

        return null;
    }
}
