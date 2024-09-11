package atlantis.protoss.reaver;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class ReaverAlwaysAttack extends Manager {
    private Selection enemies;

    public ReaverAlwaysAttack(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.cooldownRemaining() >= 10) return false;

        enemies = unit.enemiesNear();
        return enemies.notEmpty();
    }

    @Override
    public Manager handle() {
        AUnit enemy;

        if (shouldContinueAttacking()) return usedManager(this, "GoOn");

        // First attack very close enemies
        if ((enemy = enemies.canBeAttackedBy(unit, 0).nearestTo(unit)) != null) {
            unit.attackUnit(enemy);
            unit.setTooltipTactical("Tasty" + enemy.name());
            return usedManager(this);
        }

        // If no very close enemy, then attack the one most distant
        if ((enemy = enemies.canBeAttackedBy(unit, 4).nearestTo(unit)) != null) {
            unit.attackUnit(enemy);
            unit.setTooltipTactical("Nice" + enemy.name());
            return usedManager(this);
        }

        return null;
    }

    private boolean shouldContinueAttacking() {
        return unit.isAttacking() && unit.lastActionLessThanAgo(50);
    }
}
