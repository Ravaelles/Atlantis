package atlantis.combat.micro.terran.infantry.medic;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class MedicAvoidWhenAttacked extends Manager {
    private AUnit enemy;

    public MedicAvoidWhenAttacked(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.lastUnderAttackLessThanAgo(25)
            && unit.woundHp() >= 5
            && (enemy = unit.enemiesNear().havingAntiGroundWeapon().nearestTo(unit)) != null;
    }

    @Override
    public Manager handle() {
        if (unit.runningManager().runFrom(enemy, 1.5, Actions.MOVE_AVOID, true)) {
            return usedManager(this);
        }

        return null;
    }
}
