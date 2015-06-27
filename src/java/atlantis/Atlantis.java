package atlantis;

import jnibwapi.BWAPIEventListener;
import jnibwapi.JNIBWAPI;
import jnibwapi.Position;
import atlantis.commanders.AtlantisGameCommander;

public class Atlantis implements BWAPIEventListener {

	private static Atlantis instance;
	private JNIBWAPI bwapi;
	private AtlantisGameCommander gameCommander;

	// =========================================================
	// Other variables

	private boolean isPaused;

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
	}

	// =========================================================
	// Start / Pause / Unpause

	/**
	 * Starts the bot.
	 */
	public void start() {
		isPaused = false;
		bwapi.start();
	}

	/**
	 * Forces all calculations to be stopped. CPU usage should be minimal.
	 */
	public void pause() {
		isPaused = true;
	}

	/**
	 * Resumes the game after pause.
	 */
	public void unpause() {
		isPaused = false;
	}

	// =========================================================

	/**
	 * Returns JNIWAPI for this Atlantis object.
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
	}

	@Override
	public void matchFrame() {

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
	}

	@Override
	public void matchEnd(boolean winner) {
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
	}

	@Override
	public void unitDestroy(int unitID) {
	}

	@Override
	public void unitDiscover(int unitID) {
	}

	@Override
	public void unitEvade(int unitID) {
	}

	@Override
	public void unitHide(int unitID) {
	}

	@Override
	public void unitMorph(int unitID) {
	}

	@Override
	public void unitShow(int unitID) {
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
