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

    protected static AUnit _lastAUnit = null;
    protected AUnit aUnit;
    protected int _id;
    protected int _hp;
    protected int _energy;
    protected int _shields;
    protected boolean _isStimmed;
    protected APosition _lastPosition;
    protected AUnitType _lastType;
    protected boolean _isCompleted;
    protected boolean _isCloaked = false;
    protected Cache<Integer> cacheInt = new Cache<>();

    // =========================================================

    protected AbstractFoggedUnit(AUnit unit) {
        if (unit != null) {
            this._id = unit.id();
            this.aUnit = unit;
            this.update(unit);

            all.put(unit.id(), this);
        }
    }

    public static AbstractFoggedUnit from(AUnit enemyUnit) {
        return enemyUnit instanceof FakeUnit
                ? FakeFoggedUnit.fromFake((FakeUnit) enemyUnit)
                : FoggedUnit.from(enemyUnit);
    }

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

    public APlayer player() {
        return AGame.enemy();
    }

    public AUnit getUnit() {
        return aUnit;
    }

    public void update(AUnit unit) {
        updatePosition(unit);
        updateType(unit);
        aUnit = unit;
        _isCompleted = unit.isCompleted();
        _isCloaked = unit.isCloaked();
        _hp = unit.hp();
        _energy = unit.energy();
        _shields = unit.shields();
        _isStimmed = unit.isStimmed();
    }

    public void updatePosition(AUnit unit) {
//        System.err.println("unit = " + unit + " // " + unit.position());
//        if (unit.hasPosition()) {
//        if (u() != null && unit.x() > 0 && unit.y() > 0) {
        if (unit.x() > 0 && unit.y() > 0) {
            _lastPosition = APosition.createFromPixels(unit.x(), unit.y());
//            System.out.println("_position = " + _position);
            cacheInt.set("lastPositionUpdated", -1, A.now());
        }

//        if (!unit.isBuilding() && _position != null && _position.isVisible() && isAccessible()) {
//            _position = null;
//        }
    }

    @Override
    public boolean hasPosition() {
        return _lastPosition != null;
    }

    protected void updateType(AUnit unit) {
        if (_lastType == null || (unit.type() != null && !_lastType.equals(unit.bwapiType()))) {
//            System.err.println("UPDATING TYPE, current = " + _lastType
//                             + ", \n           foggedUnit = " + this
//                             + ", \n           REAL = " + unit.bwapiType().name());
            _lastType = AUnitType.from(unit.bwapiType());
        }
    }

//    public void positionUnknown() {
//        _lastPosition = null;
//        cacheInt.set("lastPositionUpdated", -1, A.now());
//    }

    public void removeKnownPositionIfNeeded() {
        if (_lastPosition != null) {
            if (_lastPosition.isPositionVisible() || _lastType == null) {
//                System.out.println("unit() = " + unit() + " / is_building:" + unit().isBuilding());
//                    System.out.println("REMOVE LAST POSITION FOR " + _lastType);
                if (_lastType != null && !_lastType.isBuilding()) {
                    _lastPosition = null;
                }
            }
        }
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
        return !AUnitType.Unknown.equals(aUnit.type());
    }

    public AUnit innerAUnit() {
        return aUnit;
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
