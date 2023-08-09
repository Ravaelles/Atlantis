package atlantis;

import atlantis.combat.squad.NewUnitsToSquadsAssigner;
import atlantis.game.*;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.information.enemy.UnitsArchive;
import atlantis.production.constructing.ProtossConstructionManager;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.production.orders.production.ProductionQueueRebuilder;
import atlantis.units.AUnit;
import atlantis.util.ProcessHelper;
import bwapi.*;

/**
 * Main bridge between the game and your code, ported to BWMirror.
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
//        game.setLocalSpeed(AtlantisConfig.GAME_SPEED);  // Change in-game speed (0 - fastest, 20 - normal)
//        game.setFrameSkip(AtlantisConfig.FRAME_SKIP);   // Number of GUI frames to skip
        game.setGUI(false);                             // Turn off GUI - will speed up game considerably
        game.enableFlag(Flag.UserInput);                // Without this flag you can't control units with mouse
//        game.enableFlag(Flag.CompleteMapInformation);   // See entire map - must be disabled for real games
    }

    /**
     * It's single time frame, entire logic goes in here. It's executed approximately 25 times per second.
     */
    @Override
    public void onFrame() {

        // === Handle PAUSE ================================================
        // If game is paused wait 100ms - pause is handled by PauseBreak button
        while (GameSpeed.isPaused()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }

        // === All game actions that take place every frame ==================================================

        try {
            Atlantis.getInstance().getGameCommander().invoke();
        }

        // === Catch any exception that occur not to "kill" the bot with one trivial error ===================
        catch (Exception e) {
            System.err.println("### AN ERROR HAS OCCURRED ###");
//            throw e;
            e.printStackTrace();
        }

        if (A.notUms() && A.now() == 1) {
            CurrentBuildOrder.get().print();
        }

        OnEveryFrame.handle();
    }

    /**
     * This is only valid to our units. We have started training a new unit. It exists in the memory, but its
     * unit.isComplete() is false and issuing orders to it has no effect. It's executed only once per unit.
     *
     * @see AUnit::unitCreate()
     */
    @Override
    public void onUnitCreate(Unit u) {
        if (u == null) {
            System.err.println("onUnitCreate got null");
            return;
        }

        AUnit unit = AUnit.createFrom(u);

        // Our unit
        if (unit.isOur() && A.now() >= 2) {
            ProductionQueueRebuilder.rebuildProductionQueueToExcludeProducedOrders();

            // Apply construction fix: detect new Protoss buildings and remove them from queue.
            if (AGame.isPlayingAsProtoss() && unit.type().isBuilding()) {
                ProtossConstructionManager.handleWarpingNewBuilding(unit);
            }

            if (unit.isABuilding()) {

            }
        }
    }

    /**
     * This is only valid to our units. New unit has been completed, it's existing on map. It's executed only
     * once per unit.
     */
    @Override
    public void onUnitComplete(Unit u) {
        AUnit unit = AUnit.getById(u);
        if (unit != null) {
            unit.refreshType();
            if (unit.isOur()) {
                ourNewUnit(unit);
            }
        }
        else {
            System.err.println("onUnitComplete null for " + u);
        }
    }

    /**
     * A unit has been destroyed. It was either our unit or enemy unit.
     */
    @Override
    public void onUnitDestroy(Unit u) {

        // Some ums maps have funky stuff happening at the start, exclude first 20 frames
        if (A.now() <= 20) {
            return;
        }

        OnUnitDestroyed.update(AUnit.createFrom(u));
    }

    /**
     * For the first time we have discovered non-our unit. It may be enemy unit, but also a <b>mineral</b> or
     * a <b>critter</b>.
     */
    @Override
    public void onUnitDiscover(Unit u) {
        AUnit unit = AUnit.createFrom(u);
        if (unit != null) {
            OnUnitDiscover.update(unit);
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

    public static void enemyNewUnit(AUnit unit) {
        EnemyUnitsUpdater.weDiscoveredEnemyUnit(unit);
    }

    public static void ourNewUnit(AUnit unit) {
        ProductionQueueRebuilder.rebuildProductionQueueToExcludeProducedOrders();
        (new NewUnitsToSquadsAssigner(unit)).possibleCombatUnitCreated();

//        System.out.println("NEW UNIT @ " + A.now() + " - " + unit);
//        System.out.println(unit.mission());
//        System.out.println(unit.squad());
//        System.out.println(unit.action());
//        System.out.println(unit.lastActionFramesAgo());
    }

    /**
     * Match has ended. Shortly after that the game will go to the menu.
     */
    @Override
    public void onEnd(boolean winner) {
        System.out.println();
        System.out.println("#####################################");
        if (winner) {
            System.out.println("############ VICTORY! ###############");
        }
        else {
            System.out.println("############ Defeat... ##############");
        }
        System.out.println("############ Lost: " + Atlantis.LOST + " ################");
        System.out.println("########## Killed: " + Atlantis.KILLED + " ################");
        System.out.println("#####################################");

        OnEnd.execute(winner);

        exitGame();
    }

    public void exitGame() {
        gameSummary();
        killProcesses();
    }

    private void killProcesses() {
        System.out.println();
        System.out.println("Killing StarCraft process... ");
        ProcessHelper.killStarcraftProcess();

        System.out.print("Killing Chaoslauncher process... ");
        ProcessHelper.killChaosLauncherProcess();

        System.out.println("Exit...");
        System.exit(0);
    }

    private void gameSummary() {
        if (Atlantis.game() == null) {
            return;
        }

        int resourcesBalance = AGame.killsLossesResourceBalance();
        System.out.println();
        System.out.println(
            "### Total time: " + AGame.timeSeconds() + " seconds. ###\r\n" +
                "### Units killed/lost:    " + Atlantis.KILLED + "/" + Atlantis.LOST + " ###\r\n" +
                "### Resource killed/lost: " + (resourcesBalance > 0 ? "+" + resourcesBalance : resourcesBalance) + " ###"
        );

        if (A.isUms()) {
            System.out.println();
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
     * You have to pass AtlantisConfig object to initialize Atlantis.
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
     * This method returns bridge connector between Atlantis and Starcraft, which is a BWMirror object. It
     * provides low-level functionality for functions like canBuildHere etc. For more details, see BWMirror
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
            System.out.print(args[i] + " / ");
        }
        System.out.println(args[args.length - 1]);
    }

}
