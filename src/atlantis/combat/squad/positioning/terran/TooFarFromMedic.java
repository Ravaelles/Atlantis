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
            && unit.friendsNear().inRadius(1, unit).count() <= 2
            && unit.friendsNear().inRadius(2, unit).count() <= 4;
    }

    protected Manager handle() {
        AUnit medic = Select.ourOfType(AUnitType.Terran_Medic).nearestTo(unit);
        if (medic != null && medic.distToMoreThan(unit, maxDistToMedic())) {
            unit.move(medic, Actions.MOVE_FORMATION, "HugMedic");
            if (isMedicFree(medic)) {
                medic.move(unit, Actions.MOVE_FORMATION, "HugUnit");
            }
            return usedManager(this);
        }

        return null;
    }

    private static boolean isMedicFree(AUnit medic) {
        return !medic.isMoving() && medic.noCooldown() && medic.friendsNear().wounded().inRadius(3, medic).empty();
    }

    private double maxDistToMedic() {
        return unit.enemiesNear().ranged().isEmpty() ? 3.5 : 2.3;
    }
}
