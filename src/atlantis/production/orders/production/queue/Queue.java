package atlantis.production.orders.production.queue;

import atlantis.production.orders.production.ProductionOrder;

import java.util.ArrayList;
import java.util.Collections;

public class Queue {
    private final ArrayList<ProductionOrder> productionOrders = new ArrayList<>();

    public Queue(ArrayList<ProductionOrder> productionOrders) {
        Collections.copy(this.productionOrders, productionOrders);
    }

    public ArrayList<ProductionOrder> productionOrders() {
        //noinspection unchecked
        return (ArrayList<ProductionOrder>) productionOrders.clone();
    }
}
