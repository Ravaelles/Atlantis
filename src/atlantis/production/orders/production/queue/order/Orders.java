package atlantis.production.orders.production.queue.order;

import atlantis.game.A;
import atlantis.production.orders.production.queue.CountInQueue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

public class Orders implements OrdersFilters, Iterable<ProductionOrder> {
    private final ArrayList<ProductionOrder> orders = new ArrayList<>();

    // =========================================================

    public Orders() {
    }

    public Orders(Collection<ProductionOrder> orders) {
        addAll(orders);
    }

    // =========================================================

    @Override
    public Iterator<ProductionOrder> iterator() {
        return orders.iterator();
    }

    // === Add =================================================

    public boolean add(int index, ProductionOrder item) {
//        if (!orders.contains(item)) {
//            System.err.println(
//                "@ " + A.now() + " added " + item + " / " +
//                    (item.unitType() == null ? "" : CountInQueue.count(item.unitType(), 30))
//            );
//        }

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
        A.println("@" + A.now() + " " + message + " (" + orders.size() + "):");
        for (ProductionOrder order : orders) {
            A.println("    " + order);
        }
        A.println("");
        return this;
    }

    public List<ProductionOrder> list() {
        return orders;
    }

    public ProductionOrder first() {
        if (orders.isEmpty()) return null;

        return orders.get(0);
    }

    public ProductionOrder last() {
        if (orders.isEmpty()) return null;

        return orders.get(orders.size() - 1);
    }

    public ProductionOrder get(int index) {
        return orders.get(index);
    }

    public void cancelAll() {
        for (ProductionOrder order : orders) {
            order.cancel();
        }
    }
}
