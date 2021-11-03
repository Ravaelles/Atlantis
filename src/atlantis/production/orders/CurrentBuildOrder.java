package atlantis.production.orders;

public class CurrentBuildOrder {

    /**
     * Build order currently in use.
     * switchToBuildOrder(ABuildOrder buildOrder)
     */
    private static ABuildOrder currentBuildOrder = null;

    /**
     * Returns currently active build order.
     */
    public static ABuildOrder get() {
        return currentBuildOrder;
    }

    public static void set(ABuildOrder buildOrder) {
        currentBuildOrder = buildOrder;
    }

}
