package atlantis.production.orders.production.queue.order;

import atlantis.game.A;
import atlantis.production.orders.production.queue.CountInQueue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Orders implements OrdersFilters {
    private final ArrayList<ProductionOrder> orders = new ArrayList<>();

    // =========================================================

    public Orders() {
    }

    public Orders(Collection<ProductionOrder> orders) {
        addAll(orders);
    }

    // === Add ==================================================

    public boolean add(int index, ProductionOrder item) {
        if (!orders.contains(item)) {
            System.err.println(
                "@ " + A.now() + " added " + item + " / " +
                    (item.unitType() == null ? "" : CountInQueue.count(item.unitType(), 30))
            );
        }
        if (!orders.contains(item)) {
            orders.add(index, item);
            return true;
        }

        return false;
    }

    public boolean add(ProductionOrder item) {
        if (!orders.contains(item)) {
            orders.add(item);
            return true;
        }

        return false;
    }

    public void addAll(Collection<ProductionOrder> items) {
        for (ProductionOrder item : items) {
            add(item);
        }
    }

    // === Remove ==============================================

    public void remove(ProductionOrder item) {
        orders.remove(item);
    }

    // =========================================================

    public int size() {
        return orders.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public void print() {
        print("Queue");
    }

    public Orders print(String message) {
        A.println(message + " (" + orders.size() + "):");
        for (ProductionOrder order : orders) {
            A.println("    " + order);
        }
        A.println("");
        return this;
    }

    public List<ProductionOrder> list() {
        return orders;
    }
}
