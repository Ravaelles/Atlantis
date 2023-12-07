package atlantis.combat.micro.terran.infantry.medic;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class MoveAwayMedicFromTanks extends Manager {
    public MoveAwayMedicFromTanks(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.nearestOurTankDist() < 1.5;
    }

    @Override
    public Manager handle() {
        unit.moveAwayFrom(unit.nearestOurTank(), 0.5, Actions.MOVE_SPACE, "Space4Tank");
        return usedManager(this);
    }
}
