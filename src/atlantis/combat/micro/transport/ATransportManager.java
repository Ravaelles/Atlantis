package atlantis.combat.micro.transport;

import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

public class ATransportManager {

    public static boolean handle(AUnit transport) {
        AUnit baby = babyToCarry(transport);
        if (baby != null) {
            carryBabyIfNeeded(transport, baby);
            return true;
        }

        if (AAvoidUnits.avoid(transport)) {
            return true;
        }

        return true;
    }

    private static boolean carryBabyIfNeeded(AUnit transport, AUnit baby) {
        return TransportUnits.transport(transport, baby);
    }

    // =========================================================

    private static AUnit babyToCarry(AUnit transport) {
        AUnit crucialBaby = Select.ourOfType(
                AUnitType.Protoss_Reaver,
                AUnitType.Terran_Siege_Tank_Tank_Mode,
                AUnitType.Terran_Siege_Tank_Siege_Mode
        ).unloaded().inRadius(15, transport).randomWithSeed(transport.getID());

        if (crucialBaby != null) {
            return crucialBaby;
        }

        crucialBaby = Select.ourOfType(
                AUnitType.Protoss_Reaver,
                AUnitType.Terran_Siege_Tank_Tank_Mode,
                AUnitType.Terran_Siege_Tank_Siege_Mode
        ).unloaded().nearestTo(transport);

        if (crucialBaby != null) {
            return crucialBaby;
        }

        return null;
    }

}
