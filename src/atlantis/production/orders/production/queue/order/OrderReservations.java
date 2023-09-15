package atlantis.production.orders.production.queue.order;

import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.util.log.ErrorLog;

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
        reservedGas = order.mineralPrice();

        ReservedResources.reserveMinerals(reservedMinerals);
        ReservedResources.reserveGas(reservedGas);
        alreadyReserved = true;
    }

    public void clearResourcesReserved() {
//        if (!alreadyReserved) ErrorLog.printMaxOncePerMinute("Trying to clear resources not reserved: " + order);
        if (!alreadyReserved) return;

        ReservedResources.reserveMinerals(-reservedMinerals);
        ReservedResources.reserveGas(-reservedGas);

        reservedMinerals = 0;
        reservedGas = 0;
        alreadyReserved = false;
    }
}
