package atlantis.production.orders;

public class BuildOrderSettings {

    public static int autoProduceWorkersMinWorkers() {
        return CurrentBuildOrder.get().settingIntValue("AUTO_PRODUCE_WORKERS_MIN_WORKERS");
    }

    public static int autoProduceWorkersMaxWorkers() {
        return CurrentBuildOrder.get().settingIntValue("AUTO_PRODUCE_WORKERS_MAX_WORKERS");
    }

    public static int autoSupplyManagerWhenSupplyExceeds() {
        return CurrentBuildOrder.get().settingIntValue("AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS");
    }

    public static int scoutIsNthWorker() {
        return CurrentBuildOrder.get().settingIntValue("SCOUT_IS_NTH_WORKER");
    }

}
