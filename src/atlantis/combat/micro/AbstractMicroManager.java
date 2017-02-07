package atlantis.combat.micro;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.combat.AtlantisCombatEvaluator;
import atlantis.combat.squad.Squad;
import atlantis.debug.AtlantisPainter;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.units.Units;
import atlantis.units.missions.UnitActions;
import atlantis.util.PositionUtil;
import atlantis.wrappers.APosition;
import bwapi.Color;
import bwapi.WeaponType;
import java.util.Collection;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public abstract class AbstractMicroManager {

    private static AUnit _nearestEnemyThatCanShootAtThisUnit = null;

    // =========================================================
    public abstract boolean update(AUnit unit);

    // =========================================================
    /**
     * If chances to win the skirmish with the nearby enemy units aren't favorable, avoid fight and retreat.
     */
    protected boolean handleUnfavorableOdds(AUnit unit) {
        boolean isNewFight = (unit.getUnitAction() != null && !unit.getUnitAction().isRunningOrRetreating());
        boolean isSituationFavorable = AtlantisCombatEvaluator.isSituationFavorable(unit, isNewFight);

        // If situation is unfavorable, retreat
        if (!isSituationFavorable && !unit.isReadyToShoot() && unit.canAnyCloseEnemyShootThisUnit()) {
            unit.setTooltip("Retreat");
            return unit.runFrom(null);
        }

        return false;
    }

    /**
     * If combat evaluator tells us that the potential skirmish with nearby enemies wouldn't result in
     * decisive victory either retreat or stand where you are.
     */
    protected boolean handleNotExtremelyFavorableOdds(AUnit unit) {
//        if (!AtlantisCombatEvaluator.isSituationExtremelyFavorable(unit)) {
//            if (isInShootRangeOfAnyEnemyUnit(unit)) {
////                unit.moveAwayFrom(_nearestEnemyThatCanShootAtThisUnit, 2);
////                return true;
//                return AtlantisRunManager.run(unit);
//            }
//        }

        return false;
    }

    /**
     * If unit is severly wounded, it should run.
     */
    protected boolean handleLowHealthIfNeeded(AUnit unit) {
        if (AtlantisGame.playsAsTerran()) {
            if (unit.getHP() <= 7) {
                AUnit rendezvousWithMedics = (AUnit) Select.ourBuildings().ofType(AtlantisConfig.BARRACKS).first();
                if (rendezvousWithMedics != null && rendezvousWithMedics.distanceTo(unit) > 5) {
                    unit.move(rendezvousWithMedics.getPosition(), UnitActions.HEAL);
                }
                return true;
            }
        }

        AUnit nearestEnemy = Select.nearestEnemy(unit.getPosition());
        if (nearestEnemy == null || PositionUtil.distanceTo(nearestEnemy, unit) > 6) {
            return false;
        }

        if (unit.getHitPoints() <= 16 || unit.getHPPercent() < 30) {
            if (Select.ourCombatUnits().inRadius(4, unit).count() <= 6) {
                return unit.getRunManager().run();
            }
        }

        return false;
    }

    /**
     * If e.g. Terran Marine stands too far forward, it makes him vulnerable. Make him go back.
     */
    protected boolean handleDontSpreadTooMuch(AUnit unit) {
        Squad squad = unit.getSquad();
        Select ourUnits = Select.from(squad.arrayList()).inRadius(10, unit);
        int ourUnitsNearby = ourUnits.count();
        int minUnitsNearby = (int) (squad.size() * 0.66);

        // =========================================================
        if (ourUnitsNearby < minUnitsNearby && ourUnitsNearby <= 3) {
            APosition goTo = PositionUtil.averagePosition(ourUnits.list());
            if (goTo != null && goTo.distanceTo(unit) > 1) {
                unit.move(goTo, UnitActions.MOVE);
                unit.setTooltip("Closer");
                return true;
            }
        }

        return false;
    }

    /**
     * If unit is ranged unit like e.g. Marine, get away from very close melee units like e.g. Zealots.
     */
    protected boolean handleAvoidCloseMeleeUnits(AUnit unit) {
        if (unit.isGroundUnit() && unit.getType().isRangedUnit()
                && (unit.getHitPoints() <= 42 || unit.getHPPercent() < 100)) {
            
            // === Handle Dragoons ========================================
            
            if (unit.getType().isDragoon() && (unit.isReadyToShoot() || unit.isJustShooting()) && unit.getHPPercent()>= 85) {
                return false;
            }
            
            // === Define safety distance ==============================

            double lowHealthBonus = Math.max(((100 - unit.getHPPercent()) / 25), 1.5);
            
            double safetyDistance;
            
            if (unit.getType().isVulture()) {
                safetyDistance = unit.getWeaponRangeGround() + 2.6;
            }
            else {
                safetyDistance = 1.0 + (((Select.enemyRealUnits().ofType(AUnitType.Protoss_Archon)
                    .inRadius(4, unit)).count() > 0) ? 2 : 0) + lowHealthBonus;
                
                if (safetyDistance > unit.getWeaponRangeGround()) {
                    safetyDistance = unit.getWeaponRangeGround() - 0.2;
                }
            }
            
            // =========================================================
            
            // Apply bonus when there are maaany enemies nearby
            int enemyNearbyCountingRadius = 6;
            int enemiesNearby = Select.enemy().inRadius(enemyNearbyCountingRadius, unit).count();
            if (enemiesNearby >= 2) {
                if (unit.getType().isVulture()) {
                    safetyDistance += Math.max((double) enemiesNearby / 2, 3.6);
                }
                else {
                    safetyDistance += Math.max((double) enemiesNearby / 3, 2);
                }
            }
            
            AtlantisPainter.paintTextCentered(unit.getPosition().translateByPixels(0, 12), 
                    "" + String.format("%.1f", safetyDistance), Color.Green);
            
//            AtlantisPainter.paintCircle(unit, enemyNearbyCountingRadius * 32, Color.Green);
//            AtlantisPainter.paintTextCentered(unit, enemiesNearby + "", Color.White);

            Select<?> closeEnemies = Select.enemyRealUnits().melee().inRadius(safetyDistance, unit);
            AUnit closeEnemy = closeEnemies.nearestTo(unit);
            if (closeEnemy != null) {
                
                double dangerousDistance = unit.getType().isVulture() ? 1.9 : 1.2;
                boolean isEnemyDangerouslyClose = closeEnemy.distanceTo(unit) < dangerousDistance;
                if (isEnemyDangerouslyClose) {
                    
                    boolean dontInterruptPendingAttack;
                    if (unit.isVulture()) {
                        dontInterruptPendingAttack = unit.isAttackFrame() && closeEnemies.size() <= 4;
                    }
                    else {
                        dontInterruptPendingAttack = (unit.isAttackFrame() || unit.isStartingAttack())
                                && unit.getHPPercent() >= 60;
                    }
                    
                    if (!dontInterruptPendingAttack && unit.runFrom(null)) {
                        AtlantisPainter.paintCircle(unit, (int) safetyDistance * 32, Color.Red);
//                        AtlantisPainter.paintCircle(unit, enemyNearbyCountingRadius * 32, Color.Red);
//                        unit.setTooltip("Melee-run " + closeEnemy.getShortName());
                        unit.setTooltip("Melee-run (" + closeEnemy.getShortName() + ")");
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * @return <b>true</b> if any of the enemy units in range can shoot at this unit.
     */
    protected boolean isInShootRangeOfAnyEnemyUnit(AUnit unit) {
        Collection<AUnit> enemiesInRange = (Collection<AUnit>) Select.enemy().combatUnits().inRadius(12, unit).listUnits();
        for (AUnit enemy : enemiesInRange) {
            WeaponType enemyWeapon = (unit.isAirUnit() ? enemy.getAirWeapon() : enemy.getGroundWeapon());
            double distToEnemy = PositionUtil.distanceTo(unit, enemy);

            // Compare against max range
            if (distToEnemy + 0.5 <= enemyWeapon.maxRange()) {
                _nearestEnemyThatCanShootAtThisUnit = enemy;
                return true;
            }

            // Compare against min range
//            if () {
//                distToEnemy >= enemyWeapon.getMinRange()
//                return true;
//            }
        }

        // =========================================================
        _nearestEnemyThatCanShootAtThisUnit = null;
        return false;
    }

}
