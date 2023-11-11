package atlantis.combat.micro.avoid.dont;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.dont.terran.TerranDontAvoidEnemy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class DontAvoidEnemy extends Manager {
    public DontAvoidEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isRanged()
            && unit.woundHp() <= 16
            && enemyAirUnitsAreNearAndWeShouldEngage();
    }

    private boolean enemyAirUnitsAreNearAndWeShouldEngage() {
        return unit.enemiesNear()
            .ofType(AUnitType.Terran_Wraith, AUnitType.Protoss_Scout).inRadius(5, unit)
            .effVisible()
            .notEmpty()
            && unit.meleeEnemiesNearCount(2.5) == 0;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TerranDontAvoidEnemy.class,
        };
    }

    @Override
    public Manager handle() {
        return null;
    }
}
