package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
A class that represents game types in Broodwar. A game type is selected when creating a game. See also GameTypes
*/
/**
Expected type constructor. If the type is an invalid type, then it becomes Types::Unknown. A type is invalid if its value is less than 0 or greater than Types::Unknown. Parameters id The id that corresponds to this type. It is typically an integer value that corresponds to an internal Broodwar type. If the given id is invalid, then it becomes Types::Unknown.
*/
public class GameType {

    public String toString() {
        return toString_native(pointer);
    }

    public static final GameType Melee = new GameType(0);

    public static final GameType Free_For_All = new GameType(0);

    public static final GameType One_on_One = new GameType(0);

    public static final GameType Capture_The_Flag = new GameType(0);

    public static final GameType Greed = new GameType(0);

    public static final GameType Slaughter = new GameType(0);

    public static final GameType Sudden_Death = new GameType(0);

    public static final GameType Ladder = new GameType(0);

    public static final GameType Use_Map_Settings = new GameType(0);

    public static final GameType Team_Melee = new GameType(0);

    public static final GameType Team_Free_For_All = new GameType(0);

    public static final GameType Team_Capture_The_Flag = new GameType(0);

    public static final GameType Top_vs_Bottom = new GameType(0);

    public static final GameType None = new GameType(0);

    public static final GameType Unknown = new GameType(0);


    private static Map<Long, GameType> instances = new HashMap<Long, GameType>();

    private GameType(long pointer) {
        this.pointer = pointer;
    }

    private static GameType get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        GameType instance = instances.get(pointer);
        if (instance == null ) {
            instance = new GameType(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native String toString_native(long pointer);


}
