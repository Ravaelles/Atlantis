package atlantis.production.orders.production.queue.order;

public enum OrderStatus {
    NOT_READY,
    READY_TO_PRODUCE,
    IN_PROGRESS,
    FINISHED;

    public boolean isInProgress() {
        return this.equals(IN_PROGRESS);
    }

    public boolean isFinished() {
        return this.equals(FINISHED);
    }

    public boolean isReady() {
        return this.equals(READY_TO_PRODUCE);
    }
}
