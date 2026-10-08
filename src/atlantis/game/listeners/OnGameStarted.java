package atlantis.game.listeners;

import atlantis.Atlantis;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.squads.alpha.Alpha;
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
import atlantis.information.strategy.protoss.ProtossStrategies;
import atlantis.information.strategy.Strategy;
import atlantis.information.strategy.StrategyChooser;
import atlantis.information.strategy.terran.TerranStrategies;
import atlantis.information.strategy.ZergStrategies;
import atlantis.game.init.AInitialActions;
import atlantis.map.AMap;
import atlantis.information.generic.InitialMainPosition;
import atlantis.map.scout.ScoutState;
import atlantis.production.orders.build.ABuildOrderLoader;
import atlantis.production.orders.build.BuildOrderSettings;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.production.orders.production.queue.QueueInitializer;
import atlantis.units.select.Select;
import atlantis.util.AConsole;
import bwapi.Race;
import main.Main;
import atlantis.util.AFile;
import atlantis.util.log.ErrorLog;
import benchmark.BenchmarkMode;
import atlantis.cherryvis.ACherryVis;

public class OnGameStarted {

    public static void execute() {
//        if (Env.isLocal() && Env.isFirstRun()) {
//            AForcedClicks.clickAltF9(); // Make ChaosLauncher double size
//        }

        System.out.println("\n########### Starting Atlantis... #################");

        // Uncomment this line to see list of units -> damage.
//        AUnitTypesHelper.displayUnitTypesDamage();

        APainter.assignBwapiInstance();

        handleCheckIfUmsMap();

        System.out.println("### enemyName   = " + AGame.enemyName());
        System.out.println("### mapFileName = " + Atlantis.game().mapFileName());

        // Atlantis can modify ChaosLauncher's config files treating AtlantisRaceConfig as the source-of-truth
        AtlantisConfigChanger.modifyRacesInConfigFileIfNeeded();

        // The race the GAME says we are is authoritative for every race branch
        // (We.protoss() and friends read AtlantisRaceConfig.MY_RACE), while
        // Main.ourRace() is what the bot ASKS to be. When those disagree the bot
        // plays the wrong race's code: it builds no units, and nothing errors.
        //
        // This is not hypothetical - it is exactly what happened (owner report,
        // 2026-10-07): bwapi.ini still said race=Terran, Main.ourRace() said
        // Protoss, so We.protoss() was false for the whole game and
        // ProtossDynamicUnitProductionCommander.applies() never ran. The bot
        // produced one Zealot and one Dragoon and then stopped asking forever.
        warnIfRequestedRaceDiffersFromTheGame();

        // Validate AtlantisRaceConfig and exit if it's invalid. Skipped when the
        // race is unknown: every race-dependent constant is null then, so the
        // check would fail for a reason that has nothing to do with the config
        // and kill the process during game start with a clean log.
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

        // Create list of all strategies in memory AND give each one its real
        // name (the build-order file name). This must happen BEFORE a strategy is
        // chosen: the chosen strategy loads its build order while being selected,
        // and it can only find the file under that name (measured 2026-10-08:
        // choosing first meant every strategy was named after its constant, so
        // "PROTOSS_Zealot_into_Goon.txt" was looked for and never existed).
        initializeAllStrategies();

        // Set strategy and unit production sequence (Build Order) to use. It can be later changed dynamically.
        initStrategyAndBuildOrder();

        InitialMainPosition.remember();

        try {
            AInitialActions.executeInitialActions();
        } catch (Exception e) {
            AConsole.errPrintln("### Early exception, but don't worry ###");
            AConsole.errPrintln("This probably means you are playing UMS map.");
            AConsole.errPrintln("Atlantis is handling this case and keeps on playing.");
            AGame.setUmsMode();
            AConsole.printStackTrace();
        }

        DetectInitialTechs.update();
//        if (A.isUms()) {
//        }

//        Alpha.get().setMission(Missions.globalMission());

        System.out.println("### Atlantis is working! ###\n");
        if (Env.isTournament()) {
            String opponent = AGame.enemyName();
            System.out.println("### Playing against: " + opponent + " ###\n");
        }

//        AUnitTypesHelper.printUnitsAndRequirements();

        if (ACherryVis.isEnabled()) ACherryVis.initialize();
        if (Env.isParamTweaker()) ParamTweakerFactory.init();
        if (Env.isBenchmark()) BenchmarkMode.onGameStarted();

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

            // Scouting asks which worker number may scout every frame. Reading the
            // build-order setting here keeps that lookup in the game root, so
            // atlantis.map.scout does not depend on atlantis.production.
            ScoutState.scoutIsNthWorker = BuildOrderSettings.scoutIsNthWorker();

//            AConsole.println("CurrentBuildOrder.get() = " + CurrentBuildOrder.get());
            if (CurrentBuildOrder.get() != null) {
                // Gated to a real local game: the stub world starts a game in every
                // test, and 99 lines in the acceptance tier is log noise, not a signal.
                if (Env.isLocal() && !Env.isTesting()) {
                    AConsole.println("Use build order: `" + CurrentBuildOrder.get() + "`");
                }
            }
            else {
                ErrorLog.printErrorOnce("Invalid (empty) build order in AtlantisRaceConfig!");
                AGame.exit();
            }
        } catch (Exception e) {
            AConsole.errPrintln("Could not load build order "
                + (CurrentBuildOrder.get() != null ? "`" + CurrentBuildOrder.get().getName() + "`" : "(none set)")
                + " for strategy `" + Strategy.current() + "`: " + e);
            AConsole.errPrintln("Build orders are resolved against BWAPI_DATA_PATH"
                + " (ENV) and the working directory. Set BWAPI_DATA_PATH to the directory that"
                + " holds AI/build_orders/ and Protoss|Terran|Zerg subdirectories.");

            if (CurrentBuildOrder.get() == null) {
                throw new RuntimeException("Current BUILD ORDER is NULL");
            }
            e.printStackTrace();
            throw new RuntimeException("Exception when loading build orders file");
        }
    }

                /**
                 * Warns when the race the game put us in differs from the one the client
                 * asked for. This mismatch is silent and expensive: every race branch
                 * ({@code We.protoss()} and friends) reads the game's race, so the bot runs
                 * the wrong race's code and simply does nothing instead of failing.
                 *
                 * <p>Measured cause (owner report, 2026-10-07): {@code Main.ourRace()} said
                 * Protoss while {@code bwapi.ini} still said {@code race=Terran}, so
                 * StarCraft started us as Terran, {@code We.protoss()} was false all game,
                 * and {@code ProtossDynamicUnitProductionCommander.applies()} never ran -
                 * one Zealot, one Dragoon, then no units ever again with minerals banked.
                 * The fix for the config is in the launcher (it now writes the client's
                 * race into bwapi.ini); this warning is what makes a future recurrence
                 * visible in the log instead of in a lost game.</p>
                 */
                private static void warnIfRequestedRaceDiffersFromTheGame() {
                    try {
                        String requested = Main.ourRace();
                        Race inGame = Atlantis.game().self().getRace();
                        if (requested == null || inGame == null) return;

                        String inGameName = inGame.toString();
                        if (!requested.equalsIgnoreCase(inGameName)) {
                            AConsole.errPrintln("");
                            AConsole.errPrintln("#######################################################");
                            AConsole.errPrintln("RACE MISMATCH: the game started us as " + inGameName
                                    + " but Main.ourRace() asks for " + requested + ".");
                            AConsole.errPrintln("Every race-specific branch follows the GAME's race, so "
                                    + requested + " code will not run at all");
                            AConsole.errPrintln("(no units will be produced). Fix bwapi.ini's race= to match"
                                    + " - the launcher scripts now do this automatically.");
                            AConsole.errPrintln("#######################################################");
                            AConsole.errPrintln("");
                        }
                    } catch (Exception e) {
                        // A diagnostic must never break the game.
                        AConsole.errPrintln("Could not compare the requested race with the game's: " + e);
                    }
                }
            }
