package atlantis.combat.micro;

import jnibwapi.Position;
import jnibwapi.Position.PosType;
import jnibwapi.Unit;
import atlantis.combat.group.missions.Missions;
import atlantis.wrappers.SelectUnits;

public class DefaultRangedManager extends MicroRangedManager {

	@Override
	public void update(Unit unit) {
		if (!unit.isStartingAttack()) {
			Unit nearestEnemy = SelectUnits.enemy().nearestTo(unit);
			if (nearestEnemy != null) {
				double distToEnemy = nearestEnemy.distanceTo(unit);

				// If unit has mission defend, don't attack close targets if further than X
				if (distToEnemy >= 8 && unit.getGroup().getMission().equals(Missions.DEFEND)) {
					return;
				}

				// Run from the enemy
				if (distToEnemy < 0.8 && unit.getHitPoints() < 30) {
					int dx = 3 * (nearestEnemy.getPX() - unit.getPX());
					int dy = 3 * (nearestEnemy.getPY() - unit.getPY());
					Position newPosition = new Position(unit.getPX() + dx, unit.getPY() + dy, PosType.PIXEL);

					unit.move(newPosition, false);
				}

				// Pursue and attack the enemy
				else {
					// if (distToEnemy > unit.getShootRangeAgainst(nearestEnemy)) {
					unit.attackUnit(nearestEnemy, false);
					// }
				}
			}
		}
	}

}
