package atlantis.combat.micro.terran.infantry;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class GoTowardsMedic extends Manager {
    public GoTowardsMedic(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isTerranInfantryWithoutMedics()) return false;

        if (unit.isHealthy() && unit.distToLeader() <= 6) {
            return false;
        }

        if (unit.cooldownRemaining() <= 3 || unit.hp() >= 26) {
            return false;
        }

//        if (unit.enemiesNear().canAttack(unit, 7).isNotEmpty()) {
//            return false;
//        }

        if (unit.friendsInRadius(10).bunkers().notEmpty() && unit.enemiesNearInRadius(2.7) > 0) {
            return false;
        }

        return true;
    }

    @Override
    protected Manager handle() {
        if (check()) {
            return usedManager(this);
        }

        return null;
    }

    public boolean check() {
        AUnit medic = Select.ourOfType(AUnitType.Terran_Medic)
            .inRadius(8, unit)
            .havingEnergy(25)
            .nearestTo(unit);
        if (medic != null && medic.distToMoreThan(unit, 2)) {
            return unit.move(medic, Actions.MOVE_SPECIAL, "BeHealed", false);
        }

        return false;
    }
}
