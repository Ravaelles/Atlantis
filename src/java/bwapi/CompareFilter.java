package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
template<typename PType, typename RType = int, class Container = std::function<RType(PType)>> class BWAPI::CompareFilter< PType, RType, Container > The CompareFilter is a container in which a stored function predicate returns a value. Arithmetic and bitwise operators will return a new CompareFilter that applies the operation to the result of the original functor. If any relational operators are used, then it creates a UnaryFilter that returns the result of the operation. Template Parameters PType The parameter type, which is the type passed into the functor. RType (optional) The functor's return type. It is int by default. Container (optional) Storage container for the function predicate. It is std::function<RType(PType)> by default.
*/
/**

*/
public class CompareFilter {


    private static Map<Long, CompareFilter> instances = new HashMap<Long, CompareFilter>();

    private CompareFilter(long pointer) {
        this.pointer = pointer;
    }

    private static CompareFilter get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        CompareFilter instance = instances.get(pointer);
        if (instance == null ) {
            instance = new CompareFilter(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;


}
