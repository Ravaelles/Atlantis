package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
A set containing Player objects.
*/
public class Playerset {

/**
Returns the set of all units that every player in this set owns. Returns Unitset containing Playerset's units See also PlayerInterface::getUnits
*/
    public List<Unit> getUnits() {
        return getUnits_native(pointer);
    }

/**
Sets the alliance status with all players contained in the Playerset. Parameters allies Set to true to set the player to allied, or false for enemy. alliedVictory Set to true to turn on allied victory, or false to disable it. See also Game::setAlliance
*/
    public void setAlliance(boolean allies) {
        setAlliance_native(pointer, allies);
    }

    public void setAlliance() {
        setAlliance_native(pointer);
    }

    public void setAlliance(boolean allies, boolean alliedVictory) {
        setAlliance_native(pointer, allies, alliedVictory);
    }


    private static Map<Long, Playerset> instances = new HashMap<Long, Playerset>();

    private Playerset(long pointer) {
        this.pointer = pointer;
    }

    private static Playerset get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        Playerset instance = instances.get(pointer);
        if (instance == null ) {
            instance = new Playerset(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native List<Unit> getUnits_native(long pointer);

    private native void setAlliance_native(long pointer, boolean allies);

    private native void setAlliance_native(long pointer);

    private native void setAlliance_native(long pointer, boolean allies, boolean alliedVictory);


}
