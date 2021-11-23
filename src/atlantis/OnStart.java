package atlantis;

import atlantis.debug.APainter;
import atlantis.debug.AUnitTypesHelper;
import atlantis.env.Env;
import atlantis.init.AInitialActions;
import atlantis.keyboard.AClicks;
import atlantis.map.AMap;
import atlantis.production.orders.CurrentBuildOrder;
import atlantis.strategy.OurStrategyManager;
import atlantis.strategy.ProtossStrategies;
import atlantis.strategy.TerranStrategies;
import atlantis.strategy.ZergStrategies;
import atlantis.tweaker.ParamTweakerFactory;

public class OnStart {

    public static void execute() {
        if (Env.isLocal()) {
            AClicks.clickAltF9(); // Make ChaosLauncher double size
        }

        System.out.println("\n############### Starting Atlantis... #############################################");

        // Uncomment this line to see list of units -> damage.
//        AUnitTypesHelper.displayUnitTypesDamage();

        APainter.assignBwapiInstance();

        handleCheckIfUmsMap();

        // Atlantis can modify ChaosLauncher's config files treating AtlantisConfig as the source-of-truth
        AtlantisConfigChanger.modifyRacesInConfigFileIfNeeded();

        // Validate AtlantisConfig and exit if it's invalid
        if (Env.isLocal()) {
            AtlantisConfig.validate();
        }

        // Game speed mode that starts fast, slows down when units are attacking
        GameSpeed.init();

        // Enable/disable painting
        APainter.init();

        // One time map analysis for every map
        AMap.initMapAnalysis();

        // Create list of all strategies in memory
        initializeAllStrategies();

        // Set strategy and unit production sequence (Build Order) to use. It can be later changed dynamically.
        initStrategyAndBuildOrder();

        try {
            AInitialActions.executeInitialActions();
        } catch (Exception e) {
            System.err.println("### Early exception ###");
            System.err.println("This probably means you are playing UMS map.");
            AGame.setUmsMode();
        }

        System.out.println("### Atlantis is working! ###\n");

//        AUnitTypesHelper.printUnitsAndRequirements();

        // Special mode - enable if want to adjust parameter values
        if (Env.isParamTweaker()) {
            ParamTweakerFactory.init();
        }
    }

    private static void handleCheckIfUmsMap() {
        if (Atlantis.game().mapPathName().contains("/ums/")) {
            AGame.setUmsMode();
        }
    }

    private static void initializeAllStrategies() {
        TerranStrategies.initialize();
        ProtossStrategies.initialize();
        ZergStrategies.initialize();
    }

    private static void initStrategyAndBuildOrder() {
        try {
            OurStrategyManager.initialize();

            if (CurrentBuildOrder.get() != null) {
                System.out.println("Use build order: `" + CurrentBuildOrder.get().getName() + "`");
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
