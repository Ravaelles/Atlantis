package atlantis.combat.micro.terran.infantry.special;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

public class SpreadWhenHighTemplarsNear extends Manager {
    private AUnit ht;

    public SpreadWhenHighTemplarsNear(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return Enemy.protoss()
            && (!unit.isAttacking() || unit.hasCooldown())
            && (ht = nearestHT()) != null
            && (ht.energy() == 0 || ht.energy(75))
            && unit.friendsInRadiusCount(2) >= 4;
    }

    private AUnit nearestHT() {
        return unit.enemiesNear().ofType(AUnitType.Protoss_High_Templar).inRadius(11, unit).nearestTo(unit);
    }

    public Manager handle() {
        if (unit.idIsOdd()) {
            unit.move(Select.mainOrAnyBuilding(), Actions.MOVE_AVOID, "SpreadFromHT");
        } else {
            unit.move(ht, Actions.MOVE_AVOID, "SpreadToHT");
        }

        return usedManager(this);
    }
}
