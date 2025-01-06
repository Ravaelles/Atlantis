package atlantis.combat.micro.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class ProtossShieldBattery extends Manager {

    public ProtossShieldBattery(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.is(AUnitType.Protoss_Shield_Battery);
    }

    protected Manager handle() {
        if (moveHealableUnitsToBattery()) return usedManager(this);

        return null;
    }

    private boolean moveHealableUnitsToBattery() {
        AUnit friend = unit.friendsNear().ofType(AUnitType.Protoss_Shield_Battery)
            .havingEnergy(40)
            .havingSeriousShieldWound()
            .notAttacking()
            .notRunning()
            .nearestTo(unit);

        if (
            friend != null && friend.distToMoreThan(unit, 11)
                && unit.move(friend, Actions.SPECIAL, "ToBattery", false)
        ) {
            return true;
        }
        return false;
    }
}
