package atlantis.combat.micro;

import atlantis.AtlantisGame;
import atlantis.wrappers.SelectUnits;
import atlantis.wrappers.Units;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType.UnitTypes;

public class DefaultMeleeManager extends MicroMeleeManager {

    @Override
    public boolean update(Unit unit) {
        if (canIssueOrderToUnit(unit)) {

            // SPECIAL UNIT TYPE action
            if (handleSpecialUnit(unit)) {
                return true;
            }

            // =========================================================
            // Check health status
            if (handleLowHealthIfNeeded(unit)) {
                return true;
            }

            // =========================================================
            // Check chances to win the fight
            if (handleUnfavorableOdds(unit)) {
                return true;
            }

            // =========================================================
            // Don't spread too much
            if (handleDontSpreadTooMuch(unit)) {
                return true;
            }

            // =========================================================
            // Attack enemy is possible
            return handleAttackEnemyUnits(unit);
        } // =========================================================
        // Can't give orders to unit right now
        else {
            return true;
        }
    }

    // =========================================================
    private boolean canIssueOrderToUnit(Unit unit) {
        return !unit.isJustShooting();
    }

    private Unit defineEnemyToAttack(Unit unit) {
        Unit nearestEnemy = null;

        if (AtlantisGame.getTimeSeconds() < 180) {
            nearestEnemy = SelectUnits.enemyRealUnit().nearestTo(unit);
            if (nearestEnemy != null && nearestEnemy.isWorker()) {
                return null;
            }
        }

        // If no real units found, try selecting important buildings
        nearestEnemy = SelectUnits.enemy().ofType(
                UnitTypes.Protoss_Zealot, UnitTypes.Protoss_Dragoon,
                UnitTypes.Terran_Marine, UnitTypes.Terran_Medic, UnitTypes.Terran_Firebat,
                UnitTypes.Zerg_Zergling, UnitTypes.Zerg_Hydralisk
        ).nearestTo(unit);
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
        nearestEnemy = SelectUnits.enemy().ofType(
                UnitTypes.Protoss_Pylon, UnitTypes.Zerg_Spawning_Pool,
                UnitTypes.Terran_Command_Center
        ).nearestTo(unit);
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

    private boolean handleSpecialUnit(Unit unit) {
        if (unit.isType(UnitTypes.Terran_Medic)) {
            MicroMedic.update(unit);
            return true;
        }
        return false;
    }

    private boolean handleAttackEnemyUnits(Unit unit) {
        Unit enemyToAttack = defineEnemyToAttack(unit);
        if (enemyToAttack != null) {
            if (!unit.isAttacking() && unit.getGroundWeaponCooldown() <= 0 && !unit.isJustShooting()) {
//                unit.attackUnit(enemyToAttack, false);
                unit.attack(enemyToAttack, false);
                unit.setTooltip(">" + enemyToAttack.getShortName() + "<");
            }
            unit.setTooltip("...");
//            unit.removeTooltip();
            return true;
        }

        unit.setTooltip("No enemy");

        return false;
    }

    private boolean handleDontSpreadTooMuch(Unit unit) {
        Units ourForcesNearby = SelectUnits.ourCombatUnits().inRadius(7, unit).exclude(unit).units();
        Position goTo = null;
        if (ourForcesNearby.isEmpty()) {
            goTo = SelectUnits.ourCombatUnits().exclude(unit).first();
        } else if (ourForcesNearby.size() <= 4) {
            goTo = ourForcesNearby.positionMedian();
        }

        if (goTo != null && unit.distanceTo(goTo) > 5) {
            unit.move(goTo);
            unit.setTooltip("Stand closer");
            return true;
        }

        return false;
    }

}
