package atlantis.production.orders.production.queue;

public class Queue {
    private static Queue instance = null;

//    private final Orders orders = new Orders();
    private final Orders allOrders = new Orders();
    private final Orders readyToProduceOrders = new Orders();
    private final Orders inProgressOrders = new Orders();
    private final Orders completedOrders = new Orders();

    // =========================================================

    public Queue() {
    }

    // =========================================================

    public void update() {
        (new QueueUpdater(this)).update();
    }

    // =========================================================

    public Orders allOrders() {
        return allOrders;
    }

    public Orders readyToProduceOrders() {
        return readyToProduceOrders;
    }

    public Orders inProgressOrders() {
        return inProgressOrders;
    }

    public Orders completedOrders() {
        return completedOrders;
    }

    // =========================================================

    public static Queue get() {
        return instance;
    }

    public static void set(Queue instance) {
        Queue.instance = instance;
    }

    public void print() {
        orders.print();
    }
}
