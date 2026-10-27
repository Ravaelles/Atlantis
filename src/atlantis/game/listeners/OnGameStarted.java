package atlantis.game.listeners;


import atlantis.Atlantis;
import atlantis.cherryvis.ACherryVis;
import atlantis.combat.BattleCommander;
import atlantis.config.AtlantisConfigChanger;
import atlantis.config.AtlantisRaceConfig;
import atlantis.config.env.Env;
import atlantis.debug.painter.APainter;
import atlantis.debug.profiler.RealTime;
import atlantis.debug.tweaker.ParamTweakerFactory;
import atlantis.game.AGame;
import atlantis.game.GameSpeed;
import atlantis.game.init.AInitialActions;
import atlantis.game.init.DetectInitialTechs;
import atlantis.game.race.EnemyRace;
import atlantis.information.generic.InitialMainPosition;
import atlantis.information.strategy.Strategy;
import atlantis.information.strategy.StrategyChooser;
import atlantis.information.strategy.ZergStrategies;
import atlantis.information.strategy.protoss.ProtossStrategies;
import atlantis.information.strategy.terran.TerranStrategies;
import atlantis.map.AMap;
import atlantis.map.scout.ScoutState;
import atlantis.production.orders.build.BuildOrderSettings;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.production.orders.production.queue.QueueInitializer;
import atlantis.units.select.Select;
import atlantis.util.AConsole;
import atlantis.util.log.ErrorLog;
import atlantis.util.log.LogLevel;
import benchmark.BenchmarkMode;
import bwapi.Race;
import main.Main;

public class OnGameStarted {

    public static void execute() {

        System.out.println("\n########### Starting Atlantis... #################");

        // Uncomment this line to see list of units -> damage.

        APainter.assignBwapiInstance();

        // Per-game state that must not leak between games (or between tests):
        // the early-scout tracker holds a defender assignment in statics, and a
        // leftover one made the bot treat a previous game's Probe as "still my
        // defender" (owner report, 2026-10-08). The end-of-game latch is the same
        // kind of state: without the reset a first, spurious onEnd disarms every
        // later one and the real end-of-game cleanup never runs (NEXT #49).
        atlantis.units.workers.defence.proxy.TrackEnemyEarlyScoutCommander.clear();
        atlantis.game.listeners.OnGameEnd.reset();
        atlantis.map.scout.ScoutState.reset();
        BattleCommander.reset();
        atlantis.battle.cluster.Missions.reset();

        // Per-game state with test-shared seams: a new game is a fresh
        // world, so unit-id-keyed deltas, throttle windows and audit rows
        // from the previous one must not leak in (same-JVM multi-game and
        // twin tests read them). Each seam is behavior-neutral on a fresh
        // game - it only drops stale entries.
        atlantis.production.orders.production.queue.add.AddToQueue.clearForTests();
        atlantis.production.requests.produce.ProduceBuilding.clearRefusedThrottleForTests();
        atlantis.combat.twins.HitTwin.clearForTests();
        atlantis.combat.twins.ShotTwin.clearForTests();
        atlantis.game.listeners.WarpedBuildingRecorder.clearForTests();
        atlantis.game.util.BuildOrderAudit.clearForTests();

        // What we built is per-game, like everything else here: as a plain static it would
        // leak one game's completion history into the next in the same JVM.
        atlantis.game.util.CompletedUnitsHistory.reset();

        // And so are the KILL/LOSS archives. `UnitsArchive.reset()` has existed with a javadoc
        // explaining exactly why it is needed ("the test harness runs many games in one JVM",
        // "a base that died in one test is still recently lost in the next") - and nothing
        // called it, so that leak was live. Found by an end-of-game test that expected one lost
        // Zealot and read four, the other three left over from earlier tests.
        atlantis.information.enemy.UnitsArchive.reset();

        handleCheckIfUmsMap();

        System.out.println("### Map: " + Atlantis.game().mapFileName() + ", enemy: " + AGame.enemyName());

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

        // Single decision door: every issued production decision executes
        // through the domain executor from here on (__PLAN-DECISIONS).
        atlantis.production.ProductionDecisionExecutor.register();

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

        // One-shot map snapshot for the StarEngine map library
        // (CONVENTIONS section 30, NEXT #191): MAP_SNAPSHOT=1 in ENV
        // writes out/map-snapshots/<Map>.json once, then the game
        // continues normally. Never throws into the startup path.
        if (atlantis.config.env.Env.isMapSnapshot()) {
            try {
                atlantis.map.snapshot.MapSnapshotDumper.dumpOnce();
            } catch (Throwable ignored) {
            }
        }

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


        System.out.println("### Atlantis is working! ###\n");
        if (Env.isTournament()) {
            String opponent = AGame.enemyName();
            System.out.println("### Playing against: " + opponent + " ###\n");
        }


        if (ACherryVis.isEnabled()) ACherryVis.initialize();
        if (ACherryVis.isEnabled()) {
            atlantis.cherryvis.simple.ACherryVis_UnitFirstSeen.seedCurrentUnits();
        }
        if (Env.isParamTweaker()) ParamTweakerFactory.init();
        if (Env.isBenchmark()) BenchmarkMode.onGameStarted();

        RealTime.gameStarted = RealTime.currentTimestamp();
    }

    private static void handleCheckIfUmsMap() {


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

            // Same bridge for combat (owner order 2026-10-26): the first-Dragoon
            // gate holds the defending line at the main vs Zerg, but only for a
            // build that trains Dragoons - combat must not read production, so
            // the build's intent is read once, here, and handed over as a flag.
            atlantis.combat.cluster.FirstDragoonGate.setBuildPlansDragoons(
                CurrentBuildOrder.plansDragoon());

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
            AConsole.errPrintln(LogLevel.HIGH, "Could not load build order "
                + (CurrentBuildOrder.get() != null ? "`" + CurrentBuildOrder.get().getName() + "`" : "(none set)")
                + " for strategy `" + Strategy.current() + "`: " + e);
            AConsole.errPrintln(LogLevel.HIGH, "Build orders are resolved against BWAPI_DATA_PATH"
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
                AConsole.errPrintln(LogLevel.HIGH, "");
                AConsole.errPrintln(LogLevel.HIGH, "#######################################################");
                AConsole.errPrintln(LogLevel.HIGH, "RACE MISMATCH: the game started us as " + inGameName
                    + " but Main.ourRace() asks for " + requested + ".");
                AConsole.errPrintln(LogLevel.HIGH, "Every race-specific branch follows the GAME's race, so "
                    + requested + " code will not run at all");
                AConsole.errPrintln(LogLevel.HIGH, "(no units will be produced). Fix bwapi.ini's race= to match"
                    + " - the launcher scripts now do this automatically.");
                AConsole.errPrintln(LogLevel.HIGH, "#######################################################");
                AConsole.errPrintln(LogLevel.HIGH, "");
            }
        } catch (Exception e) {
            // A diagnostic must never break the game.
            AConsole.errPrintln(LogLevel.HIGH, "Could not compare the requested race with the game's: " + e);
        }
    }
}
