package atlantis;

import atlantis.debug.APainter;
import atlantis.init.AInitialActions;
import atlantis.map.AMap;
import atlantis.production.orders.AProductionQueue;
import atlantis.strategy.OurStrategyManager;
import atlantis.strategy.ProtossStrategies;
import atlantis.strategy.TerranStrategies;
import atlantis.strategy.ZergStrategies;

public class OnStart {

    public static void execute() {
        System.out.println("### Starting Atlantis... ###");

        // Uncomment this line to see list of units -> damage.
//        AUnitTypesHelper.displayUnitTypesDamage();

        APainter.assignBwapiInstance();

        handleCheckIfUmsMap();

        // Atlantis can modify ChaosLauncher's config files treating AtlantisConfig as the source-of-truth
        AtlantisConfigChanger.modifyRacesInConfigFileIfNeeded();

        // Validate AtlantisConfig and exit if it's invalid
        AtlantisConfig.validate();

        // Game speed mode that starts fast, slows down when units are attacking
        GameSpeed.init();

        // One time map analysis for every map
        AMap.initMapAnalysis();

        // Create list of all strategies in memory
        initializeAllStrategies();

        // Set strategy and unit production sequence (Build Order) to use. It can be later changed dynamically.
        initializeOurStrategyAndBuildOrder();

        try {
            AInitialActions.executeInitialActions();
        } catch (Exception e) {
            System.err.println("### Early exception ###");
            System.err.println("This probably means your playing special UMS map.");
            AGame.setUmsMode(true);
        }

        System.out.println("### Atlantis is working! ###");
    }

    private static void handleCheckIfUmsMap() {
        if (Atlantis.game().mapPathName().contains("/ums/")) {
            AGame.setUmsMode(true);
        }
    }

    private static void initializeAllStrategies() {
        TerranStrategies.initialize();
        ProtossStrategies.initialize();
        ZergStrategies.initialize();
    }

    private static void initializeOurStrategyAndBuildOrder() {
        try {
            OurStrategyManager.initialize();

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
