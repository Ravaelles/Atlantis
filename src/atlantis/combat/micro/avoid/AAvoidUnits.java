package atlantis.combat.micro.avoid;

import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.combat.missions.Missions;
import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.A;
import bwapi.Color;

public abstract class AAvoidUnits {

    protected static AUnit unit;

    // =========================================================

    public static boolean avoid(AUnit unit) {
        if (AAvoidInvisibleEnemyUnits.avoid(unit)) {
            return true;
        }

        if ((new AAvoidEnemyMeleeUnits(unit)).avoid()) {
            return true;
        }

//        if ((new AAvoidEnemyRangedUnits(unit)).avoid()) {
//            return true;
//        }

        if (!Missions.isGlobalMissionAttack()) {
            if (AAvoidEnemyDefensiveBuildings.avoid(unit, false)) {
                return true;
            }
        }

        return false;
    }

    // =========================================================

    protected double getCriticalDistance(AUnit enemy) {
        if (enemy == null) {
            return Double.NEGATIVE_INFINITY;
        }

        APainter.paintCircle(enemy.getPosition(), 16, enemy.isMeleeUnit() ? Color.Orange : Color.Yellow);

        // If unit is much slower than enemy, don't run at all. It's better to shoot instead.
        double quicknessDifference = unit.getSpeed() - enemy.getSpeed();
        int beastNearby = Select.enemy().ofType(
                AUnitType.Protoss_Archon, AUnitType.Zerg_Ultralisk
        ).inRadius(5, unit).count();

        double baseCriticalDistance = 0;
        double quicknessBonus = Math.min(0.5, (quicknessDifference > 0 ? -quicknessDifference / 3 : quicknessDifference / 1.5));
        double woundedBonus = unit.getWoundPercent() / 34.0;
        double beastBonus = (beastNearby > 0 ? 1.2 : 0);
        double ourUnitNearby = Select.ourRealUnits().inRadius(0.6, unit).count() / 2.0;
        double ourMovementBonus = unit.isMoving() ? (unit.isRunning() ? 0.8 : 0) : 1.3;
        double enemyMovementBonus = (enemy != null && unit.isOtherUnitFacingThisUnit(enemy))
                ? (enemy.isMoving() ? 1.8 : 0.9) : 0;
//        APainter.paintTextCentered(unit.getPosition(), ourMovementBonus + " // " + + enemyMovementBonus, Color.White, 0, 3);

        double criticalDist = baseCriticalDistance + quicknessBonus + woundedBonus + beastBonus
                + ourUnitNearby + ourMovementBonus + enemyMovementBonus;

        return criticalDist;
    }

    protected boolean startRunningFromEnemy(AUnit unit, AUnit enemy, double runToDist, String tooltip) {
        if (unit.runFrom(enemy, runToDist)) {
            unit.setTooltip(tooltip);
            return true;
        }

        return false;
    }

    protected boolean handleErrorRun() {
        System.err.println("ERROR_RUN for " + unit.getShortNamePlusId());

        AAttackEnemyUnit.handleAttackNearbyEnemyUnits(unit);
        unit.setTooltip("Cant run, fight");

        return true;
    }

}
