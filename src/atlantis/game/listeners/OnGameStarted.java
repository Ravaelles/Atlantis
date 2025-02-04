package atlantis.game.listeners;

import atlantis.Atlantis;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.combat.squad.omega.Omega;
import atlantis.config.ActiveMap;
import atlantis.config.AtlantisRaceConfig;
import atlantis.config.AtlantisConfigChanger;
import atlantis.config.env.Env;
import atlantis.debug.painter.APainter;
import atlantis.debug.profiler.RealTime;
import atlantis.debug.tweaker.ParamTweakerFactory;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.GameSpeed;
import atlantis.game.init.DetectInitialTechs;
import atlantis.game.race.EnemyRace;
import atlantis.information.strategy.ProtossStrategies;
import atlantis.information.strategy.StrategyChooser;
import atlantis.information.strategy.terran.TerranStrategies;
import atlantis.information.strategy.ZergStrategies;
import atlantis.game.init.AInitialActions;
import atlantis.map.AMap;
import atlantis.information.generic.InitialMainPosition;
import atlantis.production.orders.build.ABuildOrderLoader;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.production.orders.production.queue.QueueInitializer;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

public class OnGameStarted {

    public static void execute() {
//        if (Env.isLocal() && Env.isFirstRun()) {
//            AForcedClicks.clickAltF9(); // Make ChaosLauncher double size
//        }

        System.out.println("\n############### Starting Atlantis... ##############################");

        // Uncomment this line to see list of units -> damage.
//        AUnitTypesHelper.displayUnitTypesDamage();

        APainter.assignBwapiInstance();

        handleCheckIfUmsMap();

        System.out.println("%%% enemyName = " + AGame.enemyName());
        System.out.println("%%% mapFileName = " + Atlantis.game().mapFileName());

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

        // Remember enemy race - could be broken for UMS maps
        EnemyRace.enemyRace();

        // Create list of all strategies in memory
        initializeAllStrategies();

        // Set strategy and unit production sequence (Build Order) to use. It can be later changed dynamically.
        initStrategyAndBuildOrder();

        InitialMainPosition.remember();

        try {
            AInitialActions.executeInitialActions();
        } catch (Exception e) {
            A.errPrintln("### Early exception, but don't worry ###");
            A.errPrintln("This probably means you are playing UMS map.");
            A.errPrintln("Atlantis is handling this case and keeps on playing.");
            AGame.setUmsMode();
            A.printStackTrace();
        }

        if (A.isUms()) {
            DetectInitialTechs.update();
        }

        Alpha.get().setMission(Missions.globalMission());
        Omega.get().setMission(Missions.DEFEND);

        System.out.println("### Atlantis is working! ###\n");
        if (Env.isTournament()) {
            String opponent = AGame.enemyName();
            System.out.println("### Playing against: " + opponent + " ###\n");
        }

//        AUnitTypesHelper.printUnitsAndRequirements();

        // Special mode - enable if want to adjust parameter values
        if (Env.isParamTweaker()) {
            ParamTweakerFactory.init();
        }

        RealTime.gameStarted = RealTime.currentTimestamp();
    }

    private static void handleCheckIfUmsMap() {
//        if (Atlantis.game().mapPathName().contains("/ums/")) {
//            AGame.setUmsMode();
//        }

//        int ours = Select.our().count();

//        if (We.zerg() ? ours != 9 : ours != 5) {
        if (Select.our().workers().atMost(3) || Select.ourBases().empty()) {
            AGame.setUmsMode();
        }
    }

    public static void initializeAllStrategies() {
        TerranStrategies.loadAll();
        ProtossStrategies.initialize();
        ZergStrategies.initialize();
    }

    public static void initStrategyAndBuildOrder() {
        try {
            StrategyChooser.initializeStrategy();
            QueueInitializer.initializeProductionQueue();

//            A.println("CurrentBuildOrder.get() = " + CurrentBuildOrder.get());
            if (CurrentBuildOrder.get() != null) {
                if (Env.isLocal()) A.println("Use build order: `" + CurrentBuildOrder.get() + "`");
            }
            else {
                ErrorLog.printErrorOnce("Invalid (empty) build order in AtlantisRaceConfig!");
                AGame.exit();
            }
        } catch (Exception e) {
            A.errPrintln("");
            A.errPrintln("#######################################################");
            A.errPrintln(
                "Make sure that " + ABuildOrderLoader.BUILD_ORDERS_PATH + " contains build_orders directory,"
            );
            A.errPrintln("copy it from Atlantis/build_orders");
            A.errPrintln("#######################################################");

            if (CurrentBuildOrder.get() == null) {
                A.errPrintln("");
                throw new RuntimeException("Current BUILD ORDER is NULL");
            }

            A.errPrintln(
                "Does file exist? "
                    + (A.fileExists(CurrentBuildOrder.get().getName()) ? "YES - " : "NO, IT DOESN'T! ")
                    + CurrentBuildOrder.get().getName()
            );
            A.errPrintln("Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Exception when loading build orders file");
        }
    }

}
