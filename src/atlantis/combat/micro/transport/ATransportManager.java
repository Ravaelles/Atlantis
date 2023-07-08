package atlantis.combat.micro.transport;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.managers.Manager;
import atlantis.units.select.Select;

import java.util.HashMap;
import java.util.Map;

public class ATransportManager extends Manager {

    private  Map<AUnit, AUnit> passengersToTransports = new HashMap<>();
    private  Map<AUnit, AUnit> transportsToPassengers = new HashMap<>();

    public ATransportManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.type().isTransportExcludeOverlords();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[] {
            TransportUnits.class,
        };
    }

    @Override
    public Manager handle() {
        AUnit baby = babyToCarry();
        if (baby != null) {
            TransportUnits transportUnits = new TransportUnits(unit);
            return transportUnits.handleTransporting(unit, baby) ? transportUnits : null;
        }

        unit.setTooltipTactical("Chill");
        return usedManager(this);
    }

    // =========================================================

//    private  boolean shouldAvoidEnemy() {
//        if ((!.hasCargo() && hasTransportUnitAnyAssignment()) && .woundPercent() > 70) {
//            return false;
//        }
//
//        return AvoidEnemies.avoidEnemiesIfNeeded();
//    }

    private  AUnit babyToCarry() {
        if (hasTransportUnitAnyAssignment()) {
            return getUnitAssignedToTransport();
        }
//        if (.hasCargo()) {
//            return .loadedUnits().get(0);
//        }

        AUnit crucialBaby = Select.ourOfType(
                AUnitType.Protoss_Reaver,
                AUnitType.Terran_Siege_Tank_Tank_Mode,
                AUnitType.Terran_Siege_Tank_Siege_Mode
        ).unloaded().inRadius(35, unit).randomWithSeed(unit.id());

        if (crucialBaby != null && !hasTransportAssigned(crucialBaby)) {
            makeAssignment(crucialBaby);
            return crucialBaby;
        }

        crucialBaby = Select.ourOfType(
                AUnitType.Protoss_Reaver,
                AUnitType.Terran_Siege_Tank_Tank_Mode,
                AUnitType.Terran_Siege_Tank_Siege_Mode
        ).unloaded().nearestTo(unit);

        if (crucialBaby != null && !hasTransportAssigned(crucialBaby)) {
            makeAssignment(crucialBaby);
            return crucialBaby;
        }

        return null;
    }

    private  void makeAssignment(AUnit passenger) {
        passengersToTransports.put(passenger, unit);
        transportsToPassengers.put(unit, passenger);
    }

    public  boolean hasNearTransportAssigned(AUnit passenger) {
        if (hasTransportAssigned(passenger)) {
            return passenger.distToLessThan(getTransportAssignedToUnit(passenger), 3);
        }

        return false;
    }

    public  AUnit getTransportAssignedToUnit(AUnit passenger) {
        return passengersToTransports.get(passenger);
    }

    private  boolean hasTransportAssigned(AUnit passenger) {
//        System.out.println("A: " + passengersToTransports.containsKey(passenger));
        if (passengersToTransports.containsKey(passenger)) {
//            System.out.println("B: " + passengersToTransports.get(passenger));
//            System.out.println("C: " + passengersToTransports.get(passenger).isAlive());
        }
        return passengersToTransports.containsKey(passenger) && passengersToTransports.get(passenger).isAlive();
    }

    private  AUnit getUnitAssignedToTransport() {
        return transportsToPassengers.get(unit);
    }

    private  boolean hasTransportUnitAnyAssignment() {
        return transportsToPassengers.containsKey(unit) && transportsToPassengers.get(unit).isAlive();
    }

}
