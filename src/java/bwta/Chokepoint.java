package bwta;

import bwta.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Player;
import bwapi.Unit;
import bwapi.Pair;
import bwapi.CenteredObject;

public class Chokepoint extends CenteredObject 
{

    public Pair<Region, Region> getRegions() {
        return getRegions_native(pointer);
    }

    public Pair<Position, Position> getSides() {
        return getSides_native(pointer);
    }

    public Position getCenter() {
        return getCenter_native(pointer);
    }

    public double getWidth() {
        return getWidth_native(pointer);
    }


    private static Map<Long, Chokepoint> instances = new HashMap<Long, Chokepoint>();

    private Chokepoint(long pointer) {
        this.pointer = pointer;
    }

    private static Chokepoint get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        Chokepoint instance = instances.get(pointer);
        if (instance == null ) {
            instance = new Chokepoint(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native Pair<Region, Region> getRegions_native(long pointer);

    private native Pair<Position, Position> getSides_native(long pointer);

    private native Position getCenter_native(long pointer);

    private native double getWidth_native(long pointer);


}
