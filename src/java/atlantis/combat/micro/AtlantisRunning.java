package atlantis.combat.micro;

import jnibwapi.Position;
import jnibwapi.Position.PosType;
import jnibwapi.Unit;

/**
 * Handles best way of running from close enemies and information about the fact if given unit is running or not.
 */
public class AtlantisRunning {

	private Unit unit;
	private Position nextPositionToRunTo = null;

	// =========================================================

	public AtlantisRunning(Unit unit) {
		super();
		this.unit = unit;
	}

	// =========================================================
	// Hi-level methods

	/**
	 * Indicates that this unit should be running from given enemy unit.
	 */
	public void runFrom(Unit nearestEnemy) {
		int dx = 3 * (nearestEnemy.getPX() - unit.getPX());
		int dy = 3 * (nearestEnemy.getPY() - unit.getPY());
		Position newPosition = new Position(unit.getPX() + dx, unit.getPY() + dy, PosType.PIXEL);

		unit.move(newPosition, false);
	}

	// =========================================================
	// Getters & Setters

	/**
	 * Returns true if given unit is currently (this frame) running from an enemy.
	 */
	public boolean isRunning() {
		return nextPositionToRunTo != null;
	}

	public Unit getUnit() {
		return unit;
	}

}
