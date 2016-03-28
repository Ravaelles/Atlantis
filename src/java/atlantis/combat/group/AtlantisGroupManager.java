package atlantis.combat.group;

import atlantis.combat.group.missions.Missions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import java.util.ArrayList;

import bwapi.UnitType;

/**
 * Commands all existing battle groups.
 */
public class AtlantisGroupManager {

    /**
     * List of all unit groups.
     */
    protected static ArrayList<Group> groups = new ArrayList<>();

    // =========================================================
    public static void possibleCombatUnitCreated(AUnit unit) {
        if (shouldSkipUnit(unit)) {
            return;
        }

        Group group = getAlphaGroup();
        group.addUnit(unit);
        Group.setGroupOfUnit(unit, group); //unit.setGroup(group);
    }

    public static void battleUnitDestroyed(AUnit unit) {
        if (shouldSkipUnit(unit)) {
            return;
        }

        Group group = Group.getGroupOfUnit(unit);	// unit.getGroup();
        if (group != null) {
            group.removeUnit(unit);
            Group.setGroupOfUnit(unit, null);		//unit.setGroup(null);
        }
    }

    /**
     * Skips buildings, workers and Zerg Larva
     * @param unit
     * @return
     */
    private static boolean shouldSkipUnit(AUnit unit) {
        return unit.getType().isBuilding() || unit.isWorker() || unit.getType().equals(AUnitType.Zerg_Larva);
    }

    // =========================================================
    // Manage squads
    /**
     * Get first, main group of units.
     */
    public static Group getAlphaGroup() {

        // If no group exists, create main group
        if (groups.isEmpty()) {
            Group unitGroup = Group.createNewGroup(null, Missions.getInitialMission());
            groups.add(unitGroup);
        }

        return groups.get(0);
    }

    // =========================================================
    // Getters & Setters
    public static ArrayList<Group> getGroups() {
        return groups;
    }

    public static void setGroups(ArrayList<Group> groups) {
        AtlantisGroupManager.groups = groups;
    }

}
