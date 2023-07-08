package atlantis.combat.targeting;

import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class ATransportTargeting {

    public static AUnit target(AUnit unit) {
        AUnit nearTransport = Select.enemy()
                .transports(true)
                .canBeAttackedBy(unit, 1)
                .inRadius(13, unit)
                .nearestTo();

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
//                .nearestTo());
        if (nearTransport != null) {
            if (nearTransport.enemiesNear().inRadius(5, nearTransport).atMost(3)) {
                unit.setTooltipTactical("Invasion!");
                if (ATargeting.DEBUG) System.out.println("TransportTarget = " + nearTransport);
                return nearTransport;
            }
        }

        return null;
    }

}
