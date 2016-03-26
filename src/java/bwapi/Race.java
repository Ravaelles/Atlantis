package bwapi;

import bwapi.*;
import java.lang.reflect.Field;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
The Race object is used to get information about a particular race. For example, the default worker and supply provider UnitType. As you should already know, Starcraft has three races: Terran , Protoss , and Zerg . See also UnitType::getRace, PlayerInterface::getRace, Races
*/
/**
Expected type constructor. If the type is an invalid type, then it becomes Types::Unknown. A type is invalid if its value is less than 0 or greater than Types::Unknown. Parameters id The id that corresponds to this type. It is typically an integer value that corresponds to an internal Broodwar type. If the given id is invalid, then it becomes Types::Unknown.
*/
public class Race {

    public String toString() {
        return toString_native(pointer);
    }

/**
Retrieves the default worker type for this Race. Note In Starcraft, workers are the units that are used to construct structures. Returns UnitType of the worker that this race uses.
*/
    public UnitType getWorker() {
        return getWorker_native(pointer);
    }

/**
Retrieves the default resource center UnitType that is used to create expansions for this Race. Note In Starcraft, the center is the very first structure of the Race's technology tree. Also known as its base of operations or resource depot. Returns UnitType of the center that this race uses.
*/
    public UnitType getCenter() {
        return getCenter_native(pointer);
    }

/**
Retrieves the default structure UnitType for this Race that is used to harvest gas from Vespene Geysers. Note In Starcraft, you must first construct a structure over a Vespene Geyser in order to begin harvesting Vespene Gas. Returns UnitType of the structure used to harvest gas.
*/
    public UnitType getRefinery() {
        return getRefinery_native(pointer);
    }

/**
Retrieves the default transport UnitType for this race that is used to transport ground units across the map. Note In Starcraft, transports will allow you to carry ground units over unpassable terrain. Returns UnitType for transportation.
*/
    public UnitType getTransport() {
        return getTransport_native(pointer);
    }

/**
Retrieves the default supply provider UnitType for this race that is used to construct units. Note In Starcraft, training, morphing, or warping in units requires that the player has sufficient supply available for their Race. Returns UnitType that provides the player with supply.
*/
    public UnitType getSupplyProvider() {
        return getSupplyProvider_native(pointer);
    }

    public static final Race Zerg = new Race(0);

    public static final Race Terran = new Race(0);

    public static final Race Protoss = new Race(0);

    public static final Race Random = new Race(0);

    public static final Race None = new Race(0);

    public static final Race Unknown = new Race(0);


    private static Map<Long, Race> instances = new HashMap<Long, Race>();

    private Race(long pointer) {
        this.pointer = pointer;
    }

    private static Race get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        Race instance = instances.get(pointer);
        if (instance == null ) {
            instance = new Race(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native String toString_native(long pointer);

    private native UnitType getWorker_native(long pointer);

    private native UnitType getCenter_native(long pointer);

    private native UnitType getRefinery_native(long pointer);

    private native UnitType getTransport_native(long pointer);

    private native UnitType getSupplyProvider_native(long pointer);
    
    // =========================================================
    // ===== Start of ATLANTIS CODE ============================
    // =========================================================
    
    private String _name = null;
    
    /**
     * Returns name of this race like "Protoss" or "Random".
     */
    public String getName() {
        if (_name == null) {
            try {
                for (Field field : Race.class.getDeclaredFields()) {
                    Race race = (Race) field.get(this);
                    if (race.equals(this)) {
                        _name = field.getName().replace("_", " ");
                        break;
                    }
                }
            } catch (Exception ex) {
                System.err.println("Can't define name for race: " + this);
                return "error"; 
            }
        }
        return _name;
    }
    
    /**
     * Return letter for the race like "P", "T", "Z", "R".
     */
    public String getLetter() {
        return getName().charAt(0) + "";
    }

}
