package atlantis.combat.micro.terran.wraith;

import atlantis.architecture.Manager;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;

public class AsAirUnitAttackAnyEnemy extends Manager {
    public AsAirUnitAttackAnyEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isAir() && unit.noCooldown() && unit.hasAnyWeapon();
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
