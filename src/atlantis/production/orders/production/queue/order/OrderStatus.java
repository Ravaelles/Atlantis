package atlantis.production.orders.production.queue.order;

public enum OrderStatus {
    NOT_READY,
    READY_TO_PRODUCE,
    IN_PROGRESS,
    COMPLETED;

    public boolean inProgress() {
        return this.equals(IN_PROGRESS);
    }

    public boolean ready() {
        return this.equals(READY_TO_PRODUCE);
    }
}
