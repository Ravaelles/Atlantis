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
		Atlantis.getBwapi().drawTargets(true);
		sideMessageCounter = 0;

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
			Atlantis.getBwapi().drawText(unit.translated(0, 15), "Position: " + unit.toString(), false);
		}
	}

	// =========================================================
	// Hi-level

	private static void paintProductionQueue() {
		ArrayList<UnitType> produceNow = AtlantisGame.getProductionStrategy().getUnitsThatShouldBeProducedNow();
		for (UnitType unitType : produceNow) {
			paintSideMessage(unitType.getShortName());
		}
	}

	// =========================================================
	// Lo-level

	private static void paintSideMessage(String text) {
		int screenX = 10;
		int screenY = 10 + 15 * sideMessageCounter;
		Atlantis.getBwapi().drawText(new Position(screenX, screenY), text, true);

		sideMessageCounter++;
	}

}
