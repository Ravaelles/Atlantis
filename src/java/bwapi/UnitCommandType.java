package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
A representation of a unit command in BWAPI. This is used by bots to notify BWAPI which commands to use. BWAPI filters commands accordingly and then converts them to Broodwar commands, which differ in complexity. See also UnitCommandTypes
*/
/**
Expected type constructor. If the type is an invalid type, then it becomes Types::Unknown. A type is invalid if its value is less than 0 or greater than Types::Unknown. Parameters id The id that corresponds to this type. It is typically an integer value that corresponds to an internal Broodwar type. If the given id is invalid, then it becomes Types::Unknown.
*/
public class UnitCommandType {

    public String toString() {
        return toString_native(pointer);
    }

    public static final UnitCommandType Attack_Move = new UnitCommandType(0);

    public static final UnitCommandType Attack_Unit = new UnitCommandType(0);

    public static final UnitCommandType Build = new UnitCommandType(0);

    public static final UnitCommandType Build_Addon = new UnitCommandType(0);

    public static final UnitCommandType Train = new UnitCommandType(0);

    public static final UnitCommandType Morph = new UnitCommandType(0);

    public static final UnitCommandType Research = new UnitCommandType(0);

    public static final UnitCommandType Upgrade = new UnitCommandType(0);

    public static final UnitCommandType Set_Rally_Position = new UnitCommandType(0);

    public static final UnitCommandType Set_Rally_Unit = new UnitCommandType(0);

    public static final UnitCommandType Move = new UnitCommandType(0);

    public static final UnitCommandType Patrol = new UnitCommandType(0);

    public static final UnitCommandType Hold_Position = new UnitCommandType(0);

    public static final UnitCommandType Stop = new UnitCommandType(0);

    public static final UnitCommandType Follow = new UnitCommandType(0);

    public static final UnitCommandType Gather = new UnitCommandType(0);

    public static final UnitCommandType Return_Cargo = new UnitCommandType(0);

    public static final UnitCommandType Repair = new UnitCommandType(0);

    public static final UnitCommandType Burrow = new UnitCommandType(0);

    public static final UnitCommandType Unburrow = new UnitCommandType(0);

    public static final UnitCommandType Cloak = new UnitCommandType(0);

    public static final UnitCommandType Decloak = new UnitCommandType(0);

    public static final UnitCommandType Siege = new UnitCommandType(0);

    public static final UnitCommandType Unsiege = new UnitCommandType(0);

    public static final UnitCommandType Lift = new UnitCommandType(0);

    public static final UnitCommandType Land = new UnitCommandType(0);

    public static final UnitCommandType Load = new UnitCommandType(0);

    public static final UnitCommandType Unload = new UnitCommandType(0);

    public static final UnitCommandType Unload_All = new UnitCommandType(0);

    public static final UnitCommandType Unload_All_Position = new UnitCommandType(0);

    public static final UnitCommandType Right_Click_Position = new UnitCommandType(0);

    public static final UnitCommandType Right_Click_Unit = new UnitCommandType(0);

    public static final UnitCommandType Halt_Construction = new UnitCommandType(0);

    public static final UnitCommandType Cancel_Construction = new UnitCommandType(0);

    public static final UnitCommandType Cancel_Addon = new UnitCommandType(0);

    public static final UnitCommandType Cancel_Train = new UnitCommandType(0);

    public static final UnitCommandType Cancel_Train_Slot = new UnitCommandType(0);

    public static final UnitCommandType Cancel_Morph = new UnitCommandType(0);

    public static final UnitCommandType Cancel_Research = new UnitCommandType(0);

    public static final UnitCommandType Cancel_Upgrade = new UnitCommandType(0);

    public static final UnitCommandType Use_Tech = new UnitCommandType(0);

    public static final UnitCommandType Use_Tech_Position = new UnitCommandType(0);

    public static final UnitCommandType Use_Tech_Unit = new UnitCommandType(0);

    public static final UnitCommandType Place_COP = new UnitCommandType(0);

    public static final UnitCommandType None = new UnitCommandType(0);

    public static final UnitCommandType Unknown = new UnitCommandType(0);


    private static Map<Long, UnitCommandType> instances = new HashMap<Long, UnitCommandType>();

    private UnitCommandType(long pointer) {
        this.pointer = pointer;
    }

    private static UnitCommandType get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        UnitCommandType instance = instances.get(pointer);
        if (instance == null ) {
            instance = new UnitCommandType(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native String toString_native(long pointer);


}
