package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class DontInterruptShootingMarines extends Manager {
    public DontInterruptShootingMarines(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isMarine()
//            && (!unit.hasMedicInHealRange() || unit.hp() <= 25)
            && !isMeleeEnemyNear();
    }

    private boolean isMeleeEnemyNear() {
        if (!unit.isGroundUnit()) return false;

//        Selection meleeEnemies = unit.enemiesNear().melee().canAttack(unit, 0.9);
        Selection meleeEnemies = unit.enemiesNear().melee().inRadius(
            Math.min(3.5, 1.7 + unit.woundPercent() / 200.0 + (unit.hasMedicInRange() ? 0 : 0.5)), unit
        );

        if (meleeEnemies.empty()) return false;

        return true;
//        return meleeEnemies.nearestTo(unit).isFacing(unit);
    }

    @Override
    public Manager handle() {
        AvoidEnemies avoidEnemies = new AvoidEnemies(unit);
        if (avoidEnemies.avoidEnemies() != null) {
            return usedManager(avoidEnemies, "Avoid");
        }

        return null;
    }
}
