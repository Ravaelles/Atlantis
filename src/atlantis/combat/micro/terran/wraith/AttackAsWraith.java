package atlantis.combat.micro.terran.wraith;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class AttackAsWraith extends AttackNearbyEnemies {
    public AttackAsWraith(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isWraith()) return false;
        if (unit.hp() <= 20) return false;
        if (unit.enemiesNear().air().havingAntiAirWeapon().canAttack(unit, 3.5).notEmpty()) return false;

        if (unit.hp() >= 110 || unit.looksIdle()) return true;
        if (unit.didntShootRecently(10)) return true;

        return (unit.lastStoppedRunningMoreThanAgo(30))
            && (
            (
                unit.enemiesNear().canAttack(unit, 2.5).empty()
                    && unit.enemiesNear().canAttack(unit, 6).notEmpty()
            )
                && TerranWraith.noAntiAirBuildingNearby(unit)
        );
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ChangeLocationIfRanTooLong.class,
            AttackOtherAirUnits.class,
            AttackWorkersWhenItMakesSense.class,
            AttackTargetInRangeIfRanTooLong.class,
            AttackTargetInRange.class,
            MoveAsLooksIdle.class,
        };
    }

    @Override
    protected Manager handle() {
        Manager submanager = handleSubmanagers();
        if (submanager != null) return usedManager(submanager);

        if (handleAttackNearEnemyUnits()) {
            return usedManager(this);
        }

        return null;
    }

    @Override
    protected AUnit bestTargetToAttack() {
        AUnit target = defineTarget();

        if (target == null || !target.hasPosition()) {
            return null;
        }

        return target;
    }

    private AUnit defineTarget() {
        Selection enemies = Select.enemy().effVisible().inRadius(999, unit)
            .farFromAntiAirBuildings(unit.groundWeaponRange() + 1.2);

        AUnit target;

        if (unit.ranRecently(5)) {
            target = enemies.nonBuildings().random();
        }
        else {
            target = enemies.nonBuildings().nearestTo(unit);
        }

        if (target != null) {
            return target;
        }

        return enemies.realUnitsAndBuildings()
            .excludeTypes(
                AUnitType.Terran_Barracks, AUnitType.Terran_Supply_Depot,
                AUnitType.Protoss_Pylon, AUnitType.Protoss_Gateway
            )
            .inShootRangeOf(unit).nearestTo(unit);
    }

    private boolean shouldStopMovingToAttack(AUnit target) {
        if (unit.enemiesNear().buildings().canAttack(unit, 3.5).notEmpty()) return true;

        if (unit.distTo(target) <= 4.9) return true;

        return false;
    }
}
