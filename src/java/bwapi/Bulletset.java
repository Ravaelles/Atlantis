package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
A container for a set of Bullet objects.
*/
public class Bulletset {


    private static Map<Long, Bulletset> instances = new HashMap<Long, Bulletset>();

    private Bulletset(long pointer) {
        this.pointer = pointer;
    }

    private static Bulletset get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        Bulletset instance = instances.get(pointer);
        if (instance == null ) {
            instance = new Bulletset(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;


}
