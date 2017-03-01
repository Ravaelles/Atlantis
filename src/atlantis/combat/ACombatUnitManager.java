package atlantis.combat;

import atlantis.AGame;
import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.combat.micro.AAvoidMeleeUnitsManager;
import atlantis.combat.micro.ABadWeather;
import atlantis.combat.micro.AbstractMicroManager;
import atlantis.combat.micro.terran.TerranMedic;
import atlantis.combat.micro.terran.TerranSiegeTankManager;
import atlantis.combat.micro.terran.TerranVultureManager;
import atlantis.combat.micro.zerg.ZergOverlordManager;
import atlantis.combat.squad.Squad;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ACombatUnitManager extends AbstractMicroManager {

    protected static boolean update(AUnit unit) {
        
        // =========================================================
        // Don't INTERRUPT shooting units
        
        if (shouldNotDisturbUnit(unit)) {
            unit.setTooltip("#DontDisturb");
            return true;
        }

        // =========================================================
        // Avoid bad weather like raining Psionic Storm or active spider mines.
        
        if (ABadWeather.avoidPsionicStormAndActiveMines(unit)) {
            return true;
        }

        // =========================================================
        // Handle some units in special way
        
        if (handledAsSpecialUnit(unit)) {
            unit.setTooltip(unit.getShortName());
            return true;
        }
        
        // =========================================================
        // Handle some units in semi-special way
        
        if (handledAsSemiSpecialUnit(unit)) {
            return true;
        }

        // =========================================================
        // Avoid melee units
        if (AAvoidMeleeUnitsManager.avoidCloseMeleeUnits(unit)) {
            return true;
        }
        
        // =========================================================
        // Early mode - Attack enemy units when in range (and choose the best target)
        boolean isAllowedToAttackWhenRetreating = isAllowedToAttackWhenRetreating(unit);
        if (isAllowedToAttackWhenRetreating && AAttackEnemyUnit.handleAttackEnemyUnits(unit)) {
            return true;
        }
        
        // =========================================================
        // If we couldn't beat nearby enemies, retreat
        if (handleUnfavorableOdds(unit)) {
            return true;
        }
        
        // =========================================================
        // Normal mode - Attack enemy units when in range (and choose the best target)
        if (!isAllowedToAttackWhenRetreating && AAttackEnemyUnit.handleAttackEnemyUnits(unit)) {
            return true;
        }

        // =========================================================
        // =========================================================
        // === If we're here, it means mission manager is allowed ==
        // === to take control over this unit, due to no action   ==
        // === needed on tactics level (proceed to strategy).     ==
        // =========================================================
        // =========================================================

        Squad squad = unit.getSquad();
        
        if (squad == null) {
            System.err.println("Unit " + unit + " has no squad assigned.");
            unit.setTooltip("Empty squad!");
            return false;
        }
        else {
            unit.setTooltip("Mission:" + squad.getMission().getName());
            return squad.getMission().update(unit);
        }
    }

    // =========================================================
    /**
     *
     */
    private static boolean shouldNotDisturbUnit(AUnit unit) {
        return (unit.isAttackFrame() 
                || ((!unit.type().isTank() || unit.getGroundWeaponCooldown() <= 0) && unit.isStartingAttack())) 
                && unit.getGroundWeaponCooldown() <= 0 && unit.getAirWeaponCooldown() <= 0;
//        return false;
//        return (unit.isAttackFrame() || unit.isStartingAttack()) &&
//                unit.getGroundWeaponCooldown() <= 0 && unit.getAirWeaponCooldown() <= 0;
//        return unit.isAttackFrame() || (!unit.type().isTank() && unit.isStartingAttack());
//        return (unit.isAttackFrame());
    }

    /**
     * There are some units that should have individual micro managers like Zerg Overlord. If unit is special
     * unit it will run proper micro managers here and return true, meaning no other managers should be used.
     * False will give command to standard Melee of Micro managers.
     * 
     * 
     */
    private static boolean handledAsSpecialUnit(AUnit unit) {
        
        // === Terran ========================================
        if (AGame.playsAsTerran()) {
            
            // MEDIC
            if (unit.isType(AUnitType.Terran_Medic)) {
                unit.setTooltip("Medic");
                return TerranMedic.update(unit);
            }
        }
        
        // === Zerg ========================================
        
        else if (AGame.playsAsZerg()) {
            
            // OVERLORD
            if (unit.getType().equals(AUnitType.Zerg_Overlord)) {
                ZergOverlordManager.update(unit);
                return true;
            } 
        } 
        
        // =========================================================
        
        return false;
    }

    /**
     * There are some units that should have additional micro manager actions like Siege Tank. If unit is 
     * semi-special it will run its micro managers after other managers have been executed.
     */
    private static boolean handledAsSemiSpecialUnit(AUnit unit) {
        if (unit.getType().isSiegeTank()) {
            return TerranSiegeTankManager.update(unit);
        } 
        
        else if (unit.getType().isVulture()) {
            return TerranVultureManager.update(unit);
        } 
        
        // Not semi-special unit
        else {
            return false;
        }
    }

    /**
     * Some units like Reavers should open fire to nearby enemies even when retreating, otherwise they'll
     * just get destroyed without firing even once.
     */
    private static boolean isAllowedToAttackWhenRetreating(AUnit unit) {
        return unit.isType(AUnitType.Protoss_Reaver);
    }

}
