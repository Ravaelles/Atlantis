package atlantis.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class ProtossReaver extends Manager {

    public ProtossReaver(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isReaver();
    }

    @Override
    protected Manager handle() {
//        if (unit.scarabCount() <= 0) {
//            unit.setTooltipTactical("NoScarab");
//            return (new AvoidEnemiesIfNeeded.avoidEnemiesIfNeeded(unit);
//        }

        if (unit.cooldownRemaining() >= 10) {
            return null;
        }

        Selection enemiesInRange = unit.enemiesNear();
        AUnit enemy;

        // First attack very close enemies
        if ((enemy = enemiesInRange.canBeAttackedBy(unit, 0).nearestTo(unit)) != null) {
            unit.attackUnit(enemy);
            unit.setTooltipTactical("Tasty" + enemy.name());
            return usedManager(this);
        }

        // If no very close enemy, then attack the one most distant
        if ((enemy = enemiesInRange.canBeAttackedBy(unit, 7).nearestTo(unit)) != null) {
            unit.attackUnit(enemy);
            unit.setTooltipTactical("Nice" + enemy.name());
            return usedManager(this);
        }

        return null;
    }

}
