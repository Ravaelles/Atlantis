package atlantis.combat.micro.attack;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ForceAttackNearestEnemy extends Manager {
    private AUnit enemy;

    public ForceAttackNearestEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.hasAnyWeapon();
    }

    private AUnit chooseEnemyToAttack() {
        return unit.enemiesNear()
            .canBeAttackedBy(unit, -0.4)
            .mostWounded();
    }

    @Override
    public Manager handle() {
        if ((enemy = chooseEnemyToAttack()) == null) return null;

        if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(enemy)) {
            return usedManager(this);
        }

        return null;
    }
}
