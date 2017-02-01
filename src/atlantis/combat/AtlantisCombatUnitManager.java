package atlantis.combat;

import atlantis.AtlantisGame;
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

    protected static boolean update(AUnit unit, Squad squad) {
//        unit.removeTooltip();
//        unit.setTooltip("M:" + AtlantisGame.getTimeFrames());
//        System.out.println("  Manager (" + (unit.getSquad() != null) + "): " + unit + " / " + unit.getTooltip());
        
        // =========================================================
        // DON'T INTERRUPT shooting units
        
        if (shouldNotDisturbUnit(unit)) {
            unit.setTooltip("#DontDisturb");
            return true;
        }

        // =========================================================
        // Handle some units in special way
        
        if (handledAsSpecialUnit(unit)) {
            return true;
        }

        // =========================================================
        // Act with proper micro-manager and decide if mission manager can issue orders afterward.
        boolean microManagerForbidsOtherActions = false;
        if (squad != null) {
            microManagerForbidsOtherActions = squad.getMicroManager().update(unit);
        }
        else {
            System.err.println(unit + " sq null for unit " + unit);
        }

        // MICRO-MANAGER indicates that its orders should not be overriden by mission manager
        if (microManagerForbidsOtherActions) {
            return true;
        } 
        
        // =========================================================
        // Handle some units in semi-special way
        
        if (handledAsSemiSpecialUnit(unit)) {
            unit.setTooltip("semiSpecial");
            return true;
        }

        // =========================================================
        // It's okay to handle MISSION orders according to current mission (e.g. DEFEND, ATTACK)
        else {
            if (squad == null) {
                System.err.println("squad is NULLz!");
                System.err.println(unit + " sq null for unit " + unit);
                unit.setTooltip("squadIsNull");
                return true;
            }
            else if (squad.getMission() == null) {
                System.err.println("squad.getMission() is NULL!");
                unit.setTooltip("missionIsNull");
                return true;
            }
            else {
                unit.setTooltip("updateMission");
                return squad.getMission().update(unit);
            }
        }
    }

    // =========================================================
    /**
     *
     */
    private static boolean shouldNotDisturbUnit(AUnit unit) {
//        return false;
        return unit.isAttackFrame() || unit.isStartingAttack();
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
            unit.setTooltip("Overlord");
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
