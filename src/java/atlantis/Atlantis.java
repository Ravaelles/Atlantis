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

public class Atlantis implements BWAPIEventListener {

    private static Atlantis instance;
    private JNIBWAPI bwapi;
    private AtlantisGameCommander gameCommander;

    // =========================================================
    // Other variables
    private boolean isStarted = false;
    private boolean isPaused = false;
    private boolean oneTimeBoolean = false;

    // =========================================================
    // Dynamic game speed adjust - see AtlantisConfig
    private boolean _isSpeedInSlodownMode = false;
    private int _lastTimeUnitDestroyed = 0;
    private int _previousSpeed = 0;

    // =========================================================
    // Counters
    public static int KILLED = 0;
    public static int LOST = 0;
    public static int KILLED_RESOURCES = 0;
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
        if (!isStarted) {
            isPaused = false;
            isStarted = true;

            bwapi.start();
        }
    }

    /**
     * Forces all calculations to be stopped. CPU usage should be minimal. Or resumes the game after pause.
     */
    public void pauseOrUnpause() {
        isPaused = !isPaused;
    }

    // =========================================================
    /**
     * This method returns bridge connector between Atlantis and Starcraft, which is JNIWAPI object. It
     * provides low-level functionality for functions like canBuildHere etc.
     */
    public static JNIBWAPI getBwapi() {
        return instance.bwapi;
    }

    // =========================================================
    @Override
    public void connected() {
    }

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

    @Override
    public void matchFrame() {

        // Initial actions
//        if (!oneTimeBoolean && RUtilities.rand(0, 100) <= 1) {
        if (!oneTimeBoolean) {
            oneTimeBoolean = true;
            System.out.println("### Starting Atlantis... ###");
            AtlantisInitialActions.executeInitialActions();
            System.out.println("### Atlantis is working! ###");
        }

        // =========================================================
        // If game is running (not paused), run all actions.
        if (!isPaused) {
            gameCommander.update();

            // =========================================================
            // Game SPEED change
            if (AtlantisConfig.USE_DYNAMIC_GAME_SPEED_SLOWDOWN && _isSpeedInSlodownMode) {
                if (_lastTimeUnitDestroyed + 3 <= AtlantisGame.getTimeSeconds()) {
                    _isSpeedInSlodownMode = false;
                    AtlantisGame.changeSpeed(_previousSpeed);
                }
            }
        } // =========================================================
        // If game is paused, wait 100ms.
        else {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // No need to handle
            }
        }
    }

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

    @Override
    public void matchEnd(boolean winner) {
        instance = new Atlantis();
    }

    @Override
    public void sendText(String text) {
    }

    @Override
    public void receiveText(String text) {
    }

    @Override
    public void nukeDetect(Position p) {
    }

    @Override
    public void nukeDetect() {
    }

    @Override
    public void playerLeft(int playerID) {
    }

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
        if (AtlantisConfig.USE_DYNAMIC_GAME_SPEED_SLOWDOWN && !_isSpeedInSlodownMode && !unit.isBuilding()) {
            enableSlowdown();
        }
    }

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

    @Override
    public void unitEvade(int unitID) {
    }

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

    @Override
    public void unitMorph(int unitID) {
//        Unit unit = Unit.getByID(unitID);
//        if (unit != null) {
//            AtlantisGroupManager.possibleCombatUnitCreated(unit);
//        } else {
//            System.err.println("Morph is null for id " + unitID);
//        }
    }

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

    @Override
    public void unitRenegade(int unitID) {
    }

    @Override
    public void saveGame(String gameName) {
    }

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

    private void enableSlowdown() {
        _previousSpeed = AtlantisConfig.GAME_SPEED;
        _lastTimeUnitDestroyed = AtlantisGame.getTimeSeconds();
        _isSpeedInSlodownMode = true;
        AtlantisGame.changeSpeed(AtlantisConfig.DYNAMIC_GAME_SPEED_SLOWDOWN);
    }

}
