package atlantis.combat.micro;

import atlantis.AtlantisGame;
import atlantis.wrappers.SelectUnits;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisEnemyTargeting {

    public static Unit defineEnemyToAttackFor(Unit unit) {
        Unit nearestEnemy = null;
        if (AtlantisGame.getTimeSeconds() < 180) {
            nearestEnemy = SelectUnits.enemyRealUnit().nearestTo(unit);
            if (nearestEnemy != null && nearestEnemy.isWorker() && nearestEnemy.distanceTo(SelectUnits.mainBase()) < 30) {
//                return null;
            }
            else {
                unit.setTooltip("Attack worker");
                return nearestEnemy;
            }
        }
        // If no real units found, try selecting important buildings
        nearestEnemy = SelectUnits.enemy().ofType(UnitType.UnitTypes.Protoss_Zealot, UnitType.UnitTypes.Protoss_Dragoon, UnitType.UnitTypes.Terran_Marine, UnitType.UnitTypes.Terran_Medic, UnitType.UnitTypes.Terran_Firebat, UnitType.UnitTypes.Zerg_Zergling, UnitType.UnitTypes.Zerg_Hydralisk).nearestTo(unit);
        if (nearestEnemy != null) {
            //            System.out.println("Nearest enemy is: " + nearestEnemy + " (dist: " + (nearestEnemy != null ? nearestEnemy.distanceTo(unit) : ""));
            unit.setTooltip("Attack " + nearestEnemy.getType().getShortName());
            return nearestEnemy;
        }
        // Try selecting real units
        nearestEnemy = SelectUnits.enemyRealUnit().nearestTo(unit);
        if (nearestEnemy != null) {
            unit.setTooltip("Engage " + nearestEnemy.getType().getShortName());
            return nearestEnemy;
        }
        // If no real units found, try selecting important buildings
        nearestEnemy = SelectUnits.enemy().ofType(UnitType.UnitTypes.Protoss_Pylon, UnitType.UnitTypes.Zerg_Spawning_Pool, UnitType.UnitTypes.Terran_Command_Center).nearestTo(unit);
        if (nearestEnemy != null) {
            unit.setTooltip("Building: " + nearestEnemy.getType().getShortName());
            return nearestEnemy;
        }
        // Okay, try targeting any-fuckin-thing
        nearestEnemy = SelectUnits.enemy().nearestTo(unit);
        if (nearestEnemy != null) {
            unit.setTooltip("building" + nearestEnemy.getType().getShortName());
            return nearestEnemy;
        }
        return nearestEnemy;
    }
    
}
