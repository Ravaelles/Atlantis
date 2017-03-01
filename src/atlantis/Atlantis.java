package atlantis;

import atlantis.combat.squad.ASquadManager;
import atlantis.constructing.AConstructionManager;
import atlantis.constructing.ConstructionOrder;
import atlantis.constructing.ConstructionOrderStatus;
import atlantis.constructing.ProtossConstructionManager;
import atlantis.enemy.AEnemyUnits;
import atlantis.information.AOurUnitsExtraInfo;
import atlantis.init.AInitialActions;
import atlantis.production.orders.ABuildOrderLoader;
import atlantis.production.orders.ABuildOrderManager;
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
    private AGameCommander gameCommander;

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
        
        // Initialize bwapi object - BWMirror wrapper of C++ BWAPI.
        bwapi = mirror.getGame();
        
        // Initialize Game Commander, a class to rule them all
        gameCommander = new AGameCommander();

        // Uncomment this line to see list of units -> damage.
//        AtlantisUnitTypesHelper.displayUnitTypesDamage();

        // #### INITIALIZE CONFIG AND PRODUCTION QUEUE ####
        // =========================================================
        // Set up base configuration based on race used.
        Race racePlayed = bwapi.self().getRace(); //AGame.getPlayerUs().getRace();
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
        
        // === Set some BWAPI params ===============================
        
        bwapi.setLocalSpeed(AtlantisConfig.GAME_SPEED); // Change in-game speed (0 - fastest, 20 - normal)
//        bwapi.setFrameSkip(2); // Number of GUI frames to skip
//        bwapi.setGUI(false); // Turn off GUI - will speed up game considerably
        bwapi.enableFlag(1);	// Enable user input - without it you can't control units with mouse

        // =========================================================
        // Set production strategy (build orders) to use. It can be always changed dynamically.
        
        try {
            ABuildOrderManager.switchToBuildOrder(AtlantisConfig.DEFAULT_BUILD_ORDER);
            
            System.out.println();
            if (ABuildOrderManager.getCurrentBuildOrder() != null) {
                System.out.println("Use build order: " + ABuildOrderManager.getCurrentBuildOrder().getName());
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
        
//        if (AtlantisConfig.buildOrdersManager == null) {
//            System.err.println("===================================");
//            System.err.println("It seems there was critical problem");
//            System.err.println("with build orders file.");
//            System.err.println("Please check the syntax.");
//            System.err.println("===================================");
//            System.exit(-1);
//        }
//        else {
//            System.out.println("Successfully loaded build orders file!");
//            System.out.println();
//        }

        // =========================================================
        // Validate AtlantisConfig and exit if it's invalid
        AtlantisConfig.validate();

        // Display ok message
        System.out.println("Atlantis config is valid.");
        System.out.println();
    }

    /**
     * It's single time frame, entire logic goes in here. It's executed approximately 25 times per second.
     */
    @Override
    public void onFrame() {

        // === Handle PAUSE ================================================
        // If game is paused wait 100ms - pause is handled by PauseBreak button
        while (AGame.isPaused()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // No need to handle
            }
        } 

        // === All game actions that take place every frame ==================================================
        
        try {
            
            // Initial actions - those should be executed only once (optimally assign mineral gatherers).
            if (!_initialActionsExecuted) {
                
                System.out.println("### Starting Atlantis... ###");
                AInitialActions.executeInitialActions();
                System.out.println("### Atlantis is working! ###");
                _initialActionsExecuted = true;
            }

            // =========================================================
            // If game is running (not paused), proceed with all actions.
            if (gameCommander != null) {
                gameCommander.update();
            }
            else {
                System.err.println("Game Commander is null, totally screwed.");
            }
            
            return;
        } 

        // === Catch any exception that occur not to "kill" the bot with one trivial error ===================
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
            unit.removeTooltip();

            // Our unit
            if (unit.isOurUnit()) {
                ABuildOrderManager.rebuildQueue();

                // Apply construction fix: detect new Protoss buildings and remove them from queue.
                if (AGame.playsAsProtoss() && unit.getType().isBuilding()) {
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
            unit.refreshType();
            
            ABuildOrderManager.rebuildQueue();

            // Our unit
            if (unit.isOurUnit()) {
                ASquadManager.possibleCombatUnitCreated(unit);
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
        AUnit unit = AUnit.createFrom(u);

//        Unit theUnit = AtlantisUnitInformationManager.getUnitDataByID(unit.getID()).getUnit();
        if (unit != null) {
            if (unit.isEnemyUnit()) {
                AEnemyUnits.unitDestroyed(unit);
            }
            else {
                AOurUnitsExtraInfo.idsOfOurDestroyedUnits.add(unit.getID());
//                System.err.println(unit.getID() + " destroyed [*]");
            }

            // Our unit
            if (unit.isOurUnit()) {
                ABuildOrderManager.rebuildQueue();
                ASquadManager.battleUnitDestroyed(unit);
                LOST++;
                LOST_RESOURCES += unit.getType().getTotalResources();
            } else {
                KILLED++;
                KILLED_RESOURCES += unit.getType().getTotalResources();
            }
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
                AEnemyUnits.discoveredEnemyUnit(unit);
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
                ASquadManager.battleUnitDestroyed(unit);
            } else {
                AEnemyUnits.unitDestroyed(unit);
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
                    for (ConstructionOrder order : AConstructionManager.getAllConstructionOrders()) {
                        if (order.getBuildingType().equals(AtlantisConfig.GAS_BUILDING)
                                && order.getStatus().equals(ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED)) {
                            order.setConstruction(unit);
                            break;
                        }
                    }
                }

                // =========================================================
                ABuildOrderManager.rebuildQueue();

                // Add to combat squad if it's military unit
                if (unit.isActualUnit()) {
                    ASquadManager.possibleCombatUnitCreated(unit);
                }
            } // Enemy unit
            else {
                AEnemyUnits.refreshEnemyUnit(unit);
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
            AEnemyUnits.updateEnemyUnitPosition(unit);
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
     * AtlantisConfig.GAME_SPEED = 0; } AGame.changeSpeed(AtlantisConfig.GAME_SPEED); } // 109 (-) -
     * decrease game speed else if (keyCode == 109) { AtlantisConfig.GAME_SPEED += 2;
     * AGame.changeSpeed(AtlantisConfig.GAME_SPEED); }
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
        _dynamicSlowdown_lastTimeUnitDestroyed = AGame.getTimeSeconds();
        _dynamicSlowdown_isSlowdownActive = true;
    }
    
}
