package atlantis.production.orders.production.queue.order;

import atlantis.units.AUnitType;
import bwapi.TechType;
import bwapi.UpgradeType;

import java.util.List;
import java.util.stream.Collectors;

public interface OrdersFilters {
    List<ProductionOrder> list();

    default Orders all() {
        return (Orders) this;
    }

    default Orders readyToProduce() {
        return new Orders(
            list().stream()
                .filter(ProductionOrder::isReadyToProduce)
                .collect(Collectors.toList())
        );
    }

    default Orders inProgress() {
        return new Orders(
            list().stream()
                .filter(ProductionOrder::isInProgress)
                .collect(Collectors.toList())
        );
    }

    default Orders nonCompleted() {
        return new Orders(
            list().stream()
                .filter(order -> !order.isCompleted())
                .collect(Collectors.toList())
        );
    }

    default Orders completed() {
        return new Orders(
            list().stream()
                .filter(ProductionOrder::isCompleted)
                .collect(Collectors.toList())
        );
    }

    default Orders ofType(AUnitType unitType) {
        return new Orders(
            list().stream()
                .filter(order -> (order.unitType() != null && order.unitType().equals(unitType)))
                .collect(Collectors.toList())
        );
    }

    default Orders exclude(ProductionOrder orderToExclude) {
        return new Orders(
            list().stream()
                .filter(order -> !order.equals(orderToExclude))
                .collect(Collectors.toList())
        );
    }

    default Orders next(int n) {
        return new Orders(
            list().stream()
                .limit(n)
                .collect(Collectors.toList())
        );
    }

    default Orders techType(TechType type) {
        return new Orders(
            list().stream()
                .filter(order -> (order.tech() != null && order.tech().equals(type)))
                .collect(Collectors.toList())
        );
    }

    default Orders upgradeType(UpgradeType type) {
        return new Orders(
            list().stream()
                .filter(order -> (order.upgrade() != null && order.upgrade().equals(type)))
                .collect(Collectors.toList())
        );
    }

    default Orders priorityAtLeast(ProductionOrderPriority priority) {
        return new Orders(
            list().stream()
                .filter(order -> order.priority().isAtLeast(priority))
                .collect(Collectors.toList())
        );
    }
}
