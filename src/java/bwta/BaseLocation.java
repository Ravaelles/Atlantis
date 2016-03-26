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
import bwapi.PositionedObject;

public class BaseLocation extends PositionedObject 
{

    public Position getPosition() {
        return getPosition_native(pointer);
    }

    public TilePosition getTilePosition() {
        return getTilePosition_native(pointer);
    }

    public Region getRegion() {
        return getRegion_native(pointer);
    }

    public int minerals() {
        return minerals_native(pointer);
    }

    public int gas() {
        return gas_native(pointer);
    }

    public List<Unit> getMinerals() {
        return getMinerals_native(pointer);
    }

    public List<Unit> getStaticMinerals() {
        return getStaticMinerals_native(pointer);
    }

    public List<Unit> getGeysers() {
        return getGeysers_native(pointer);
    }

    public double getGroundDistance(BaseLocation other) {
        return getGroundDistance_native(pointer, other);
    }

    public double getAirDistance(BaseLocation other) {
        return getAirDistance_native(pointer, other);
    }

    public boolean isIsland() {
        return isIsland_native(pointer);
    }

    public boolean isMineralOnly() {
        return isMineralOnly_native(pointer);
    }

    public boolean isStartLocation() {
        return isStartLocation_native(pointer);
    }


    private static Map<Long, BaseLocation> instances = new HashMap<Long, BaseLocation>();

    private BaseLocation(long pointer) {
        this.pointer = pointer;
    }

    private static BaseLocation get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        BaseLocation instance = instances.get(pointer);
        if (instance == null ) {
            instance = new BaseLocation(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native Position getPosition_native(long pointer);

    private native TilePosition getTilePosition_native(long pointer);

    private native Region getRegion_native(long pointer);

    private native int minerals_native(long pointer);

    private native int gas_native(long pointer);

    private native List<Unit> getMinerals_native(long pointer);

    private native List<Unit> getStaticMinerals_native(long pointer);

    private native List<Unit> getGeysers_native(long pointer);

    private native double getGroundDistance_native(long pointer, BaseLocation other);

    private native double getAirDistance_native(long pointer, BaseLocation other);

    private native boolean isIsland_native(long pointer);

    private native boolean isMineralOnly_native(long pointer);

    private native boolean isStartLocation_native(long pointer);


}
