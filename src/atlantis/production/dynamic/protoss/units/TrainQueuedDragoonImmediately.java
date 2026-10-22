package atlantis.production.dynamic.protoss.units;

import atlantis.game.event.AutomaticListener;
import atlantis.game.event.Event;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;

/**
 * Same-frame production for a dynamic Dragoon queue row (owner): when the
 * feeder (or any dynamic author) queues a Dragoon, waiting for the next
 * queue pass can strand it behind starving rows - so the row's own event
 * starts its training immediately in the nearest-to-enemy Gateway
 * (which, with the enemy coming through the natural, is the natural-side
 * Gateway). No layer call: the queue only announces, this listener owns
 * the train. If the engine refuses (no resources, no usable Gateway) the
 * row simply stays queued with its zero supply gate and the normal flow
 * picks it up - the refusal changes nothing.
 *
 * <p>Auto-registered via {@link atlantis.game.event.AutoRegisterEventListeners}.</p>
 */
public class TrainQueuedDragoonImmediately extends AutomaticListener {

    public TrainQueuedDragoonImmediately() {
    }

    @Override
    public Event listensTo() {
        return Event.DYNAMIC_UNIT_QUEUED;
    }

    @Override
    public void onEvent(Event event, Object... data) {
        if (data.length == 0 || !(data[0] instanceof ProductionOrder)) return;

        ProductionOrder order = (ProductionOrder) data[0];
        if (order.unitType() == null || !order.unitType().isDragoon()) return;

        AUnit gateway = GatewayClosestToEnemy.get();
        if (gateway == null) return;

        // The train IS this row's realization: on success the row is
        // consumed so the normal flow never trains it a second time.
        if (gateway.train(order.unitType(), ForcedDirectProductionOrder.create(order.unitType()))) {
            order.cancel("trained-immediately");
        }
    }
}
