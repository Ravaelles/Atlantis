package atlantis;

import atlantis.combat.squad.AtlantisSquadManager;
import atlantis.constructing.AtlantisConstructionManager;
import atlantis.constructing.ConstructionOrder;
import atlantis.constructing.ConstructionOrderStatus;
import atlantis.constructing.ProtossConstructionManager;
import atlantis.enemy.AtlantisEnemyUnits;
import atlantis.init.AtlantisInitialActions;
import atlantis.production.orders.AtlantisBuildOrdersManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.ProcessHelper;
import bwapi.*;
import bwta.BWTA;

/**
 * Main bridge between the game and your code, ported to BWMirror.
 */
public class Atlantis implements BWEventListener {

    /**
     * Singleton instance.
     */
    private static Atlantis instance;

    /**
     * BWMirror core class.
     */
    private static Mirror mirror = new Mirror();

    /**
     * BWMirror game object, contains lo-level methods.
     */
    private Game bwapi;

    /**
     * Top abstraction-level class that governs all units, buildings etc.
     */
    private AtlantisGameCommander gameCommander;

    // =========================================================
    // Other variables
    private boolean _isStarted = false; // Has game been started
    private boolean _isPaused = false; // Is game currently paused
    private boolean _initialActionsExecuted = false; // Have executed one-time actions at match start?

    // =========================================================
    // DYNAMIC SLODOWN game speed adjust - see AtlantisConfig
    // Should we use speed auto-slowdown when fighting
    private boolean _dynamicSlowdown_isSlowdownActive = false;

    // Last time unit has died; when unit dies, game slows down
    private int _dynamicSlowdown_lastTimeUnitDestroyed = 0;

    // Normal game speed, outside autoSlodown mode.
    private int _dynamicSlowdown_previousSpeed = 0;

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
        bwapi = mirror.getGame();

        // Uncomment this line to see list of units -> damage.
//        AtlantisUnitTypesHelper.displayUnitTypesDamage();
        // #### INITIALIZE CONFIG AND PRODUCTION QUEUE ####
        // =========================================================
        // Set up base configuration based on race used.
        Race racePlayed = bwapi.self().getRace(); //AtlantisGame.getPlayerUs().getRace();
        if (racePlayed.equals(Race.Protoss)) {
            AtlantisConfig.useConfigForProtoss();
        } else if (racePlayed.equals(Race.Terran)) {
            AtlantisConfig.useConfigForTerran();
        } else if (racePlayed.equals(Race.Zerg)) {
            AtlantisConfig.useConfigForZerg();
        }

        System.out.print("Analyzing map... ");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Map data ready.");

        // =========================================================
        // Set production strategy (build orders) to use. It can be always changed dynamically.
        AtlantisConfig.useBuildOrders(AtlantisBuildOrdersManager.loadBuildOrders());

        // =========================================================
        // Validate AtlantisConfig and exit if it's invalid
        AtlantisConfig.validate();

        // Display ok message
        System.out.println("Atlantis config is valid.");

