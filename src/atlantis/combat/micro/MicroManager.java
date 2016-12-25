package atlantis.combat.micro;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.combat.AtlantisCombatEvaluator;
import atlantis.combat.squad.Squad;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.util.PositionUtil;
import atlantis.wrappers.APosition;
import bwapi.WeaponType;
import java.util.Collection;

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
        boolean isSituationFavorable = AtlantisCombatEvaluator.isSituationFavorable(unit);
        
        // If situation is unfavorable, retreat
        if (!isSituationFavorable) {
            if (unit.isAttackFrame() || unit.isStartingAttack()) { //replacing isJustShooting
                unit.setTooltip("SHOOTS");
                return true;
            }
            else {
                unit.setTooltip("Runs");
                return AtlantisRunManager.run(unit);
            }
        }
        else {
            unit.setTooltip("---");
            
            // If unit is running, allow it to stop running only if chances are quite favorable
            if (unit.isRunning()) {
                AtlantisRunManager.unitWantsStopRunning(unit);
            }
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
        if (AtlantisGame.playsAsTerran()) {
            if (unit.getHP() <= 7) {
                AUnit rendezvousWithMedics = (AUnit) Select.ourBuildings().ofType(AtlantisConfig.BARRACKS).first();
                if (rendezvousWithMedics != null && rendezvousWithMedics.distanceTo(unit) > 5) {
                    unit.move(rendezvousWithMedics.getPosition());
                }
                return true;
            }
        }
        
        AUnit nearestEnemy = Select.nearestEnemy(unit.getPosition());
        if (nearestEnemy == null || PositionUtil.distanceTo(nearestEnemy, unit) > 6) {
            return false;
        }
        
        if (unit.getHitPoints() <= 16 || unit.getHPPercent() < 30) {
            if (Select.ourCombatUnits().inRadius(4, unit.getPosition()).count() <= 6) {
                return AtlantisRunManager.run(unit);
            }
        }

        return false;
    }

    /**
     * If e.g. Terran Marine stands too far forward, it makes him vulnerable. Make him go back.
     */
    protected boolean handleDontSpreadTooMuch(AUnit unit) {
        Squad squad = unit.getSquad();
        Select ourUnits = Select.from(squad.arrayList()).inRadius(10, unit.getPosition());
        int ourUnitsNearby = ourUnits.count();
        int minUnitsNearby = (int) (squad.size() * 0.66);
        
        // =========================================================

        if (ourUnitsNearby < minUnitsNearby && ourUnitsNearby <= 3) {
            APosition goTo = PositionUtil.averagePosition(ourUnits.list());
            if (goTo != null && goTo.distanceTo(unit) > 1) {
                unit.move(goTo);
                unit.setTooltip("Closer");
                return true;
            }
        }

        return false;
    }
    
    /**
     * @return <b>true</b> if any of the enemy units in range can shoot at this unit.
     */
    protected boolean isInShootRangeOfAnyEnemyUnit(AUnit unit) {
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
