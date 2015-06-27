package atlantis.commanders;

import jnibwapi.Unit;
import jnibwapi.util.BWColor;
import atlantis.Atlantis;
import atlantis.wrappers.SelectUnits;

public class AtlantisGameCommander {

	public void update() {
		for (Unit u : SelectUnits.our().list()) {
			Atlantis.getBwapi().drawCircle(u.getPosition(), 5, BWColor.Green, true, false);
		}
		for (Unit u : SelectUnits.enemy().list()) {
			Atlantis.getBwapi().drawCircle(u.getPosition(), 5, BWColor.Red, true, false);
		}
		for (Unit u : SelectUnits.neutral().list()) {
			Atlantis.getBwapi().drawCircle(u.getPosition(), 5, BWColor.Blue, true, false);
		}
	}

}