        // =========================================================
        gameCommander = new AtlantisGameCommander();
        bwapi.setLocalSpeed(AtlantisConfig.GAME_SPEED);
        bwapi.setFrameSkip(2);
//        bwapi.setGUI(false);
        bwapi.enableFlag(1);	// Enable user input - will be disabled for tournaments
    }

    /**
     * It's single time frame, entire logic goes in here. It's executed approximately 25 times per second.
     */
    @Override
    public void onFrame() {

        // If game is PAUSED, wait 100ms - pause is handled by Escape button
        if (AtlantisGame.isPaused()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // No need to handle
            }
            if (_dynamicSlowdown_lastTimeUnitDestroyed + 3 <= AtlantisGame.getTimeSeconds()) {
                _dynamicSlowdown_isSlowdownActive = false;
                AtlantisGame.changeSpeedTo(_dynamicSlowdown_previousSpeed);
            }
        } //        } 

        // =========================================================
        
        try {
            // Initial actions - those should be executed only once.
            if (!_initialActionsExecuted) {
                _initialActionsExecuted = true;
                System.out.println("### Starting Atlantis... ###");
                AtlantisInitialActions.executeInitialActions();
                System.out.println("### Atlantis is working! ###");
            }

            // =========================================================
            // If game is running (not paused), proceed with all actions.
//        if (!_isPaused) {
            if (gameCommander != null) {
                gameCommander.update();
            }

            // =========================================================
            // Game SPEED change using DYNAMIC SLODOWN
            if (AtlantisConfig.USE_DYNAMIC_GAME_SPEED_SLOWDOWN && _dynamicSlowdown_isSlowdownActive) {
                if (_dynamicSlowdown_lastTimeUnitDestroyed + 3 <= AtlantisGame.getTimeSeconds()) {
                    _dynamicSlowdown_isSlowdownActive = false;
                    AtlantisGame.changeSpeedTo(_dynamicSlowdown_previousSpeed);
                }
            }
        } // Catch any exception that occur not to "kill" the bot with one trivial error
        catch (Exception e) {
            System.err.println("### AN ERROR HAS OCCURRED ###");
            e.printStackTrace();
        }
    }

    /**
     * This is only valid to our units. We have started training a new unit. It exists in the memory, but its
     * unit.isComplete() is false and issuing orders to it has no effect. It's executed only once per unit.
     *
     * @see unitCreate()
     */
    @Override
    public void onUnitCreate(Unit u) {
        AUnit unit = AUnit.createFrom(u);
        if (unit != null) {

            // Our unit
            if (unit.isOurUnit()) {
                AtlantisGame.getBuildOrders().rebuildQueue();

                // Apply construction fix: detect new Protoss buildings and remove them from queue.
                if (AtlantisGame.playsAsProtoss() && unit.getType().isBuilding()) {
                    ProtossConstructionManager.handleWarpingNewBuilding(unit);
                }
            }
        }
    }

    /**
     * This is only valid to our units. New unit has been completed, it's existing on map. It's executed only
     * once per unit.
     */
    @Override
    public void onUnitComplete(Unit u) {
        AUnit unit = AUnit.createFrom(u);
        if (unit != null) {
            AtlantisGame.getBuildOrders().rebuildQueue();

            // Our unit
            if (unit.isOurUnit()) {
                AtlantisSquadManager.possibleCombatUnitCreated(unit);
            }
        }
    }

    /**
     * A unit has been destroyed. It was either our unit or enemy unit.
     */
    @Override
    public void onUnitDestroy(Unit u) {
        AUnit unit = AUnit.createFrom(u);

//        Unit theUnit = AtlantisUnitInformationManager.getUnitDataByID(unit.getID()).getUnit();
        if (unit != null) {
            if (unit.isEnemyUnit()) {
                AtlantisEnemyUnits.unitDestroyed(unit);
            }

            // Our unit
            if (unit.isOurUnit()) {
                AtlantisGame.getBuildOrders().rebuildQueue();
                AtlantisSquadManager.battleUnitDestroyed(unit);
                LOST++;
                LOST_RESOURCES += unit.getType().getTotalResources();
            } else {
                KILLED++;
                KILLED_RESOURCES += unit.getType().getTotalResources();
            }
        }

        // =========================================================
        // Game SPEED change
        if (AtlantisConfig.USE_DYNAMIC_GAME_SPEED_SLOWDOWN
                && !_dynamicSlowdown_isSlowdownActive && !unit.getType().isBuilding()) {
            activateDynamicSlowdownMode();
        }
    }

    /**
     * For the first time we have discovered non-our unit. It may be enemy unit, but also a <b>mineral</b> or
     * a <b>critter</b>.
     */
    @Override
    public void onUnitDiscover(Unit u) {
        AUnit unit = AUnit.createFrom(u);
        if (unit != null) {

            // Enemy unit
            if (unit.isEnemyUnit()) {
                AtlantisEnemyUnits.discoveredEnemyUnit(unit);
            }
        }
    }

    /**
     * Called when unit is hidden by a fog war and it becomes inaccessible by the BWAPI.
     */
    @Override
    public void onUnitEvade(Unit u) {
//        AUnit unit = AUnit.createFrom(u);
    }

    /**
     * Called just as a visible unit is becoming invisible.
     */
    @Override
    public void onUnitHide(Unit u) {
//        AUnit unit = AUnit.createFrom(u);
    }

    /**
     * Called when a unit changes its AUnitType.
     *
     * For example, when a Drone transforms into a Hatchery, a Siege Tank uses Siege Mode, or a Vespene Geyser
     * receives a Refinery.
     */
    @Override
    public void onUnitMorph(Unit u) {
        AUnit unit = AUnit.createFrom(u);

        // A bit of safe approach: forget the unit and remember it again.
        // =========================================================
        // Forget unit
        if (unit != null) {
            if (unit.isOurUnit()) {
                AtlantisSquadManager.battleUnitDestroyed(unit);
            } else {
                AtlantisEnemyUnits.unitDestroyed(unit);
            }
        }

        // =========================================================
        // Remember the unit
        if (unit != null) {
            unit.refreshType();

            // Our unit
            if (unit.isOurUnit()) {

                // === Fix for Zerg Extractor ========================================
                // Detect morphed gas building meaning construction has just started
                if (unit.getType().isGasBuilding()) {
                    for (ConstructionOrder order : AtlantisConstructionManager.getAllConstructionOrders()) {
                        if (order.getBuildingType().equals(AtlantisConfig.GAS_BUILDING)
                                && order.getStatus().equals(ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED)) {
                            order.setConstruction(unit);
                            break;
                        }
                    }
                }

                // =========================================================
                AtlantisGame.getBuildOrders().rebuildQueue();

                // Add to combat squad if it's military unit
                if (unit.isActualUnit()) {
                    AtlantisSquadManager.possibleCombatUnitCreated(unit);
                }
            } // Enemy unit
            else {
                AtlantisEnemyUnits.refreshEnemyUnit(unit);
            }
        }
    }

    /**
     * Called when a previously invisible unit becomes visible.
     */
    @Override
    public void onUnitShow(Unit u) {
        AUnit unit = AUnit.createFrom(u);
        if (unit.isEnemyUnit()) {
            AtlantisEnemyUnits.updateEnemyUnitPosition(unit);
        }
    }

    /**
     * Unit has been converted and joined the enemy (by Dark Archon).
     */
    @Override
    public void onUnitRenegade(Unit u) {
//        AUnit unit = AUnit.createFrom(u);
    }

    /**
     * Any key has been pressed. Can be used to define key shortcuts.
     *
     * @Override public void keyPressed(int keyCode) { //
     * System.err.println("########################################"); // System.err.println("############KEY
     * = " + keyCode + "############################"); //
     * System.err.println("########################################");
     *
     * // 27 (Esc) - pause/unpause game if (keyCode == 27) { pauseOrUnpause(); } // 107 (+) - increase game
     * speed else if (keyCode == 107) { AtlantisConfig.GAME_SPEED -= 2; if (AtlantisConfig.GAME_SPEED < 0) {
     * AtlantisConfig.GAME_SPEED = 0; } AtlantisGame.changeSpeed(AtlantisConfig.GAME_SPEED); } // 109 (-) -
     * decrease game speed else if (keyCode == 109) { AtlantisConfig.GAME_SPEED += 2;
     * AtlantisGame.changeSpeed(AtlantisConfig.GAME_SPEED); }
     *
     * // =========================================================
     * // 107 (+) - increase game speed
     * if (AtlantisConfig.GAME_SPEED > 2) { AtlantisConfig.GAME_SPEED -= 10; } else { }
     *
     *
     * // ========================================================= // 109 (-) - decrease game speed if
     * (AtlantisConfig.GAME_SPEED > 2) { AtlantisConfig.GAME_SPEED += 10; } else { } }
     */
    /**
     * Match has ended. Shortly after that the game will go to the menu.
     */
    @Override
    public void onEnd(boolean winner) {
//        instance = new Atlantis();
        ProcessHelper.killStarcraftProcess();
        ProcessHelper.killChaosLauncherProcess();
        System.out.println();
        System.out.println("Exiting...");
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

            mirror.getModule().setEventListener(this);
            mirror.startGame();
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
    public static Game getBwapi() {
        return getInstance().bwapi;
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

    /**
     * Decreases game speed to the value specified in AtlantisConfig on death of any unit.
     */
    private void activateDynamicSlowdownMode() {
        _dynamicSlowdown_previousSpeed = AtlantisConfig.GAME_SPEED;
        _dynamicSlowdown_lastTimeUnitDestroyed = AtlantisGame.getTimeSeconds();
        _dynamicSlowdown_isSlowdownActive = true;
        AtlantisGame.changeSpeedTo(AtlantisConfig.DYNAMIC_GAME_SPEED_SLOWDOWN);
    }

}
