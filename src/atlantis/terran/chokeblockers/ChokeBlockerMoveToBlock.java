package atlantis.terran.chokeblockers;

import atlantis.architecture.Manager;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import bwapi.Color;

public class ChokeBlockerMoveToBlock extends Manager {
    private final APosition blockChokePosition;

    public ChokeBlockerMoveToBlock(AUnit unit) {
        super(unit);
        this.blockChokePosition = unit.specialPosition();
    }

    @Override
    public boolean applies() {
        if (blockChokePosition == null) return false;
        if (unit.isZealot() && unit.enemiesNearInRadius(1.15) > 0) return false;

        return unit.hp() >= 25
            || unit.enemiesNear().inRadius(7, unit).groundUnits().havingAntiGroundWeapon().empty();
    }

    public Manager handle() {
        double dist = unit.distTo(blockChokePosition);
        unit.paintLine(blockChokePosition, Color.White);
        unit.paintLine(blockChokePosition.translateByPixels(1, 1), Color.White);

        if (dist > 0.12 || (dist > 0.02 && !unit.isHoldingPosition())) {
            unit.move(blockChokePosition, Actions.SPECIAL, "ChokeBlocker");
        }
        else {
            unit.holdPosition("ChokeBlocker");
//            unit.repair(ChokeBlockersAssignments.get().otherBlocker(unit), "Hold!");
        }
        unit.setAction(Actions.SPECIAL);

        return usedManager(this);
    }
}
