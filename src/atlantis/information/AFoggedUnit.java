package atlantis.information;

import atlantis.position.APosition;
import atlantis.position.APositionedObject;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

/**
 * Stores information about units in order to retrieve them when they are out of sight
 *
 * @author Anderson
 *
 */
public class AFoggedUnit extends APositionedObject {

    private APosition position;
    private final AUnit unit;
    private AUnitType type;
    private AUnitType _lastCachedType;
    private final AUnitType buildType;
    
    // =========================================================

    public AFoggedUnit(AUnit unit) {
        this.unit = unit;
        position = new APosition(unit.getPosition());
//        type = unit.getType();
        type = AUnitType.createFrom(unit.u().getType());
        _lastCachedType = type;
        buildType = unit.getBuildType();
    }

    // =========================================================
    
    /**
     * Updates last known position of this unit.
     */
    public void updatePosition(APosition position) {
        this.position = new APosition(position);
    }
    
    @Override
    public APosition getPosition() {
        return position;
    }
    
    // =========================================================
    
    /**
     * Returns unit type from BWMirror OR if type is Unknown (behind fog of war) it will return last cached 
     * type.
     */
    public AUnitType getType() {
        if (type.equals(AUnitType.Unknown)) {
            return _lastCachedType;
        }
        else {
            _lastCachedType = type;
            return type;
        }
    }

    public AUnitType getUnitType() {
        return buildType;
    }

    public AUnit getUnit() {
        return unit;
    }

    public AFoggedUnit update(AUnit updated) {
        if (updated.getID() != unit.getID()) {
            throw new RuntimeException(
                    String.format("Unexpected unit ID. Expected %d, received %d", unit.getID(), updated.getID())
            );
        }
        position = updated.getPosition();
        type = unit.getType();

        return this;
    }

}
