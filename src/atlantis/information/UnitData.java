package atlantis.information;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.wrappers.APosition;
import atlantis.wrappers.APositionedObject;
import bwapi.Position;
import bwapi.PositionedObject;

import bwapi.UnitType;

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
    private final AUnitType buildType;
    
    // =========================================================

    public UnitData(AUnit unit) {
        this.unit = unit;
        position = new APosition(unit.getPosition());
        type = unit.getType();
        buildType = unit.getBuildType();
    }

    // =========================================================
    
    @Override
    public APosition getPosition() {
        return position;
    }
    
    public AUnitType getType() {
        return type;
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
