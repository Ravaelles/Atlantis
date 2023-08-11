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
            (
                unit.enemiesNear().canAttack(unit, 2.5).empty()
                    && unit.enemiesNear().canAttack(unit, 6).notEmpty()
            )
        );
    }

    @Override
    protected AUnit bestTargetToAttack() {
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
        AUnit target = Select.enemyRealUnits().effVisible().inRadius(4.5, unit).nearestTo(unit);
        if (target != null) {
            return target;
        }

        return Select.enemyRealUnitsWithBuildings().effVisible().inShootRangeOf(unit).nearestTo(unit);
    }

    private boolean shouldStopMovingToAttack(AUnit target) {
        if (unit.enemiesNear().buildings().canAttack(unit, 3.5).notEmpty()) {
            return true;
        }

        if (unit.distTo(target) <= 4.9) {
            return true;
        }

        return false;
    }
}
