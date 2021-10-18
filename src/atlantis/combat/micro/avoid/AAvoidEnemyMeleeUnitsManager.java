package atlantis.combat.micro.avoid;

import atlantis.AGame;
import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.A;
import bwapi.Color;


public class AAvoidEnemyMeleeUnitsManager {
    
    private AUnit nearestEnemy = null;
    private final AUnit unit;

    // =========================================================

    public AAvoidEnemyMeleeUnitsManager(AUnit unit) {
        this.unit = unit;
        this.nearestEnemy = nearestEnemy();
    }

    // =========================================================

    /**
     * If unit is ranged unit like e.g. Marine, get away from very close melee units like e.g. Zealots.
     */
    public boolean avoid() {
        if (shouldNotAvoidMeleeUnits()) {
            return false;
        }

        if (!isEnemyCriticallyClose()) {
            return false;
        }

        // === Run the fuck outta here ==============================

        if (unit.runFrom(nearestEnemy, 3.5)) {
            unit.setTooltip("MeleeRun");
            return true;
        }

        return handleErrorRun();
    }

    public static boolean shouldRunFromAnyEnemyMeleeUnit(AUnit unit) {
        return (new AAvoidEnemyMeleeUnitsManager(unit)).isEnemyCriticallyClose();
    }

    public double getCriticalDistance() {
        if (nearestEnemy == null) {
            return Double.NEGATIVE_INFINITY;
        }

        APainter.paintCircleFilled(nearestEnemy.getPosition(), 10, Color.Teal);

        // If unit is much slower than enemy, don't run at all. It's better to shoot instead.
        double quicknessDifference = unit.getSpeed() - nearestEnemy.getSpeed();
        int beastNearby = Select.enemy().ofType(AUnitType.Protoss_Archon, AUnitType.Zerg_Ultralisk).inRadius(5, unit).count();

        double baseCriticalDistance = 0;
        double quicknessBonus = Math.min(0.5, (quicknessDifference > 0 ? -quicknessDifference / 3 : quicknessDifference / 1.5));
        double woundedBonus = unit.getWoundPercent() / 34.0;
        double beastBonus = (beastNearby > 0 ? 1.2 : 0);
        double ourUnitNearby = Select.ourRealUnits().inRadius(0.6, unit).count() / 2.0;
        double ourMovementBonus = unit.isMoving() ? (unit.isRunning() ? 0.8 : 0) : 1.3;
        double enemyMovementBonus = (nearestEnemy != null && unit.isOtherUnitFacingThisUnit(nearestEnemy))
                ? (nearestEnemy.isMoving() ? 1.8 : 0.9) : 0;
//        APainter.paintTextCentered(unit.getPosition(), ourMovementBonus + " // " + + enemyMovementBonus, Color.White, 0, 3);

        double criticalDist = baseCriticalDistance + quicknessBonus + woundedBonus + beastBonus
                + ourUnitNearby + ourMovementBonus + enemyMovementBonus;
        return A.inRange(0.1, criticalDist, 4.8);
    }

    // =========================================================

    private boolean handleErrorRun() {
        System.err.println("ERROR_RUN for " + unit.getShortNamePlusId());

        AAttackEnemyUnit.handleAttackNearbyEnemyUnits(unit);
        unit.setTooltip("Cant run, fight");

        return true;
    }

    private boolean shouldNotAvoidMeleeUnits() {
        if (nearestEnemy == null) {
            return true;
        }

        boolean shouldSkip = (unit.isAirUnit() || unit.isMeleeUnit());
        if (shouldSkip) {
            return true;
        }

        if (unit.isWorker() && unit.isFullyHealthy()) {
            return Select.enemyCombatUnits().inRadius(3.5, unit).count() <= 1;
        }

        if (Select.enemyCombatUnits().inRadius(6, unit).count() <= 0) {
            return false;
        }

        if (isEnoughOfOurUnitsNearbyToNotAvoid()) {
            return false;
        }

        if (unit.isFullyHealthy() && !unit.isWorker() && !nearestEnemy.isType(AUnitType.Protoss_Dark_Templar)) {
            return true;
        }

        // === Reaver should not avoid if has no cooldown ===============================

        if (AGame.isPlayingAsProtoss()) {
            if (
                Select.ourCombatUnits().inRadius(7, unit).count() >= 7
                && Select.ourCombatUnits().inRadius(4, unit).count() >= 3
            ) {
                return false;
            }

            return unit.isType(AUnitType.Protoss_Reaver) && unit.getCooldownCurrent() <= 4;
        }

        // =========================================================

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
        if (nearestEnemy != null && nearestEnemy.isAlive()) {
            return nearestEnemy;
        }

        return nearestEnemy = Select.enemyCombatUnits().melee().visible().inRadius(6, unit).nearestTo(unit);
    }

    private boolean isEnemyCriticallyClose() {
        if (nearestEnemy == null) {
            return false;
        }

        APainter.paintCircleFilled(nearestEnemy.getPosition(), 10, Color.Teal);

        double criticalDistance = getCriticalDistance();
        double enemyDistance = nearestEnemy.distanceTo(unit);

        return enemyDistance <= criticalDistance;
    }

}
