package atlantis.wrappers;

import atlantis.units.AUnit;
import bwapi.Position;
import bwapi.Unit;
import java.util.HashMap;
import java.util.Map;

/**
 * Atlantis uses wrapper for BWMirror native classes which can't extended.<br /><br />
 * <b>APosition</b> class contains numerous helper methods, but if you think some methods are missing
 * you can create missing method here and you can reference original Position class via p() method.
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class APosition extends Position {
    
    private static final Map<Position, APosition> instances = new HashMap<>();
    
    private Position p;
    
    // =========================================================

    private APosition(Position p) {
        super(p.getX(), p.getY());
        this.p = p;
    }
    
    /**
     * Atlantis uses wrapper for BWMirror native classes which aren't extended.<br />
     * <b>APosition</b> class contains numerous helper methods, but if you think some methods are missing
     * you can create missing method here and you can reference original Position class via p() method.
     */
    public static APosition createFrom(Position p) {
        if (instances.containsKey(p)) {
            return instances.get(p);
        }
        else {
            APosition position = new APosition(p);
            instances.put(p, position);
            return position;
        }
    }
    
    // =========================================================
    
    /**
     * <b>AVOID USAGE AS MUCH AS POSSIBLE</b> outside APosition class.
     * APosition class should be used always in place of Position when possible.
     */
    public Position p() {
        return p;
    }
    
}
