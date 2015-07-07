package atlantis.combat.group.missions;

import jnibwapi.Unit;

/**
 * Represents behavior for group of units e.g. DEFEND, ATTACK etc.
 */
public abstract class Mission {

	/**
	 * If returns true, it's not allowed for micro managers to act.
	 */
	public abstract boolean update(Unit unit);

}
