package atlantis.units.fogged;

import atlantis.core.world.UnitSnapshot;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.player.APlayer;
import atlantis.information.enemy.UnitsArchive;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.UnitOrigin;
import atlantis.util.AConsole;
import atlantis.util.cache.Cache;
import atlantis.util.log.ErrorLog;

import java.util.TreeMap;

/**
 * Stores information about units in order to retrieve them when they are out of sight
 */
public class AbstractFoggedUnit extends AUnit {
    protected final static TreeMap<Integer, AbstractFoggedUnit> all = new TreeMap<>();

    protected AUnit _lastAUnit = null;
    protected int _id;
//    protected int _hp;
    protected int _energy;
//    protected int _shields;
//    protected boolean _isEnemy;
    protected boolean _isStimmed;
    protected APosition _lastPosition;
    protected AUnitType _lastType;
    protected boolean _isCompleted;
    protected boolean _isCloaked = false;
    protected boolean _isDetected;
    protected Cache<Integer> cacheInt = new Cache<>();

    // =========================================================

    protected AbstractFoggedUnit(AUnit unit) {
        super(AbstractFoggedUnit.class);

        if (unit != null) {
            this.onAbstractFoggedUnitCreated(unit);

            all.put(unit.id(), this);
        }
    }

    /**
     * How a unit that went behind the fog is wrapped.
     *
     * <p>In a game it is {@link FoggedUnit#from(AUnit)}, built from the engine
     * object. A unit
     * that has no engine object behind it - a test double - cannot be, which is
     * why this used to be an {@code instanceof FakeUnit} branch here, with the
     * fake subclass living in the production tree. Now the wrapping is a port:
     * production installs nothing and gets {@code FoggedUnit}, the harness
     * installs a factory that answers with its own subclass, and the production
     * tree stops knowing what a fake is.</p>
     */
    public interface FoggedUnitFactory {
        AbstractFoggedUnit wrap(AUnit unit);
    }

    private static FoggedUnitFactory factory = FoggedUnit::from;

    public static void useFactory(FoggedUnitFactory newFactory) {
        factory = newFactory;
    }

    public static void useGameFactory() {
        factory = FoggedUnit::from;
    }

    public static AbstractFoggedUnit from(AUnit enemyUnit) {
        return factory.wrap(enemyUnit);
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
//        _isEnemy = unit.isEnemy();
//        _hp = unit.hp();
        _energy = unit.energy();
//        _shields = unit.shields();
        _isStimmed = unit.isStimmed();
    }

    public void updatePosition(AUnit unit) {
        if (unit instanceof AbstractFoggedUnit) {
            System.err.println("updatePosition got AbstractFoggedUnit: " + unit);
            AConsole.printStackTrace();
        }

        updateLastPosition(unit);
    }

    private void updateLastPosition(AUnit unit) {
        // No engine object means the game has not shown us this unit, so there is
        // nothing to remember. A double the harness put in the world is the
        // exception: no engine object, but it does know where it is, and that is
        // the position worth remembering (UnitOrigin).
        if (unit.u() == null && !UnitOrigin.isSimulated(unit)) return;
//        if (unit.x() <= 0 || unit.x() >= 32000) return;

        // If the unit.u is defined, it means it's visible, so it has valid x,y
        _lastPosition = new APosition(unit.x(), unit.y());
        cacheInt.set("lastPositionUpdated", -1, A.now());
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
     * Returns unit type from JBWAPI OR if type is Unknown (behind fog of war) it will return last cached
     * type.
     */
    @Override
    public AUnitType type() {
        if (_lastType == null) {
            if (_lastAUnit != null) _lastType = _lastAUnit.type();
            else _lastType = super.type();
        }

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


//                if (_lastType != null && (!_lastType.isBuilding() || _lastPosition.isPositionVisible())) {
//                    _lastPosition = null;
//                }
//            }
//        }
//    }

    public void foggedUnitNoLongerWhereItWasBefore() {
//        if (_lastType != null && _lastType.isABuilding()) return;

        updateLastPosition(_lastAUnit);
    }

    public int lastPositionUpdated() {
        return cacheInt.get("lastPositionUpdated");
    }

    @Override
    public int lastPositionUpdatedAgo() {
        if (cacheInt.get("lastPositionUpdated") == null) {
            return -666;
        }

        return A.ago(cacheInt.get("lastPositionUpdated"));
    }

//    public boolean isAccessible() {
//        return !AUnitType.Unknown.equals(_lastAUnit.type());
//    }
//
//    public AUnit innerAUnit() {
//        return _lastAUnit;
//    }

    public void forceSetPositionNull() {
        _lastPosition = null;
    }

    // =========================================================

    @Override
    public String toString() {
        return "F_" + getClass().getSimpleName() + " " + nameWithId()
            + " at " + (_lastPosition != null ? _lastPosition.toString() : "unknown_pos")
            + " (" + (isEnemy() ? "Enemy" : (isOur() ? "Our" : "Neutral")) + ")";
    }

    // =========================================================

//    @Override
//    public boolean exists() {
//        return true;
//    }

    @Override
    public APosition position() {
        return _lastPosition;
    }

    @Override
    public int energy() {
        return _energy;
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
    public boolean isCompleted() {
        return _isCompleted;
    }

    @Override
    public boolean isCloaked() {
        return _isCloaked;
    }

    @Override
    public boolean isDetected() {
        return _isDetected;
    }

    @Override
    public boolean isEnemy() {
        return !type().isNeutralType();
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
    public boolean isStimmed() {
        return _isStimmed;
    }

    @Override
    public boolean isVisibleUnitOnMap() {
        return position() != null && position().isPositionVisible();
    }

    /**
     * Unknown, deliberately reported as -69: the many {@code hp() <= 0} guards
     * in combat code use it to skip units whose hit points cannot be read, so a
     * fogged unit must not look alive here.
     *
     * <p>Both subclasses override this with {@code maxHp()}, because an army
     * score that ignores every unit behind the fog is not an army score - see
     * {@link FoggedUnit#hp()} and {@link FakeFoggedUnit#hp()}. A new subclass
     * that forgets to will quietly deflate every total it appears in.</p>
     */
    @Override
    public int hp() {
        return -69;
    }

    @Override
    public int shields() {
        return 0;
//        return _shields;
    }

    @Override
    public AUnit target() {
        return null;
    }

    @Override
    public int x() {
        return _lastPosition.x();
    }

    /**
     * Stage E: last-known-state projection of this fogged unit.
     * Additive spike — no production reader migrated yet (see REVIEW §16).
     *
     * <p>Hit points behind the fog are unknown: the snapshot carries the
     * compensated value ({@link #hp()}, i.e. {@code maxHp()} for army sums)
     * with {@code hpKnown = false}, so no reader can mistake it for a
     * full-health reading. See {@code _AI/NEXT.md} #2.</p>
     */
    public UnitSnapshot snapshot() {
        return new UnitSnapshot(id(), type(), position(), hp(), shields(), false);
    }

    @Override
    public int y() {
        return _lastPosition.y();
    }
}
