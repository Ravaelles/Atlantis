package atlantis.combat.micro.avoid.dont.terran;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;

public class ScvDontAvoidEnemy extends Manager {
    public ScvDontAvoidEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isScv() && unit.hpMoreThan(50) && !unit.looksIdle();
    }

    @Override
    protected Manager handle() {
        Selection enemiesNear = unit.enemiesNear().inRadius(11, unit);

        if (enemiesNear.onlyAir()
            && (
                enemiesNear.onlyOfType(AUnitType.Protoss_Scout)
                || enemiesNear.canAttack(unit, 0).atMost(2)
            )
        ) {
            return usedManager(this);
        }

        return null;
    }
}
