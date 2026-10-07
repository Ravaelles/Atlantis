package atlantis;

import atlantis.core.world.Worlds;
import atlantis.config.AtlantisConfig;
import atlantis.config.env.Env;
import atlantis.game.*;
import atlantis.game.event.AutoRegisterEventListeners;
import atlantis.game.event.Event;
import atlantis.game.event.Events;
import atlantis.game.listeners.*;
import atlantis.units.AUnit;
import atlantis.util.AConsole;
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
    public static int KILLED_BUILDINGS = 0;

    /**
     * How many units we have lost.
     */
    public static int LOST = 0;
    public static int LOST_BUILDINGS = 0;

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

        OnGameStarted.execute();

        // HELLO_ATLANTIS is the owner's connection marker: when this line
        // prints, the BWAPI client is attached and the bot is actually playing.
        // The Wine launcher kills SC+Chaos when it does NOT appear in time
        // (see WineClientSupervisor), so silence has a loud consequence.
        AConsole.println("HELLO_ATLANTIS - BWAPI attached, Atlantis is playing!");

        // Force the game out of StarCraft's pause. Without this a fresh game
        // sits paused until a human presses a key (owner report, twice:
        // "the game starts paused, you have to press control"), and the bot
        // then does nothing while looking perfectly attached.
        //
        // Why the bot can fix it and why it was left to a keypress before:
        // BWAPI exposes resumeGame() (CommandType.ResumeGame) while
        // pauseGame()/isPaused() only ever PAUSE - there is no "setPaused(false)"
        // call, so the pause has to be lifted with this command. Measured from
        // the vendored jar.
        //
        // Doing it here (onStart) is the earliest moment the game exists for the
        // client, and it is the same moment HELLO_ATLANTIS is printed, so the
        // owner's marker now means "attached AND running" rather than
        // "attached, maybe still paused". unpauseIfPaused() also runs on the
        // first frames (see onFrame) because Wine can re-pause on focus loss.
        unpauseIfPaused("onStart");
    }

    /**
     * Lifts StarCraft's pause if the game is paused. Idempotent by design: it is
     * called on start and for the first frames, and issuing resumeGame() when
     * the game is already running is harmless but noisy in the log.
     */
    private void unpauseIfPaused(String where) {
        try {
            if (game != null && game.isPaused()) {
                game.resumeGame();
                AConsole.println("Game was paused - sent ResumeGame (" + where + ")");
            }
        } catch (Exception e) {
            // A resume that fails must never take the bot down: the game may
            // simply not be interactive yet on this frame.
            AConsole.println("Could not resume game (" + where + "): " + e.getClass().getSimpleName());
        }
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
        // Wine (and any windowed run) can pause StarCraft when the window loses
        // focus, which happens right after startup while the desktop is being
        // arranged. Keep unpausing through the first seconds so the game cannot
        // sit paused with the bot attached to it (owner report, twice).
        if (game != null && game.getFrameCount() <= 120) {
            unpauseIfPaused("frame " + game.getFrameCount());
        }

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
        OnUnitCompleted.update(u);
    }

    /**
     * A unit has been destroyed. It was either our unit or enemy unit.
     */
    @Override
    public void onUnitDestroy(Unit u) {
        OnUnitDestroyed.onUnitDestroyed(Worlds.units().createFrom(u));
    }

    /**
     * For the first time we have discovered non-our unit. It may be enemy unit, but also a <b>mineral</b> or
     * a <b>critter</b>.
     */
    @Override
    public void onUnitDiscover(Unit u) {
        AUnit unit = Worlds.units().createFrom(u);
        if (unit != null) {
            if (unit.isEnemy()) OnEnemyNewUnitDiscovered.update(unit);
            else if (unit.isNeutral()) OnNeutralNewUnitDiscovered.update(unit);
//            if (!unit.isRealUnit() && !unit.type().isInvincible()) {

            Events.dispatch(Event.UNIT_DISCOVERED, unit);
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
        AUnit unit = Worlds.units().getById(u);
        OnUnitMorph.update(unit);
    }

    /**
     * Called when a previously invisible unit becomes visible.
     */
    @Override
    public void onUnitShow(Unit u) {
        OnEnemyUnitShow.update(Worlds.units().createFrom(u));
    }

    /**
     * Unit has been converted and joined the enemy (by Dark Archon).
     */
    @Override
    public void onUnitRenegade(Unit u) {
        onUnitDestroy(u);
        AUnit newUnit = Worlds.units().createFrom(u);
        OnUnitRenegade.update(newUnit);
    }

    /**
     * Match has ended. Shortly after that the game will go to the menu.
     */
    @Override
    public void onEnd(boolean winner) {
        OnGameEnd.execute(winner);
    }

    public void exitGame(boolean winner) {
        killProcesses();
    }

    private void killProcesses() {
        if (Env.isOpenBW()) {
            // The OpenBW twin of the Wine/Windows exit path: kill the game host
            // and clear its shared state, then exit. Without this branch the
            // code fell through to the Windows taskkill calls, which do not
            // exist on Linux (IOException on every exit, measured 2026-10-06).
            AConsole.println("\nKilling OpenBW game processes... ");
            ProcessHelper.killOpenBWProcesses();

            AConsole.println("Exit...");
            System.exit(0);
            return;
        }

        if (Env.isWineClient()) {
            // This JVM is the bot under Wine: the game must die with it too,
            // win or lose - the owner's rule is that the end of the match is
            // the end of the process tree. killWineHostProcesses runs on the
            // host through sh, so it works from inside Wine as well.
            AConsole.println("\nKilling Wine game processes... ");
            ProcessHelper.killWineHostProcesses();

            AConsole.println("Exit...");
            System.exit(0);
            return;
        }

        if (Env.isWine()) {
            // There is no taskkill on Linux. Saying "Killing StarCraft process"
            // and then shelling out to taskkill is what produced the
            // IOException in the exit path; on Wine the game host is killed by
            // ProcessHelper on the line below.
            AConsole.println("\nKilling Wine game processes... ");
            ProcessHelper.killWineProcessesIfOnWine();

            AConsole.println("Exit...");
            System.exit(0);
            return;
        }

        AConsole.println("\nKilling StarCraft process... ");
        ProcessHelper.killStarcraftProcess();

        AConsole.println("Killing Chaoslauncher process... ");
        ProcessHelper.killChaosLauncherProcess();

        AConsole.println("Exit...");
        System.exit(0);
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
    private Atlantis() {
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
            AutoRegisterEventListeners.initializeListeners();
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
