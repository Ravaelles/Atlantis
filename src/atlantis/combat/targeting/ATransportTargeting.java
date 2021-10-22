package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.Select;

public class ATransportTargeting {

    public static AUnit target(AUnit unit) {
//        if (unit.isMelee()) {
        AUnit nearTransport = Select.enemy().transports(true).inRadius(13, unit).nearestTo(unit);
        if (nearTransport != null) {
            unit.setTooltip("Invasion!");
            return unit;
        }
//        }

        return null;
    }

}
