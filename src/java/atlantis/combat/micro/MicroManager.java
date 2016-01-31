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
     * If chances to win the skirmish with the nearby enemy units aren't favorable, safely retreat.
     */
    protected boolean handleUnfavorableOdds(Unit unit) {
        if (!AtlantisCombatEvaluator.isSituationFavorable(unit)) {
            if (unit.isJustShooting()) {
                return true;
            }

            return AtlantisRunManager.run(unit);
        }

        AtlantisRunManager.unitWantsStopRunning(unit);
        return false;
    }

    /**
     *
     */
    protected boolean handleNotExtremelyFavorableOdds(Unit unit) {
        if (!AtlantisCombatEvaluator.isSituationExtremelyFavorable(unit)) {
            if (isInShootRangeOfAnyEnemyUnit(unit)) {
                unit.moveAwayFrom(_nearestEnemyThatCanShootAtThisUnit, 2);
                return true;
            }
        }
        
        return false;
    }

    /**
     * If unit is severly wounded, it should run.
     */
    protected boolean handleLowHealthIfNeeded(Unit unit) {
        if (unit.getHP() <= 11) {
            return AtlantisRunManager.run(unit);
        }

        return false;
    }

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
