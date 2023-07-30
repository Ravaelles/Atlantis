package atlantis.combat.micro.terran.wraith;

import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class AttackAsWraith extends AttackNearbyEnemies {
    public AttackAsWraith(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isWraith()
            && unit.hp() >= 90
            && (
//            unit.lastStartedAttackMoreThanAgo(30 * 5)
//                ||
            (
                unit.enemiesNear().canAttack(unit, 2.5).empty()
                    && unit.enemiesNear().canAttack(unit, 6).notEmpty()
            )
        );
    }

    @Override
    protected AttackAsWraith getInstance(AUnit unit) {
        return this;
    }

    @Override
    protected AUnit bestTargetToAttack() {
//        ATargetingForAirUnits targeting = new ATargetingForAirUnits(unit, true);
//        AUnit target = targeting.targetForAirUnit();

        AUnit target = defineTarget();

//        if (target != null) {
//            System.out.println("target = " + target + " / " + unit.distTo(target) + " / " + unit.groundWeaponRange());
//        }

        if (target != null && shouldStopMovingToAttack(target)) {
            unit.holdPosition("HoldToAttack");
        }

        return target;
    }

    private AUnit defineTarget() {
        AUnit target = Select.enemyRealUnits().effVisible().inShootRangeOf(unit).nearestTo(unit);
        if (target != null) {
            return target;
        }

        return Select.enemyRealUnitsWithBuildings().effVisible().inShootRangeOf(unit).nearestTo(unit);
    }

    private boolean shouldStopMovingToAttack(AUnit target) {
//        if (unit.isMoving()) {
//            return true;
//        }

//        System.out.println(unit.enemiesNear().buildings().canAttack(unit, 3.5).size());
        if (unit.enemiesNear().buildings().canAttack(unit, 3.5).notEmpty()) {
//            System.err.println("HOLD");
            return true;
//            if (unit.isMoving() || unit.isAccelerating()) {
//            }
        }

        if (unit.distTo(target) <= 4.9) {
            return true;
        }

        return false;
    }
}
