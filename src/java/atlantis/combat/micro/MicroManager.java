package atlantis.combat.micro;

import atlantis.combat.AtlantisCombatEvaluator;
import atlantis.wrappers.SelectUnits;
import jnibwapi.Unit;
import jnibwapi.types.WeaponType;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public abstract class MicroManager {
    
    private static Unit _nearestEnemyThatCanShootAtThisUnit = null;
    
    // =========================================================

    /**
     * If chances to win the skirmish with the nearby enemy units aren't favorable, avoid fight and retreat.
     */
    protected boolean handleUnfavorableOdds(Unit unit) {
        
        // If situation is unfavorable, retreat
        if (!AtlantisCombatEvaluator.isSituationFavorable(unit)) {
            if (unit.isJustShooting()) {
                return true;
            }
            else {
                return AtlantisRunManager.run(unit);
            }
        }

        // If unit is running, allow it to stop running only if chances are quite favorable
        if (unit.isRunning() && AtlantisCombatEvaluator.evaluateSituation(unit) >= 0.3) {
            AtlantisRunManager.unitWantsStopRunning(unit);
        }
        
        return false;
    }

    /**
     * If combat evaluator tells us that the potential skirmish with nearby enemies wouldn't result in 
     * decisive victory either retreat or stand where you are.
     */
    protected boolean handleNotExtremelyFavorableOdds(Unit unit) {
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
    protected boolean handleLowHealthIfNeeded(Unit unit) {
        Unit nearestEnemy = SelectUnits.nearestEnemy(unit);
        if (nearestEnemy == null || nearestEnemy.distanceTo(unit) > 6) {
            return false;
        }
        
        if (unit.getHP() <= 16 || unit.getHPPercent() < 30) {
            if (SelectUnits.ourCombatUnits().inRadius(4, unit).count() <= 6) {
                return AtlantisRunManager.run(unit);
            }
        }

        return false;
    }

    /**
     * @return <b>true</b> if any of the enemy units can shoot at this unit.
     */
    private boolean isInShootRangeOfAnyEnemyUnit(Unit unit) {
        for (Unit enemy : SelectUnits.enemy().combatUnits().inRadius(12, unit).list()) {
            WeaponType enemyWeapon = (unit.isAirUnit() ? enemy.getAirWeapon() : enemy.getGroundWeapon());
            double distToEnemy = unit.distanceTo(enemy);
            
            // Compare against max range
            if (distToEnemy + 0.5 <= enemyWeapon.getMaxRange()) {
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
