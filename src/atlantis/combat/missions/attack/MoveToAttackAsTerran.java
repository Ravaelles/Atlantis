package atlantis.combat.missions.attack;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.architecture.Manager;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class MoveToAttackAsTerran extends Manager {

    public MoveToAttackAsTerran(AUnit unit) {
        super(unit);
    }

    public boolean handledTerranAdvance() {
        if (unit.isTerranInfantry() && unit.isWounded() && !unit.isMedic() && Count.medics() >= 1) {
            AUnit medic = Select.ourOfType(AUnitType.Terran_Medic)
                .havingEnergy(20)
                .inRadius(15, unit)
                .nearestTo(unit);

            if (medic != null && (!medic.hasTarget() || medic.target().equals())) {
                return unit.move(medic, Actions.MOVE_FOCUS, "Regenerate", false);
            }
        }

        return false;
    }
}
