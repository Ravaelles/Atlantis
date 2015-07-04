package atlantis;

import jnibwapi.BWAPIEventListener;
import jnibwapi.JNIBWAPI;
import jnibwapi.Position;
import jnibwapi.Unit;
import atlantis.information.AtlantisInformationCommander;
import atlantis.init.AtlantisInitActions;
import atlantis.util.RUtilities;

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
	// Constructors

	/**
	 * You have to pass AtlantisConfig object to initialize Atlantis.
	 */
	public Atlantis() {

		// Save static reference to this instance, acting as last-singleton
		instance = this;

		// Standard procedure: create and save Jnibwapi reference
		bwapi = new JNIBWAPI(this, false);

		// Validate AtlantisConfig and exit if it's invalid
		AtlantisConfig.validate();

		// Display ok message
		System.out.println("Atlantis config is valid.");
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
	 * This method returns bridge connector between Atlantis and Starcraft, which is JNIWAPI object. It provides
	 * low-level functionality for functions like canBuildHere etc.
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
		gameCommander = new AtlantisGameCommander();
		bwapi.setGameSpeed(AtlantisConfig.GAME_SPEED);
		bwapi.enableUserInput();
	}

	@Override
	public void matchFrame() {
		if (!oneTimeBoolean && RUtilities.rand(0, 100) <= 1) {
			oneTimeBoolean = true;
			System.out.println("### Starting Atlantis... ###");
			AtlantisInitActions.executeInitialActions();
			System.out.println("### Atlantis is working! ###");
		}

		// If game is running (not paused), run all actions.
		if (!isPaused) {
			gameCommander.update();
		}

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
		System.err.println("############KEY = " + keyCode + "############################");
		// System.err.println("########################################");

		// 27 (Esc) - pause/unpause game
		if (keyCode == 27) {
			pauseOrUnpause();
		}

		// 115 (+) - increase game speed
		else if (keyCode == 115) {
			AtlantisConfig.GAME_SPEED -= 2;
			if (AtlantisConfig.GAME_SPEED < 0) {
				AtlantisConfig.GAME_SPEED = 0;
			}
		}

		// 109 (-) - decrease game speed
		else if (keyCode == 107) {
			AtlantisConfig.GAME_SPEED += 2;
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
			AtlantisInformationCommander.addOurUnfinishedUnit(unit.getType());

			// Our unit
			if (unit.getPlayer().isSelf()) {
				AtlantisGame.getProductionStrategy().rebuildQueue();
			}
		}
	}

	@Override
	public void unitDestroy(int unitID) {
		Unit unit = Unit.getByID(unitID);
		if (unit != null) {
			AtlantisInformationCommander.unitDestroyed(unit);

			// Our unit
			if (unit.getPlayer().isSelf()) {
				AtlantisGame.getProductionStrategy().rebuildQueue();
			}
		}
	}

	@Override
	public void unitDiscover(int unitID) {
		Unit unit = Unit.getByID(unitID);
		if (unit != null) {

			// Enemy unit
			if (unit.getPlayer().isEnemy()) {
				AtlantisInformationCommander.discoveredEnemyUnit(unit);
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
				AtlantisInformationCommander.removeEnemyUnitVisible(unit);
			}
		}
	}

	@Override
	public void unitMorph(int unitID) {
	}

	@Override
	public void unitShow(int unitID) {
		Unit unit = Unit.getByID(unitID);
		if (unit != null) {

			// Enemy unit
			if (unit.getPlayer().isEnemy()) {
				AtlantisInformationCommander.addEnemyUnitVisible(unit);
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
	}

	@Override
	public void playerDropped(int playerID) {
	}

}
