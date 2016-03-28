package atlantis.combat.micro;

import java.util.Collection;

import atlantis.combat.AtlantisCombatEvaluator;
import atlantis.units.AUnit;
import atlantis.util.PositionUtil;
import atlantis.util.UnitUtil;
import atlantis.units.Select;

import bwapi.WeaponType;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public abstract class MicroManager {
    
    private static AUnit _nearestEnemyThatCanShootAtThisUnit = null;
    
    // =========================================================

    /**
     * If chances to win the skirmish with the nearby enemy units aren't favorable, avoid fight and retreat.
     */
    protected boolean handleUnfavorableOdds(AUnit unit) {
        
        // If situation is unfavorable, retreat
        if (!AtlantisCombatEvaluator.isSituationFavorable(unit)) {
            if (unit.isAttackFrame() || unit.isStartingAttack()) { //replacing isJustShooting
                return true;
            }
            else {
                return AtlantisRunManager.run(unit);
            }
        }

        // If unit is running, allow it to stop running only if chances are quite favorable
        if (AtlantisRunning.isRunning(unit) && AtlantisCombatEvaluator.evaluateSituation(unit) >= 0.3) {
            AtlantisRunManager.unitWantsStopRunning(unit);
        }
        
        return false;
    }

    /**
     * If combat evaluator tells us that the potential skirmish with nearby enemies wouldn't result in 
     * decisive victory either retreat or stand where you are.
     */
    protected boolean handleNotExtremelyFavorableOdds(AUnit unit) {
        if (!AtlantisCombatEvaluator.isSituationExtremelyFavorable(unit)) {
            if (isInShootRangeOfAnyEnemyUnit(unit)) {
//                unit.moveAwayFrom(_nearestEnemyThatCanShootAtThisUnit, 2);
//                return true;
                return AtlantisRunManager.run(unit);
            }
        }
        
        return false;
    }

    /**
     * If unit is severly wounded, it should run.
     */
    protected boolean handleLowHealthIfNeeded(AUnit unit) {
        AUnit nearestEnemy = Select.nearestEnemy(unit.getPosition());
        if (nearestEnemy == null || PositionUtil.distanceTo(nearestEnemy, unit) > 6) {
            return false;
        }
        
        if (unit.getHitPoints() <= 16 || UnitUtil.getHPPercent(unit) < 30) {
            if (Select.ourCombatUnits().inRadius(4, unit.getPosition()).count() <= 6) {
                return AtlantisRunManager.run(unit);
            }
        }

        return false;
    }

    /**
     * @return <b>true</b> if any of the enemy units can shoot at this unit.
     */
    private boolean isInShootRangeOfAnyEnemyUnit(AUnit unit) {
    	Collection<AUnit> enemiesInRange = (Collection<AUnit>) Select.enemy().combatUnits().inRadius(12, unit.getPosition()).listUnits();
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
