package atlantis.debug;

import java.util.ArrayList;

import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;
import jnibwapi.util.BWColor;
import atlantis.Atlantis;
import atlantis.AtlantisGame;
import atlantis.wrappers.SelectUnits;

/**
 * Here you can include code that will draw things over units etc.
 */
public class AtlantisPainter {

	private static int sideMessageCounter = 0;

	// =========================================================

	/**
	 * Executed once per frame, at the end of all other actions.
	 */
	public static void paint() {
		sideMessageCounter = 0;

		// =========================================================

		// Atlantis.getBwapi().drawTargets(true); // Draws line from unit to the target position
		// Atlantis.getBwapi().getMap().drawTerrainData(Atlantis.getBwapi());

		// =========================================================

		paintProductionQueue();

		// =========================================================

		for (Unit u : SelectUnits.our().list()) {
			Atlantis.getBwapi().drawLine(u.translated(-5, 0), u.translated(5, 0), BWColor.Green, false);
		}
		for (Unit u : SelectUnits.enemy().list()) {
			Atlantis.getBwapi().drawLine(u.translated(-5, 0), u.translated(5, 0), BWColor.Red, false);
		}
		for (Unit u : SelectUnits.neutral().list()) {
			Atlantis.getBwapi().drawLine(u.translated(-5, 0), u.translated(5, 0), BWColor.Blue, false);
		}

		// =========================================================
		// Paint TOOLTIPS over units
		for (Unit unit : SelectUnits.our().list()) {
			if (unit.hasTooltip()) {
				Atlantis.getBwapi().drawText(unit, unit.getTooltip(), false);
			}
		}
	}

	// =========================================================
	// Hi-level

	private static void paintProductionQueue() {

		// Display units currently in production
		for (Unit unit : SelectUnits.ourUnfinished().list()) {
			paintSideMessage(unit.getType().getShortName(), BWColor.Green);
		}

		// Display units that should be produced right now or any time
		ArrayList<UnitType> produceNow = AtlantisGame.getProductionStrategy().getUnitsToProduceRightNow();
		for (UnitType unitType : produceNow) {
			paintSideMessage(unitType.getShortName(), BWColor.Yellow);
		}

		// Display next units to produce
		ArrayList<UnitType> fullQueue = AtlantisGame.getProductionStrategy().getProductionQueueNextUnits(
				6 - produceNow.size());
		for (int index = produceNow.size(); index < fullQueue.size(); index++) {
			UnitType type = fullQueue.get(index);
			if (type != null && type.getShortName() != null) {
				paintSideMessage(type.getShortName(), BWColor.Red);
			}
		}
	}

	// =========================================================
	// Lo-level

	private static void paintSideMessage(String text, BWColor color) {
		int screenX = 10;
		int screenY = 10 + 15 * sideMessageCounter;
		paintMessage(text, color, screenX, screenY, true);

		sideMessageCounter++;
	}

	private static void paintMessage(String text, BWColor color, int x, int y, boolean screenCoord) {
		Atlantis.getBwapi().drawText(new Position(x, y), BWColor.getColorString(color) + text, screenCoord);
	}

}
