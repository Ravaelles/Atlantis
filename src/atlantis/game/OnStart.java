package atlantis.game;

import atlantis.Atlantis;
import atlantis.config.AtlantisConfig;
import atlantis.config.AtlantisConfigChanger;
import atlantis.config.env.Env;
import atlantis.debug.painter.APainter;
import atlantis.debug.tweaker.ParamTweakerFactory;
import atlantis.information.strategy.OurStrategyChooser;
import atlantis.information.strategy.ProtossStrategies;
import atlantis.information.strategy.TerranStrategies;
import atlantis.information.strategy.ZergStrategies;
import atlantis.init.AInitialActions;
import atlantis.map.AMap;
import atlantis.production.orders.build.CurrentBuildOrder;

public class OnStart {

    public static void execute() {
//        if (Env.isLocal() && Env.isFirstRun()) {
//            AForcedClicks.clickAltF9(); // Make ChaosLauncher double size
//        }

        System.out.println("\n############### Starting Atlantis... ##############################");

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

    public static void initializeAllStrategies() {
        TerranStrategies.initialize();
        ProtossStrategies.initialize();
        ZergStrategies.initialize();
    }

    public static void initStrategyAndBuildOrder() {
        try {
            OurStrategyChooser.initialize();

            if (Env.isLocal()) {
                if (CurrentBuildOrder.get() != null) {
                    System.out.println("Use build order: `" + CurrentBuildOrder.get() + "`");
                }
                else {
                    System.err.println("Invalid (empty) build order in AtlantisConfig!");
                    AGame.exit();
                }
            }
        }
        catch (Exception e) {
            System.err.println("Does file exist? " + CurrentBuildOrder.get().getName());
            throw new RuntimeException("Exception when loading build orders file");
        }
    }

}
