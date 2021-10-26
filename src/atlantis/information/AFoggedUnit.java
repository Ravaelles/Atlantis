package atlantis.information;

import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

/**
 * Stores information about units in order to retrieve them when they are out of sight
 *
 * @author Anderson
 *
 */
//public class AFoggedUnit implements HasPosition {
public class AFoggedUnit extends AUnit {

    private final AUnit unit;
    private APosition _position;
    private AUnitType _lastCachedType;

    // =========================================================

    public AFoggedUnit(AUnit unit) {
        super(unit.u());

        this.unit = unit;
        _position = new APosition(unit.getPosition());
        _lastCachedType = AUnitType.createFrom(unit.u().getType());
    }

    // =========================================================
    
    /**
     * Updates last known position of this unit.
     */
//    public void updatePosition(APosition position) {
//        this._position = new APosition(position);
//    }

    @Override
    public APosition getPosition() {
        return _position;
    }

    @Override
    public int x() {
        return getPosition().getX();
    }

    @Override
    public int y() {
        return getPosition().getY();
    }

    @Override
    public String toString() {
        return "AFoggedUnit{" +
                "unit=" + unit +
                ", _position=" + _position +
                ", _lastCachedType=" + _lastCachedType +
                '}';
    }

    // =========================================================
    
    /**
     * Returns unit type from BWMirror OR if type is Unknown (behind fog of war) it will return last cached 
     * type.
     */
    @Override
    public AUnitType type() {
        if (_lastCachedType == null) {
            _lastCachedType = super.type();
        }

        return _lastCachedType;
    }

    public AUnit getUnit() {
        return unit;
    }

    public void update(AUnit enemyUnit) {
        _position = enemyUnit.getPosition();
        if (_lastCachedType == null || !_lastCachedType.equals(enemyUnit.type())) {
            _lastCachedType = enemyUnit.type();
        }
    }

//    public AFoggedUnit update(AUnit updated) {
//        if (updated.getID() != unit.getID()) {
//            throw new RuntimeException(
//                    String.format("Unexpected unit ID. Expected %d, received %d", unit.getID(), updated.getID())
//            );
//        }
//
//        _position = updated.getPosition();
//        _lastCachedType = unit.type();
//
//        return this;
//    }

}
