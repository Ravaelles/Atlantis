package atlantis.combat.targeting.generic;

import atlantis.combat.targeting.air.DontAttackOverlords;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.AliveEnemies;
import atlantis.units.select.Selection;

import static atlantis.debug.DebugFlags.DEBUG_TARGETING;

public class ATargetingStandard extends ATargeting {
    public ATargetingStandard(AUnit unit) {
        super(unit);
    }

    public AUnit target() {

        // =========================================================
        // =========================================================
        // =========== REMEMBER, AT THIS POINT =====================
        // ======== ENEMY IS AT MOST 15 TILES AWAY =================
        // =========================================================
        // =========================================================

        AUnit target;

        // =========================================================
        // WORKERS IN RANGE

        Selection workersInRange = enemyUnits.workers().inShootRangeOf(unit);
        if (unit.isMelee()) {
            target = workersInRange.mostWounded();
        }
        else {
            target = workersInRange.randomWithSeed(unit.id());
        }

        if (target != null) {
           debug(unit, "D1 = " + target);
            return target;
        }

        // =========================================================
        // Near WORKERS for MELEE

        if (unit.isMelee()) {
            target = enemyUnits
                .workers()
                .inRadius(3, unit)
                .nearestTo(unit);
            if (target != null) {
               debug(unit, "D1b = " + target);
                return target;
            }
        }

        // =========================================================
        // Quite near WORKERS

        target = enemyUnits
            .workers()
            .inRadius(unit.isMelee() ? 8 : 12, unit)
            .nearestTo(unit);

        if (target != null) {
           debug(unit, "D2 = " + target);
            return target;
        }

        // =========================================================
        // Target real units - exclude MEDICS

        target = enemyUnits
            .excludeMedics()
//            .inRadius(12, unit)
            .inShootRangeOf(unit)
            .mostWounded();
//            .nearestTo(unit);

        if (target != null && (!target.isAir() || unit.isOtherUnitFacingThisUnit(target))) {
           debug(unit, "D3 = " + target);
            return target;
        }

        // === Tanks ===============================================

        if (Enemy.terran()) {
            target = enemyUnits
                .tanks()
                .inRadius(16, unit)
                .nearestTo(unit);
            if (target != null) {
                debug(unit, "D3_Tank = " + target);
                return target;
            }
        }

        // =========================================================
        // Bases

        target = enemyBuildings
            .bases()
            .onlyCompleted()
            .inRadius(10, unit)
            .mostWounded();
        if (target != null) {
           debug(unit, "D4 = " + target);
            return target;
        }

        // =========================================================
        // A bit further WORKERS

        target = enemyUnits
            .workers()
            .inRadius(17, unit)
            .nearestTo(unit);
        if (target != null && enemyUnits.ofType(target.type()).inRadius(5, target).atLeast(2)) {
            if (target.friendsNear().buildings().inRadius(10, target).notEmpty()) {
               debug(unit, "D5 = " + target);
                return target;
            }
        }

        // =========================================================
        // Distant bases

        target = enemyBuildings
            .bases()
            .onlyCompleted()
            .nearestTo(unit);
        if (target != null) {
           debug(unit, "D6b = " + target);
            return target;
        }

        // =========================================================
        // Destroy Pylons + Spawning Pools

        target = enemyBuildings
            .ofType(AUnitType.Protoss_Pylon, AUnitType.Zerg_Spawning_Pool)
            .inRadius(15, unit)
            .nearestTo(unit);
        if (target != null) {
           debug(unit, "D6c = " + target);
            return target;
        }

        // =========================================================
        // Okay, try targeting overlords

        if (
            !unit.shotSecondsAgo(3) || !DontAttackOverlords.forbidden(unit)
        ) {
            target = AliveEnemies.get()
                .overlords()
                .canBeAttackedBy(unit, 0.9)
                .mostWoundedOrNearest(unit);
            if (target != null) {
               debug(unit, "D7-Overlords = " + target);
                return target;
            }
        }

        // =========================================================
        // Okay, try targeting any-fuckin-thing

        // Non medics nearby
        target = enemyUnits
            .nonBuildings()
            .excludeMedics()
            .canBeAttackedBy(unit, 15)
            .nearestTo(unit);
        if (target != null) {
           debug(unit, "D8 = " + target);
            return target;
        }

        // =========================================================

        target = enemyUnits
            .havingPosition()
            .canBeAttackedBy(unit, 150)
            .nearestTo(unit);
        if (target != null && DEBUG_TARGETING) System.out.println("Any_EnemyUnit = " + target);

        // =========================================================

        target = enemyBuildings
            .havingPosition()
            .canBeAttackedBy(unit, 150)
            .nearestTo(unit);
        if (target != null && DEBUG_TARGETING) System.out.println("Any_EnemyBuilding = " + target);

        // =========================================================

        return target;
    }

}
