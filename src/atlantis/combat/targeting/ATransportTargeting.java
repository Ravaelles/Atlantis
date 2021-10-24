package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.Select;

public class ATransportTargeting {

    public static AUnit target(AUnit unit) {
        AUnit nearTransport = Select.enemy()
                .transports(true)
                .canBeAttackedBy(unit, false, true)
                .inRadius(13, unit)
                .nearestTo(unit);
        if (nearTransport != null) {
            if (Select.enemyCombatUnits().inRadius(5, nearTransport).atMost(3)) {
                unit.setTooltip("Invasion!");
                return unit;
            }
        }

        return null;
    }

}
