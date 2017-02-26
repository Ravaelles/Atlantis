package atlantis.combat.squad;

import atlantis.combat.squad.missions.Mission;
import atlantis.combat.squad.missions.Missions;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Units;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents battle squad (unit squad) that contains multiple battle units (could be one unit as well).
 */
public class Squad extends Units {

    private static int firstFreeID = 1;
    private int ID = firstFreeID++;

    /**
     * Auxilary name for the squad e.g. "Alpha", "Bravo", "Delta".
     */
    private String name;

    /**
     * Current mission object for this squad.
     */
    private Mission mission;

    /**
     * Manager that handles microing of units.
     */
//    private AbstractMicroManager microManager;
    
    // =========================================================
    
    private Squad(String name, Mission mission) {
        super();
        this.name = name;
        this.setMission(mission);
//        this.setMicroManager(new AMicroManager());
    }

    // =========================================================
    
    /**
     * Creates new squad, designated by the given name. If <b>name</b> is null, default numeration "Alpha",
     * "Bravo", "Charlie", "Delta" will be used.
     */
    public static Squad createNewSquad(String name, Mission mission) {

        // Name is null, use autonaming
        if (name == null) {
            String[] names = new String[]{"Alpha", "Bravo", "Charlie", "Delta", "Echo"};
            name = names[ASquadManager.squads.size()];
        }

        Squad squad = new Squad(name, mission);
        return squad;
    }

    // =========================================================
    
    /**
     * Returns median <b>position</b> of all units. It's better than the average, because the outliners
     * don't affect the end result so badly.
     */
    public APosition getMedianUnitPosition() {
        ArrayList<Integer> xCoords = new ArrayList<>();
        ArrayList<Integer> yCoords = new ArrayList<>();
        
        for (AUnit unit : list()) {
            xCoords.add(unit.getPosition().getX());
            yCoords.add(unit.getPosition().getY());
        }
        
        Collections.sort(xCoords);
        Collections.sort(yCoords);
        
        return new APosition(xCoords.get(xCoords.size() / 2), yCoords.get(yCoords.size() / 2));
    }
    
    // =========================================================
    
    /**
     * Convenience name for the squad e.g. "Alpha", "Bravo", "Charlie", "Delta".
     */
    public String getName() {
        return name;
    }

    /**
     * Convenience name for the squad e.g. "Alpha", "Bravo", "Charlie", "Delta".
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Current mission object for this squad.
     */
    public Mission getMission() {
        return mission;
    }

    /**
     * Current mission object for this squad.
     */
    public void setMission(Mission mission) {
        if (mission == null) {
            throw new RuntimeException("Assigned null Mission to squad");
        }
        this.mission = mission;
    }

    /**
     * Manager for microing units.
     */
//    public AbstractMicroManager getMicroManager() {
//        return microManager;
//    }
//
//    /**
//     * Manager for microing units.
//     */
//    public void setMicroManager(AbstractMicroManager microManager) {
//        if (microManager == null) {
//            throw new RuntimeException("Assigned null MicroManager to squad");
//        }
//        this.microManager = microManager;
//    }

    /**
     * Returns ID for this battle squad (1, 2, 3, 4 etc).
     */
    public int getID() {
        return ID;
    }
    
    // =========================================================

    public boolean isMissionDefend() {
        return Missions.DEFEND.equals(getMission());
    }

}
