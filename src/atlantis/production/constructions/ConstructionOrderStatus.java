package atlantis.production.constructions;

public enum ConstructionOrderStatus {
    /**
     * Site picked in advance, no builder assigned, nothing issued (owner
     * 2026-10-14): opened one supply before the order unlocks so the
     * building is aimed before it is affordable. Waiting by design -
     * overdue, builders and status-changing ignore it; adoption (builder
     * + NOT_STARTED) happens in ProduceBuilding once the order is ready.
     */
    PREPARING, NOT_STARTED, IN_PROGRESS, FINISHED
}
