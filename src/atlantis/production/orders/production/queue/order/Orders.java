package atlantis.production.orders.production.queue.order;

import atlantis.game.A;

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

    public void add(int index, ProductionOrder item) {
        if (!orders.contains(item)) orders.add(index, item);
    }

    public void add(ProductionOrder item) {
        if (!orders.contains(item)) orders.add(item);
    }

    public void addAll(Collection<ProductionOrder> items) {
        for (ProductionOrder item : items) {
            add(item);
        }
    }

    public void prepend(ProductionOrder item) {
        if (!orders.contains(item)) orders.add(0, item);
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

    public void print(String message) {
        A.println(message + " (" + orders.size() + "):");
        for (ProductionOrder order : orders) {
            A.println("    " + order);
        }
        A.println("");
    }

    public List<ProductionOrder> list() {
        return orders;
    }
}
