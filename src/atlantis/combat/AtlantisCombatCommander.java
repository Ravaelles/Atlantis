package atlantis.combat;

import atlantis.combat.group.AtlantisGroupManager;
import atlantis.combat.group.Group;
import atlantis.combat.group.missions.Missions;
import atlantis.combat.micro.zerg.ZergOverlordManager;
import atlantis.debug.tooltip.TooltipManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

import bwapi.UnitType;

public class AtlantisCombatCommander {
    
    /**
     * Acts with all battle units.
     */
    public static void update() {
        Missions.handleGlobalMission();
        handleAllBattleGroups();
    }

    // =========================================================
    
    /**
     * Acts with all units that are part of given battle group, according to the GroupMission object and using
     * proper micro managers.
     */
    private static void handleBattleGroup(Group group) {

        // Make sure this battle group has up-to-date strategy
        if (!Missions.getCurrentGlobalMission().equals(group.getMission())) {
            group.setMission(Missions.getCurrentGlobalMission());
        }

        // =========================================================
        // =========================================================
        // =========================================================
        // Act with every unit
        for (AUnit unit : group.arrayList()) {

            // DON'T INTERRUPT shooting units
            if (shouldNotDisturbUnit(unit)) {
                continue;
            }

            // =========================================================
            // Handle MICRO-MANAGERS for given unit according to its type
            if (!handledAsSpecialUnit(unit)) {
                boolean microManagerForbidsOtherActions;
//                if (unit.isRangedUnit()) {
//                    group.getMicroRangedManager().update(unit);
//                } else {
                microManagerForbidsOtherActions = group.getMicroMeleeManager().update(unit);
//                }

                // =========================================================
                // Do ONLY MICRO-MANAGER actions
                if (microManagerForbidsOtherActions) {
                    continue;
                }
                
                // =========================================================
                // Handle MISSION actions according to current mission (e.g. DEFEND, ATTACK)
                else {
//                    if (!unit.isMoving() && !unit.isAttacking() && !unit.isJustShooting()) {
                        group.getMission().update(unit);
//                    }
                }
            }
        }
    }

    /**
     * There are some units that should have individual micro managers like Zerg Overlord. If unit is special
     * unit it will run proper micro managers here and return true, meaning no other managers should be used.
     * False will give command to standard Melee of Micro managers.
     */
    private static boolean handledAsSpecialUnit(AUnit unit) {
        if (unit.getType().equals(AUnitType.Zerg_Overlord)) {
            ZergOverlordManager.update(unit);
            TooltipManager.setTooltip(unit, "Overlord");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Acts with all (combat) units that are part of a unit group.
     */
    private static void handleAllBattleGroups() {
        for (Group group : AtlantisGroupManager.getGroups()) {
            handleBattleGroup(group);
        }
    }

    // =========================================================
    
    private static boolean shouldNotDisturbUnit(AUnit unit) {
    	//this below replaces unit.isJustShooting()
        return unit.isAttackFrame() || unit.isStartingAttack();
    }

}
