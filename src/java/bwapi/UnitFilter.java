package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

public class UnitFilter {


    private static Map<Long, UnitFilter> instances = new HashMap<Long, UnitFilter>();

    private UnitFilter(long pointer) {
        this.pointer = pointer;
    }

    private static UnitFilter get(long pointer) {
        UnitFilter instance = instances.get(pointer);
        if (instance == null ) {
            instance = new UnitFilter(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;


}
