package atlantis.combat.micro.protoss;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.managers.Manager;
import atlantis.units.select.Select;

public class ProtossShieldBattery extends Manager {

    public ProtossShieldBattery(AUnit unit) {
        super(unit);
    }

    public Manager handle() {
        AUnit battery = Select.ourWithUnfinished(AUnitType.Protoss_Shield_Battery)
            .havingEnergy(40)
            .nearestTo(unit);
        if (
            battery != null && battery.distToMoreThan(unit, 6)
                && unit.move(battery, Actions.MOVE_SPECIAL, "ToBattery", false)
        ) {
            return usingManager(this);
        }

        return null;
    }
}
