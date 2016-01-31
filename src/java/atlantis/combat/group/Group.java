package atlantis.combat.group;

import atlantis.combat.group.missions.Mission;
import atlantis.combat.micro.DefaultMeleeManager;
import atlantis.combat.micro.DefaultRangedManager;
import atlantis.combat.micro.MicroMeleeManager;
import atlantis.combat.micro.MicroRangedManager;
import atlantis.wrappers.Units;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;
import jnibwapi.Position;
import jnibwapi.Unit;

/**
 * Represents battle group (unit squad) that contains multiple battle units (could be one unit as well).
 */
public class Group extends Units {

    private static int firstFreeID = 1;
    private int ID = firstFreeID++;

    /**
     * Convenience name for the group e.g. "Alpha", "Bravo", "Delta".
     */
    private String name;

    /**
     * Current mission object for this group.
     */
    private Mission mission;

    /**
     * Manager for microing ranged units.
     */
    private MicroRangedManager microRangedManager;

    /**
     * Manager for microing melee units.
     */
    private MicroMeleeManager microMeleeManager;

    // =========================================================
    
    private Group(String name, Mission mission) {
        super();
        this.name = name;
        this.mission = mission;
        this.microRangedManager = new DefaultRangedManager();
        this.microMeleeManager = new DefaultMeleeManager();
    }

    // =========================================================
    
    /**
     * Creates new group, designated by the given name. If <b>name</b> is null, default numeration "Alpha",
     * "Bravo", "Charlie", "Delta" will be used.
     */
    public static Group createNewGroup(String name, Mission mission) {

        // Name is null, use autonaming
        if (name == null) {
            String[] names = new String[]{"Alpha", "Bravo", "Charlie", "Delta", "Echo"};
            name = names[AtlantisGroupManager.groups.size()];
        }

        Group group = new Group(name, mission);
        return group;
    }

    // =========================================================
    
    /**
     * Returns median <b>position</b> of all units. It's better than the average, because the outliners
     * don't affect the end result so badly.
     */
    public Position getMedianUnitPosition() {
        ArrayList<Integer> xCoords = new ArrayList<>();
        ArrayList<Integer> yCoords = new ArrayList<>();
        
        for (Unit unit : list()) {
            xCoords.add(unit.getPX());
            yCoords.add(unit.getPY());
        }
        
        Collections.sort(xCoords);
        Collections.sort(yCoords);
        
        return new Position(xCoords.get(xCoords.size() / 2), yCoords.get(yCoords.size() / 2));
    }
    
    // =========================================================
    
    /**
     * Convenience name for the group e.g. "Alpha", "Bravo", "Charlie", "Delta".
     */
    public String getName() {
        return name;
    }

    /**
     * Convenience name for the group e.g. "Alpha", "Bravo", "Charlie", "Delta".
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Current mission object for this group.
     */
    public Mission getMission() {
        return mission;
    }

    /**
     * Current mission object for this group.
     */
    public void setMission(Mission mission) {
        this.mission = mission;
    }

    /**
     * Manager for microing ranged units.
     */
    public MicroRangedManager getMicroRangedManager() {
        return microRangedManager;
    }

    /**
     * Manager for microing ranged units.
     */
    public void setMicroRangedManager(MicroRangedManager microRangedManager) {
        this.microRangedManager = microRangedManager;
    }

    /**
     * Manager for microing melee units.
     */
    public MicroMeleeManager getMicroMeleeManager() {
        return microMeleeManager;
    }

    /**
     * Manager for microing melee units.
     */
    public void setMicroMeleeManager(MicroMeleeManager microMeleeManager) {
        this.microMeleeManager = microMeleeManager;
    }

    /**
     * Returns ID for this battle group (1, 2, 3, 4 etc).
     */
    public int getID() {
        return ID;
    }

}
