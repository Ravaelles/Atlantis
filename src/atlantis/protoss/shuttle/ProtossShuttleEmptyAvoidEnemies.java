package atlantis.protoss.shuttle;

import atlantis.architecture.Manager;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import static atlantis.units.AUnitType.Protoss_Reaver;

public class ProtossShuttleEmptyAvoidEnemies extends Manager {
    private Selection enemies;

    public ProtossShuttleEmptyAvoidEnemies(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        enemies = unit.enemiesNear().canAttack(unit, marginToLeaveAgainstEnemies());

        return enemies.notEmpty();
    }

    /**
     * How much room to leave around this unit: 1.5 tiles, and half a tile more
     * for every percent of shield already gone (so a unit at half shields leaves
     * 2.7).
     *
     * <p>{@code shieldWoundPercent()} is {@code NaN} for a unit that cannot have
     * shields, and it used to be added straight into the margin.
     * {@code canAttack(unit, NaN)} ends up comparing {@code dist <= range + NaN},
     * which is false for every distance - so the selection came back empty and
     * this manager never applied, whatever was standing next to the unit. A unit
     * with no shields has no shields to lose, which is worth the base margin and
     * nothing more.</p>
     */
    private double marginToLeaveAgainstEnemies() {
        double shieldsAlreadyGone = unit.maxShields() > 0 ? unit.shieldWoundPercent() / 25.0 : 0;

        return 1.5 + shieldsAlreadyGone;
    }

    @Override
    public Manager handle() {
        APosition center = enemies.center();
        if (center == null) return null;

        if (unit.moveAwayFrom(center, 5, Actions.MOVE_AVOID, "ShuttleAvoid")) {
            return usedManager(this);
        }

        return null;
    }
}
