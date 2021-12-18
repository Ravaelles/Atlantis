package atlantis.information;

import atlantis.position.APosition;
import atlantis.tests.FakeUnit;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.A;
import atlantis.util.Cache;
import java.util.TreeMap;

/**
 * Stores information about units in order to retrieve them when they are out of sight
 *
 * @author Anderson
 *
 */
//public class AFoggedUnit implements HasPosition {
public class AFoggedUnit extends AUnit {
//public class AFoggedUnit implements UnitInterface, HasPosition, Comparable<AUnit> {

    protected final static TreeMap<Integer, AFoggedUnit> all = new TreeMap<>();

    protected static AUnit _lastAUnit = null;
    protected AUnit aUnit;
    protected int _id;
    protected APosition _position;
    protected AUnitType _lastType;
    private Cache<Integer> cacheInt = new Cache<>();

    // =========================================================

    public static AFoggedUnit from(AUnit unit) {
        _lastAUnit = unit;

        AFoggedUnit foggedUnit = all.get(unit.id());
        if (foggedUnit != null) {
            return foggedUnit;
        }

        return new AFoggedUnit(unit);
    }

    protected AFoggedUnit(AUnit unit) {
        super(unit.u());

        this._id = unit.id();
        this.aUnit = unit;
        this.update(unit);

        all.put(unit.id(), this);
    }

    protected AFoggedUnit(FakeUnit unit) {
    }

    // =========================================================

    public static void clearCache() {
        all.clear();
    }

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
        return "AFoggedUnit{#" + _id + " " + _lastType + " at " + _position + "}";
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

    @Override
    public int compareTo(AUnit o) {
        if (o == null) {
            return 1;
        }

        return o.compareTo(aUnit);
    }

    @Override
    public boolean isPowered() {
        return true;
    }

    @Override
    public boolean isMoving() {
        return false;
    }

    @Override
    public AUnit target() {
        return null;
    }

    // =========================================================
    
    /**
     * Returns unit type from BWMirror OR if type is Unknown (behind fog of war) it will return last cached 
     * type.
     */
    @Override
    public AUnitType type() {
        if (_lastType == null) {
            if (aUnit == null) {
                aUnit = _lastAUnit;
            }
            _lastType = aUnit.type();
        }

        return _lastType;
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
        if (_lastType == null || (unit.type() != null && !_lastType.equals(unit.type()))) {
            _lastType = aUnit.type();
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
        return !AUnitType.Unknown.equals(aUnit.type());
    }

}
