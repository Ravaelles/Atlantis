package atlantis.combat.squad;

import atlantis.combat.micro.DefaultMeleeManager;
import atlantis.combat.micro.DefaultRangedManager;
import atlantis.combat.micro.MicroMeleeManager;
import atlantis.combat.micro.MicroRangedManager;
import atlantis.combat.squad.missions.Mission;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.wrappers.APosition;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents battle squad (unit squad) that contains multiple battle units (could be one unit as well).
 */
public class Squad extends Units {

    private static int firstFreeID = 1;
    private int ID = firstFreeID++;

    /**
     * Convenience name for the squad e.g. "Alpha", "Bravo", "Delta".
     */
    private String name;

    /**
     * Current mission object for this squad.
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
    
    /**
     * Stores the squad that each unit belongs to. Intends to replace Unit.setSquad() and getSquad() methods.
     */
//    private static java.util.Map<AUnit, Squad> squadOfUnit = new HashMap<>();
    
    
    /**
     * Stores that a AUnit belongs to a Squad
     * @param unit
     * @param g
     */
//    public static void setSquadOfUnit(AUnit unit, Squad g){
//    	squadOfUnit.put(unit, g);
//    }
    
    /**
     * Retrieves the squad that the unit belongs to
     * @param unit
     * @return
     */
//    public static Squad getSquadOfUnit(AUnit unit){
//    	return squadOfUnit.get(unit);
//    }

    // =========================================================
    
    private Squad(String name, Mission mission) {
        super();
        this.name = name;
        this.mission = mission;
        this.microRangedManager = new DefaultRangedManager();
        this.microMeleeManager = new DefaultMeleeManager();
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
            name = names[AtlantisSquadManager.squads.size()];
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
     * Returns ID for this battle squad (1, 2, 3, 4 etc).
     */
    public int getID() {
        return ID;
    }

}
