package atlantis.game;

import atlantis.config.AtlantisRaceConfig;
import atlantis.config.AtlantisConfigChanger;
import atlantis.config.env.Env;
import atlantis.debug.painter.APainter;
import atlantis.debug.tweaker.ParamTweakerFactory;
import atlantis.information.strategy.ProtossStrategies;
import atlantis.information.strategy.StrategyChooser;
import atlantis.information.strategy.TerranStrategies;
import atlantis.information.strategy.ZergStrategies;
import atlantis.init.AInitialActions;
import atlantis.map.AMap;
import atlantis.production.dynamic.expansion.InitialMainPosition;
import atlantis.production.orders.build.ABuildOrderLoader;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.units.select.Select;
import atlantis.util.We;

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

        // Atlantis can modify ChaosLauncher's config files treating AtlantisRaceConfig as the source-of-truth
        AtlantisConfigChanger.modifyRacesInConfigFileIfNeeded();

        // Validate AtlantisRaceConfig and exit if it's invalid
        if (Env.isLocal()) {
            AtlantisRaceConfig.validate();
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

        InitialMainPosition.remember();

        try {
            AInitialActions.executeInitialActions();
        } catch (Exception e) {
            System.err.println("### Early exception, but don't worry ###");
            System.err.println("This probably means you are playing UMS map.");
            System.err.println("Atlantis is handling this case and keeps on playing.");
            AGame.setUmsMode();
            A.printStackTrace();
        }

        System.out.println("### Atlantis is working! ###\n");

//        AUnitTypesHelper.printUnitsAndRequirements();

        // Special mode - enable if want to adjust parameter values
        if (Env.isParamTweaker()) {
            ParamTweakerFactory.init();
        }
    }

    private static void handleCheckIfUmsMap() {
//        if (Atlantis.game().mapPathName().contains("/ums/")) {
//            AGame.setUmsMode();
//        }

        int ours = Select.our().count();

        if (We.zerg() ? ours != 9 : ours != 5) {
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
            StrategyChooser.initialize();

            if (Env.isLocal()) {
                if (CurrentBuildOrder.get() != null) {
                    System.out.println("Use build order: `" + CurrentBuildOrder.get() + "`");
                }
                else {
                    System.err.println("Invalid (empty) build order in AtlantisRaceConfig!");
                    AGame.exit();
                }
            }
        } catch (Exception e) {
            System.err.println();
            System.err.println("#######################################################");
            System.err.println(
                "Make sure that Starcraft/" + ABuildOrderLoader.BUILD_ORDERS_PATH
                    + " contains build_orders directory,"
            );
            System.err.println("copy it from Atlantis/build_orders");
            System.err.println("#######################################################");

            if (CurrentBuildOrder.get() == null) {
                System.err.println();
                throw new RuntimeException("Current BUILD ORDER is NULL");
            }

            System.err.println(
                "Does file exist? "
                    + (A.fileExists(CurrentBuildOrder.get().getName()) ? "YES - " : "NO, IT DOESN'T! ")
                    + CurrentBuildOrder.get().getName()
            );
            System.err.println(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Exception when loading build orders file");
        }
    }

}
