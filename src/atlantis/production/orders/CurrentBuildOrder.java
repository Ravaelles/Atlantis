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

    public static int settingAutoProduceWorkersMinWorkers() {
        return get().settingIntValue("AUTO_PRODUCE_WORKERS_MIN_WORKERS");
    }

    public static int settingAutoProduceWorkersMaxWorkers() {
        return get().settingIntValue("AUTO_PRODUCE_WORKERS_MAX_WORKERS");
    }

    public static int settingScoutIsNthWorker() {
        return get().settingIntValue("SCOUT_IS_NTH_WORKER");
    }

    public static int settingAutoSupplyManagerWhenSupplyExceeds() {
        return get().settingIntValue("AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS");
    }
}
