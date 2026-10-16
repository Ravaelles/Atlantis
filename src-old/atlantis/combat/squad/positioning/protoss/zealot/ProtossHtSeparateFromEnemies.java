package atlantis.combat.squad.positioning.protoss.zealot;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class ProtossHtSeparateFromEnemies extends Manager {
    private Selection enemies;

    public ProtossHtSeparateFromEnemies(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isHt()
            && unit.energy() <= 190
            && (enemies = unit.enemiesNear().havingWeapon()).notEmpty()
            && enemies.canAttack(unit, 4).notEmpty();
    }

    @Override
    public Manager handle() {
        if (unit.moveToSafety(Actions.MOVE_SAFETY, "FragileHT")) return usedManager(this);

        return null;
    }
}
