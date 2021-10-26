package atlantis.information;

import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.A;
import atlantis.util.Cache;

/**
 * Stores information about units in order to retrieve them when they are out of sight
 *
 * @author Anderson
 *
 */
//public class AFoggedUnit implements HasPosition {
public class AFoggedUnit extends AUnit {

    private final AUnit aUnit;
    private APosition _position;
    private AUnitType _lastCachedType;
    private Cache<Integer> cacheInt = new Cache<>();

    // =========================================================

    public AFoggedUnit(AUnit unit) {
        super(unit.u());

        this.aUnit = unit;
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
                "unit=" + aUnit +
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
        return aUnit;
    }

    public void update(AUnit unit) {
        updatePosition(unit);
        updateType(unit);
    }

    protected void updatePosition(AUnit unit) {
        if (unit.getPosition() != null) {
//            System.out.println("Update " + unit.shortName() + " to " + unit.getPosition());
            _position = new APosition(unit.x(), unit.y());
            cacheInt.set("lastPositionUpdated", -1, A.now());
        }
    }

    protected void updateType(AUnit unit) {
        if (_lastCachedType == null || (unit.type() != null && !_lastCachedType.equals(unit.type()))) {
            _lastCachedType = AUnitType.createFrom(unit.u().getType());
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
        return A.ago(cacheInt.get("lastPositionUpdated"));
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
