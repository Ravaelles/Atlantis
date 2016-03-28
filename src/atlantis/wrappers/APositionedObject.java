package atlantis.wrappers;

import atlantis.units.AUnit;
import bwapi.Position;
import bwapi.PositionedObject;
import bwapi.Unit;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public abstract class APositionedObject extends PositionedObject {
    
    /**
     * Returns distance in tiles (1 tile = 32 pixels) to the target.
     */
    public double distanceTo(Object target) {
        if (target instanceof Unit) {
            return (double) getDistance((Unit) target) / 32;
        }
        else if (target instanceof AUnit) {
            return (double) getDistance(((AUnit) target).u()) / 32;
        }
        else {
            return (double) getDistance((Position) target) / 32;
        }
    }
    
    public int getTileX() {
        return getX() / 32;
    }
    
    public int getTileY() {
        return getY() / 32;
    }
    
}
