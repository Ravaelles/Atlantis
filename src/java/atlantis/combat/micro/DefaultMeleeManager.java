package atlantis.combat.micro;

import atlantis.wrappers.SelectUnits;
import jnibwapi.Unit;
import jnibwapi.types.UnitType.UnitTypes;

public class DefaultMeleeManager extends MicroMeleeManager {

    @Override
    public void update(Unit unit) {
        if (canIssueOrderToUnit(unit)) {

            // SPECIAL UNIT TYPE action
            if (unit.isType(UnitTypes.Terran_Medic)) {
                MicroMedic.update(unit);
                return;
            }

            // =========================================================
            // Check health status
            if (handleLowHealthIfNeeded(unit)) {
                return;
            }

            // =========================================================
            // Check chances to win the fight
            if (handleUnfavorableOdds(unit)) {
                return;
            }

            // =========================================================
            // Define target to attack
            Unit enemyToAttack = defineEnemyToAttack(unit);

            // =========================================================
            // Attack enemy is possible
            if (enemyToAttack != null) {
                if (unit.getGroundWeaponCooldown() == 0 && !unit.isAttacking()) {
                    unit.attackUnit(enemyToAttack, false);
                }
                unit.removeTooltip();
            }
        }
    }

    // =========================================================
    private boolean canIssueOrderToUnit(Unit unit) {
        return !unit.isAttacking() && !unit.isStartingAttack() && !unit.isAttackFrame();
    }

    private Unit defineEnemyToAttack(Unit unit) {
        Unit nearestEnemy;

        // If no real units found, try selecting important buildings
        nearestEnemy = SelectUnits.enemy().ofType(
                UnitTypes.Protoss_Zealot, UnitTypes.Protoss_Dragoon,
                UnitTypes.Terran_Marine, UnitTypes.Terran_Medic, UnitTypes.Terran_Firebat,
                UnitTypes.Zerg_Zergling, UnitTypes.Zerg_Hydralisk
        ).nearestTo(unit);
        if (nearestEnemy != null) {
            unit.setTooltip("Attack unit");
            return nearestEnemy;
        }

        nearestEnemy = SelectUnits.enemy().ofType(
                UnitTypes.Protoss_Pylon, UnitTypes.Zerg_Spawning_Pool,
                UnitTypes.Terran_Command_Center
        ).nearestTo(unit);
        if (nearestEnemy != null) {
            unit.setTooltip("Important building");
            return nearestEnemy;
        }

        // Try selecting real units
        nearestEnemy = SelectUnits.enemyRealUnit().nearestTo(unit);
        if (nearestEnemy != null) {
            unit.setTooltip("Engage unit");
            return nearestEnemy;
        }

        // If no real units found, try selecting important buildings
        nearestEnemy = SelectUnits.enemy().ofType(
                UnitTypes.Protoss_Pylon, UnitTypes.Zerg_Spawning_Pool,
                UnitTypes.Terran_Command_Center
        ).nearestTo(unit);
        if (nearestEnemy != null) {
            unit.setTooltip("Important building");
            return nearestEnemy;
        }

        // Okay, try targeting any-fuckin-thing
        nearestEnemy = SelectUnits.enemy().nearestTo(unit);
        if (nearestEnemy != null) {
            unit.setTooltip("Attack building");
            return nearestEnemy;
        }

        return nearestEnemy;
    }

}
