package atlantis.information;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.Position;
import bwapi.PositionedObject;

import bwapi.UnitType;

/**
 * Stores information about units in order to retrieve them when they are out of sight
 *
 * @author Anderson
 *
 */
public class UnitData extends PositionedObject {

    private Position position;
    private AUnit unit;
    private AUnitType type, buildType;

    public UnitData(AUnit unit) {
        unit = unit;
        position = unit.getPosition();
        type = unit.getType();
        buildType = unit.getBuildType();
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

    public Position getPosition() {
        return position;
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
