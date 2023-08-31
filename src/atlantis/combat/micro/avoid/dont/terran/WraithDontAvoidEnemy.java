package atlantis.combat.micro.avoid.dont.terran;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.util.We;

public class WraithDontAvoidEnemy extends Manager {
    public WraithDontAvoidEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isWraith()
            && hasAirTargets()
            && isSafeFromGroundEnemies();
    }

    private boolean isSafeFromGroundEnemies() {
        return unit.enemiesNear().groundUnits().canAttack(unit, 2.5).empty();
    }

    private boolean hasAirTargets() {
        return unit.enemiesNear().air().excludeOverlords().canBeAttackedBy(unit, 2.5).notEmpty();
    }

    @Override
    public Manager handle() {
        return this;
    }
}
