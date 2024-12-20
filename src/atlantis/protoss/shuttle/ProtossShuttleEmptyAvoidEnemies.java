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
        enemies = unit.enemiesNear().canAttack(unit, 1.5 + unit.shieldWoundPercent() / 25.0);

        return enemies.notEmpty();
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
