package atlantis.information;

import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.A;
import atlantis.util.Cache;

import java.util.TreeMap;

/**
 * Stores information about units in order to retrieve them when they are out of sight
 */
public abstract class AFoggedUnit extends AUnit {

    protected final static TreeMap<Integer, AFoggedUnit> all = new TreeMap<>();

    protected static AUnit _lastAUnit = null;
    protected AUnit aUnit;
    protected int _id;
    protected APosition _position;
    protected AUnitType _lastType;
    protected boolean _isCompleted;
    protected Cache<Integer> cacheInt = new Cache<>();

//    protected AFoggedUnit(FakeUnit unit) {
//        super();
//
//        this._id = unit.id();
//        this.aUnit = unit;
//        this.update(unit);
//
//        all.put(unit.id(), this);
//    }

    // =========================================================

    public static void clearCache() {
        all.clear();
    }

    @Override
    public int id() {
        return _id;
    }

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
        _isCompleted = unit.isCompleted();
    }

    public void updatePosition(AUnit unit) {
//        if (unit.isBuilding()) {
//            System.out.println(unit.name() + " // " + unit.x() + "," + unit.y());
//        }

//        if (unit.isVisible()) {
        if (unit.x() > 0 && unit.y() > 0) {
            _position = new APosition(unit.x(), unit.y());
            cacheInt.set("lastPositionUpdated", -1, A.now());
        }
        
        if (!unit.isBuilding() && _position != null && _position.isVisible() && isAccessible()) {
            _position = null;
        }
    }

    protected void updateType(AUnit unit) {
        if (_lastType == null || (unit.type() != null && !_lastType.equals(unit.bwapiType()))) {
//            System.err.println("UPDATING TYPE, current = " + _lastType
//                             + ", \n           foggedUnit = " + this
//                             + ", \n           REAL = " + unit.bwapiType().name());
            _lastType = AUnitType.from(unit.bwapiType());
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

    // =========================================================

    @Override
    public String toString() {
        return getClass().getSimpleName() + " "
                + nameWithId() +
                " at " + _position;
    }

    // =========================================================

    @Override
    public boolean isCompleted() {
        return _isCompleted;
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
    public boolean effCloaked() {
        return false;
    }

    @Override
    public boolean effVisible() {
        return true;
    }

    @Override
    public AUnit target() {
        return null;
    }

}
