package atlantis.terran.chokeblockers;

import atlantis.architecture.Manager;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import bwapi.Color;

public class ChokeBlockerMoveToBlock extends Manager {
    private final APosition blockChokePoint;

    public ChokeBlockerMoveToBlock(AUnit unit) {
        super(unit);
        this.blockChokePoint = unit.specialPosition();
    }

    @Override
    public boolean applies() {
        return unit.hp() >= 25
            || unit.enemiesNear().inRadius(7, unit).groundUnits().havingAntiGroundWeapon().empty();
    }

    public Manager handle() {
        double dist = unit.distTo(blockChokePoint);
        unit.paintLine(blockChokePoint, Color.White);

        if (dist > 1 || (dist > 0.05 && !unit.isHoldingPosition())) {
            unit.move(blockChokePoint, Actions.SPECIAL, "ChokeBlocker");
        }
        else {
            unit.holdPosition("ChokeBlocker");
//            unit.repair(ChokeBlockersAssignments.get().otherBlocker(unit), "Hold!");
        }
        unit.setAction(Actions.SPECIAL);

        return usedManager(this);
    }
}
