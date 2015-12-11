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
//                if (MicroMedic.update(unit)) {
//                    return;
//                }
                return;
            }

            // =========================================================
            // STANDARD actions
            Unit nearestEnemy = null;

            // If no real units found, try selecting important buildings
            nearestEnemy = SelectUnits.enemy().ofType(
                    UnitTypes.Protoss_Zealot, UnitTypes.Protoss_Dragoon,
                    UnitTypes.Terran_Marine, UnitTypes.Terran_Medic, UnitTypes.Terran_Firebat,
                    UnitTypes.Zerg_Zergling, UnitTypes.Zerg_Hydralisk
            ).nearestTo(unit);
            if (nearestEnemy == null) {
                nearestEnemy = SelectUnits.enemy().ofType(
                        UnitTypes.Protoss_Pylon, UnitTypes.Zerg_Spawning_Pool,
                        UnitTypes.Terran_Command_Center
                ).nearestTo(unit);
                if (nearestEnemy != null) {
                    unit.setTooltip("Important building");
                }
            }

            // Try selecting real units
            nearestEnemy = SelectUnits.enemyRealUnit().nearestTo(unit);
            if (nearestEnemy != null) {
                unit.setTooltip("Engage unit");
            }

            // If no real units found, try selecting important buildings
            if (nearestEnemy == null) {
                nearestEnemy = SelectUnits.enemy().ofType(
                        UnitTypes.Protoss_Pylon, UnitTypes.Zerg_Spawning_Pool,
                        UnitTypes.Terran_Command_Center
                ).nearestTo(unit);
                if (nearestEnemy != null) {
                    unit.setTooltip("Important building");
                }
            }

            // Okay, try targeting any-fuckin-thing
            if (nearestEnemy == null) {
                nearestEnemy = SelectUnits.enemy().nearestTo(unit);
                if (nearestEnemy != null) {
                    unit.setTooltip("Attack building");
                }
            }

            if (unit.getGroundWeaponCooldown() == 0 && !unit.isAttacking()) {
                unit.attackUnit(nearestEnemy, false);
            } else if (nearestEnemy != null) {
                unit.removeTooltip();
            }
        }
    }

// =========================================================
    private boolean canIssueOrderToUnit(Unit unit) {
        return !unit.isAttacking() && !unit.isStartingAttack() && !unit.isAttackFrame();
    }

}
