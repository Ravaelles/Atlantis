package atlantis.terran.chokeblockers;

import atlantis.architecture.Manager;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ChokeBlockerMoveToBlock extends Manager {
    private final APosition blockChokePoint;

    public ChokeBlockerMoveToBlock(AUnit unit) {
        super(unit);
        this.blockChokePoint = unit.specialPosition();
    }

    public Manager handle() {
        unit.move(blockChokePoint, Actions.MOVE_SPECIAL, "ChokeBlocker");
        return usedManager(this);
    }
}
