package atlantis.production.orders.production.queue;

import atlantis.game.A;
import atlantis.production.orders.production.ProductionOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Orders {
    private final ArrayList<ProductionOrder> list = new ArrayList<>();

    // =========================================================

    public List<ProductionOrder> requirementsFulfilled() {
        // Filter completed orders using stream
//        ArrayList<ProductionOrder> completed = new ArrayList<>();
//        for (ProductionOrder order : list) {
//            if (order.isCompleted()) {
//                completed.add(order);
//            }
//        }
//        return completed;

        return list.stream()
            .filter(order -> !order.isCompleted())
            .collect(Collectors.toList());
    }

    // =========================================================

    public int size() {
        return list.size();
    }

    public void add(ProductionOrder item) {
        list.add(item);
    }

    public void addAll(List<ProductionOrder> items) {
        list.addAll(items);
    }

    public void prepend(ProductionOrder item) {
        list.add(0, item);
    }

    public void remove(ProductionOrder item) {
        list.remove(item);
    }

    public void print() {
        A.println("Queue (" + list.size() + "):");
        for (ProductionOrder order : list) {
            A.println("    " + order);
        }
    }

    public List<ProductionOrder> list() {
        return list;
    }
}
