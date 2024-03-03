package atlantis;

import atlantis.config.AtlantisConfig;
import atlantis.config.env.Env;
import atlantis.debug.profiler.LongFrames;
import atlantis.game.*;
import atlantis.game.events.*;
import atlantis.information.enemy.UnitsArchive;
import atlantis.units.AUnit;
import atlantis.util.ProcessHelper;
import bwapi.*;

/**
 * Main bridge between the game and your code, ported to JBWAPI.
 */
public class Atlantis implements BWEventListener {

    /**
     * Singleton instance.
     */
    private static Atlantis instance;

    /**
     * JBWAPI core class.
     */
    private static BWClient bwClient;

    /**
     * JBWAPI's game object, contains low-level methods.
     */
    private Game game;

    /**
     * Class controlling game speed.
     */
    private static GameSpeed gameSpeed;

    /**
     * Top abstraction-level class that governs all units, buildings etc.
     */
    private AtlantisGameCommander gameCommander;

    // =========================================================
    // Other variables
    private boolean _isStarted = false; // Has game been started
    private boolean _isPaused = false; // Is game currently paused
    private final boolean _initialActionsExecuted = false; // Have executed one-time actions at match start?

    // =========================================================
    // Counters
    /**
     * How many units we have killed.
     */
    public static int KILLED = 0;

    /**
     * How many units we have lost.
     */
    public static int LOST = 0;

    /**
     * How many resources (minerals+gas) units we have killed were worth.
     */
    public static int KILLED_RESOURCES = 0;

    /**
     * How many resources (minerals+gas) units we have lost were worth.
     */
    public static int LOST_RESOURCES = 0;

    // =========================================================

    /**
     * It's executed only once, before the first game frame happens.
     */
    @Override
    public void onStart() {

        // Initialize game object - JBWAPI's representation of game and its state.
        setGame(bwClient.getGame());

        // Initialize Game Commander, a class to rule them all
        gameCommander = new AtlantisGameCommander();

        // Allow user input etc
        setBwapiFlags();

        // =========================================================

        OnStart.execute();
    }

    private void setBwapiFlags() {
//        game.setLocalSpeed(AtlantisRaceConfig.GAME_SPEED);  // Change in-game speed (0 - fastest, 20 - normal)
//        game.setFrameSkip(AtlantisRaceConfig.FRAME_SKIP);   // Number of GUI frames to skip
        game.setGUI(!AtlantisConfig.DISABLE_GUI);             // Turn off GUI - speeds up game considerably
        game.enableFlag(Flag.UserInput);                      // Without this flag you can't control units with mouse
//        game.enableFlag(Flag.CompleteMapInformation);       // See entire map - must be disabled for real games
    }

    /**
     * It's single time frame, entire logic goes in here. It's executed approximately 25 times per second.
     */
    @Override
    public void onFrame() {
        OnEveryFrame.update();
    }

    /**
     * This is only valid to our units. We have started training a new unit. It exists in the memory, but its
     * unit.isComplete() is false and issuing orders to it has no effect. It's executed only once per unit.
     *
     * @see AUnit::unitCreate()
     */
    @Override
    public void onUnitCreate(Unit u) {
        OnUnitCreated.onUnitCreated(u);
    }

    /**
     * This is only valid to our units. New unit has been completed, it's existing on map. It's executed only
     * once per unit.
     */
    @Override
    public void onUnitComplete(Unit u) {
        OnUnitCompleted.onUnitCompleted(u);
    }

    /**
     * A unit has been destroyed. It was either our unit or enemy unit.
     */
    @Override
    public void onUnitDestroy(Unit u) {
        OnUnitDestroyed.onUnitDestroyed(AUnit.createFrom(u));
    }

    /**
     * For the first time we have discovered non-our unit. It may be enemy unit, but also a <b>mineral</b> or
     * a <b>critter</b>.
     */
    @Override
    public void onUnitDiscover(Unit u) {
        AUnit unit = AUnit.createFrom(u);
        if (unit != null) {
            if (unit.isEnemy()) OnEnemyNewUnitDiscovered.update(unit);
//            if (!unit.isRealUnit() && !unit.type().isInvincible()) {
        }
    }

    /**
     * Called when unit is hidden by a fog war and it becomes inaccessible by the BWAPI.
     */
    @Override
    public void onUnitEvade(Unit u) {
//        AUnit unit = AUnit.getById(u);
//        if (unit.isEnemy()) {
//            EnemyUnitsUpdater.updateUnitTypeAndPosition(unit);
//        }
    }

    /**
     * Called just as a visible unit is becoming invisible.
     */
    @Override
    public void onUnitHide(Unit u) {
//        AUnit unit = AUnit.getById(u);
//        if (unit.isEnemy()) {
//            EnemyUnitsUpdater.updateUnitTypeAndPosition(unit);
//        }
    }

    /**
     * Called when a unit changes its AUnitType.
     * <p>
     * For example, when a Drone transforms into a Hatchery, a Siege Tank uses Siege Mode, or a Vespene Geyser
     * receives a Refinery.
     */
    @Override
    public void onUnitMorph(Unit u) {
        AUnit unit = AUnit.getById(u);
        OnUnitMorph.update(unit);
    }

