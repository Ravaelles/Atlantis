package atlantis.combat.squad.positioning.terran;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class TooFarFromMedic extends Manager {
    public TooFarFromMedic(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return Count.medics() >= 2
            && unit.isTerranInfantryWithoutMedics()
            && unit.friendsNear().inRadius(1.5, unit).count() <= 3;
    }

    protected Manager handle() {
        AUnit medic = Select.ourOfType(AUnitType.Terran_Medic).nearestTo(unit);
        if (medic != null && medic.distToMoreThan(unit, maxDistToMedic())) {
            unit.move(medic, Actions.MOVE_FORMATION, "HugMedic");
            if (!medic.isMoving() || medic.hasCooldown()) {
                medic.move(unit, Actions.MOVE_FORMATION, "HugUnit");
            }
            return usedManager(this);
        }

        return null;
    }

    private double maxDistToMedic() {
        return unit.enemiesNear().ranged().isEmpty() ? 5 : 2.3;
    }
}
