package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
A container that holds a set of Region objects.
*/
public class Regionset {

/**
Retrieves the center of the region. This position is used as the node of the region. Returns A Position indicating the center location of the Region, in pixels.
*/
    public Position getCenter() {
        return getCenter_native(pointer);
    }

/**
Retrieves a Unitset containing all the units that are in this region. Also has the ability to filter the units before the creation of the Unitset. Parameters pred (optional) If this parameter is used, it is a UnitFilter or function predicate that will retrieve only the units whose attributes match the given criteria. If omitted, then a default value of nullptr is used, in which case there is no filter. Returns A Unitset containing all units in this region that have met the requirements of pred. See also UnitFilter
*/
    public List<Unit> getUnits() {
        return getUnits_native(pointer);
    }


    private static Map<Long, Regionset> instances = new HashMap<Long, Regionset>();

    private Regionset(long pointer) {
        this.pointer = pointer;
    }

    private static Regionset get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        Regionset instance = instances.get(pointer);
        if (instance == null ) {
            instance = new Regionset(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native Position getCenter_native(long pointer);

    private native List<Unit> getUnits_native(long pointer);


}
