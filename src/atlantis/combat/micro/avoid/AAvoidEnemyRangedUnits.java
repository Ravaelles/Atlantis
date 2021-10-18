package atlantis.combat.micro.avoid;

import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.A;
import bwapi.Color;


public class AAvoidEnemyRangedUnits extends AAvoidUnits {

    /**
     * An enemy unit that has most range towards us.
     */
    private AUnit worstEnemy = null;

    // =========================================================

    public AAvoidEnemyRangedUnits(AUnit unit) {
        this.unit = unit;
        this.worstEnemy = nearestEnemy();
    }

    // =========================================================

    public boolean avoid() {
        if (shouldNotAvoidRangedUnits()) {
            return false;
        }

        if (!isEnemyCriticallyClose()) {
            return false;
        }

        // === Run the fuck outta here ==============================

        if (unit.runFrom(worstEnemy, 3.5)) {
            unit.setTooltip("MeleeRun");
            return true;
        }

        return handleErrorRun();
    }

    public static boolean shouldRunFromAnyEnemyMeleeUnit(AUnit unit) {
        return (new AAvoidEnemyRangedUnits(unit)).isEnemyCriticallyClose();
    }

    public double getCriticalDistance() {
        if (worstEnemy == null) {
            return Double.NEGATIVE_INFINITY;
        }

        APainter.paintCircleFilled(worstEnemy.getPosition(), 10, Color.Teal);

        // If unit is much slower than enemy, don't run at all. It's better to shoot instead.
        double quicknessDifference = unit.getSpeed() - worstEnemy.getSpeed();
        int beastNearby = Select.enemy().ofType(AUnitType.Protoss_Archon, AUnitType.Zerg_Ultralisk).inRadius(5, unit).count();

        double baseCriticalDistance = 0;
        double quicknessBonus = Math.min(0.5, (quicknessDifference > 0 ? -quicknessDifference / 3 : quicknessDifference / 1.5));
        double woundedBonus = unit.getWoundPercent() / 34.0;
        double beastBonus = (beastNearby > 0 ? 1.2 : 0);
        double ourUnitNearby = Select.ourRealUnits().inRadius(0.6, unit).count() / 2.0;
        double ourMovementBonus = unit.isMoving() ? (unit.isRunning() ? 0.8 : 0) : 1.3;
        double enemyMovementBonus = (worstEnemy != null && unit.isOtherUnitFacingThisUnit(worstEnemy))
                ? (worstEnemy.isMoving() ? 1.8 : 0.9) : 0;
//        APainter.paintTextCentered(unit.getPosition(), ourMovementBonus + " // " + + enemyMovementBonus, Color.White, 0, 3);

        double criticalDist = baseCriticalDistance + quicknessBonus + woundedBonus + beastBonus
                + ourUnitNearby + ourMovementBonus + enemyMovementBonus;
        return A.inRange(0.1, criticalDist, 4.8);
    }

    // =========================================================

    private boolean shouldNotAvoidRangedUnits() {
        if (worstEnemy == null) {
            return true;
        }



        return false;
    }

    private boolean isEnoughOfOurUnitsNearbyToNotAvoid() {
        Select<AUnit> selector = Select.ourCombatUnits();
        if (selector.clone().inRadius(1, unit).count() >= 3) {
            return true;
        }

        return unit.getHPPercent() > 50 && selector.clone().inRadius(4, unit).count() >= 12;
    }

    private AUnit nearestEnemy() {
        if (worstEnemy != null && worstEnemy.isAlive()) {
            return worstEnemy;
        }

        return worstEnemy = Select.enemyCombatUnits().melee().visible().inRadius(6, unit).nearestTo(unit);
    }

    private boolean isEnemyCriticallyClose() {
        if (worstEnemy == null) {
            return false;
        }

        APainter.paintCircleFilled(worstEnemy.getPosition(), 10, Color.Teal);

        double criticalDistance = getCriticalDistance();
        double enemyDistance = worstEnemy.distanceTo(unit);

        return enemyDistance <= criticalDistance;
    }

}
