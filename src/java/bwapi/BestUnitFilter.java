package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

public class BestUnitFilter {


    private static Map<Long, BestUnitFilter> instances = new HashMap<Long, BestUnitFilter>();

    private BestUnitFilter(long pointer) {
        this.pointer = pointer;
    }

    private static BestUnitFilter get(long pointer) {
        BestUnitFilter instance = instances.get(pointer);
        if (instance == null ) {
            instance = new BestUnitFilter(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;


}
