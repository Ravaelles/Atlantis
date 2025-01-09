package atlantis.combat.squad.positioning.protoss.formations.crescent;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ProtossCrescentTooBehind extends Manager {
    private double deltaDist;

    public ProtossCrescentTooBehind(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return ProtossCrescent.deltaDist > 0.05;
    }

    @Override
    protected Manager handle() {
        if (ProtossCrescent.conventionalPoint.isWalkable()) {
            if (unit.move(ProtossCrescent.conventionalPoint, Actions.MOVE_FORMATION, "CrescentTooBehind")) {
                return usedManager(this);
            }
        }

        return null;
    }
}
