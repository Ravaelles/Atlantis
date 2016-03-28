package atlantis;

import atlantis.combat.group.AtlantisGroupManager;
import atlantis.constructing.ProtossConstructionManager;
import atlantis.debug.AtlantisUnitTypesHelper;
import atlantis.enemy.AtlantisEnemyUnits;
import atlantis.information.AtlantisUnitInformationManager;
import atlantis.init.AtlantisInitialActions;
import atlantis.production.strategies.AtlantisProductionStrategy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.UnitUtil;
import bwapi.*;
import bwta.BWTA;
import bwapi.UnitType;

/**
 * Main bridge between the game and your code, ported to BWMirror.
 */
public class Atlantis implements BWEventListener {

    /**
     * Singleton instance.
     */
    private static Atlantis instance;

    /**
     * BWAPI is core
     */
    private static Mirror mirror = new Mirror();
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
    // Constructors
    /**
     * You have to pass AtlantisConfig object to initialize Atlantis.
     */
    public Atlantis() {

        // Save static reference to this instance, acting as last-singleton
        instance = this;

        // Standard procedure: create and save Jnibwapi reference
        //bwapi = new JNIBWAPI(this, true);
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
    
    /**
     * It's executed only once, before the first game frame happens.
     */
    @Override
    public void onStart() {
        bwapi = mirror.getGame();
        bwapi.enableFlag(1);	//FIXME: use the Enum'ed value
        
        // Uncomment this line to see list of units -> damage.
        AtlantisUnitTypesHelper.displayUnitTypesDamage();

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

        System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Map data ready");

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
        System.out.println("gameCommander = " + gameCommander);
        bwapi.setLocalSpeed(AtlantisConfig.GAME_SPEED);
    }

    /**
     * It's single time frame, entire logic goes in here. It's executed approximately 25 times per second.
     */
    @Override
    public void onFrame() {
        System.out.println("&" + AtlantisGame.getTimeFrames());
        
        if (gameCommander == null) {
            gameCommander = new AtlantisGameCommander();
            System.out.println("LOL WHAT DE FUUUUUUUUUCK");
        }
        
        try {
            playerOnFrame();
        } catch (Exception e) {
            System.err.println("### AN ERROR HAS OCCURRED ###");
            e.printStackTrace();
        }

    }

    /**
     * This was the previous onFrame. Now it is wrapped in a try-catch
     */
    private void playerOnFrame() {
        
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
        } // =========================================================
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
            if (unit.getPlayer().equals(bwapi.self())) {
                AtlantisGame.getProductionStrategy().rebuildQueue();

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

            // Our unit
            if (unit.getPlayer().equals(bwapi.self()) && !(unit.getType().equals(AUnitType.Zerg_Larva) 
                    || unit.getType().equals(AUnitType.Zerg_Egg))) {
                AtlantisGroupManager.possibleCombatUnitCreated(unit);
            }
        } else {
            System.err.println("Unit complete is null " + unit.getID());
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
            AtlantisUnitInformationManager.unitDestroyed(unit);

            // Our unit
            if (unit.getPlayer().equals(bwapi.self())) {
                AtlantisGame.getProductionStrategy().rebuildQueue();
                AtlantisGroupManager.battleUnitDestroyed(unit);
                LOST++;
                LOST_RESOURCES += UnitUtil.getTotalPrice(unit.getType());
            } else {
                KILLED++;
                KILLED_RESOURCES += UnitUtil.getTotalPrice(unit.getType());
            }
        }

        // Forever forget this poor unit
        AtlantisUnitInformationManager.forgetUnit(unit.getID());

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
            AtlantisEnemyUnits.unitDestroyed(unit);

            // Our unit
            if (unit.getPlayer().equals(bwapi.self())) {
                AtlantisGame.getProductionStrategy().rebuildQueue();
                AtlantisGroupManager.battleUnitDestroyed(unit);
            } else if (unit.isEnemyUnit()) {
                AtlantisEnemyUnits.discoveredEnemyUnit(unit);
            }
        }

        // Forever forget this poor unit
        AtlantisUnitInformationManager.forgetUnit(unit.getID());

        // =========================================================
        // Remember the unit
        if (unit != null) {

            // Our unit
            if (unit.getPlayer().equals(bwapi.self()) && !(unit.getType().equals(AUnitType.Zerg_Larva)
                    || unit.getType().equals(AUnitType.Zerg_Egg))) {
//                AtlantisUnitInformationManager.addOurFinishedUnit(unit.getType());
                AtlantisGroupManager.possibleCombatUnitCreated(unit);
            }
        }
            
            // Enemy unit
            else if (unit.isEnemyUnit()) {
                AtlantisEnemyUnits.refreshEnemyUnit(unit);
            }
    }

    /**
     * Called when a previously invisible unit becomes visible.
     */
    @Override
    public void onUnitShow(Unit u) {
//        AUnit unit = AUnit.createFrom(u);
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

        // =========================================================
        // 107 (+) - increase game speed
            if (AtlantisConfig.GAME_SPEED > 2) {
                AtlantisConfig.GAME_SPEED -= 10;
            }
            else {
            }
            

        // =========================================================
        // 109 (-) - decrease game speed
            if (AtlantisConfig.GAME_SPEED > 2) {
                AtlantisConfig.GAME_SPEED += 10;
            }
            else {
            }
    }
     */
    /**
     * Match has ended. Shortly after that the game will go to the menu.
     */
    @Override
    public void onEnd(boolean winner) {
        instance = new Atlantis();
        System.out.println("Exiting now...");
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
     * @Override public void nukeDetect() {
    }
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
