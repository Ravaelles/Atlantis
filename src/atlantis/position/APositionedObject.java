package atlantis.position;

import atlantis.util.PositionUtil;
import bwapi.PositionedObject;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public abstract class APositionedObject extends PositionedObject {
    
    /**
     * Returns distance in tiles (1 tile = 32 pixels) to the target.
     */
    public double distanceTo(Object target) {
        return PositionUtil.distanceTo(this, target);
    }
    
    public int getTileX() {
        return getX() / 32;
    }
    
    public int getTileY() {
        return getY() / 32;
    }
    
}
