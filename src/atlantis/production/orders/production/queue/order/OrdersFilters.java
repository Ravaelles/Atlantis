package atlantis.production.orders.production.queue.order;

import atlantis.units.AUnitType;
import bwapi.TechType;
import bwapi.UpgradeType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface OrdersFilters {
    List<ProductionOrder> list();

    default Orders all() {
        return (Orders) this;
    }

//    default Orders all() {
//        return new Orders(
//            new ArrayList<>(list())
//        );
//    }

    default Orders readyToProduce() {
        return new Orders(
            list().stream()
                .filter(ProductionOrder::isReadyToProduce)
//                .filter(o -> o != null && o.isReadyToProduce())
                .collect(Collectors.toList())
        );
    }

    default Orders inProgress() {
        return new Orders(
            list().stream()
//                .filter(o -> o != null && o.isInProgress())
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

    default Orders nonCompletedNext30() {
        return new Orders(
            list().stream()
                .filter(order -> !order.isCompleted())
                .limit(30)
                .collect(Collectors.toList())
        );
    }

    default Orders notInProgress() {
        return new Orders(
            list().stream()
                .filter(order -> !order.isInProgress())
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

    default Orders forCurrentSupply() {
        return new Orders(
            list().stream()
                .filter(order -> order.supplyRequirementFulfilled())
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
                .filter(order -> (!order.isCompleted() && !order.shouldIgnore() && !order.isInProgress()))
                .limit(n)
                .collect(Collectors.toList())
        );
    }

    default Orders techType(TechType tech) {
        return new Orders(
            list().stream()
                .filter(order -> (order.tech() != null && order.tech().equals(tech)))
                .collect(Collectors.toList())
        );
    }

    default Orders upgradeType(UpgradeType upgrade) {
        return new Orders(
            list().stream()
                .filter(order -> (order.upgrade() != null && order.upgrade().equals(upgrade)))
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

    default Orders dynamic() {
        return new Orders(
            list().stream()
                .filter(order -> order.isDynamic())
                .collect(Collectors.toList())
        );
    }

    default Orders buildings() {
        return new Orders(
            list().stream()
                .filter(order -> order.unitType() != null && order.unitType().isABuilding())
                .collect(Collectors.toList())
        );
    }

    default Orders units() {
        return new Orders(
            list().stream()
                .filter(order -> order.unitType() != null && !order.unitType().isABuilding())
                .collect(Collectors.toList())
        );
    }

    default Orders supplyAtMost(int maxSupply) {
        return new Orders(
            list().stream()
                .filter(order -> order.minSupply() <= maxSupply)
                .collect(Collectors.toList())
        );
    }

    default int sumMinerals() {
        return list().stream()
            .mapToInt(ProductionOrder::mineralPrice)
            .sum();
    }

    default Orders infantry() {
        return new Orders(
            list().stream()
                .filter(order -> order.unitType() != null && order.unitType().isInfantry())
                .collect(Collectors.toList())
        );
    }
}
