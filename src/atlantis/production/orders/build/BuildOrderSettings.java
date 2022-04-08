package atlantis.production.orders.build;

import java.util.Map;

public class BuildOrderSettings {

    public static final String AUTO_PRODUCE_WORKERS_MIN_SUPPLY = "AUTO_PRODUCE_WORKERS_MIN_SUPPLY";
    public static final String AUTO_PRODUCE_WORKERS_MAX_WORKERS = "AUTO_PRODUCE_WORKERS_MAX_WORKERS";
    public static final String AUTO_PRODUCE_ZEALOTS = "AUTO_PRODUCE_ZEALOTS";
    public static final String AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS = "AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS";
    public static final String SCOUT_IS_NTH_WORKER = "SCOUT_IS_NTH_WORKER";
//    public static final String  = "";
//    public static final String  = "";

    // =========================================================

    public static int autoProduceWorkersMinWorkers() {
        return settingAsInt(AUTO_PRODUCE_WORKERS_MIN_SUPPLY, 1);
    }

    public static int autoProduceWorkersMaxWorkers() {
        return settingAsInt(AUTO_PRODUCE_WORKERS_MAX_WORKERS, 55);
    }

    public static boolean autoProduceZealots() {
        return settingAsBoolean(AUTO_PRODUCE_ZEALOTS, false);
    }

    public static int autoSupplyManagerWhenSupplyExceeds() {
        return settingAsInt(AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS, 25);
    }

    public static int scoutIsNthWorker() {
        return settingAsInt(SCOUT_IS_NTH_WORKER, 9);
    }

    // =========================================================

    protected static int settingAsInt(String key, int fallbackOrNull) {
        if (!settings().containsKey(key)) {
            return fallbackOrNull;
//            throw new RuntimeException("No setting in build order: " + key);
        }

        return CurrentBuildOrder.get().settings.get(key).valueInt();
    }

    protected static boolean settingAsBoolean(String key, boolean fallbackOrNull) {
        if (!settings().containsKey(key)) {
            return fallbackOrNull;
//            throw new RuntimeException("No setting in build order: " + key);
        }

        return CurrentBuildOrder.get().settings.get(key).valueBoolean();
    }

    private static Map<String, BuildOrderSetting> settings() {
        return CurrentBuildOrder.get().settings;
    }

}
