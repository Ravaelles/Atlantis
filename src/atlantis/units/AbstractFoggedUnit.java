package atlantis.units;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.APlayer;
import atlantis.map.position.APosition;
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
    protected APosition _position;
    protected AUnitType _lastType;
    protected boolean _isCompleted;
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
        _hp = unit.hp();
        _energy = unit.energy();
        _shields = unit.shields();
        _isStimmed = unit.isStimmed();
    }

    public void updatePosition(AUnit unit) {
//        if (unit.x() > 0 && unit.y() > 0) {
//        System.out.println("unit = " + unit);
        if (unit.hasPosition()) {
            _position = new APosition(unit.x(), unit.y());
//            System.out.println("_position = " + _position);
            cacheInt.set("lastPositionUpdated", -1, A.now());
        }

//        if (!unit.isBuilding() && _position != null && _position.isVisible() && isAccessible()) {
//            _position = null;
//        }
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

    @Override
    public boolean hasPosition() {
        return _position != null;
    }

    public void removeKnownPosition() {
        _position = null;
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
                + nameWithId() + " at " + _position
                + " (" + (isEnemy() ? "Enemy" : (isOur() ? "Our" : "Neutral")) + ")";
    }

    // =========================================================

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public APosition position() {
        return _position;
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
        return _position.x();
    }

    @Override
    public int y() {
        return _position.y();
    }

}
