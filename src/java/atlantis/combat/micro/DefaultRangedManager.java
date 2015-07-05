package atlantis.combat.micro;

import jnibwapi.Position;
import jnibwapi.Position.PosType;
import jnibwapi.Unit;
import atlantis.wrappers.SelectUnits;

public class DefaultRangedManager extends MicroRangedManager {

	@Override
	public void update(Unit unit) {
		Unit nearestEnemy = SelectUnits.enemy().nearestTo(unit);
		if (nearestEnemy != null) {
			double distToEnemy = nearestEnemy.distanceTo(unit);

			// Run from the enemy
			if (distToEnemy < 0.4) {
				int dx = nearestEnemy.getPX() - unit.getPX();
				int dy = nearestEnemy.getPY() - unit.getPY();
				Position newPosition = new Position(unit.getPX() + dx, unit.getPY() + dy, PosType.PIXEL);

				unit.move(newPosition, false);
			}

			// Pursue and attack the enemy
			else {
				if (!unit.isStartingAttack()) {
					if (distToEnemy > unit.getShootRangeAgainst(nearestEnemy)) {
						unit.attack(nearestEnemy, false);
					}
				}
			}
		}
	}

}
