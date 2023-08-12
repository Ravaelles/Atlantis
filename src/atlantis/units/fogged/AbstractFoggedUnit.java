package atlantis.units.fogged;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.APlayer;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.cache.Cache;
import tests.unit.FakeUnit;

import java.util.TreeMap;

/**
 * Stores information about units in order to retrieve them when they are out of sight
 */
public class AbstractFoggedUnit extends AUnit {

    protected final static TreeMap<Integer, AbstractFoggedUnit> all = new TreeMap<>();

    protected AUnit _lastAUnit = null;
    protected int _id;
    protected int _hp;
    protected int _energy;
    protected int _shields;
    protected boolean _isStimmed;
    protected APosition _lastPosition;
    protected AUnitType _lastType;
    protected boolean _isCompleted;
    protected boolean _isCloaked = false;
    protected boolean _isDetected;
    protected Cache<Integer> cacheInt = new Cache<>();

    // =========================================================

    protected AbstractFoggedUnit(AUnit unit) {
        if (unit != null) {
            this.onAbstractFoggedUnitCreated(unit);

            all.put(unit.id(), this);
        }
    }

    public static AbstractFoggedUnit from(AUnit enemyUnit) {
        return enemyUnit instanceof FakeUnit
            ? FakeFoggedUnit.fromFake((FakeUnit) enemyUnit)
            : FoggedUnit.from(enemyUnit);
    }

    // =========================================================

    protected void onAbstractFoggedUnitCreated(AUnit unit) {
        _id = unit.id();
        _lastAUnit = unit;

        updatePosition(unit);
        updateType(unit);

        _isCompleted = unit.isCompleted();
        _isCloaked = unit.isCloaked();
        _isDetected = unit.isDetected();
        _hp = unit.hp();
        _energy = unit.energy();
        _shields = unit.shields();
        _isStimmed = unit.isStimmed();
    }

    public void updatePosition(AUnit unit) {
        if (unit instanceof AbstractFoggedUnit) {
            System.err.println("updatePosition got AbstractFoggedUnit: " + unit);
            A.printStackTrace();
        }

//        if (unit.hasPosition()) {
//        if (u() != null && unit.x() > 0 && unit.y() > 0) {
//        if (!(unit instanceof AbstractFoggedUnit) || unit.position().isPositionVisible()) {
//        if (unit.x() > 0 && unit.y() > 0 && unit.position().isPositionVisible()) {
//            _lastPosition = APosition.createFromPixels(unit.u().getX(), unit.u().getY());
//        if (!(unit instanceof AbstractFoggedUnit) || unit.u() != null) {
//        if (unit.isVisibleUnitOnMap()) {
//            if (unit.type().isBase()) {
//                System.err.println("--------- " + unit + " / x:" + unit.x() + " , y:" + unit.y());
//            }

//            _lastPosition = APosition.createFromPixels(unit.x(), unit.y());
        updateLastPosition(unit);
//            System.out.println("UPDATED _lastPosition = " + _lastPosition);
//            if (unit.type().isBase()) {
//                System.err.println("--AFTER-- " + unit + " / x:" + unit.x() + " , y:" + unit.y());
//            }
//        }

//        if (!unit.isBuilding() && _position != null && _position.isVisible() && isAccessible()) {
//            _position = null;
//        }
    }

    private void updateLastPosition(AUnit unit) {
//        _lastPosition = unit == null ? null : APosition.create(_lastAUnit.position());
//        if (unit.isBuilding() && _lastPosition != null) System.err.println("PRE " + unit.name() + " x:" + _lastPosition.x + ", y:" + _lastPosition.y);

        _lastPosition = APosition.create(unit.position());
        cacheInt.set("lastPositionUpdated", -1, A.now());

//        if (unit.isBuilding() && _lastPosition != null) System.err.println("POST " + unit.name() + " x:" + _lastPosition.x + ", y:" + _lastPosition.y);
    }


