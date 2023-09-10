package atlantis.production.orders.production.queue;

public class ResourcesReserved {
    private static int minerals = 0;
    private static int gas = 0;

    // =========================================================

    public void reset() {
        minerals = 0;
        gas = 0;
    }

    // =========================================================

    public static void reserveMinerals(int minerals) {
        ResourcesReserved.minerals += minerals;
    }

    public static void reserveGas(int gas) {
        ResourcesReserved.gas += gas;
    }

    // =========================================================

    public static int minerals() {
        return minerals;
    }

    public static int gas() {
        return gas;
    }
}
