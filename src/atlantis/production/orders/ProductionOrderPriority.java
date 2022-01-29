package atlantis.production.orders;

public enum ProductionOrderPriority implements Comparable<ProductionOrderPriority> {

    STANDARD(1),
    HIGH(23),
    TOP(34);

    // =========================================================

    private int number;

    ProductionOrderPriority(int number) {
        this.number = number;
    }
}
