package atlantis.debug;

import jnibwapi.Unit;
import jnibwapi.util.BWColor;
import atlantis.Atlantis;
import atlantis.wrappers.SelectUnits;

/**
 * Here you can include code that will draw things over units etc.
 */
public class AtlantisPainter {

	/**
	 * Executed once per frame, at the end of all other actions.
	 */
	public static void paint() {
		for (Unit u : SelectUnits.our().list()) {
			Atlantis.getBwapi().drawCircle(u.getPosition(), 5, BWColor.Green, true, false);
		}
		for (Unit u : SelectUnits.enemy().list()) {
			Atlantis.getBwapi().drawCircle(u.getPosition(), 5, BWColor.Red, true, false);
		}
		for (Unit u : SelectUnits.neutral().list()) {
			Atlantis.getBwapi().drawCircle(u.getPosition(), 5, BWColor.Blue, true, false);
		}

		// =========================================================
		// Paint TOOLTIPS over units
		for (Unit unit : SelectUnits.our().list()) {
			if (unit.hasTooltip()) {
				Atlantis.getBwapi().drawText(unit.getPosition(), unit.getTooltip(), false);
			}
			Atlantis.getBwapi().drawText(unit.getPosition().translated(0, 15),
					"Position: " + unit.getPosition().toString(), false);
		}
	}

}