    /**
     * Called when a previously invisible unit becomes visible.
     */
    @Override
    public void onUnitShow(Unit u) {
//        AUnit unit = AUnit.getById(u);
//        if (unit.isEnemy()) {
//            EnemyUnits.updateEnemyUnitPosition(unit);
//        }
    }

    /**
     * Unit has been converted and joined the enemy (by Dark Archon).
     */
    @Override
    public void onUnitRenegade(Unit u) {
        onUnitDestroy(u);
        AUnit newUnit = AUnit.createFrom(u);
        OnUnitRenegade.update(newUnit);
    }

    /**
     * Match has ended. Shortly after that the game will go to the menu.
     */
    @Override
    public void onEnd(boolean winner) {
        if (Env.isTesting()) {
            exitGame();
            return;
        }

        String result = "#####################################\n";
        result += "############ " + (winner ? "VICTORY!" : "Defeat...") + " ###############\n";
        result += "############ Lost: " + Atlantis.LOST + " ################\n";
        result += "########## Killed: " + Atlantis.KILLED + " ################\n";
        result += "#####################################\n";

        LongFrames.printSummary();

        A.println(result);

        OnEnd.execute(winner);

        if (Env.isLocal()) exitGame();
    }

    public void exitGame() {
        if (!Env.isTesting()) gameSummary();
        killProcesses();
    }

    private void killProcesses() {
        A.println("\nKilling StarCraft process... ");
        ProcessHelper.killStarcraftProcess();

        A.println("Killing Chaoslauncher process... ");
        ProcessHelper.killChaosLauncherProcess();

        A.println("Exit...");
        System.exit(0);
    }

    private void gameSummary() {
        if (Atlantis.game() == null) {
            return;
        }

        int resourcesBalance = AGame.killsLossesResourceBalance();
        A.println(
            "\n### Total time: " + AGame.timeSeconds() + " seconds. ###\r\n" +
                "### Units killed/lost:    " + Atlantis.KILLED + "/" + Atlantis.LOST + " ###\r\n" +
                "### Resource killed/lost: " + (resourcesBalance > 0 ? "+" + resourcesBalance : resourcesBalance) + " ###"
        );

        if (A.isUms()) {
            UnitsArchive.paintLostUnits();
            UnitsArchive.paintKilledUnits();
        }
        UnitsArchive.paintKillLossResources();
    }

    /**
     * Send text using in game chat. Can be used to type in cheats.
     */
    @Override
    public void onSendText(String text) {
    }

    /**
     * The other bot or observer has sent a message using game chat.
     */
    @Override
    public void onReceiveText(Player player, String s) {

    }

    /**
     * "Nuclear launch detected".
     */
    @Override
    public void onNukeDetect(Position p) {
    }

    /**
     * "Nuclear launch detected".
     * <b>Guess: I guess in this case we don't know the exact point of it.</b>
     *
     * @Override public void nukeDetect() { }
     */
    /**
     * Other player has left the game.
     */
    @Override
    public void onPlayerLeft(Player player) {
    }

    /**
     * <b>Not sure, haven't used it, sorry</b>.
     */
    @Override
    public void onSaveGame(String gameName) {
    }

    /**
     * Other player has been thrown out from the game.
     */
    @Override
    public void onPlayerDropped(Player player) {
    }

    // =========================================================
    // Constructors

    /**
     * You have to pass AtlantisRaceConfig object to initialize Atlantis.
     */
    public Atlantis() {
        instance = this; // Save static reference to this instance, act like a singleton.
    }

    /**
     * Returns the current Atlantis instance, useful for retrieving game information
     *
     * @return
     */
    public static Atlantis getInstance() {
        if (instance == null) {
            instance = new Atlantis();
        }
        return instance;
    }

    // =========================================================
    // Start / Pause / Unpause

    /**
     * Starts the bot.
     */
    public void run() {
        if (!_isStarted) {
            _isPaused = false;
            _isStarted = true;

            bwClient = new BWClient(this);
            bwClient.startGame();
        }
    }

    /**
     * Forces all calculations to be stopped. CPU usage should be minimal. Or resumes the game after pause.
     */
    public void pauseOrUnpause() {
        _isPaused = !_isPaused;
    }

    // =========================================================

    /**
     * This method returns bridge connector between Atlantis and Starcraft, which is a JBWAPI object. It
     * provides low-level functionality for functions like canBuildHere etc. For more details, see JBWAPI
     * project documentation.
     */
    public static Game game() {
        return getInstance().game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public AtlantisGameCommander getGameCommander() {
        return gameCommander;
    }

    // =========================================================
    // Utility / Axuliary methods

    /**
     * This is convenience that takes any number of arguments and displays them in one line.
     */
    public static void debug(Object... args) {
        for (int i = 0; i < args.length - 1; i++) {
            (System.out).print(args[i] + " / ");
        }
        (System.out).println(args[args.length - 1]);
    }

}
