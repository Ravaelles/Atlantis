package atlantis.combat.group;

import atlantis.combat.group.missions.Missions;
import java.util.ArrayList;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

/**
 * Commands all existing battle groups.
 */
public class AtlantisGroupManager {

    /**
     * List of all unit groups.
     */
    protected static ArrayList<Group> groups = new ArrayList<>();

    // =========================================================
    public static void possibleCombatUnitCreated(Unit unit) {
        if (shouldSkipUnit(unit)) {
            return;
        }

        Group group = getAlphaGroup();
        group.addUnit(unit);
        unit.setGroup(group);
    }

    public static void battleUnitDestroyed(Unit unit) {
        if (shouldSkipUnit(unit)) {
            return;
        }

        Group group = unit.getGroup();
        if (group != null) {
            group.removeUnit(unit);
            unit.setGroup(null);
        }
    }

    private static boolean shouldSkipUnit(Unit unit) {
        return unit.isBuilding() || unit.isWorker() || unit.isType(UnitType.UnitTypes.Zerg_Larva);
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
