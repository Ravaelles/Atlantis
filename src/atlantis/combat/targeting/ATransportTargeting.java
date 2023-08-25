package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.units.select.Select;

public class ATransportTargeting extends HasUnit {

    public ATransportTargeting(AUnit unit) {
        super(unit);
    }

    public AUnit target() {
        AUnit nearTransport = Select.enemy()
                .transports(true)
                .canBeAttackedBy(unit, 1)
                .inRadius(13, unit)
                .nearestTo(unit);

//        System.out.println("H");
//        System.out.println(Select.enemy()
//                .transports(true));
//        System.out.println("I");
//        System.out.println(Select.enemy()
//                .transports(true)
//                .canBeAttackedBy(unit, false, true));
//        System.out.println("J");
//        System.out.println(Select.enemy()
//                .transports(true)
//                .canBeAttackedBy(unit, false, true)
//                .inRadius(13, unit));
//        System.out.println("K");
//        System.out.println(Select.enemy()
//                .transports(true)
//                .canBeAttackedBy(unit, false, true)
//                .inRadius(13, unit)
//                .nearestTo(unit));
        if (nearTransport != null) {
            if (nearTransport.enemiesNear().inRadius(5, nearTransport).atMost(3)) {
                unit.setTooltipTactical("Invasion!");
                debug(("TransportTarget = " + nearTransport);
                return nearTransport;
            }
        }

        return null;
    }

}