    public void updateType(AUnit unit) {
        if (_lastType == null || (unit.type() != null && !_lastType.equals(unit.bwapiType()))) {
//            System.err.println("UPDATING TYPE, current = " + _lastType
//                             + ", \n           foggedUnit = " + this
//                             + ", \n           REAL = " + unit.bwapiType().name());
            _lastAUnit = unit;
            _lastType = AUnitType.from(unit.bwapiType());
        }
    }

    /**
     * Returns unit type from BWMirror OR if type is Unknown (behind fog of war) it will return last cached
     * type.
     */
    @Override
    public AUnitType type() {
//        if (_lastType == null) {
//            if (_lastAUnit == null) {
//                _lastAUnit = _lastAUnit;
//            }
//            _lastType = _lastAUnit.type();
//        }

        return _lastType;
    }

    // =========================================================

    @Override
    public int hashCode() {
        return _id;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof AUnit)) return false;

        return id() == ((AUnit) other).id();

    }

    // =========================================================

    public static void clearCache() {
        all.clear();
    }

    @Override
    public int id() {
        return _id;
    }

    public APlayer player() {
        return AGame.enemy();
    }

    public AUnit getUnit() {
        return _lastAUnit;
    }

    @Override
    public boolean hasPosition() {
        return _lastPosition != null && _lastPosition.x() < 32000;
    }

//    public void positionUnknown() {
//        _lastPosition = null;
//        cacheInt.set("lastPositionUpdated", -1, A.now());
//    }

//    public void removeKnownPositionIfNeeded() {
//        if (_lastPosition != null && _lastPosition.isPositionVisible()) {
////                System.out.println("unit() = " + unit() + " / is_building:" + unit().isBuilding());
////                    System.out.println("REMOVE LAST POSITION FOR " + _lastType);
//                if (_lastType != null && (!_lastType.isBuilding() || _lastPosition.isPositionVisible())) {
//                    _lastPosition = null;
//                }
//            }
//        }
//    }

    public void foggedUnitNoLongerWhereItWasBefore() {
//        _lastPosition = null;
//        _lastPosition = _lastAUnit == null ? null : APosition.create(_lastAUnit.position());

//        if (_lastAUnit != null && _lastAUnit.isBuilding()) {
//            System.out.println("\n_lastAUnit = " + _lastAUnit + " / x:" + _lastAUnit.x() + "," + _lastAUnit.y());
//            System.out.println("_thisFogged = " + this + " / x:" + this.x() + "," + this.y());
//        }

        updateLastPosition(_lastAUnit);

//        if (_lastAUnit != null && _lastAUnit.isBuilding()) {
//            System.err.println("#" + id() + " _lastPosition = " + _lastPosition + "\n");
//        }
    }

    public int lastPositionUpdated() {
        return cacheInt.get("lastPositionUpdated");
    }

    public int lastPositionUpdatedAgo() {
        if (cacheInt.get("lastPositionUpdated") == null) {
            return -666;
        }

        return A.ago(cacheInt.get("lastPositionUpdated"));
    }

    public boolean isAccessible() {
        return !AUnitType.Unknown.equals(_lastAUnit.type());
    }

    public AUnit innerAUnit() {
        return _lastAUnit;
    }

    // =========================================================

    @Override
    public String toString() {
        return getClass().getSimpleName() + " "
            + nameWithId() + " at " + _lastPosition
            + " (" + (isEnemy() ? "Enemy" : (isOur() ? "Our" : "Neutral")) + ")";
    }

    // =========================================================

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public APosition position() {
        return _lastPosition;
    }

    @Override
    public boolean isVisibleUnitOnMap() {
        return position() != null && position().isPositionVisible();
    }

    @Override
    public boolean isCompleted() {
        return _isCompleted;
    }

    @Override
    public boolean isDetected() {
        return _isDetected;
    }

    @Override
    public boolean isCloaked() {
        return _isCloaked;
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
    public boolean effUndetected() {
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

    @Override
    public int hp() {
        return _hp;
    }

    @Override
    public int energy() {
        return _energy;
    }

    @Override
    public int shields() {
        return _shields;
    }

    @Override
    public boolean isStimmed() {
        return _isStimmed;
    }

    @Override
    public int x() {
        return _lastPosition.x();
    }

    @Override
    public int y() {
        return _lastPosition.y();
    }

}
