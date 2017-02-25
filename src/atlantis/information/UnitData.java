package atlantis.information;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.position.APosition;
import atlantis.position.APositionedObject;

/**
 * Stores information about units in order to retrieve them when they are out of sight
 *
 * @author Anderson
 *
 */
public class UnitData extends APositionedObject {

    private APosition position;
    private final AUnit unit;
    private AUnitType type;
    private AUnitType _lastCachedType;
    private final AUnitType buildType;
    
    // =========================================================

    public UnitData(AUnit unit) {
        this.unit = unit;
        position = new APosition(unit.getPosition());
        type = unit.getType();
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

    public AUnitType getBuildType() {
        return buildType;
    }

    public AUnit getUnit() {
        return unit;
    }

    public UnitData update(AUnit updated) {
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
