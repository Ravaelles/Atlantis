package atlantis.combat.group.missions;

import jnibwapi.Unit;

/**
 * Represents behavior for group of units e.g. DEFEND, ATTACK etc.
 */
public abstract class Mission {

	public abstract void update(Unit unit);

}
