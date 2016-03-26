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

public class Region extends CenteredObject 
{

    public Polygon getPolygon() {
        return getPolygon_native(pointer);
    }

    public Position getCenter() {
        return getCenter_native(pointer);
    }

    public List<Chokepoint> getChokepoints() {
        return getChokepoints_native(pointer);
    }

    public List<BaseLocation> getBaseLocations() {
        return getBaseLocations_native(pointer);
    }

    public boolean isReachable(Region region) {
        return isReachable_native(pointer, region);
    }

    public List<Region> getReachableRegions() {
        return getReachableRegions_native(pointer);
    }

    public int getMaxDistance() {
        return getMaxDistance_native(pointer);
    }


    private static Map<Long, Region> instances = new HashMap<Long, Region>();

    private Region(long pointer) {
        this.pointer = pointer;
    }

    private static Region get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        Region instance = instances.get(pointer);
        if (instance == null ) {
            instance = new Region(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native Polygon getPolygon_native(long pointer);

    private native Position getCenter_native(long pointer);

    private native List<Chokepoint> getChokepoints_native(long pointer);

    private native List<BaseLocation> getBaseLocations_native(long pointer);

    private native boolean isReachable_native(long pointer, Region region);

    private native List<Region> getReachableRegions_native(long pointer);

    private native int getMaxDistance_native(long pointer);


}
