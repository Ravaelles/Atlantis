package atlantis.combat.advance;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.architecture.Manager;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class AdvanceAsTerran extends Manager {

    public AdvanceAsTerran(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTerran();
    }

    @Override
    public Manager handle() {
        if (unit.isTerranInfantry() && unit.isWounded() && !unit.isMedic() && Count.medics() >= 2) {
            AUnit medic = Select.ourOfType(AUnitType.Terran_Medic)
                .havingEnergy(20)
                .inRadius(15, unit)
                .nearestTo(unit);

            if (medic != null && (!medic.hasTarget() || medic.target().equals(unit))) {
                unit.move(medic, Actions.MOVE_FOCUS, "Regenerate", false);
                return usedManager(this);
            }
        }

        return null;
    }
}
