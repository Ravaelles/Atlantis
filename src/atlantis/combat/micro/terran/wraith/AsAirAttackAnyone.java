package atlantis.combat.micro.terran.wraith;

import atlantis.architecture.Manager;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;

public class AsAirAttackAnyone extends Manager {
    public AsAirAttackAnyone(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isAir() && unit.noCooldown() && unit.hasAnyWeapon() && unit.looksIdle() && !otherAirUnitsNear();
    }

    private boolean otherAirUnitsNear() {
        return unit.enemiesNear()
            .air()
            .havingAntiAirWeapon()
            .canAttack(unit, 3.5)
            .notEmpty();
    }

    @Override
    public Manager handle() {
        AUnit enemy = EnemyUnits.discovered().canBeAttackedBy(unit, 500).random();

        if (enemy != null) {
            unit.attackUnit(enemy);
            return usedManager(this);
        }

        return null;
    }
}
