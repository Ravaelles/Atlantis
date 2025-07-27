package atlantis.production.orders.production.queue.order;

public enum ProductionOrderPriority implements Comparable<ProductionOrderPriority> {
    STANDARD(12),
    HIGH(23),
    TOP(34);

    // =========================================================

    private int number;

    ProductionOrderPriority(int number) {
        this.number = number;
    }

    public boolean isAtLeast(ProductionOrderPriority priority) {
        return this.number >= priority.number;
    }

    public boolean isStandard() {
        return this == STANDARD;
    }

    public boolean isHighOrHigher() {
        return number >= HIGH.number;
    }
}
