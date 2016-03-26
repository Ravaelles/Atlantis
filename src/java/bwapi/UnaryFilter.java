package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
template<class PType, class Container = std::function<bool(PType)>> class BWAPI::UnaryFilter< PType, Container > UnaryFilter allows for logical functor combinations. Unit myUnit; // The following two if statements are equivalent if ( myUnit->getType().isWorker() && myUnit->isCompleted() && myUnit->isIdle() ) {} if ( (IsWorker && IsCompleted && IsIdle)(myUnit) ) {} Template Parameters PType The type being passed into the predicate, which will be of type bool(PType). Container (optional) Storage container for the function predicate. It is std::function<bool(PType)> by default.
*/
/**

*/
public class UnaryFilter {


    private static Map<Long, UnaryFilter> instances = new HashMap<Long, UnaryFilter>();

    private UnaryFilter(long pointer) {
        this.pointer = pointer;
    }

    private static UnaryFilter get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        UnaryFilter instance = instances.get(pointer);
        if (instance == null ) {
            instance = new UnaryFilter(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;


}
