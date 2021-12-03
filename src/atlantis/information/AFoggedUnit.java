package atlantis.information;

import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.A;
import atlantis.util.Cache;

import java.util.Objects;

/**
 * Stores information about units in order to retrieve them when they are out of sight
 *
 * @author Anderson
 *
 */
//public class AFoggedUnit implements HasPosition {
public class AFoggedUnit extends AUnit {

    private final AUnit aUnit;
    private int _id;
    private APosition _position;
    private AUnitType _lastCachedType;
    private Cache<Integer> cacheInt = new Cache<>();

    // =========================================================

    public AFoggedUnit(AUnit unit) {
        super(unit.u());

        this.aUnit = unit;
        this._id = unit.id();
        update(unit);
    }

    // =========================================================
    
    /**
     * Updates last known position of this unit.
     */
//    public void updatePosition(APosition position) {
//        this._position = new APosition(position);
//    }

    @Override
    public APosition position() {
        return _position;
    }

    @Override
    public int x() {
        return position().getX();
    }

    @Override
    public int y() {
        return position().getY();
    }

    @Override
    public String toString() {
        return "AFoggedUnit{" +
                "_position=" + _position +
                ",_Type=" + _lastCachedType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        AUnit that = (AUnit) o;
//        return _position.distToLessThan(that.position(), 0.05);
        return _id == that.id();
    }

    @Override
    public int hashCode() {
        return _id;
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
        return aUnit;
    }

    public void update(AUnit unit) {
        updatePosition(unit);
        updateType(unit);
    }

    public void updatePosition(AUnit unit) {
//        if (unit.isBuilding()) {
//            System.out.println(unit.shortName() + " // " + unit.x() + "," + unit.y());
//        }

//        if (unit.isVisible()) {
//            System.out.println("Update " + unit.shortName() + " to " + unit.getPosition());
            if (unit.x() > 0 && unit.y() > 0) {
                _position = new APosition(unit.x(), unit.y());
                cacheInt.set("lastPositionUpdated", -1, A.now());
            }
//        }
    }

    protected void updateType(AUnit unit) {
        if (_lastCachedType == null || (unit.type() != null && !_lastCachedType.equals(unit.type()))) {
            _lastCachedType = AUnitType.create(unit.u().getType());
//            _lastCachedType = unit.type();
        }
    }

    public void positionUnknown() {
        _position = null;
        cacheInt.set("lastPositionUpdated", -1, A.now());
    }

    public boolean hasKnownPosition() {
        return _position != null;
    }

    public int lastPositionUpdated() {
        return cacheInt.get("lastPositionUpdated");
    }

    public int lastPositionUpdatedAgo() {
        if (cacheInt.get("lastPositionUpdated") == null) {
            return -1;
        }

        return A.ago(cacheInt.get("lastPositionUpdated"));
    }

    public boolean isAccessible() {
        return !AUnitType.Unknown.equals(super.type());
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
