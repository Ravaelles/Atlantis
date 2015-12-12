package atlantis.combat;

import atlantis.combat.group.AtlantisGroupManager;
import atlantis.combat.group.Group;
import atlantis.combat.group.missions.Mission;
import atlantis.combat.group.missions.Missions;
import atlantis.combat.micro.zerg.ZergOverlordManager;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

public class AtlantisCombatCommander {

    /**
     * This is the mission for main battle group forces. E.g. initially it will be DEFEND, then it should be
     * PREPARE (go near enemy) and then ATTACK.
     */
    private static Mission currentGlobalMission;

    // =========================================================
    /**
     * Acts with all battle units.
     */
    public static void update() {
        handleGlobalMission();
        handleAllBattleGroups();
    }

    // =========================================================
    /**
     * Acts with all units that are part of given battle group, according to the GroupMission object and using
     * proper micro managers.
     */
    private static void handleBattleGroup(Group group) {

        // Make sure this battle group has up-to-date strategy
        if (!currentGlobalMission.equals(group.getMission())) {
            group.setMission(currentGlobalMission);
        }

        // Act with every unit
        for (Unit unit : group.arrayList()) {

            // Never interrupt shooting units
            if (shouldNotDisturbUnit(unit)) {
                return;
            }

            // Handle micro-managers for given unit according to its type
            if (!handleSpecialUnit(unit)) {
                if (unit.isRangedUnit()) {
                    System.err.println(unit + " " + unit.getType());
                    unit.setTooltip("Ranged");
                    group.getMicroRangedManager().update(unit);
                } else {
                    unit.setTooltip("Melee");
                    group.getMicroMeleeManager().update(unit);
                }
            }

            // Handle generic actions according to current mission (e.g. DEFEND, ATTACK)
            if (!unit.isAttacking() && !unit.isStartingAttack()) {
//                unit.setTooltip("Mission");
                group.getMission().update(unit);
            }

//            // Handle generic actions according to current mission (e.g. DEFEND,
//            // ATTACK)
//            boolean microDisallowed = group.getMission().update(unit);
//
//            if (!microDisallowed) {
//
//                // Handle micro-managers for given unit according to its type
//                if (unit.isMeleeUnit()) {
//                    group.getMicroMeleeManager().update(unit);
//                } else {
//                    group.getMicroRangedManager().update(unit);
//                }
//            }
        }
    }

    /**
     * There are some units that should have individual micro managers like Zerg Overlord. If unit is special
     * unit it will run proper micro managers here and return true, meaning no other managers should be used.
     * False will give command to standard Melee of Micro managers.
     */
    private static boolean handleSpecialUnit(Unit unit) {
        if (unit.getType().equals(UnitType.UnitTypes.Zerg_Overlord)) {
            ZergOverlordManager.update(unit);
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

    /**
     * Takes care of current strategy.
     */
    private static void handleGlobalMission() {
        if (currentGlobalMission == null) {
            currentGlobalMission = Missions.ATTACK;
        }
    }

    // =========================================================
    private static boolean shouldNotDisturbUnit(Unit unit) {
        return unit.isStartingAttack() || unit.isAttackFrame();
    }

}
