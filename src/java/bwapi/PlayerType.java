package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
Represents the type of controller for the player slot (i.e. human, computer). See also PlayerTypes
*/
/**
Expected type constructor. If the type is an invalid type, then it becomes Types::Unknown. A type is invalid if its value is less than 0 or greater than Types::Unknown. Parameters id The id that corresponds to this type. It is typically an integer value that corresponds to an internal Broodwar type. If the given id is invalid, then it becomes Types::Unknown.
*/
public class PlayerType {

    public String toString() {
        return toString_native(pointer);
    }

/**
Identifies whether or not this type is used for the pre-game lobby. A type such as PlayerTypes::ComputerLeft would only appear in-game when a computer player is defeated. Returns true if this type can appear in the pre-game lobby, false otherwise.
*/
    public boolean isLobbyType() {
        return isLobbyType_native(pointer);
    }

/**
Identifies whether or not this type is used in-game. A type such as PlayerTypes::Closed would not be a valid in-game type. Returns true if the type can appear in-game, false otherwise. See also isLobbyType
*/
    public boolean isGameType() {
        return isGameType_native(pointer);
    }

    public static final PlayerType None = new PlayerType(0);

    public static final PlayerType Computer = new PlayerType(0);

    public static final PlayerType Player = new PlayerType(0);

    public static final PlayerType RescuePassive = new PlayerType(0);

    public static final PlayerType EitherPreferComputer = new PlayerType(0);

    public static final PlayerType EitherPreferHuman = new PlayerType(0);

    public static final PlayerType Neutral = new PlayerType(0);

    public static final PlayerType Closed = new PlayerType(0);

    public static final PlayerType PlayerLeft = new PlayerType(0);

    public static final PlayerType ComputerLeft = new PlayerType(0);

    public static final PlayerType Unknown = new PlayerType(0);


    private static Map<Long, PlayerType> instances = new HashMap<Long, PlayerType>();

    private PlayerType(long pointer) {
        this.pointer = pointer;
    }

    private static PlayerType get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        PlayerType instance = instances.get(pointer);
        if (instance == null ) {
            instance = new PlayerType(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native String toString_native(long pointer);

    private native boolean isLobbyType_native(long pointer);

    private native boolean isGameType_native(long pointer);


}
