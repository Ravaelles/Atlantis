package atlantis;

import atlantis.debug.AUnitTypesHelper;
import atlantis.init.AInitialActions;
import atlantis.map.AMap;
import atlantis.production.orders.AProductionQueue;
import atlantis.production.orders.AProductionQueueManager;

public class OnStart {

    public static void execute() {
        System.out.println("### Starting Atlantis... ###");

        handleCheckIfUmsMap();

        // Uncomment this line to see list of units -> damage.
//        AUnitTypesHelper.displayUnitTypesDamage();

        // Atlantis can modify ChaosLauncher's config files treating AtlantisConfig as the source-of-truth
        AtlantisConfigChanger.modifyRacesInConfigFileIfNeeded();

        // Validate AtlantisConfig and exit if it's invalid
        AtlantisConfig.validate();

        // Game speed mode that starts fast, slows down when units are attacking
        AGameSpeed.init();
//        AGameSpeed.allowToDynamicallySlowdownGameOnFirstFighting();

        // One time map analysis for every map
        AMap.initMapAnalysis();

        // Set prodction strategy (build orders) to use. It can be always changed dynamically.
        initializeBuildOrder();

        AInitialActions.executeInitialActions();

        System.out.println("### Atlantis is working! ###");
    }

    private static void handleCheckIfUmsMap() {
        if (Atlantis.game().mapPathName().contains("/ums/")) {
            AGame.setUmsMode(true);
        }
    }

    private static void initializeBuildOrder() {
        try {
            AProductionQueueManager.switchToBuildOrder(AtlantisConfig.DEFAULT_BUILD_ORDER);

            if (AProductionQueue.getCurrentBuildOrder() != null) {
                System.out.println("Use build order: `" + AProductionQueue.getCurrentBuildOrder().getName() + "`");
            }
            else {
                System.err.println("Invalid (empty) build order in AtlantisConfig!");
                AGame.exit();
            }
        }
        catch (Exception e) {
            System.err.println("Exception when loading build orders file");
            e.printStackTrace();
        }
    }

}
