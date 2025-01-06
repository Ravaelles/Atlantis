package atlantis.combat.micro.avoid.dont;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.dont.protoss.ProtossDontAvoidEnemy;
import atlantis.combat.micro.avoid.dont.terran.TerranDontAvoidEnemy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class DontAvoidEnemy extends Manager {
    public DontAvoidEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (unit.lastOrderMinFramesAgo(1)) return true;
//        System.out.println("-------------- " + unit.lastActionAgo(Actions.ATTACK_UNIT));
//        if (unit.lastActionLessThanAgo(5, Actions.ATTACK_UNIT)) return false;

        if (We.protoss()) return (new ProtossDontAvoidEnemy(unit)).applies();
        if (We.terran()) return (new TerranDontAvoidEnemy(unit)).anySubmanagerApplies() != null;

        return false;
//        return anySubmanagerApplies() != null;
    }

//    protected boolean enemyCombatBuildingNearAndWeAreStacked() {
//        return unit.isGroundUnit()
//            && unit.friendsNear().groundUnits().combatUnits().inRadius(5, unit).atLeast(14)
//            && unit.enemiesNear()
//            .combatBuildings(false)
//            .inRadius(8, unit)
//            .notEmpty()
//            && (unit.woundHp() <= 16 || unit.meleeEnemiesNearCount(2.5) == 0);
//    }

//    protected boolean enemyAirUnitsAreNearAndWeShouldEngage() {
//        return unit.enemiesNear()
//            .ofType(AUnitType.Terran_Wraith, AUnitType.Protoss_Scout).inRadius(5, unit)
//            .effVisible()
//            .notEmpty()
//            && unit.meleeEnemiesNearCount(2.5) == 0;
//    }

//    @Override
//    protected Class<? extends Manager>[] managers() {
//        return new Class[]{
//            ProtossDontAvoidEnemy.class,
//            TerranDontAvoidEnemy.class,
//        };
//    }

//    @Override
//    public Manager handle() {
//        return handleSubmanagers();
//    }
}
