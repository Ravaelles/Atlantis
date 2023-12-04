package atlantis.combat.advance.tank;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.tank.TerranTank;
import atlantis.combat.missions.MissionManager;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class AdvanceAsTankWounded extends MissionManager {
    public AdvanceAsTankWounded(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isWounded();
    }

    @Override
    protected Manager handle() {
        if (unit.isBeingRepaired() || unit.hasCloseRepairer()) return usedManager(this);

        else {
            AUnit repairer = unit.repairer();
            if (repairer != null && repairer.distTo(unit) > 1.5) {
                repairer.move(unit, Actions.MOVE_REPAIR, "GetRepeyr");
                return usedManager(this);
            }
        }

        return null;
    }
}
