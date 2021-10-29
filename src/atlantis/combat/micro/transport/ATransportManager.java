package atlantis.combat.micro.transport;

import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

import java.util.HashMap;
import java.util.Map;

public class ATransportManager {

    private static Map<AUnit, AUnit> passengersToTransports = new HashMap<>();
    private static Map<AUnit, AUnit> transportsToPassengers = new HashMap<>();

    public static boolean handle(AUnit transport) {
        AUnit baby = babyToCarry(transport);
        if (baby != null) {
            return TransportUnits.handleTransporting(transport, baby);
        }

        if (shouldAvoidEnemy(transport)) {
            return true;
        }

        transport.setTooltip("Chill");
        return true;
    }

    // =========================================================

    private static boolean shouldAvoidEnemy(AUnit transport) {
        if ((!transport.hasCargo() && hasTransportUnitAnyAssignment(transport)) && transport.woundPercent() > 70) {
            return false;
        }

        return AAvoidUnits.avoidEnemiesIfNeeded(transport);
    }

    private static AUnit babyToCarry(AUnit transport) {
        if (hasTransportUnitAnyAssignment(transport)) {
            return getUnitAssignedToTransport(transport);
        }
//        if (transport.hasCargo()) {
//            return transport.loadedUnits().get(0);
//        }

        AUnit crucialBaby = Select.ourOfType(
                AUnitType.Protoss_Reaver,
                AUnitType.Terran_Siege_Tank_Tank_Mode,
                AUnitType.Terran_Siege_Tank_Siege_Mode
        ).unloaded().inRadius(35, transport).randomWithSeed(transport.getID());

        if (crucialBaby != null && !hasTransportAssigned(crucialBaby)) {
            makeAssignment(transport, crucialBaby);
            return crucialBaby;
        }

        crucialBaby = Select.ourOfType(
                AUnitType.Protoss_Reaver,
                AUnitType.Terran_Siege_Tank_Tank_Mode,
                AUnitType.Terran_Siege_Tank_Siege_Mode
        ).unloaded().nearestTo(transport);

        if (crucialBaby != null && !hasTransportAssigned(crucialBaby)) {
            makeAssignment(transport, crucialBaby);
            return crucialBaby;
        }

        return null;
    }

    private static void makeAssignment(AUnit transport, AUnit passenger) {
        passengersToTransports.put(passenger, transport);
        transportsToPassengers.put(transport, passenger);
    }

    public static boolean hasNearbyTransportAssigned(AUnit passenger) {
        if (hasTransportAssigned(passenger)) {
            return passenger.distToLessThan(getTransportAssignedToUnit(passenger), 3);
        }

        return false;
    }

    public static AUnit getTransportAssignedToUnit(AUnit passenger) {
        return passengersToTransports.get(passenger);
    }

    private static boolean hasTransportAssigned(AUnit passenger) {
//        System.out.println("A: " + passengersToTransports.containsKey(passenger));
        if (passengersToTransports.containsKey(passenger)) {
//            System.out.println("B: " + passengersToTransports.get(passenger));
//            System.out.println("C: " + passengersToTransports.get(passenger).isAlive());
        }
        return passengersToTransports.containsKey(passenger) && passengersToTransports.get(passenger).isAlive();
    }

    private static AUnit getUnitAssignedToTransport(AUnit transport) {
        return transportsToPassengers.get(transport);
    }

    private static boolean hasTransportUnitAnyAssignment(AUnit transport) {
        return transportsToPassengers.containsKey(transport) && transportsToPassengers.get(transport).isAlive();
    }

}
