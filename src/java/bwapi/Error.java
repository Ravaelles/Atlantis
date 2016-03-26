package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
The Error object is generally used to determine why certain functions in BWAPI have failed. For example, you may not have enough resources to construct a unit. See also Game::getLastError, Game::setLastError, Errors
*/
/**
Expected type constructor. If the type is an invalid type, then it becomes Types::Unknown. A type is invalid if its value is less than 0 or greater than Types::Unknown. Parameters id The id that corresponds to this type. It is typically an integer value that corresponds to an internal Broodwar type. If the given id is invalid, then it becomes Types::Unknown.
*/
public class Error {

    public String toString() {
        return toString_native(pointer);
    }

    public static final Error Unit_Does_Not_Exist = new Error(0);

    public static final Error Unit_Not_Visible = new Error(0);

    public static final Error Unit_Not_Owned = new Error(0);

    public static final Error Unit_Busy = new Error(0);

    public static final Error Incompatible_UnitType = new Error(0);

    public static final Error Incompatible_TechType = new Error(0);

    public static final Error Incompatible_State = new Error(0);

    public static final Error Already_Researched = new Error(0);

    public static final Error Fully_Upgraded = new Error(0);

    public static final Error Currently_Researching = new Error(0);

    public static final Error Currently_Upgrading = new Error(0);

    public static final Error Insufficient_Minerals = new Error(0);

    public static final Error Insufficient_Gas = new Error(0);

    public static final Error Insufficient_Supply = new Error(0);

    public static final Error Insufficient_Energy = new Error(0);

    public static final Error Insufficient_Tech = new Error(0);

    public static final Error Insufficient_Ammo = new Error(0);

    public static final Error Insufficient_Space = new Error(0);

    public static final Error Invalid_Tile_Position = new Error(0);

    public static final Error Unbuildable_Location = new Error(0);

    public static final Error Unreachable_Location = new Error(0);

    public static final Error Out_Of_Range = new Error(0);

    public static final Error Unable_To_Hit = new Error(0);

    public static final Error Access_Denied = new Error(0);

    public static final Error File_Not_Found = new Error(0);

    public static final Error Invalid_Parameter = new Error(0);

    public static final Error None = new Error(0);

    public static final Error Unknown = new Error(0);


    private static Map<Long, Error> instances = new HashMap<Long, Error>();

    private Error(long pointer) {
        this.pointer = pointer;
    }

    private static Error get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        Error instance = instances.get(pointer);
        if (instance == null ) {
            instance = new Error(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native String toString_native(long pointer);


}
