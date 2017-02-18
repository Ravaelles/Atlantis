package atlantis.combat;

import atlantis.AGame;
import atlantis.combat.micro.terran.TerranSiegeTankManager;
import atlantis.combat.micro.zerg.ZergOverlordManager;
import atlantis.combat.squad.Squad;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisCombatUnitManager {

    protected static boolean update(AUnit unit) {
//        unit.removeTooltip();
        unit.setTooltip("Commander:" + AGame.getTimeFrames());
//        System.out.println("  Manager (" + (unit.getSquad() != null) + "): " + unit + " / " + unit.getTooltip());
        
        // =========================================================
        // DON'T INTERRUPT shooting units
        
//        if (unit.getType().isTank()) {
//            System.out.println("----------------------------");
//            System.out.println(unit + " PRE DISTURB");
//        }
        
        if (shouldNotDisturbUnit(unit)) {
            unit.setTooltip("#DontDisturb");
            return true;
        }

        // =========================================================
        // Handle some units in special way
        
//        if (unit.getType().isTank()) {
//            System.out.println(unit + " PRE specialUnit");
//        }
            
        if (handledAsSpecialUnit(unit)) {
            unit.setTooltip(unit.getShortName());
            return true;
        }
        
        // =========================================================
        // Handle some units in semi-special way
        
//        if (unit.getType().isTank()) {
//            System.out.println(unit + " PRE SemiSpecial");
//        }
//        
        if (handledAsSemiSpecialUnit(unit)) {
            unit.setTooltip("Siege Tank");
            return true;
        }

        // =========================================================
        // Act with proper micro-manager and decide if mission manager can issue orders afterward.
        Squad squad = unit.getSquad();
        
//        if (unit.getType().isTank()) {
//            System.out.println(unit + " PRE Micro");
//        }
        
        boolean isMissionManagerControlForbbiden = squad.getMicroManager().update(unit);
        
//        if (unit.getType().isTank()) {
//            System.out.println(unit + " mission control: " + !isMissionManagerControlForbbiden);
//        }

        // =========================================================
        // MISSION manager execution is FORBIDDEN
        if (isMissionManagerControlForbbiden) {
            return true;
        } 

        // MISSION manager exection is ALLOWED
        else {
//            if (squad == null) {
//                System.err.println("squad is NULLz!");
//                System.err.println(unit + " sq null for unit " + unit);
//                unit.setTooltip("squadIsNull");
//                return true;
//            }
//            else if (squad.getMission() == null) {
//                System.err.println("squad.getMission() is NULL!");
//                unit.setTooltip("missionIsNull");
//                return true;
//            }
//            else {
            unit.setTooltip("Mission:" + squad.getMission().getName());
            return squad.getMission().update(unit);
//            }
        }
    }

    // =========================================================
    /**
     *
     */
    private static boolean shouldNotDisturbUnit(AUnit unit) {
        return (unit.isAttackFrame() || (!unit.type().isTank() && unit.isStartingAttack())) &&
                unit.getGroundWeaponCooldown() <= 0 && unit.getAirWeaponCooldown() <= 0;
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
        if (unit.getType().equals(AUnitType.Zerg_Overlord)) {
            ZergOverlordManager.update(unit);
            return true;
        } else {
            return false;
        }
    }

    /**
     * There are some units that should have additional micro manager actions like Siege Tank. If unit is 
     * semi-special it will run its micro managers after other managers have been executed.
     */
    private static boolean handledAsSemiSpecialUnit(AUnit unit) {
        if (unit.getType().isSiegeTank()) {
            boolean dontDoAnythingElse = TerranSiegeTankManager.update(unit);
            return dontDoAnythingElse;
        } else {
            return false;
        }
    }

}
