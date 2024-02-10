package atlantis.production.orders.production.queue.order;

import atlantis.production.orders.production.queue.ReservedResources;

public class OrderReservations {
    private final ProductionOrder order;
    private boolean alreadyReserved = false;
    private int reservedMinerals = 0;
    private int reservedGas = 0;

    public OrderReservations(ProductionOrder order) {
        this.order = order;
    }

    public void reserveResources() {
//        if (alreadyReserved) ErrorLog.printMaxOncePerMinute("Trying to reserve reserved resources: " + order);
        if (alreadyReserved) return;

        reservedMinerals = order.mineralPrice();
        reservedGas = order.gasPrice();

        ReservedResources.reserveMinerals(reservedMinerals, order.toString());
        ReservedResources.reserveGas(reservedGas, order.toString());
        alreadyReserved = true;

        System.out.println("        reserved=" + reservedMinerals + " for " + order + " / " + ReservedResources.minerals());
    }

    public void clearResourcesReserved() {
//        if (!alreadyReserved) ErrorLog.printMaxOncePerMinute("Trying to clear resources not reserved: " + order);
        if (!alreadyReserved) return;

        ReservedResources.reserveMinerals(-reservedMinerals, order.toString());
        ReservedResources.reserveGas(-reservedGas, order.toString());

        reservedMinerals = 0;
        reservedGas = 0;
        alreadyReserved = false;

//        System.err.println("CLEARED reservedMinerals = " + reservedMinerals + " for " + order + " / " + ReservedResources.minerals());
    }

    public int minerals() {
        return reservedMinerals;
    }

    public int gas() {
        return reservedGas;
    }
}
