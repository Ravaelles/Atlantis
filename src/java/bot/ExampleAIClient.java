package bot;

import java.util.HashSet;

import jnibwapi.BWAPIEventListener;
import jnibwapi.JNIBWAPI;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.TechType;
import jnibwapi.types.TechType.TechTypes;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.types.UpgradeType;
import jnibwapi.types.UpgradeType.UpgradeTypes;

/**
 * Example Java AI Client using JNI-BWAPI.
 * 
 * Executes a 5-pool rush and cheats using perfect information.
 * 
 * Note: the agent often gets stuck when attempting to build the spawning pool.
 * It works best on maps where the overlord spawns with plenty of free space
 * around it.
 */
public class ExampleAIClient implements BWAPIEventListener {

	/** reference to JNI-BWAPI */
	private final JNIBWAPI bwapi;

	/** used for mineral splits */
	private final HashSet<Unit> claimedMinerals = new HashSet<>();

	/** have drone 5 been morphed */
	private boolean morphedDrone;

	/** the drone that has been assigned to building a pool */
	private Unit poolDrone;

	/** when should the next overlord be spawned? */
	private int supplyCap;

	/**
	 * Create a Java AI.
	 */
	public static void main(String[] args) {
		new ExampleAIClient();
	}

	/**
	 * Instantiates the JNI-BWAPI interface and connects to BWAPI.
	 */
	public ExampleAIClient() {
		bwapi = new JNIBWAPI(this, true);
		bwapi.start();
	}

	/**
	 * Connection to BWAPI established.
	 */
	@Override
	public void connected() {
		System.out.println("Connected");
	}

	/**
	 * Called at the beginning of a game.
	 */
	@Override
	public void matchStart() {
		System.out.println("Game Started");

		bwapi.enableUserInput();
		bwapi.enablePerfectInformation();
		bwapi.setGameSpeed(0);

		// reset agent state
		claimedMinerals.clear();
		morphedDrone = false;
		poolDrone = null;
		supplyCap = 0;
	}

	/**
	 * Called each game cycle.
	 */
	@Override
	public void matchFrame() {
		// print out some info about any upgrades or research happening
		String msg = "=";
		for (TechType t : TechTypes.getAllTechTypes()) {
			if (bwapi.getSelf().isResearching(t)) {
				msg += "Researching " + t.getName() + "=";
			}
			// Exclude tech that is given at the start of the game
			UnitType whatResearches = t.getWhatResearches();
			if (whatResearches == UnitTypes.None) {
				continue;
			}
			if (bwapi.getSelf().isResearched(t)) {
				msg += "Researched " + t.getName() + "=";
			}
		}
		for (UpgradeType t : UpgradeTypes.getAllUpgradeTypes()) {
			if (bwapi.getSelf().isUpgrading(t)) {
				msg += "Upgrading " + t.getName() + "=";
			}
			if (bwapi.getSelf().getUpgradeLevel(t) > 0) {
				int level = bwapi.getSelf().getUpgradeLevel(t);
				msg += "Upgraded " + t.getName() + " to level " + level + "=";
			}
		}
		bwapi.drawText(new Position(0, 20), msg, true);

		// draw the terrain information
		// bwapi.getMap().drawTerrainData(bwapi);

		// spawn a drone
		for (Unit unit : bwapi.getMyUnits()) {
			// Note you can use referential equality
			if (unit.getType() == UnitTypes.Zerg_Larva) {
				if (bwapi.getSelf().getMinerals() >= 50 && !morphedDrone) {
					unit.morph(UnitTypes.Zerg_Drone);
					morphedDrone = true;
				}
			}
		}

		// collect minerals
		for (Unit unit : bwapi.getMyUnits()) {
			if (unit.getType() == UnitTypes.Zerg_Drone) {
				// You can use referential equality for units, too
				if (unit.isIdle() && unit != poolDrone) {
					for (Unit minerals : bwapi.getNeutralUnits()) {
						if (minerals.getType().isMineralField()
								&& !claimedMinerals.contains(minerals)) {
							double distance = unit.getDistance(minerals);

							if (distance < 300) {
								unit.rightClick(minerals, false);
								claimedMinerals.add(minerals);
								break;
							}
						}
					}
				}
			}
		}

		// build a spawning pool
		if (bwapi.getSelf().getMinerals() >= 200 && poolDrone == null) {
			System.out.println("BUILD");
			for (Unit unit : bwapi.getMyUnits()) {
				if (unit.getType() == UnitTypes.Zerg_Drone) {
					poolDrone = unit;
					break;
				}
			}

			// build the pool under the overlord
			for (Unit unit : bwapi.getMyUnits()) {
				if (unit.getType() == UnitTypes.Zerg_Hatchery) {
					poolDrone.build(unit.getPosition().translated(new Position(0, -120)),
							UnitTypes.Zerg_Spawning_Pool);
				}
				// if (unit.getType() == UnitTypes.Zerg_Overlord) {
				// poolDrone.build(unit.getPosition().translated(new
				// Position(20, 20)),
				// UnitTypes.Zerg_Spawning_Pool);
				// }
			}
		}

		// spawn overlords
		if (bwapi.getSelf().getSupplyUsed() + 2 >= bwapi.getSelf().getSupplyTotal()
				&& bwapi.getSelf().getSupplyTotal() > supplyCap) {
			if (bwapi.getSelf().getMinerals() >= 100) {
				for (Unit larva : bwapi.getMyUnits()) {
					if (larva.getType() == UnitTypes.Zerg_Larva) {
						larva.morph(UnitTypes.Zerg_Overlord);
						supplyCap = bwapi.getSelf().getSupplyTotal();
					}
				}
			}
		}
		// spawn zerglings
		else if (bwapi.getSelf().getMinerals() >= 50) {
			for (Unit unit : bwapi.getMyUnits()) {
				if (unit.getType() == UnitTypes.Zerg_Spawning_Pool && unit.isCompleted()) {
					for (Unit larva : bwapi.getMyUnits()) {
						if (larva.getType() == UnitTypes.Zerg_Larva) {
							larva.morph(UnitTypes.Zerg_Zergling);
						}
					}
				}
			}
		}

		// attack move toward an enemy
		if (bwapi.getSelf().getSupplyUsed() >= 8)
			for (Unit unit : bwapi.getMyUnits()) {
				if (unit.getType() == UnitTypes.Zerg_Zergling && unit.isIdle()) {
					for (Unit enemy : bwapi.getEnemyUnits()) {
						unit.attack(enemy.getPosition(), false);
						break;
					}
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
