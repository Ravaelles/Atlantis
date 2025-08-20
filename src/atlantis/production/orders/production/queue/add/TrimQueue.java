package atlantis.production.orders.production.queue.add;

import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.util.Counter;

public class TrimQueue {
    public static void trimIfTooBig(Queue queue) {
        if (queue == null) return;
        Orders orders = trimmableOrders(queue);
        if (orders.size() <= 12) return;

        Counter<AUnitType> typeCount = unitTypeCount(orders);

        int counter = 0;
        while (true) {
            AUnitType dominantType = typeCount.keyWithHighestValue();
            if (dominantType == null) return;

            int maxCount = typeCount.getValueFor(dominantType);

            if (maxCount >= (dominantType.isCombatBuilding() ? 3 : 2)) {
                int cancelCount = maxCount - 1;
                cancelOrders(orders, dominantType, cancelCount, typeCount);
            } else {
                break; // Exit if no type has 2 or more orders
            }

            if (counter++ >= 10) break;
        }

        cancelExcessivePylonsIfNeeded(orders, typeCount);

//        orders.cancelAll("▓▓▓▓▓▓▓▓ TRIM TOO BIG QUEUE");
    }

    private static void cancelExcessivePylonsIfNeeded(Orders orders, Counter<AUnitType> typeCount) {
        int pylonCount = typeCount.getValueFor(AUnitType.Protoss_Pylon);
        if (pylonCount >= 3) {
            cancelOrders(orders, AUnitType.Protoss_Pylon, 1, typeCount);
            typeCount.decrementValueFor(AUnitType.Protoss_Pylon);
        }
    }

    private static void cancelOrders(Orders orders, AUnitType maxType, int cancelCount, Counter<AUnitType> typeCount) {
        for (ProductionOrder orderToCancel : orders.ofType(maxType)) {
            if (!orderToCancel.priority().isStandard()) continue;
            if (cancelCount-- <= 0) break;

            orderToCancel.cancel("▓▓▓ Cancel EXCESSIVE " + maxType);
            typeCount.decrementValueFor(maxType);
        }
    }

    private static Counter<AUnitType> unitTypeCount(Orders orders) {
        Counter<AUnitType> typeCount = new Counter<>();
        for (ProductionOrder order : orders) {
            AUnitType type = order.unitType();
            if (type == null) continue;

            typeCount.incrementValueFor(type);
        }

        return typeCount;
    }

    private static Orders trimmableOrders(Queue queue) {
        return queue.notInProgress();
    }
}
