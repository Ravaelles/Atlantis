package atlantis;

import atlantis.combat.group.AtlantisGroupManager;
import atlantis.constructing.ProtossConstructionManager;
import atlantis.information.AtlantisUnitInformationManager;
import atlantis.init.AtlantisInitialActions;
import atlantis.production.strategies.AtlantisProductionStrategy;
import jnibwapi.BWAPIEventListener;
import jnibwapi.JNIBWAPI;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.RaceType;

/**
 * Main bridge between the game and your code.
 */
public class Atlantis implements BWAPIEventListener {

    /**
     * Singleton instance.
     */
    private static Atlantis instance;
    
    /**
     * JNIBWAPI is core
     */
    private JNIBWAPI bwapi;
    
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
    // Constructors
    /**
     * You have to pass AtlantisConfig object to initialize Atlantis.
     */
    public Atlantis() {

        // Save static reference to this instance, acting as last-singleton
        instance = this;

        // Standard procedure: create and save Jnibwapi reference
        bwapi = new JNIBWAPI(this, true);
    }

    // =========================================================
    // Start / Pause / Unpause
    /**
     * Starts the bot.
     */
    public void start() {
        if (!_isStarted) {
            _isPaused = false;
            _isStarted = true;

            bwapi.start();
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
     * This method returns bridge connector between Atlantis and Starcraft, which is JNIWAPI object. It
     * provides low-level functionality for functions like canBuildHere etc. For more details,
     * see JNIBWAPI project documentation.
     */
    public static JNIBWAPI getBwapi() {
        return instance.bwapi;
    }

    // =========================================================
    
    /**
     * Client (bot) has connected to the game.
     */
    @Override
    public void connected() {
    }

    /**
     * It's executed only once, before the first game frame happens.
     */
    @Override
    public void matchStart() {

        // #### INITIALIZE CONFIG AND PRODUCTION QUEUE ####
        // =========================================================
        // Set up base configuration based on race used.
        RaceType racePlayed = AtlantisGame.getPlayerUs().getRace();
        if (racePlayed.equals(RaceType.RaceTypes.Protoss)) {
            AtlantisConfig.useConfigForProtoss();
        } else if (racePlayed.equals(RaceType.RaceTypes.Terran)) {
            AtlantisConfig.useConfigForTerran();
        } else if (racePlayed.equals(RaceType.RaceTypes.Zerg)) {
            AtlantisConfig.useConfigForZerg();
        }

        // =========================================================
        // Set production strategy (build orders) to use. It can be always changed dynamically.
        AtlantisConfig.useProductionStrategy(AtlantisProductionStrategy.loadProductionStrategy());

        // =========================================================
        // Validate AtlantisConfig and exit if it's invalid
        AtlantisConfig.validate();

        // Display ok message
        System.out.println("Atlantis config is valid.");

        // =========================================================
        gameCommander = new AtlantisGameCommander();
        bwapi.setGameSpeed(AtlantisConfig.GAME_SPEED);
        bwapi.enableUserInput();
    }

    /**
     * It's single time frame, entire logic goes in here. It's executed approximately 25 times per second.
     */
    @Override
    public void matchFrame() {

        // Initial actions - those should be executed only once.
        if (!_initialActionsExecuted) {
            _initialActionsExecuted = true;
            System.out.println("### Starting Atlantis... ###");
            AtlantisInitialActions.executeInitialActions();
            System.out.println("### Atlantis is working! ###");
        }

        // =========================================================
        // If game is running (not paused), proceed with all actions.
        if (!_isPaused) {
            gameCommander.update();

            // =========================================================
            // Game SPEED change using DYNAMIC SLODOWN
            if (AtlantisConfig.USE_DYNAMIC_GAME_SPEED_SLOWDOWN && _dynamicSlowdown_isSlowdownActive) {
                if (_dynamicSlowdown_lastTimeUnitDestroyed + 3 <= AtlantisGame.getTimeSeconds()) {
                    _dynamicSlowdown_isSlowdownActive = false;
                    AtlantisGame.changeSpeed(_dynamicSlowdown_previousSpeed);
                }
            }
        } 

        // =========================================================
        // If game is PAUSED, wait 100ms - pause is handled by Escape button
        else {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // No need to handle
            }
        }
    }
    
    
    /**
     * This is only valid to our units. We have started training a new unit. It exists in the memory, but
     * its unit.isComplete() is false and issuing orders to it has no effect.
     * It's executed only once per unit.
     * @see unitCreate()
     */
    @Override
    public void unitCreate(int unitID) {
        Unit unit = Unit.getByID(unitID);
        if (unit != null) {
            AtlantisUnitInformationManager.rememberUnit(unit);

            // Our unit
            if (unit.getPlayer().isSelf()) {
//                AtlantisUnitInformationManager.addOurUnfinishedUnit(unit.getType());
                AtlantisGame.getProductionStrategy().rebuildQueue();
                
                // Apply construction fix: detect new Protoss buildings and remove them from queue.
                if (AtlantisGame.playsAsProtoss() && unit.isBuilding()) {
                    ProtossConstructionManager.handleWarpingNewBuilding(unit);
                }
            }
        }
    }

    /**
     * This is only valid to our units. 
     * New unit has been completed, it's existing on map. It's executed only once per unit.
     */
    @Override
    public void unitComplete(int unitID) {
        Unit unit = Unit.getByID(unitID);
        if (unit != null) {

            // Our unit
            if (unit.getPlayer().isSelf() && !unit.isLarvaOrEgg()) {
//                AtlantisUnitInformationManager.addOurFinishedUnit(unit.getType());
                AtlantisGroupManager.possibleCombatUnitCreated(unit);
            }
        } else {
            System.err.println("Unit complete is null " + unitID);
        }
    }
    
    /**
     * A unit has been destroyed. It was either our unit or enemy unit. 
     */
    @Override
    public void unitDestroy(int unitID) {

        // We need to get unit by ID, but we need to use our own solution, because normally if we iterated against
        // objects in getAllUnits(), dead unit objects would be gone. But if we manually save them, we can access them
        // at this point, when they're already dead.
        Unit unit = AtlantisUnitInformationManager.getUnitByID(unitID);

        if (unit != null) {
            AtlantisUnitInformationManager.unitDestroyed(unit);

            // Our unit
            if (unit.getPlayer().isSelf()) {
                AtlantisGame.getProductionStrategy().rebuildQueue();
                AtlantisGroupManager.battleUnitDestroyed(unit);
                LOST++;
                LOST_RESOURCES += unit.getType().getTotalResources();
            } else {
                KILLED++;
                KILLED_RESOURCES += unit.getType().getTotalResources();
            }
        }

        // Forever forget this poor unit
        AtlantisUnitInformationManager.forgetUnit(unitID);

        // =========================================================
        // Game SPEED change
        if (AtlantisConfig.USE_DYNAMIC_GAME_SPEED_SLOWDOWN && !_dynamicSlowdown_isSlowdownActive && !unit.isBuilding()) {
            activateDynamicSlowdownMode();
        }
    }

    /**
     * For the first time we have discovered non-our unit. It may be enemy unit, but also 
     * a <b>mineral</b> or a <b>critter</b>.
     */
    @Override
    public void unitDiscover(int unitID) {
        Unit unit = Unit.getByID(unitID);
        if (unit != null) {
            AtlantisUnitInformationManager.rememberUnit(unit);

            // Enemy unit
            if (unit.getPlayer().isEnemy()) {
                AtlantisUnitInformationManager.discoveredEnemyUnit(unit);

                // =========================================================
                // Game SPEED change
//                if (AtlantisConfig.USE_DYNAMIC_GAME_SPEED && !_isSpeedInSlodownMode) {
//                    enableSlowdown();
//                }
            }
        }
    }

    /**
     * Called when unit is hidden by a fog war and it becomes inaccessible by the BWAPI.
     */
    @Override
    public void unitEvade(int unitID) {
    }

    /**
     * Called just as a visible unit is becoming invisible.
     */
    @Override
    public void unitHide(int unitID) {
        Unit unit = Unit.getByID(unitID);
        if (unit != null) {

            // Enemy unit
            if (unit.getPlayer().isEnemy()) {
                AtlantisUnitInformationManager.removeEnemyUnitVisible(unit);
            }
        }
    }

    /**
     * Called when a unit changes its UnitType. 
     * 
     * For example, when a Drone transforms into a Hatchery, a Siege Tank uses Siege Mode, or a Vespene Geyser receives a Refinery.
     */
    @Override
    public void unitMorph(int unitID) {
    }

    /**
     * Called when a previously invisible unit becomes visible.
     */
    @Override
    public void unitShow(int unitID) {
        Unit unit = Unit.getByID(unitID);
        if (unit != null) {

            // Enemy unit
            if (unit.getPlayer().isEnemy()) {
                AtlantisUnitInformationManager.addEnemyUnitVisible(unit);
            }
        }
    }

    /**
     * Unit has been converted and joined the enemy (by Dark Archon).
     */
    @Override
    public void unitRenegade(int unitID) {
    }

    /**
     * Any key has been pressed. Can be used to define key shortcuts.
     */
    @Override
    public void keyPressed(int keyCode) {
        // System.err.println("########################################");
        // System.err.println("############KEY = " + keyCode + "############################");
        // System.err.println("########################################");

        // 27 (Esc) - pause/unpause game
        if (keyCode == 27) {
            pauseOrUnpause();
        } // 107 (+) - increase game speed
        else if (keyCode == 107) {
            AtlantisConfig.GAME_SPEED -= 2;
            if (AtlantisConfig.GAME_SPEED < 0) {
                AtlantisConfig.GAME_SPEED = 0;
            }
            AtlantisGame.changeSpeed(AtlantisConfig.GAME_SPEED);
        } // 109 (-) - decrease game speed
        else if (keyCode == 109) {
            AtlantisConfig.GAME_SPEED += 2;
            AtlantisGame.changeSpeed(AtlantisConfig.GAME_SPEED);
        }
    }

    /**
     * Match has ended. Shortly after that the game will go to the menu.
     */
    @Override
    public void matchEnd(boolean winner) {
        instance = new Atlantis();
    }

    /**
     * Send text using in game chat. Can be used to type in cheats.
     */
    @Override
    public void sendText(String text) {
    }

    /**
     * The other bot or observer has sent a message using game chat.
     */
    @Override
    public void receiveText(String text) {
    }

    /**
     * "Nuclear launch detected".
     */
    @Override
    public void nukeDetect(Position p) {
    }

    /**
     * "Nuclear launch detected". 
     * <b>Guess: I guess in this case we don't know the exact point of it.</b>
     */
    @Override
    public void nukeDetect() {
    }

    /**
     * Other player has left the game.
     */
    @Override
    public void playerLeft(int playerID) {
    }

    /**
     * <b>Not sure, haven't used it, sorry</b>.
     */
    @Override
    public void saveGame(String gameName) {
    }

    /**
     * Other player has been thrown out from the game.
     */
    @Override
    public void playerDropped(int playerID) {
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
        AtlantisGame.changeSpeed(AtlantisConfig.DYNAMIC_GAME_SPEED_SLOWDOWN);
    }

}
