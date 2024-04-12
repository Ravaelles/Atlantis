package atlantis.combat.micro.avoid.dont;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.dont.protoss.ProtossDontAvoidEnemy;
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
            && (
            enemyAirUnitsAreNearAndWeShouldEngage()
                || enemyCombatBuildingNearAndWeAreStacked()
        );
    }

    private boolean enemyCombatBuildingNearAndWeAreStacked() {
        return unit.isGroundUnit()
            && unit.friendsNear().groundUnits().combatUnits().inRadius(5, unit).atLeast(14)
            && unit.enemiesNear()
            .combatBuildings(false)
            .inRadius(8, unit)
            .notEmpty()
            && (unit.woundHp() <= 16 || unit.meleeEnemiesNearCount(2.5) == 0);
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
            ProtossDontAvoidEnemy.class,
        };
    }

    @Override
    public Manager handle() {
        return usedManager(this);
    }
}
