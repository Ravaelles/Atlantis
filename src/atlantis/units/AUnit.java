package atlantis.units;

import atlantis.architecture.Manager;
import atlantis.combat.generic.DoNothing;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.eval.AtlantisJfap;
import atlantis.combat.micro.avoid.margin.UnitRange;
import atlantis.combat.micro.terran.infantry.medic.TerranMedic;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.Missions;
import atlantis.combat.eval.protoss.ProtossJfapTweaksConsiderChokesEtc;
import atlantis.combat.running.ARunningManager;
import atlantis.combat.squad.NewUnitsToSquadsAssigner;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.combat.state.AttackState;
import atlantis.config.AtlantisRaceConfig;
import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.player.APlayer;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.UnitsArchive;
import atlantis.information.generic.Army;
import atlantis.information.tech.ATech;
import atlantis.information.tech.SpellCoordinator;
import atlantis.map.base.Bases;
import atlantis.map.bullets.ABullet;
import atlantis.map.bullets.DeadMan;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.choke.IsUnitWithinChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.PositionUtil;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.builders.BuilderManager;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.terran.FlyingBuildingScoutCommander;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.actions.Action;
import atlantis.units.actions.Actions;
import atlantis.units.actions.move.MoveAway;
import atlantis.units.attacked_by.UnderAttack;
import atlantis.units.detected.IsOurUnitUndetected;
import atlantis.units.fogged.AbstractFoggedUnit;
import atlantis.units.fogged.FoggedUnit;
import atlantis.units.interrupt.UnitAttackWaitFrames;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.units.workers.AMineralGathering;
import atlantis.util.CappedList;
import atlantis.util.Vector;
import atlantis.util.Vectors;
import atlantis.util.We;
import atlantis.util.cache.Cache;
import atlantis.util.log.ErrorLog;
import atlantis.util.log.Log;
import atlantis.util.log.LogUnitsToFiles;
import bwapi.*;
import tests.fakes.FakeUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static atlantis.units.actions.Actions.RUN_RETREAT;

/**
 * Wrapper for bwapi Unit class that makes units much easier to use.<br /><br />
 * Atlantis uses wrappers for bwapi native classes which can't be extended.<br /><br />
 * <b>AUnit</b> class contains number of helper methods, but if you think some methods are missing you can
 * add them here.
 * <p>
 * Also you can always reference original Unit class via u() method, but please avoid it as code will be very
 * hard to migrate to another bridge. I've already used 3 of them in my career so far.
 */
//public class AUnit implements UnitInterface, Comparable<AUnit>, HasPosition, AUnitOrders {
public class AUnit implements Comparable<AUnit>, HasPosition, AUnitOrders {
    /**
     * Mapping of native unit IDs to AUnit objects
     */
    private static final Map<Integer, AUnit> instances = new HashMap<>();

    /**
     * Inner BWAPI Unit object that we extend for easier code maintainability.
     */
    private Unit u;

    /**
     * Last manager used by this unit. Null means that no manager has been used.
     * You need to set this variable manually each time by using setManagerUsed().
     */
    private Manager manager;

    /**
     * Cache var storing generic Object-type keys.
     */
    private Cache<Object> cache = new Cache<>();
    private Cache<Integer> cacheInt = new Cache<>();
    private Cache<Boolean> cacheBoolean = new Cache<>();

    /**
     * Last type that this unit was seen with. It changes e.g. for Zerg Eggs.
     */
    protected AUnitType _lastType = null;
    private Log log = new Log(Log.UNIT_LOG_EXPIRE_AFTER_FRAMES, Log.UNIT_LOG_SIZE);
    private Log managerLogs = new Log(30 * 30, 5);
    private Log commandHistory = new Log(-1, 10);
    private UnderAttack underAttack = new UnderAttack(this);
    private Action unitAction = Actions.INIT;
    private Action _prevAction = null;
    private AttackState attackState = AttackState.getDefault();
    private ABullet _lastBullet = null;

    /**
     * Production order object for a unit that's currently being trained/produced by this unit.
     */
    private ProductionOrder productionOrder = null;

    /**
     * For buildings, this is the construction object that represents the building process.
     */
    private Construction construction = null;

    public CappedList<Integer> _lastHitPoints = new CappedList<>(20);
//    private AUnit runningFrom = null;

    /**
     * Can be used for special missions to remember position assigned for this unit.
     */
    private APosition specialPosition = null;
    private int _lastActionReceived = 0;
    public int _lastAttackOrder = -9999;
    public int _lastAttackFrame = -9999;
    private int _lastAttackCommand = -9876;
    public int _lastCommandIssued = -9877;
    public int _lastCooldown;
    public int _lastFrameOfStartingAttack = -9999;
    public int _lastRetreat = -9998;
    public int _lastStartedRunning = -999;
    public int _lastStoppedRunning = -999;
    public int _lastRunningPositionChange = -999;
    public int _lastStartedAttack = -999;
    public AUnit _lastTarget = null;
    public AUnitType _lastTargetType = null;
    public int _lastTargetToAttackAcquired = -999;
    public TechType _lastTech;
    public APosition _lastTechPosition;
    public AUnit _lastTechUnit;
    public int _lastUnderAttack = -999;
    public int _hitCount = 0;
    public static int _totalHitCount = 0;
    public int _lastX = -1;
    public int _lastY = -1;
    public int _lastPositionChanged = -999;
    public HasPosition _lastPositionRunInAnyDir = null;
    private AUnit _targetUnitToAttack;

    private boolean isScout = false;
    private boolean isSquadScout = false;

    // =========================================================

    public static AUnit createFrom(Unit u) {
        return createFrom(u, true);
    }

    /**
     * Atlantis uses wrapper for BWAPI classes.
     *
     * <b>AUnit</b> class contains numerous helper methods, but if you think some methods are missing you can
     * create missing method here and you can reference original Unit class via u() method.
     * <p>
     * The idea why we don't use inner Unit class is because if you change game bridge (JBWAPI, JNIBWAPI, JBWAPI etc)
     * you need to change half of your codebase. I've done it 3 times already ;__:
     */
    public static AUnit createFrom(Unit u, boolean throwErrorOnNull) {
        if (u == null) {
            if (!throwErrorOnNull) return null;
            throw new RuntimeException("AUnit constructor: unit is null");
        }

        AUnit unit;
        if (instances.containsKey(u.getID())) {
            unit = instances.get(u.getID());
//            if (unit != null && unit.isAlive()) {
            if (unit != null) {
                return unit;
            }
//            instances.remove(id());
        }

        unit = new AUnit(u);
        instances.put(unit.id(), unit);
        return unit;
    }

    public static AUnit getById(Unit u) {
        return createFrom(u);
    }

    // =========================================================
    // Constructors only used for tests

    protected AUnit() {
        initManagers();
    }

    protected AUnit(FakeUnit unit) {
        initManagers();
    }

    // =========================================================

    protected AUnit(Unit u) {
        this.u = u;
        initManagers();

        // Cached type helpers
        refreshType();

        // Repair & Heal
        this._repairableMechanically = isABuilding() || isVehicle();
        this._healable = isInfantry() || isWorker();

        // Military building
        this._isMilitaryBuildingAntiGround = is(
            AUnitType.Terran_Bunker, AUnitType.Protoss_Photon_Cannon, AUnitType.Zerg_Sunken_Colony
        );
        this._isMilitaryBuildingAntiAir = is(
            AUnitType.Terran_Bunker, AUnitType.Terran_Missile_Turret,
            AUnitType.Protoss_Photon_Cannon, AUnitType.Zerg_Spore_Colony
        );
    }

    // =========================================================

    /**
     * Last Manager used by this unit. Null means to manager has been used.
     */
    public Manager manager() {
        return manager;
    }

    private void initManagers() {
        manager = new DoNothing(this);

        runningManager = new ARunningManager(this);
    }

    /**
     * Indicate that this is the Manager used by the unit at the moment.
     */
//    public boolean useManager(Manager manager) {
//        this.manager = manager;
//
//        return manager != null;
//    }

    // =========================================================
    protected void clearAUnitCache() {
        cache.clear();
        cacheInt.clear();
        cacheBoolean.clear();
        if (Env.isTesting()) instances.clear();
    }

    // =========================================================

    public static void forgetUnitEntirely(AUnit unit) {
        instances.remove(unit.id());
    }

    /**
     * Returns unit type from bridge OR if type is Unknown (behind fog of war) it will return last cached type.
     */
    public AUnitType type() {
        if (_lastType != null) {
            return _lastType;
        }

        return cacheType();
    }

    public UnitType bwapiType() {
        if (this instanceof AbstractFoggedUnit) {
            return type().ut();
        }
        return u.getType();
    }

    public void refreshType() {
        cache.clear();
        cacheBoolean.clear();
        cacheInt.clear();
        cacheType();
    }

    protected AUnitType cacheType() {
        _lastType = u != null ? AUnitType.from(u.getType()) : null;
        return _lastType;
    }

    @Override
    public APosition position() {
        return APosition.create(u.getPosition());
    }

    /**
     * <b>AVOID USAGE AS MUCH AS POSSIBLE</b> outside AUnit class. AUnit class should be used always in place
     * of Unit.
     */
    @Override
    public Unit u() {
        return u;
    }

    /**
     * This method exists only to allow reference in UnitActions class.
     */
    @Override
    public AUnit unit() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof AUnit)) return false;
        AUnit aUnit = (AUnit) o;
        return id() == aUnit.id();
    }

    @Override
    public int hashCode() {
        return id();
    }

    // =========================================================
    // =========================================================
    // =========================================================

    private Squad squad;
    private ARunningManager runningManager;

    private boolean _repairableMechanically = false;
    private boolean _healable = false;
    private boolean _isMilitaryBuildingAntiGround = false;
    private boolean _isMilitaryBuildingAntiAir = false;

    // =========================================================
    // Important methods

    /**
     * Unit will move by given distance (in build tiles) from given *from*.
     */
    public boolean moveAwayFrom(HasPosition from, double moveDistance, Action action, String tooltip) {
        return MoveAway.from(this, from, moveDistance, action, tooltip);
    }

    public boolean moveAwayFrom(HasPosition from, double moveDistance, Action action) {
        return MoveAway.from(this, from, moveDistance, action, "");
    }

    // =========================================================

    @Override
    public String toString() {
        if (type() == null) {
            ErrorLog.printMaxOncePerMinute("AUnit type() is NULL");
            return "ERROR_NULL_TYPE";
        }
        return idWithHash() + " "
            + (type() != null ? type().name() : "NULL_TYPE")
            + " @" + position()
            + (isWorker() && isOur() ? " (" + manager + ")" : "");
    }

    @Override
    public int compareTo(AUnit otherUnit) {
        return Integer.compare(this.hashCode(), otherUnit.hashCode());
    }

    // =========================================================
    // Compare type methods

    public boolean isAlive() {
        return exists() && ((hp() > 0) || !UnitsArchive.isDestroyed(id()));
    }

    public boolean isDead() {
        return !isAlive();
    }

    public boolean canBeHealed() {
        return _repairableMechanically || _healable;
    }

    public boolean canBeRepaired() {
        return _repairableMechanically;
    }

    public boolean isHealable() {
        return _healable;
    }

    /**
     * Returns true if given unit is OF TYPE BUILDING.
     */
    public boolean isABuilding() {
        return type().isABuilding() || type().isAddon();
    }

    public boolean isWorker() {
        return type().isWorker();
    }

    public boolean isWraith() {
        return type().is(AUnitType.Terran_Wraith);
    }

    public boolean isBunker() {
        return type().equals(AUnitType.Terran_Bunker);
    }

    public boolean isCannon() {
        return type().equals(AUnitType.Protoss_Photon_Cannon);
    }

    public boolean isBase() {
        return is(
            AUnitType.Terran_Command_Center, AUnitType.Protoss_Nexus,
            AUnitType.Zerg_Hatchery, AUnitType.Zerg_Lair, AUnitType.Zerg_Hive
        );
    }

    public boolean isInfantry() {
        return (boolean) cache.get(
            "isInfantry",
            -1,
            () -> type().isOrganic()
        );
    }

    public boolean isVehicle() {
        return type().isMechanical();
    }

    /**
     * Returns true if given unit is considered to be "ranged" unit (not melee).
     */
    public boolean isRanged() {
        return (boolean) cache.get(
            "isRanged",
            -1,
            () -> type().isRanged()
        );
    }

    /**
     * Returns true if given unit is considered to be "melee" unit (not ranged).
     */
    public boolean isMelee() {
        return (boolean) cache.get(
            "isMelee",
            -1,
            () -> type().isMelee()
        );
    }

    // =========================================================
    // Auxiliary methods
    public boolean ofType(AUnitType type) {
        return type().equals(type);
    }

    public boolean isFullyHealthy() {
        return hp() >= maxHp();
    }

    public int hpPercent() {
        return 100 * hp() / maxHp();
    }

    public boolean hpPercent(int minPercent) {
        return hpPercent() >= minPercent;
    }

    public int woundHp() {
        return maxHp() - hp();
    }

    public double woundPercent() {
        return 100 - 100.0 * hp() / maxHp();
    }

    public boolean woundPercentMin(int minWoundPercent) {
        return woundPercent() >= minWoundPercent;
    }

    public boolean woundPercentMax(int maxWoundPercent) {
        return woundPercent() <= maxWoundPercent;
    }

    public boolean isWounded() {
        return hp() < maxHP() || shields() < maxShields();
    }

    public boolean isExists() {
        return u().exists();
    }

    public int shields() {
        return u().getShields();
    }

    public int maxShields() {
        return type().ut().maxShields();
    }

    public int maxHP() {
        return maxHp() + maxShields();
    }

    public int minesCount() {
        return u().getSpiderMineCount();
    }

    public String name() {
        return type().name();
    }

    public String nameWithId() {
        return type().name() + " #" + id();
    }

    public boolean isTargetInWeaponRangeAccordingToGame(AUnit target) {
        return target == null || hasWeaponRangeByGame(target);
    }

    public boolean isTargetInWeaponRangeAccordingToGame() {
        AUnit target = target();
        if (target == null) return false;

        return hasWeaponRangeByGame(target);
    }

    /**
     * Returns max shoot range (in build tiles) of this unit against land targets.
     */
    public int groundWeaponRange() {
        int range = cacheInt.get(
            "groundWeaponRange",
            60,
            () -> type().groundWeapon().maxRange() / 32
        );

        if (isMarine()) {
            if (isLoaded()) {
                range++;
            }
            if (isOur() && ATech.isResearched(UpgradeType.U_238_Shells)) {
                range++;
            }
        }
        else if (isDragoon()) {
            if (ATech.isResearched(UpgradeType.Singularity_Charge)) {
                range += 2;
            }
        }

        return range;
    }

    /**
     * Returns max shoot range (in build tiles) of this unit against land targets.
     */
    public double groundWeaponMinRange() {
        return cacheInt.get(
            "getGroundWeaponMinRange",
            60,
            () -> type().groundWeapon().minRange() / 32
        );
    }

    /**
     * Returns max shoot range (in build tiles) of this unit against land targets.
     */
    public double airWeaponRange() {
        return cacheInt.get(
            "airWeaponRange",
            60,
            () -> type().airWeapon().maxRange() / 32
        );
    }

    /**
     * Returns max range of otherUnit's weapon against this unit.
     */
    public int enemyWeaponRangeAgainstThisUnit(AUnit otherUnit) {
        return otherUnit.type().weaponRangeAgainst(this)
            + UnitRange.unitRangeBonus(otherUnit);
    }

    public int weaponRangeAgainst(AUnit enemyUnit) {
        return type().weaponRangeAgainst(enemyUnit);
    }

//    public boolean hasBiggerWeaponRangeThan(AUnit enemyUnit) {
//        return type().weaponRangeAgainst(enemyUnit) > enemyUnit.type().weaponRangeAgainst(this);
//    }

    /**
     * Returns which unit of the same type this unit is. E.g. it can be first (0) Overlord or third (2)
     * Zergling. It compares IDs of units to return correct result.
     */
    public int getUnitIndexInBwapi() {
        return cacheInt.get(
            "getUnitIndexInBwapi",
            2,
            () -> {
                int index = 0;
                for (AUnit otherUnit : Select.ourOfType(type()).list()) {
                    if (otherUnit.id() < this.id()) index++;
                }
                return index;
            }
        );
    }

    // ===  Debugging / Painting methods ========================================

    private String tooltip;
    private String tooltipForManager;

    public AUnit setTooltipTactical(String tooltip) {
        return setTooltip(tooltip, false);
    }

    public AUnit setTooltip(String tooltip, boolean strategicLevel) {
        if (strategicLevel) {
            this.tooltip = tooltip;
        }
        this.tooltip = tooltip;

        if (Log.SAVE_UNIT_LOGS_TO_FILES > 0) {
            LogUnitsToFiles.saveUnitLogToFile(tooltip, this);
        }

        return this;
    }

    public AUnit setTooltipAndLog(String tooltip) {
        setTooltip(tooltip);
        addLog(tooltip);
        return this;
    }

    public boolean setTooltip(String tooltip) {
        this.tooltip = tooltip;
        return true;
    }

    public String tooltip() {
        return tooltip;
    }

    public String managerTooltip() {
        return tooltipForManager;
    }

    public void removeTooltip() {
        this.tooltip = null;
    }

    public boolean hasTooltip() {
        return tooltip != null && !tooltip.equals("");
    }

    // =========================================================
    // Very specific auxiliary methods

    /**
     * Returns true if given unit is one of buildings like Bunker, Photon Cannon etc. For more details, you
     * have to specify at least one <b>true</b> to the params.
     */
    public boolean isMilitaryBuilding(boolean canShootGround, boolean canShootAir) {
        if (!isABuilding()) return false;
        if (canShootGround && _isMilitaryBuildingAntiGround) return true;
        else return canShootAir && _isMilitaryBuildingAntiAir;
    }

    public boolean isGroundUnit() {
        return !type().isAir();
    }

    public boolean isAir() {
        return type().isAir();
    }

    public boolean isMine() {
        return type().equals(AUnitType.Terran_Vulture_Spider_Mine);
    }

    public boolean isLarvaOrEgg() {
        return type().equals(AUnitType.Zerg_Larva) || type().equals(AUnitType.Zerg_Egg);
    }

    public boolean isLarva() {
        return type().equals(AUnitType.Zerg_Larva);
    }

    public boolean isEgg() {
        return type().equals(AUnitType.Zerg_Egg);
    }

    /**
     * Not that we're racists, but buildings, spider mines and larvas aren't really units...
     */
    public boolean isRealUnit() {
        return type().isRealUnit();
    }

    public boolean isRealUnitOrBuilding() {
        return type().isRealUnitOrBuilding();
    }

    public boolean isRealUnitOrCombatBuilding() {
        return type().isRealUnitOrCombatBuilding();
    }

    // =========================================================
    // Auxiliary

    public double distTo(AUnit otherUnit) {
//        System.err.println("AUnit::distTo (AUnit)");
//        System.err.println("This = " + this);
//        System.err.println("otherUnit = " + otherUnit);

        return PositionUtil.distanceTo(this, otherUnit);
    }

    public double distTo(Object o) {
//        System.err.println("AUnit::distTo (Object)");

        return PositionUtil.distanceTo(position(), o);
    }

    /**
     * Converts collection of <b>Unit</b> variables into collection of <b>AUnit</b> variables.
     */
    private static Object convertToAUnitCollection(Object collection) {
        if (collection instanceof Map) {
            Map<AUnit, Integer> result = new HashMap<>();
            for (Object key : ((Map) collection).keySet()) {
                Unit u = (Unit) key;
                AUnit unit = createFrom(u);
                result.put(unit, (Integer) ((Map) collection).get(u));
            }
            return result;
        }
        else if (collection instanceof List) {
            List<AUnit> result = new ArrayList<>();
            for (Object key : (List) collection) {
                Unit u = (Unit) key;
                AUnit unit = createFrom(u);
                result.add(unit);
            }
            return result;
        }
        else {
            throw new RuntimeException("I don't know how to convert collection of type: "
                + collection.toString());
        }
    }

    // =========================================================
    // RANGE and ATTACK methods

    /**
     * Returns true if this unit is capable of attacking <b>target</b>. For example Zerglings can't attack
     * flying targets and Corsairs can't attack ground targets.
     */
    public boolean canAttackTarget(AUnit target) {
        return canAttackTarget(target, false, true);
    }

    public boolean canAttackTarget(
        AUnit target,
        boolean checkShootingRange
    ) {
        return canAttackTarget(target, checkShootingRange, true, false, 0);
    }

    public boolean canAttackTarget(
        AUnit target,
        boolean checkShootingRange,
        boolean checkVisibility
    ) {
        return canAttackTarget(target, checkShootingRange, checkVisibility, false, 0);
    }

    public boolean canAttackTargetWithBonus(
        AUnit target,
        double rangeBonus
    ) {
        return canAttackTarget(target, false, false, false, rangeBonus);
    }

    public boolean canAttackTarget(
        AUnit target,
        boolean checkShootingRange,
        boolean checkVisibility,
        boolean includeCooldown,
        double extraMargin
    ) {
        if (target == null || !target.hasPosition()) return false;

        // Target is GROUND unit
        if (target.isGroundUnit() && (!canAttackGroundUnits() || (includeCooldown && cooldownRemaining() >= 4)))
            return false;

        // Target is AIR unit
        if (target.isAir() && (!canAttackAirUnits() || (includeCooldown && cooldownRemaining() >= 4))) return false;

        if (hasNoWeaponAtAll() || !hasWeaponToAttackThisUnit(target)) return false;
        if (checkVisibility && target.effUndetected()) return false;
        if (checkShootingRange && !hasWeaponRangeToAttack(target, extraMargin)) return false;
        if (isRanged() && target.isUnderDarkSwarm()) return false;
        if (isABuilding() && isProtoss() && !isPowered()) return false;

        return true;
    }

    public boolean hasWeaponToAttackThisUnit(AUnit otherUnit) {
        // Enemy is GROUND unit
        if (otherUnit.isGroundUnit()) {
            return canAttackGroundUnits();
        }

        // Enemy is AIR unit
        else {
            return canAttackAirUnits();
        }
    }

    /**
     * Returns <b>true</b> if this unit can attack <b>targetUnit</b> in terms of both min and max range
     * conditions fulfilled.
     */
    public boolean hasWeaponRangeByGame(AUnit targetUnit) {
        if (Env.isTesting()) return hasWeaponRangeToAttack(targetUnit, 0);

        return this.u.isInWeaponRange(targetUnit.u);
    }

    public boolean hasWeaponRangeToAttack(AUnit targetUnit, double extraMargin) {
        if (!targetUnit.isDetected() || targetUnit.position() == null) return false;
//
//        if (isDragoon()) return distToLessThan(targetUnit, OurDragoonRange.range());
//        if (isBunker()) return distToLessThan(targetUnit, 7);
//
//        WeaponType weaponAgainstThisUnit = weaponAgainst(targetUnit);
//        if (weaponAgainstThisUnit == WeaponType.None) return false;

        double dist = this.distTo(targetUnit);

        if (isTankSieged() && dist < 2) return false;

        return dist <= (weaponRangeAgainst(targetUnit) + extraMargin);
    }

    /**
     * Returns weapon that would be used to attack given target. If no such weapon, then WeaponTypes.None will
     * be returned.
     */
    public WeaponType weaponAgainst(AUnit target) {
        if (target.isGroundUnit()) {
            return groundWeapon();
        }
        else {
            return airWeapon();
        }
    }

    public int damageAgainst(AUnit target) {
        WeaponType weapon = weaponAgainst(target);
        if (weapon == null) {
            return 0;
        }

        return weapon.damageAmount() * weapon.damageFactor();
    }

    public boolean distToLessThan(AUnit target, double maxDist) {
        if (target == null) return false;

        return distTo(target) <= maxDist;
    }

    public boolean distToMoreThan(AUnit target, double minDist) {
        if (target == null) return false;

        return distTo(target) >= minDist;
    }

    // === Getters ============================================= & setters

    /**
     * Returns true if given unit is currently (this frame) running from an enemy.
     */
    public boolean isRunning() {
//        if (runningManager.isRunning()) {
//            APainter.paintCircleFilled(this, 5, Color.Red);
//        }

//        if (true) return false;

        return runningManager.isRunning();
    }

    public boolean isRetreating() {
        return isRunning() && lastActionLessThanAgo(20, RUN_RETREAT);
    }

    public boolean lastOrderMinFramesAgo(int minFramesAgo) {
        return AGame.now() - _lastActionReceived >= minFramesAgo;
    }

    public boolean lastOrderMaxFramesAgo(int maxFramesAgo) {
        return AGame.now() - _lastActionReceived <= maxFramesAgo;
    }

    public int lastOrderWasFramesAgo() {
        return AGame.now() - _lastActionReceived;
    }

    /**
     * Returns battle squad object for military units or null for non military-units (or buildings).
     */
    public Squad squad() {
//        if (squad == null && isOur()) {
//            NewUnitsToSquadsAssigner.possibleCombatUnitCreated(this);
//            A.printStackTrace("Should not be here");
//        }

        return squad;
    }

    /**
     * Assign battle squad object for military units.
     */
    public AUnit setSquad(Squad squad) {
        this.squad = squad;

        if (squad != null) squad.addUnit(this);

        return this;
    }

    /**
     * Returns AtlantisRunning object for this unit.
     */
    public ARunningManager runningManager() {
        return runningManager;
    }

    /**
     * Returns true if unit is in the middle of an attack and should not be interrupted otherwise
     * it would never shoot, just raise the weapon.
     */
//    public boolean isJustShooting() {
//        return DontInterruptStartedAttacks.shouldNotInterrupt(unit());
//    }

    /**
     * Returns the frames counter (time) when the unit had been issued any command.
     */
    public int lastUnitOrderTime() {
        return _lastActionReceived;
    }

    /**
     * Returns the frames counter (time) since the unit had been issued any command.
     */
    public int lastActionFramesAgo() {
        return AGame.now() - _lastActionReceived;
    }

    /**
     * Indicate that in this frame unit received some command (attack, move etc).
     */
    public AUnit setLastActionReceivedNow() {
        this._lastActionReceived = AGame.now();
        return this;
    }

    /**
     * Returns true if unit has anti-ground weapon.
     */
    public boolean canAttackGroundUnits() {
        return (boolean) cache.get(
            "canAttackGroundUnits",
            -1,
            () -> type().canAttackGround()
        );
    }

    /**
     * Returns true if unit has anti-air weapon.
     */
    public boolean canAttackAirUnits() {
        return (boolean) cache.get(
            "canAttackAirUnits",
            -1,
            () -> type().canAttackAir()
        );
    }

    public WeaponType airWeapon() {
        return type().airWeapon();
    }

    public WeaponType groundWeapon() {
        return type().groundWeapon();
    }

    /**
     * Returns number of frames unit has to wait between the shots.
     * E.g. for Dragoon this value will be always 30.
     */
    public int cooldownAbsolute() {
        if (canAttackGroundUnits()) {
            return groundWeapon().damageCooldown();
        }
        if (canAttackAirUnits()) {
            return airWeapon().damageCooldown();
        }
        return 0;
    }

    /**
     * Returns number of frames unit STILL has to wait before it can shoot again.
     * E.g. for Dragoon this value will vary between 0 and 30 inclusive.
     */
    public int cooldownRemaining() {
        if (u == null || !isVisibleUnitOnMap()) return 0;

        if (canAttackGroundUnits()) {
            return groundWeaponCooldown();
        }
        if (canAttackAirUnits()) {
            return airWeaponCooldown();
        }
        return 0;
    }

    /**
     * Indicates that this unit should be running from given enemy unit.
     * If enemy parameter is null, it will try to determine the best run behavior.
     * If enemy is not null, it will try running straight from this unit.
     */
//    public boolean runFrom(HasPosition runFrom, double dist) {
//        return runningManager.runFrom(runFrom, dist);
//    }
//
//    public boolean runFrom() {
//        return runningManager.runFromCloseEnemies();
//    }

    /**
     * Returns <b>true</b> if this unit is supposed to "build" something. It will return true even if the unit
     * wasn't issued yet actual build order, but we've created ConstructionOrder and assigned it as a builder,
     * so it will return true.
     */
    public boolean isBuilder() {
        return BuilderManager.isBuilder(this);
    }

    /**
     * If this unit is supposed to build something it will return ConstructionOrder object assigned to the
     * construction.
     */
    public Construction construction() {
        return construction;
    }

    public Construction setConstruction(Construction construction) {
        this.construction = construction;
        return construction;
    }

//    public Construction construction() {
//        return ConstructionRequests.constructionFor(this);
//    }

    /**
     * Returns true if this unit belongs to the enemy.
     */
    public boolean isEnemy() {
        if (u == null) return true;

        return AGame.getPlayerUs().isEnemy(player());
    }

    /**
     * Returns true if this unit belongs to us.
     */
    public boolean isOur() {
        if (u == null || player() == null) return false;

        return player().equals(AGame.getPlayerUs());
    }

    /**
     * Returns true if this unit is neutral (minerals, geysers, critters).
     */
    public boolean isNeutral() {
        return player().equals(AGame.neutralPlayer());
    }

    /**
     * Returns true if given building is able to build add-on like Terran Machine Shop.
     */
    public boolean canHaveAddon() {
        return type().canHaveAddon();
    }

    public int id() {
        return u.getID();
    }

    public String idWithHash() {
        return "#" + id();
    }

    public String idWithType() {
        return typeWithUnitId();
    }

    public String typeWithHash() {
        return "#" + type();
    }

    public String typeWithUnitId() {
        return type() + "#" + id();
    }

    // =========================================================
    // Method intermediates between bridge and Atlantis

    public APlayer player() {
        return new APlayer(u.getPlayer());
    }
//    public Player player() {
//        return u.getPlayer();
//    }

    public int x() {
        return u.getX();
    }

    public int y() {
        return u.getY();
    }

    public boolean isCompleted() {
        return u.isCompleted();
    }

    public boolean exists() {
        return u() != null ? u.exists() : true;
    }

    public boolean isConstructing() {
        return u.isConstructing();
    }

    public boolean hasAddon() {
        return u().getAddon() != null;
    }

    public int hp() {
        return hitPoints() + shields();
    }

    protected int hitPoints() {
        return u.getHitPoints();
    }

    public int maxHp() {
        return (int) cache.get(
            "maxHp",
            -1,
            () -> {
                int hp = type().maxHp() + maxShields();
                if (hp == 0 && !type().isSpell()) {
                    System.err.println("Max HP = 0 for");
                    System.err.println(this);
                }

                return hp > 0 ? hp : 1;
            }
        );
    }

    public boolean isResearching() {
        return u.isResearching();
    }

    public boolean isUpgradingSomething() {
        return u.isUpgrading();
    }

    public TechType whatIsResearching() {
        if (u.getLastCommand() == null) {
            return null;
        }
        return u.getLastCommand().getTechType();
    }

    public UpgradeType whatIsUpgrading() {
        if (u.getLastCommand() == null) {
            return null;
        }
        return u.getLastCommand().getUpgradeType();
    }

    public boolean isIdle() {
        return u.isIdle() || (u.getLastCommand() == null || u.getLastCommand().getType().equals(UnitCommandType.None));
    }

    public boolean isBusy() {
        return !isIdle();
    }

    public boolean isFree() {
        return isIdle();
    }

    private boolean ensnared() {
        return u.isEnsnared();
    }

    private boolean plagued() {
        return u.isPlagued();
    }

    /**
     * RETURNS TRUE IF UNIT IS VISIBLE ON MAP (NOT THAT UNIT IS NOT CLOAKED!).
     */
    public boolean isVisibleUnitOnMap() {
        return u != null && u.isVisible();
    }

    public boolean effVisible() {
        return isVisibleUnitOnMap() && (isDetected() || !effUndetected());
    }

    /**
     * Unit is effectvely cloaked and we can't attack it. Need to detect it first.
     */
    public boolean effUndetected() {
        if (isOur()) return IsOurUnitUndetected.check(this);

        return (!isDetected() || hp() == 0);

//        return true;
//        return hp() == 0;
//        return !unit().isDetected();
//        if (isOur()) {
//            return ;
//        }
//        effectivelyCloaked: Boolean = (
//                cloakedOrBurrowed
//                        && ! ensnared
//                        && ! plagued
//                        && (
//        if (isOurs) (
//                ! tile.enemyDetected
//                        && ! matchups.enemies.exists(_.orderTarget.contains(this))
//                        && ! With.bullets.all.exists(_.targetUnit.contains(this)))
//        else ! detected))
    }

    public boolean isDetected() {
        return u.isDetected() && hp() > 0;
    }

    public boolean notVisible() {
        return !u.isVisible();
    }

    public boolean isMiningOrExtractingGas() {
        return isGatheringMinerals() || isGatheringGas();
    }

    public boolean isGatheringMinerals() {
        return u != null && u.isGatheringMinerals();
    }

    public boolean isGatheringGas() {
        return u != null && u.isGatheringGas();
    }

    public boolean isCarryingMinerals() {
        return u != null && u.isCarryingMinerals();
    }

    public boolean isCarryingGas() {
        return u != null && u.isCarryingGas();
    }

    public boolean isCloaked() {
        return u.isCloaked() || u.isBurrowed();
    }

    public boolean isBurrowed() {
        if (u == null) return false;

        return u.isBurrowed() || hp() <= 0;
    }

    public boolean isBurrowing() {
        if (u == null) return false;

        /*
         * Apply fix for enemy lurkers. We can't access lastCommand, but we can not if unit is not interruptible.
         * If it is and the unit is not burrowed, then it probably means it's burrowing.
         */
        if (!isOur()) {
            if (!u.isInterruptible() && !isBurrowed() && lastPositionChangedMoreThanAgo(1)) {
//                System.out.println("Enemy burrowing detected: " + this + " / int:" + u.isInterruptible() + " / bur:" + isBurrowed());
                return true;
            }
        }

        return isCommand(UnitCommandType.Burrow);
    }

    public boolean isRepairing() {
        return u != null && u.isRepairing();
    }

    public int groundWeaponCooldown() {
        return u != null ? u.getGroundWeaponCooldown() : 0;
    }

    public int cooldown() {
        return Math.max(groundWeaponCooldown(), airWeaponCooldown());
    }

    public int airWeaponCooldown() {
        return u != null ? u.getAirWeaponCooldown() : 0;
    }

    public boolean isAttackFrame() {
        return u.isAttackFrame();
    }

    public boolean isStartingAttack() {
        return u.isStartingAttack();
    }

    public boolean isStopped() {
        return u != null && (u.getLastCommand() == null || u.getLastCommand().getType() == UnitCommandType.Stop);
    }

    public boolean isStuck() {
        if (u == null) return false;

        return u.isStuck();
    }

    public boolean isHoldingPosition() {
        return u.isHoldingPosition();
    }

    public boolean isPatrolling() {
        return u.isPatrolling();
    }

    public boolean isSieged() {
        return u.isSieged();
    }

    public boolean isUnsieged() {
        return !u.isSieged();
    }

    public boolean isUnderAttack(int inLastFrames) {
        // In-game solutions sucks ass badly
//        return u.isUnderAttack();

        if (_lastHitPoints.size() < inLastFrames) return false;

        return hp() < _lastHitPoints.get(inLastFrames - 1);
    }

    public boolean isUnderAttack() {
        // In-game solutions sucks ass badly
//        return u.isUnderAttack();

        return isUnderAttack(1);
    }

    public List<AUnitType> trainingQueue() {
        return (List<AUnitType>) AUnitType.convertToAUnitTypesCollection(u.getTrainingQueue());
    }

    public boolean isUpgrading() {
        return u.isUpgrading();
    }

    public List<AUnit> getLarva() {
        return (List<AUnit>) convertToAUnitCollection(u.getLarva());
    }

    public AUnit target() {
        if (u.getTarget() != null) {
            _lastTarget = AUnit.getById(u.getTarget());
            _lastTargetType = _lastTarget != null ? _lastTarget.type() : null;

            return _lastTarget;
        }

//        if (Actions.MOVE_ATTACK.equals(unitAction)) {
//            return _lastTargetToAttack = targetUnitToAttack();
//        }

        _lastTarget = orderTarget();
        _lastTargetType = _lastTarget != null ? _lastTarget.type() : null;
        return _lastTarget;
    }

    public AUnit lastTarget() {
        return _lastTarget;
    }

    public AUnitType lastTargetType() {
        return _lastTargetType;
    }

    public boolean hasTarget() {
        return target() != null;
    }

    public boolean noTarget() {
        return target() == null;
    }

    public boolean hasTargetPosition() {
        return u.getTargetPosition() != null;
    }

    public APosition targetPosition() {
        return APosition.create(u.getTargetPosition());
    }

    public AUnit orderTarget() {
        if (u == null) return null;
        return u.getOrderTarget() != null ? AUnit.getById(u.getOrderTarget()) : null;
    }

    public AUnit buildUnit() {
        if (u == null) {
            return null;
        }
        return u.getBuildUnit() != null ? AUnit.getById(u.getBuildUnit()) : null;
    }

    public AUnitType buildType() {
        if (u == null) {
            return null;
        }
        return u.getBuildType() != null ? AUnitType.from(u.getBuildType()) : null;
    }

    public boolean isVulture() {
        return type().isVulture();
    }

    /**
     * Terran_SCV     - 4.92
     * Terran_Vulture - 6.4
     */
    public double maxSpeed() {
        return type().ut().topSpeed();
    }

    public boolean isTank() {
        return type().isTank();
    }

    public boolean isTankSieged() {
        return type().isTankSieged();
    }

    public boolean isTankUnsieged() {
        return type().isTankUnsieged();
    }

    public boolean isMorphing() {
        return u.isMorphing();
    }

    public boolean isMoving() {
        return u.isMoving();
    }

    public boolean isAttacking() {
        return (u != null && u.isAttacking()) || (isOur() && isCommand(UnitCommandType.Attack_Unit));
    }

    public boolean isAttackingRecently() {
        return u.isAttacking() && lastActionLessThanAgo(30);
    }

    public boolean isAttackingOrMovingToAttack() {
        return hasValidTarget() && (
            isAttacking() || (action() != null && action().isAttacking())
        );
    }

    public boolean hasValidTarget() {
        return target() != null
            && target().isAlive()
            && !target().isDeadMan();
    }

    /**
     * Returns true for flying Terran building.
     */
    public boolean isLifted() {
        if (u == null) return false;
        if (!We.terran()) return false;
        return u.isLifted();
    }

    /**
     * Returns true if unit is inside bunker or dropship/shuttle.
     */
    public boolean isLoaded() {
        if (u == null) return false;
        return u.isLoaded();
    }

    public boolean isUnderDisruptionWeb() {
        if (u == null) return false;
        return u.isUnderDisruptionWeb();
    }

    public boolean isUnderDarkSwarm() {
        if (u == null) return false;
        return u.isUnderDarkSwarm();
    }

    public boolean isUnderStorm() {
        if (u == null) return false;
        return u.isUnderStorm();
    }

    public int getRemainingBuildTime() {
        return u().getRemainingBuildTime();
    }

    public int remainingResearchTime() {
        return u().getRemainingResearchTime();
    }

    public int remainingTrainTime() {
        return u() != null ? u().getRemainingTrainTime() : -1;
    }

    public int getTotalTrainTime() {
        return type().totalTrainTime();
    }

    public int remainingUpgradeTime() {
        return u().getRemainingUpgradeTime();
    }

    /**
     * Returns true if given position has land connection to given point.
     */
    public boolean hasPathTo(HasPosition point) {
        return u.hasPath(point.position().p());
    }

    public boolean hasPathTo(AUnit unit) {
        return u.hasPath(unit.position().p());
    }

    public boolean isTrainingAnyUnit() {
        return u.isTraining();
    }

    public boolean isBeingConstructed() {
        return u.isBeingConstructed();
    }

    public boolean isInterruptible() {
        return u != null && u.isInterruptible();
    }

    public UnitCommand getLastCommandRaw() {
        if (u == null) return null;
        return u.getLastCommand();
    }

    public boolean isCommand(UnitCommandType command) {
        return u.getLastCommand() != null && u.getLastCommand().getType().equals(command);
    }

    public Action action() {
        if (unitAction == null) return Actions.INVALID;

        return unitAction;
    }

    // === Unit actions ========================================

    public boolean isAction(Action constant) {
        return unitAction == constant;
    }

    public boolean isAction(Action... oneOfActions) {
        for (Action action : oneOfActions) {
            if (unitAction == action) {
                return true;
            }
        }
        return false;
    }

    public boolean isUnitActionAttack() {
        return unitAction == Actions.ATTACK_UNIT || unitAction == Actions.ATTACK_POSITION;
    }

//    public boolean isUnitActionMove() {
//        return unitAction.name().startsWith("MOVE_");
//    }

    public boolean isUnitActionRepair() {
        return unitAction == Actions.REPAIR || unitAction == Actions.MOVE_REPAIR || unitAction == Actions.MOVE_PROTECT;
    }

    public AUnit setAction(Action unitAction) {
        if (Log.logUnitActionChanges && this._prevAction != unitAction) {
            System.err.println(nameWithId() + " ACTION (@ " + A.now() + "): " + unitAction);
        }

        this._prevAction = this.unitAction;
        this.unitAction = unitAction;

        rememberSpecificUnitAction(unitAction);
        return this;
    }

    public AUnit setAction(Action unitAction, TechType tech, APosition usedAt) {
        this._lastTech = tech;
        this._lastTechPosition = usedAt;
        SpellCoordinator.newSpellAt(usedAt, tech);

        return setAction(unitAction);
    }

    public AUnit setAction(Action unitAction, TechType tech, AUnit usedOn) {
        this._lastTech = tech;
        this._lastTechUnit = usedOn;

//        if (ATech.isOffensiveSpell(tech)) {
        SpellCoordinator.newSpellAt(usedOn.position(), tech);
//        }

        return setAction(unitAction);
    }

    private void rememberSpecificUnitAction(Action unitAction) {
        if (unitAction == null) {
            return;
        }
        cacheInt.set(
            "_last" + unitAction.name(),
            -1,
            A.now()
        );
    }

    public boolean lastActionMoreThanAgo(int framesAgo) {
        if (unitAction == null && !isWorker()) {
            ErrorLog.printErrorOnce("unitAction null for " + this);
            return true;
        }

        if (unitAction == null && isWorker()) {
//            ErrorLog.printErrorOnce("Null action for worker");
            return true;
        }

        return lastActionAgo(unitAction) >= framesAgo;
    }

    public boolean lastActionLessThanAgo(int framesAgo) {
        return lastActionAgo(unitAction) <= framesAgo;
    }

    public boolean lastActionMoreThanAgo(int framesAgo, Action unitAction) {
        if (unitAction == null) {
            System.err.println("unitAction B null for " + this);
            return true;
        }

//        if (unitAction.equals(Actions.ATTACK_UNIT)) {
//            System.out.println("lastActionAgo(ATTACK_UNIT) = " + lastActionAgo(unitAction));
//        }

        return lastActionAgo(unitAction) >= framesAgo;
    }

    public boolean lastActionLessThanAgo(int framesAgo, Action unitAction) {
        if (unitAction == null) return false;

        return lastActionAgo(unitAction) <= framesAgo;
    }

    public boolean lastActionLessThanAgo(int framesAgo, Action... oneOfUnitActions) {
        if (oneOfUnitActions == null || !isAction(oneOfUnitActions)) return false;

        for (Action action : oneOfUnitActions) {
            if (lastActionAgo(action) <= framesAgo) return true;
        }

        return false;
    }

    public int lastActionAgo(Action unitAction) {
        if (unitAction == null) {
            return 99998;
        }

        String cacheKey = "_last" + unitAction.name();

        if (!cacheInt.has(cacheKey)) {
//            throw new RuntimeException("No cacheKey `" + cacheKey + "` for " + this);
            return 99999;
        }

        Integer time = cacheInt.get(cacheKey);

//        if (!cacheInt.isEmpty()) {
//            cacheInt.print("lastActionAgo", true);
//        }

        if (time == null) {
            return 9997;
        }
        return A.now() - time;
    }

    public int lastActionFrame(Action unitAction) {
        Integer time = cacheInt.get("_last" + unitAction.name());

        if (time == null) {
            return 0;
        }
        return time;
    }

    // =========================================================

    public boolean noCooldown() {
        return groundWeaponCooldown() <= 2 || airWeaponCooldown() <= 2;
    }

    public boolean hasCooldown() {
        return groundWeaponCooldown() > 2 || airWeaponCooldown() > 2;
    }

    public int scarabCount() {
        return u().getScarabCount();
    }

    public boolean isRepairerOfAnyKind() {
        if (!We.terran()) return false;

        return RepairAssignments.isRepairerOfAnyKind(this) || RepairAssignments.isProtector(this);
    }

    public boolean isScout() {
        return isScout;
    }

    public void setScout(boolean isScout) {
        this.isScout = isScout;
    }

    public boolean isSquadScout() {
        return isSquadScout;
    }

    public void setSquadScout() {
        isSquadScout = true;
    }

//    public boolean isScout() {
//        return ScoutCommander.isScout(this);
//    }

    public boolean isFlyingScout() {
        return FlyingBuildingScoutCommander.isFlyingBuilding(this);
    }

    public int getSpaceProvided() {
        return type().ut().spaceProvided();
    }

    public int spaceRequired() {
        return type().ut().spaceRequired();
    }

    public int spaceRemaining() {
        return u().getSpaceRemaining();
    }

//    public AUnit getCachedNearestMeleeEnemy() {
//        return _cachedNearestMeleeEnemy;
//    }

//    public void setCachedNearestMeleeEnemy(AUnit _cachedNearestMeleeEnemy) {
//        this._cachedNearestMeleeEnemy = _cachedNearestMeleeEnemy;
//    }

    public boolean lastStartedAttackMoreThanAgo(int framesAgo) {
        return A.ago(_lastStartedAttack) >= framesAgo;
    }

    public boolean lastStartedAttackLessThanAgo(int framesAgo) {
        return A.ago(_lastStartedAttack) <= framesAgo;
    }

    public boolean lastUnderAttackLessThanAgo(int framesAgo) {
        return A.ago(_lastUnderAttack) <= framesAgo;
    }

    public boolean lastUnderAttackMoreThanAgo(int framesAgo) {
        return A.ago(_lastUnderAttack) >= framesAgo;
    }

    public boolean lastPositionChangedLessThanAgo(int framesAgo) {
        return A.ago(_lastPositionChanged) <= framesAgo;
    }

    public boolean lastPositionChangedMoreThanAgo(int framesAgo) {
        return A.ago(_lastPositionChanged) >= framesAgo;
    }

    public int lastPositionChangedAgo() {
        return A.ago(_lastPositionChanged);
    }

    public boolean lastAttackFrameMoreThanAgo(int framesAgo) {
        return A.ago(_lastAttackFrame) >= framesAgo;
    }

    public boolean lastAttackFrameLessThanAgo(int framesAgo) {
        return A.ago(_lastAttackFrame) <= framesAgo;
    }

    public int lastUnderAttackAgo() {
        return A.ago(_lastUnderAttack);
    }

    public boolean underAttackSecondsAgo(double seconds) {
        return (A.ago(_lastUnderAttack) / 30.0) <= seconds;
    }

    public boolean lastAttackOrderLessThanAgo(int framesAgo) {
        return A.ago(_lastAttackOrder) <= framesAgo;
    }

    public boolean lastAttackOrderMoreThanAgo(int framesAgo) {
        return A.ago(_lastAttackOrder) <= framesAgo;
    }

    public int lastAttackFrameAgo() {
        return A.ago(_lastAttackFrame);
    }

    public int lastAttackOrderAgo() {
        return A.ago(_lastAttackOrder);
    }

    public boolean lastFrameOfStartingAttackMoreThanAgo(int framesAgo) {
        return A.ago(_lastFrameOfStartingAttack) >= framesAgo;
    }

    public boolean lastFrameOfStartingAttackLessThanAgo(int framesAgo) {
        return A.ago(_lastFrameOfStartingAttack) <= framesAgo;
    }

    public int lastFrameOfStartingAttackAgo() {
        return A.ago(_lastFrameOfStartingAttack);
    }

    public int lastStartedAttackAgo() {
        return A.ago(_lastStartedAttack);
    }

    public int lastSiegedAgo() {
        return lastActionAgo(Actions.SIEGE);
    }

    public int lastUnsiegedAgo() {
        return lastActionAgo(Actions.UNSIEGE);
    }

    public int lastRetreatedAgo() {
        return A.ago(_lastRetreat);
    }

    public int lastRunningPositionChangeAgo() {
        return A.ago(_lastRunningPositionChange);
    }

    public int lastStartedRunningAgo() {
        return A.ago(_lastStartedRunning);
    }

    public boolean lastStartedRunningMoreThanAgo(int framesAgo) {
        return A.ago(_lastStartedRunning) >= framesAgo;
    }

    public boolean lastStartedRunningLessThanAgo(int framesAgo) {
        return A.ago(_lastStartedRunning) <= framesAgo;
    }

    public int lastStoppedRunningAgo() {
        return A.ago(_lastStoppedRunning);
    }

    public boolean lastStoppedRunningLessThanAgo(int framesAgo) {
        return A.ago(_lastStoppedRunning) <= framesAgo;
    }

    public boolean lastStoppedRunningMoreThanAgo(int framesAgo) {
        return A.ago(_lastStoppedRunning) >= framesAgo;
    }

    public boolean hasNotMovedInAWhile() {
        return x() == _lastX && y() == _lastY;
    }

    public boolean hasNotShotInAWhile() {
        return lastAttackFrameMoreThanAgo(30 * 3);
    }

    public boolean isQuick() {
        return maxSpeed() >= 5.8;
    }

    public boolean isAccelerating() {
        return u().isAccelerating();
    }

    public boolean isBraking() {
        return u().isBraking();
    }

    public double getAngle() {
        return u().getAngle();
    }

    public boolean isFacingItsTarget() {
        return target() != null && target().isOtherUnitFacingThisUnit(this);
    }

    public boolean isOtherUnitFacingThisUnit(AUnit otherUnit) {
        if (otherUnit.hasNoU() && !Env.isTesting()) return false;

        Vector positionDifference = Vectors.fromPositionsBetween(this, otherUnit);
        Vector otherUnitLookingVector = Vectors.vectorFromAngle(otherUnit.getAngle(), positionDifference.length());

        return positionDifference.isAngleAlmostIdentical(otherUnitLookingVector);
    }

    public boolean isOtherUnitShowingBackToUs(AUnit otherUnit) {
        if (otherUnit.hasNoU() && !Env.isTesting()) return false;

        Vector positionDifference = Vectors.fromPositionsBetween(this, otherUnit);
        Vector otherUnitLookingVector = Vectors.vectorFromAngle(otherUnit.getAngle(), positionDifference.length());

        return positionDifference.isAngleAlmostOpposite(otherUnitLookingVector);
    }

    public boolean isFacing(AUnit otherUnit) {
        if (otherUnit.hasNoU() && !Env.isTesting()) return false;
        if (hasNoU() && !Env.isTesting()) return false;

        Vector positionDifference = Vectors.fromPositionsBetween(this, otherUnit);
        Vector thisUnitLookingVector = Vectors.vectorFromAngle(this.getAngle(), positionDifference.length());

        return positionDifference.isAngleAlmostIdentical(thisUnitLookingVector);
    }

    /**
     * Inner BWAPI unit object, if null it means unit is not visible.
     */
    public boolean hasNoU() {
        return u == null;
    }

    public boolean isFirstCombatUnit() {
        return id() == Select.ourCombatUnits().first().id();
    }

    public Mission micro() {
        return squad().mission();
    }

    public int squadSize() {
        if (squad() == null) {
            if (Env.isTesting()) return Alpha.count();

            if (!isWorker()) {
                ErrorLog.printMaxOncePerMinute("Squad null for " + nameWithId());
            }
            return 0;
        }
        return squad().size();
    }

    public HasPosition squadCenter() {
        if (!hasSquad()) {
            if (Env.isTesting()) return Select.our().nonBuildings().first();
            return Alpha.alphaCenter();
        }

        return squad().center();
    }

    public AUnit squadCenterUnit() {
        if (!hasSquad()) {
            if (Env.isTesting()) return Select.our().nonBuildings().first();
            return (AUnit) Alpha.alphaCenter();
        }

        return (AUnit) squad().center();
    }

    public Selection squadCenterEnemiesNear() {
        if (squadCenter() == null) return Select.from(new ArrayList<>(), "squadCenterEnemiesNear_0");

        return (Selection) cache.get(
            "squadCenterEnemiesNear",
            1,
            () -> Select.enemy().havingPosition().inRadius(18, squadCenter())
        );
    }

    public int energy() {
        return u.getEnergy();
    }

    public boolean energy(int min) {
        return energy() >= min;
    }

    /**
     * If anotherUnit is null it returns FALSE.
     * Returns TRUE if anotherUnit is the same unit as this unit (and it's alive and not null).
     */
    public boolean is(AUnit isTheSameAliveNotNullUnit) {
        return isTheSameAliveNotNullUnit != null && isTheSameAliveNotNullUnit.isAlive() && !this.equals(isTheSameAliveNotNullUnit);
    }

    public int cooldownPercent() {
        if (cooldownRemaining() <= 0 || cooldownAbsolute() == 0) {
            return 100;
        }

        return 100 * cooldownRemaining() / (cooldownAbsolute() + 1);
    }

    /**
     * Current mission object for this unit's squad.
     */
    public Mission mission() {
        if (squad == null) {
            if (isCombatUnit() && !isABuilding()) {
//                if (!A.isUms()) System.err.println("Empty unit squad for: " + this);
                (new NewUnitsToSquadsAssigner(this)).possibleCombatUnitCreated();
            }
            return Missions.DEFEND;
        }
        else if (squad.mission() == null) {
            A.errPrintln("Empty squad mission for: " + squad);
            return Missions.DEFEND;
        }

        return squad != null ? squad.mission() : null;
    }

    public boolean isQuickerOrSameSpeedAs(Units enemies) {
        return enemies.stream().noneMatch(u -> u.maxSpeed() > this.maxSpeed());
    }

    public boolean isQuickerOrSameSpeedAs(AUnit enemy) {
        return enemy.maxSpeed() < this.maxSpeed();
    }

    public boolean isSlowerThan(Units enemies) {
        return enemies.stream().anyMatch(u -> u.maxSpeed() > this.maxSpeed());
    }

    public boolean hasBiggerWeaponRangeThan(AUnit enemy) {
        if (isGroundUnit()) {
            return groundWeaponRange() > enemy.groundWeaponRange();
        }
        return airWeaponRange() > enemy.airWeaponRange();
    }

    public boolean hasBiggerWeaponRangeThan(Units enemies) {
        if (isGroundUnit()) {
            return enemies.stream().noneMatch(u -> u.groundWeaponRange() > this.groundWeaponRange());
        }
        else {
            return enemies.stream().noneMatch(u -> u.groundWeaponRange() > this.airWeaponRange());
        }
    }

    public boolean hasNothingInQueue() {
        return trainingQueue().size() <= 1;
    }

    public boolean canCloak() {
        return type().isCloakable() && !isCloaked();
    }

    public boolean is(AUnitType type) {
        return cacheBoolean.get(
            "isType:" + type.id(),
            2,
            () -> type().is(type)
        );
    }

    public boolean is(AUnitType... types) {
        return type().is(types);
    }

    public boolean isTargetedBy(AUnit attacker) {
        return this.equals(attacker.target());
    }

    public boolean isArchon() {
        return is(AUnitType.Protoss_Archon);
    }

    public boolean isUltralisk() {
        return is(AUnitType.Zerg_Ultralisk);
    }

    public List<AUnit> loadedUnits() {
        List<AUnit> loaded = new ArrayList<>();
        for (Unit unit : u.getLoadedUnits()) {
            loaded.add(AUnit.getById(unit));
        }
        return loaded;
    }

//    public boolean loadedUnitsHas(AUnitType type) {
//        System.err.println("u.getLoadedUnits() = " + u.getLoadedUnits().size());
//        for (Unit unit : u.getLoadedUnits()) {
//            System.err.println("unit.getType() = " + unit.getType());
//            if (unit.getType().equals(type.ut())) {
//                return true;
//            }
//        }
//        return false;
//    }

    public AUnit loadedUnitsGet(AUnitType type) {
        for (Unit loaded : u.getLoadedUnits()) {
            AUnit au = AUnit.getById(loaded);
//            System.err.println("au = " + au);
//            System.err.println("type = " + type);
//            System.err.println("au.is(type) = " + au.is(type));
//            System.err.println("au.is(SHUT) = " + au.is(AUnitType.Protoss_Shuttle));
            if (au.is(type)) {
                return au;
            }
        }
        return null;
    }

    public int lastTechUsedAgo() {
        return lastActionAgo(Actions.USING_TECH);
    }

    public TechType lastTechUsed() {
        return _lastTech;
    }

    public APosition lastTechPosition() {
        return _lastTechPosition;
    }

    public AUnit lastTechUnit() {
        return _lastTechUnit;
    }

    public boolean hasCargo() {
        return u.getLoadedUnits().size() > 0;
    }

    public boolean hasFreeSpaceFor(AUnit passenger) {
        return spaceRemaining() >= passenger.spaceRequired();
    }

    public boolean hasAnyWeapon() {
        return !hasNoWeaponAtAll();
    }

    public boolean hasNoWeaponAtAll() {
        return cacheBoolean.get(
            "hasNoWeaponAtAll",
            -1,
            () -> !isBunker() && type().hasNoWeaponAtAll()
        );

//        if (type().isReaver() && scarabCount() == 0) {
//            return true;
//        }
    }

    public boolean recentlyAcquiredTargetToAttack() {
        if (target() == null) return false;

        int targetAcquiredAgo = lastTargetToAttackAcquiredAgo();

        return target().isAlive()
            && (
            (targetAcquiredAgo <= 70 && unit().woundPercent() <= 5 && !lastUnderAttackMoreThanAgo(30 * 6))
                || targetAcquiredAgo <= cooldownAbsolute() / 1.1
        );

//        return target().isAlive()
//                && (
//                    targetAcquiredAgo <= 4
////                    (targetAcquiredAgo <= 45 && unit().woundPercent() <= 5 && !lastUnderAttackMoreThanAgo(30 * 10))
////                    || targetAcquiredAgo <= cooldownAbsolute() / 1.1
//                );
    }

    public int lastTargetToAttackAcquiredAgo() {
        return A.ago(_lastTargetToAttackAcquired);
    }

    public boolean isAirUnitAntiAir() {
        return type().isAirUnitAntiAir();
    }

    private Mission squadMission() {
        if (squad() == null) {
            return null;
        }

        return squad.mission();
    }

    public boolean isNotAttackableByRangedDueToSpell() {
        return isUnderDarkSwarm();
    }

    public boolean isStimmed() {
        return u.isStimmed();
    }

    public boolean isStasised() {
        return u != null && u.isStasised();
    }

    public boolean isLockedDown() {
        return u != null && u.isLockedDown();
    }

    public boolean isDefenseMatrixed() {

        return u != null && u.isDefenseMatrixed();
    }

    public int stimTimer() {
        return u.getStimTimer();
    }

    public double combatEvalAbsolute() {
        // New Jfap solution
        return (new AtlantisJfap(this, false)).evaluateCombatSituation();

        // Old Heuristic implementation
//        return ACombatEvaluator.absoluteEvaluation(this);
    }

    /**
     * Relative local combat evaluation compared to enemy forces.
     * 0.8 means that our army is 20% weaker than enemy.
     * 1.0 means that our army is as strong.
     * 1.3 means that our army is 30% stronger than enemy.
     */
    public double eval() {
        if (!isOur()) return ownCombatEvalRelative();

        Squad squad = squad();
        if (squad != null && squad.isAlpha()) {
            AUnit leader = squadLeader();
            if (leader != null && leader.id() != this.id() && !isLeader()) {
                return leader.eval();
            }
        }

        return ownCombatEvalRelative();
    }

    public double ownCombatEvalRelative() {
        return (double) cache.get(
            "combatEvalRelative",
            7,
            () -> {
                // New Jfap solution
                return freshCombatEvalRelative();

                // Old manual implementation
//                return ACombatEvaluator.relativeAdvantage(this);
            }
        );
    }

    private double freshCombatEvalRelative() {
        double eval = (new AtlantisJfap(this, true)).evaluateCombatSituation();

        if (We.protoss()) return ProtossJfapTweaksConsiderChokesEtc.apply(this, eval);

        return eval;
    }

    public String evalDigit() {
        return A.digit(eval());
    }

    public boolean isMedic() {
        return type().isMedic();
    }

    public boolean isTerranInfantry() {
        return type().isTerranInfantry();
    }

    public boolean isTerranInfantryWithoutMedics() {
        return type().isTerranInfantryWithoutMedics();
    }

    public boolean isCarrier() {
        return type().isCarrier();
    }

    public boolean isDefiler() {
        return type().isDefiler();
    }

    public boolean isLurker() {
        return type().isLurker();
    }

    public boolean isLurkerEgg() {
        return type().isLurkerEgg();
    }

    public boolean hpLessThan(int min) {
        return hp() < min;
    }

    public boolean hpMoreThan(int max) {
        return hp() > max;
    }

    public boolean isSupplyDepot() {
        return type().is(AUnitType.Terran_Supply_Depot);
    }

    public boolean isSunken() {
        return type().isSunken();
    }

    // Approximate unit width (in tiles).
    public double size() {
        return (type().dimensionLeftPixels() + type().dimensionRightPixels() + 2) / 64.0;
    }

    public boolean isMarine() {
        return type().isMarine();
    }

    public boolean isGhost() {
        return type().isGhost();
    }

    public boolean isFirebat() {
        return type().isFirebat();
    }

    public boolean isRepairable() {
        return (boolean) cache.get(
            "isRepairable",
            -1,
            () -> type().isMechanical() || isABuilding()
        );
    }

    public int totalCost() {
        return type().totalCost();
    }

    public AUnit loadedInto() {
        return (AUnit) cache.getIfValid(
            "loadedInto",
            7,
            () -> {
                if (!isLoaded()) {
                    return null;
                }

                for (AUnit transport : Select.ourOfType(
                    AUnitType.Terran_Bunker,
                    AUnitType.Terran_Dropship,
                    AUnitType.Protoss_Shuttle,
                    AUnitType.Zerg_Overlord
                ).list()) {
                    if (transport.hasCargo()) {
                        if (transport.loadedUnits().contains(this)) {
                            return transport;
                        }
                    }
                }

//                ErrorLog.printMaxOncePerMinute("Cant find loaded into for " + this);
                return null;
//                throw new RuntimeException("Cant find loaded into");
            }
        );
    }

    public boolean isCombatBuilding() {
        return type().isCombatBuilding();
    }

    public boolean isMutalisk() {
        return type().isMutalisk();
    }

    public boolean isZealot() {
        return type().isZealot();
    }

    public boolean isZergling() {
        return type().isZergling();
    }

    public boolean isMissileTurret() {
        return type().isMissileTurret();
    }

    public boolean isScv() {
        return type().isScv();
    }

    public boolean isScienceVessel() {
        return type().isScienceVessel();
    }

    public boolean isCombatUnit() {
        return type().isCombatUnit();
    }

    public Selection meleeEnemiesNear() {
        return enemiesNear().melee();
    }

    public Selection enemiesNear() {
        return ((Selection) cache.get(
            "enemiesNear",
            7,
            () -> {
                if (unit().isOur()) {
//                    System.out.println("Considered #FRIEND unit: " + this
//                        + " / " + AliveEnemies.get().size()
//                        + " / " + EnemyUnits.discovered().size()
//                    );
//                    if (AliveEnemies.get().size() != EnemyUnits.discovered().size()) {
//                        AliveEnemies.get().print("Alive enemies:");
//                        EnemyUnits.discovered().print("Discovered enemies:");
//                    }

//                    return EnemyUnits.discovered()
                    return AliveEnemies.get()
                        .realUnitsAndBuildings()
                        .inRadius(15, this)
                        .exclude(this);
                }
                else if (unit().isEnemy() || (unit().type().isAddon() && unit().isNeutral())) {
//                    System.out.println("Considered enemy unit: " + this
//                        + " / " + Select.our().size()
//                        + " / " + Select.ourRealUnits().size()
//                    );
                    return Select.ourRealUnits()
                        .inRadius(15, this)
                        .exclude(this);
                }
                else {
//                    System.out.println("$$$ Weird case: " + this);
                    if (Army.strength() <= 700 && !unit().type().isGeyser()) {
                        System.err.println("enemiesNear invoked for neutral?");
                        System.err.println("ThisContext = " + this);
                        System.err.println("alive=" + unit().isAlive() + " / hp=" + unit().hp());
                        System.err.println("enemy=" + unit().isEnemy() + " / our=" + unit().isOur());
                        A.printStackTrace("This is weird, should not be here");
                    }
                    return Select.from(new Units());
                }
            }
        ));
    }

    public Selection enemiesNear(double radius) {
        return enemiesNear().inRadius(radius, this);
    }

    public int enemiesNearInRadius(double maxDist) {
        return enemiesNear().inRadius(maxDist, this).count();
//        return ((Selection) cache.get(
//            "enemiesNearInRadius:" + maxDist,
//            5,
//            () -> enemiesNear().inRadius(maxDist, this)
//        ));
    }

    public int meleeEnemiesNearCount() {
        return cacheInt.get(
            "meleeEnemiesNearCount",
            3,
            () -> enemiesNear().melee().inRadius(2.7, this).size()
        );
    }

    public int friendsNearCount() {
        return cacheInt.get(
            "friendsNearCount",
            3,
            () -> friendsNear().size()
        );
    }

    public Selection friendsNear() {
        return ((Selection) cache.get(
            "friendsNear",
            7,
            () -> {
                if (unit().isOur()) {
                    return Select.ourRealUnits()
                        .inRadius(15, this)
                        .exclude(this);
                }
                else if (unit().isEnemy()) {
                    return EnemyUnits.discovered()
                        .inRadius(15, this)
                        .exclude(this);
                }
                else {
                    return Select.from(new Units());
                }
            }
        ));
    }

//    public Selection friendsInRadius(double radius) {
//        return friendsNear().inRadius(radius, this);
//    }
//
//    public int friendsInRadiusCount(double radius) {
//        return friendsNear().inRadius(radius, this).count();
//    }

    public Selection allUnitsNear() {
        return ((Selection) cache.get(
            "allUnitsNear",
            5,
            () -> Select.all()
                .inRadius(15, this)
                .exclude(this)
        ));
    }

    public boolean hasMedicInRange() {
        return cacheBoolean.get(
            "hasMedicInRange",
            2,
            () -> Select.ourOfType(AUnitType.Terran_Medic).inRadius(2.1, this).notEmpty()
        );
    }

    public double nearestMedicDist() {
        return (double) cache.get(
            "nearestMedicDist",
            3,
            () -> {
                AUnit medic = unit().friendsNear().medics().nearestTo(this);
                if (medic != null) {
                    return medic.distTo(this);
                }
                return 999.9;
            }
        );
    }

    public boolean isProtoss() {
        return type().isProtoss();
    }

    public boolean isTerran() {
        return type().isTerran();
    }

    public boolean isZerg() {
        return type().isZerg();
    }

    public boolean isPowered() {
        return u.isPowered();
    }

    private boolean isSpell() {
        return type().isSpell();
    }

    public boolean hasMedicInHealRange() {
        return cacheBoolean.get(
            "hasMedicInHealRange",
            4,
            () -> Select.ourOfType(AUnitType.Terran_Medic)
                .inRadius(2, this)
                .havingEnergy(12)
                .atLeast(1)
        );
    }

    public boolean isOverlord() {
        return cacheBoolean.get(
            "isOverlord",
            -1,
            () -> is(AUnitType.Zerg_Overlord)
        );
    }

    public boolean isDragoon() {
        return cacheBoolean.get(
            "isDragoon",
            -1,
            () -> is(AUnitType.Protoss_Dragoon)
        );
    }

    public boolean isGoon() {
        return isDragoon();
    }

    public boolean isGoliath() {
        return cacheBoolean.get(
            "isGoliath",
            -1,
            () -> is(AUnitType.Terran_Goliath)
        );
    }

    public boolean isHydralisk() {
        return cacheBoolean.get(
            "isHydralisk",
            -1,
            () -> is(AUnitType.Zerg_Hydralisk)
        );
    }

    public boolean isCommandCenter() {
        return cacheBoolean.get(
            "isCommandCenter",
            -1,
            () -> is(AUnitType.Terran_Command_Center)
        );
    }

    public boolean isDT() {
        return cacheBoolean.get(
            "isDT",
            -1,
            () -> is(AUnitType.Protoss_Dark_Templar)
        );
    }

    public boolean isDarkTemplar() {
        return cacheBoolean.get(
            "isDarkTemplar",
            -1,
            () -> is(AUnitType.Protoss_Dark_Templar)
        );
    }

    public boolean isCorsair() {
        return cacheBoolean.get(
            "isCorsair",
            -1,
            () -> is(AUnitType.Protoss_Corsair)
        );
    }

    public boolean isObserver() {
        return cacheBoolean.get(
            "isObserver",
            -1,
            () -> is(AUnitType.Protoss_Observer)
        );
    }

    public boolean isBeingHealed() {
        return cacheBoolean.get(
            "isBeingHealed",
            6,
            () -> (
                friendsInRadius(2).ofType(AUnitType.Terran_Medic).havingTargeted(this).notEmpty()
                    || TerranMedic.isAnyCloseMedicAssignedTo(this)
            )
        );
    }

    public int meleeEnemiesNearCount(double maxDistToEnemy) {
        return cacheInt.get(
            "meleeEnemiesNear:" + maxDistToEnemy,
            2,
            () -> enemiesNear().melee().inRadius(maxDistToEnemy, this).size()
        );
    }

    public boolean isFoggedUnitWithUnknownPosition() {
        return this instanceof AbstractFoggedUnit && !hasPosition();
    }

    public boolean isFoggedUnitWithKnownPosition() {
        return this instanceof FoggedUnit && hasPosition();
    }

    public boolean isHealthy() {
        return !isWounded();
    }

    public boolean shieldDamageAtMost(int maxDamage) {
        if (!We.protoss()) return false;

        return shields() + maxDamage >= maxShields();
    }

    public boolean shieldDamageAtLeast(int minDamage) {
        if (!We.protoss()) return false;

        return shields() + minDamage <= maxShields();
    }

    public boolean targetPositionAtLeastAway(double minTiles) {
        return targetPosition() != null && targetPosition().distToMoreThan(this, minTiles);
    }

//    public void paintInfo(String text) {
//        APainter.paintTextCentered(this, text, Color.White);
//    }
//
//    public void paintInfo(String text, Color color, double dty) {
//        APainter.paintTextCentered(this, text, color, 0, dty / 32.0);
//    }

    public boolean addLog(String message) {
        if (!log.lastMessageWas(message)) {
            log.addMessage(message, this);
        }

        return true;
    }

    public void addFileLog(String message) {
        LogUnitsToFiles.saveUnitLogToFile(message, this);
    }

    public Log log() {
        return log;
    }

    /**
     * History of changes of Managers used by this unit.
     */
    public Log managerLogs() {
        return managerLogs;
    }

    public Log commandHistory() {
        return commandHistory;
    }

    public int friendlyZealotsNearCount(double maxDist) {
        return friendsNear().ofType(AUnitType.Protoss_Zealot).inRadius(maxDist, this).count();
    }

    public int enemyZealotsNearCount(double maxDist) {
        return enemiesNear().ofType(AUnitType.Protoss_Zealot).inRadius(maxDist, this).count();
    }

    public boolean isNearEnemyBuilding() {
        AUnit nearestEnemyBuilding = enemiesNear().buildings().nearestTo(this);

        if (nearestEnemyBuilding != null) {
            return nearestEnemyBuilding.distToLessThan(this, 9);
        }

        return false;
    }

    public boolean isMissionSparta() {
        if (mission() == null) return false;
        return mission().equals(Missions.SPARTA);
    }

    public boolean isMissionDefend() {
        if (mission() == null) return false;
        return mission().equals(Missions.DEFEND);
    }

    public boolean isMissionDefendOrSparta() {
        if (mission() == null) return false;
        return isMissionDefend() || isMissionSparta();
    }

    public boolean isMissionAttack() {
        if (mission() == null) return false;
        return mission().isMissionAttack();
    }

    public boolean isMissionAttackOrGlobalAttack() {
        return isMissionAttack() || Missions.isGlobalMissionAttack();
    }

    public boolean isMissionContain() {
        if (mission() == null) return false;
        return mission().isMissionContain();
    }

    public boolean isReaver() {
        return type().isReaver();
    }

    public boolean isShuttle() {
        return type().isShuttle();
    }

    public boolean isHighTemplar() {
        return type().isHighTemplar();
    }

    public boolean recentlyMoved() {
        return action().isMoving() && lastActionLessThanAgo(40);
    }

    public boolean recentlyMoved(int framesAgo) {
//        return action().isMoving() && lastActionLessThanAgo(framesAgo);
        return lastPositioningActionLessThanAgo(framesAgo);
    }

    public boolean idIsEven() {
        return id() % 2 == 0;
    }

    public boolean idIsOdd() {
        return id() % 2 > 0;
    }

    public AUnit nearestGroundCombatEnemy() {
        return (AUnit) cache.getIfValid(
            "nearestGroundCombatEnemy",
            2,
            () -> enemiesNear().combatUnits().havingPosition().groundUnits().nearestTo(this)
        );
    }

    public AUnit nearestEnemy() {
        return (AUnit) cache.getIfValid(
            "nearestEnemy",
            2,
            () -> enemiesNear().havingPosition().canAttack(this, 5).nearestTo(this)
        );
    }

    public double nearestEnemyDist() {
        AUnit nearestEnemy = nearestEnemy();

        if (nearestEnemy != null) {
            return distTo(nearestEnemy);
        }

        return 999;
    }

    public double nearestMeleeEnemyDist() {
        AUnit nearestEnemy = meleeEnemiesNear().nearestTo(this);

        if (nearestEnemy != null) {
            return distTo(nearestEnemy);
        }

        return 999;
    }

    public double nearestOurTankDist() {
        AUnit tank = friendsNear().tanks().nearestTo(this);

        if (tank != null) {
            return distTo(tank);
        }

        return 999;
    }

    public AUnit nearestOurTank() {
        return friendsNear().tanks().nearestTo(this);
    }

    public Selection friendsInRadius(double radius) {
        return friendsNear().inRadius(radius, this);
    }

    public int friendsInRadiusCount(double radius) {
        return friendsNear().inRadius(radius, this).count();
    }

    public double distToLeader() {
        if (squad == null || squad.leader() == null) {
            return 0;
        }

        return (double) cache.get(
            "distToLeader",
            5,
            () -> squad.leader().distTo(this)
        );
    }

    public double distToDragoon() {
        return (double) cache.get(
            "distToDragoon",
            5,
            () -> friendsNear().dragoons().distToNearest(this)
        );
    }

    public boolean hasSquad() {
        return squad != null;
    }

    public double squadRadius() {
        if (this.squad == null) {
            return 988;
        }
        return squad.radius();
    }

    public boolean outsideSquadRadius() {
        return (boolean) cache.get(
            "outsideSquadRadius",
            3,
            () -> distToLeader() > squadRadius()
        );
    }

    public boolean isProtector() {
        return RepairAssignments.isProtector(this);
    }

    public boolean kitingUnit() {
        return isDragoon() || isVulture();
    }

    public double distToFocusPoint() {
        Mission mission = mission();
        if (mission == null) {
            return 0;
        }

        AFocusPoint focusPoint = mission.focusPoint();
        if (focusPoint == null) {
            return 0;
        }

        if (focusPoint.isAroundChoke()) {
            return (focusPoint.choke().center()).distTo(this);
        }

        return focusPoint.distTo(this);
    }

    public double distToBase() {
        AUnit base = Select.ourBasesWithUnfinished().exclude(this).nearestTo(this);
        if (base == null) return 999;

        return base.distTo(this);
    }

    public double distToBuilding() {
        AUnit building = Select.ourBuildingsWithUnfinished().nearestTo(this);
        if (building == null) return 999;

        return building.distTo(this);
    }

    public double distToCannon() {
        AUnit cannon = Select.ourOfType(AUnitType.Protoss_Photon_Cannon).nearestTo(this);
        if (cannon == null) return 999;

        return cannon.distTo(this);
    }

    public double distToBunker() {
        AUnit bunker = Select.ourOfType(AUnitType.Terran_Bunker).nearestTo(this);
        if (bunker == null) return 999;

        return bunker.distTo(this);
    }

    public double ourNearestBuildingDist() {
        AUnit building = Select.ourBuildings().nearestTo(this);
        if (building == null) return 999;

        return building.distTo(this);
    }

    public double ourNearestBuildingWithUnfinishedDist() {
        AUnit building = Select.ourBuildingsWithUnfinished().nearestTo(this);
        if (building == null) return 999;

        return building.distTo(this);
    }

    public boolean lastPositioningActionMoreThanAgo(int minFramesAgo) {
        return lastActionMoreThanAgo(minFramesAgo, Actions.MOVE_FORMATION);
//            && lastActionMoreThanAgo(minFramesAgo, Actions.MOVE_ENGAGE);
    }

    public boolean lastPositioningActionLessThanAgo(int minFramesAgo) {
        return lastActionLessThanAgo(minFramesAgo, Actions.MOVE_FORMATION);
//            && lastActionLessThanAgo(minFramesAgo, Actions.MOVE_ENGAGE);
    }

//    public boolean shouldRetreat() {
//        return ShouldRetreat.shouldRetreat(this);
//    }

    public boolean isMechanical() {
        return type() != null && type().isMechanical();
    }

    public void gatherBestResources() {
        AMineralGathering.gatherResources(this);
    }

    public boolean isValid() {
        return isAlive() && hasPosition() && effVisible();
    }

    public boolean notImmobilized() {
        return !isStasised() && !isLockedDown();
    }

    public boolean debug() {
        if (action().equals(Actions.INIT)) {
            System.out.println("UNIT " + this + " " + this.idWithHash());
//            A.printStackTrace("Ahhhhhhhh");
            return true;
        }

        return false;
    }

    public AUnit repairer() {
        if (!isRepairable()) {
            return null;
        }

        return RepairAssignments.getClosestRepairerAssignedTo(this);
    }

    public boolean isBeingRepaired() {
        for (AUnit worker : Select.ourWorkers().inRadius(1.1, this).list()) {
            if (worker.isRepairing() && worker.isTarget(this)) {
                return true;
            }
        }

        return false;
    }

    public boolean isTarget(AUnit otherUnit) {
        return otherUnit.equals(target());
    }

    public boolean isTarget(AUnitType type) {
        AUnit target = target();
        return target != null && target.is(type);
    }

    public boolean isTargetRanged() {
        AUnit target = target();
        return target != null && target.isRanged();
    }

    public boolean isTargetMelee() {
        AUnit target = target();
        return target != null && target.isMelee();
    }

    public boolean isFlying() {
        return isAir() || isLifted();
    }

    public boolean looksIdle() {
        return (isIdle() && !isMoving())
            || (!isMoving() && !isAccelerating() && noCooldown());
    }

    public AUnit squadLeader() {
        return squad != null ? squad.leader() : null;
    }

    public void setManagerUsed(Manager managerUsed) {
//        if (A.now() >= 50 && isDragoon()) A.errPrintln(typeWithUnitId() + ": used " + managerUsed);

        setManagerUsed(managerUsed, null);
    }

    public void setManagerUsed(Manager managerUsed, String message) {
        if (!manager.equals(managerUsed)) {
            managerLogs.addMessage(managerUsed.toString(), this);

            this.manager = managerUsed;

            addLog(managerUsed.toString());
        }
        else {
            managerLogs.replaceLastWith(managerUsed.toString(), this);
        }

        this.tooltipForManager = message;
    }

    public boolean isActiveManager(Manager manager) {
        return manager.getClass().equals(this.manager.getClass());
    }

    public boolean isActiveManager(Class aClass) {
        return manager.getClass().equals(aClass);
    }

    public boolean isActiveManager(Class... classes) {
        for (Class aClass : classes) {
            if (isActiveManager(aClass)) {
                return true;
            }
        }
        return false;
    }

    public boolean canLift() {
        return u().canLift();
    }

    public boolean everyOneInNUnits(int n) {
        return id() % n == 1;
    }

    public boolean isCrucialUnit() {
        return isTank()

            || isReaver()
            || isArchon()
            || isHighTemplar()
            || isDT()
            || isCarrier()

            || isDefiler()
            || isUltralisk()
            || isLurker();
    }

//    public void setRunningFrom(HasPosition enemy) {
//        this.runningManager.setRunFrom(enemy);
//    }

    public HasPosition runningFromPosition() {
        return this.runningManager.runningFromPosition();
    }

    public AUnit runningFromUnit() {
        return this.runningManager.runningFromUnit();
    }

//    public HasPosition runningFrom() {
//        return this.runningManager.runFrom();
//    }

    public boolean hasSiegedRecently() {
        return lastActionLessThanAgo(30 * 6 + unit().id() % 3, Actions.SIEGE);
    }

    public boolean hasUnsiegedRecently() {
        return lastActionLessThanAgo(30 * 6 + unit().id() % 3, Actions.UNSIEGE);
    }

    public boolean hasSiegedOrUnsiegedRecently() {
        return hasSiegedRecently() || hasUnsiegedRecently();
    }

    public int lastSiegedOrUnsiegedAgo() {
        return Math.min(
            lastActionAgo(Actions.SIEGE),
            lastActionAgo(Actions.UNSIEGE)
        );
    }

    public APosition lastPosition() {
        return APosition.createFromPixels(_lastX, _lastY);
    }

    public boolean hasChangedPositionRecently() {
        return !position().equals(lastPosition());
    }

    public boolean loadedIntoBunker() {
        if (!isLoaded()) return false;
        AUnit loadedInto = loadedInto();

        return loadedInto != null && loadedInto.isBunker();
    }

    public boolean didntShootRecently(int minSeconds) {
        return lastAttackFrameMoreThanAgo(30 * minSeconds);
    }

    public boolean shotSecondsAgo(double minSeconds) {
        return lastAttackFrameLessThanAgo((int) (30 * minSeconds));
    }

    public boolean ranRecently(int minSeconds) {
        return lastStartedRunningLessThanAgo(30 * minSeconds);
    }

    public boolean moveToLeader(Action action, String tooltip) {
        AUnit leader = squadLeader();
        if (leader == null) return false;

        if (distTo(leader) <= 1.5) return false;

        return move(leader, action, tooltip);
    }

    public boolean moveToSafety(Action action) {
        return moveToSafety(action, null);
    }

    public boolean moveToSafety(Action action, String tooltip) {
        HasPosition safety = safetyPosition();
        if (safety == null) return false;

        double minDist = 1.3;
        double distTo = distTo(safety);

        if (distTo >= 15 && isCombatUnit()) {
            AUnit enemy = nearestEnemy();
            if (enemy == null) return false;

            if (runOrMoveAway(enemy, 6)) return true;
        }

        if (distTo >= minDist) {
            move(safety, action, tooltip);
            return true;
        }

        return false;
    }

    public boolean moveToNearestBase(Action action, HasPosition nearestToOrNull) {
        if (nearestToOrNull == null) nearestToOrNull = Select.mainOrAnyBuilding();

        AUnit base = Select.ourBasesWithUnfinished().nearestTo(nearestToOrNull);
        if (base == null) return false;

        return move(base, action);
    }

    public HasPosition safetyPosition() {
        AUnitType groundCb = AtlantisRaceConfig.DEFENSIVE_BUILDING_ANTI_LAND;
        AUnit cb = Select.ourOfTypeWithUnfinished(groundCb).nearestTo(this);
        if (cb != null) return cb;

        if (Count.ourCombatUnits() >= 8 && Count.bases() >= 2) {
            return Bases.natural().translateTilesTowards(3, Chokes.natural());
        }

        AUnit base = Select.naturalOrMain();
        if (base == null) return null;

        APosition position = base.translateByTiles(1, -3);
        if (position != null && position.isWalkable()) return position;

        return base;
    }

    public boolean hasAirWeapon() {
        return airWeaponRange() > 0;
    }

    public boolean hasGroundWeapon() {
        return groundWeaponRange() > 0;
    }

    public Selection enemiesThatCanAttackMe(double safetyMargin) {
        return enemiesNear().canAttack(this, safetyMargin);
    }

    public Selection enemiesICanAttack(double safetyMargin) {
        return enemiesNear().canBeAttackedBy(this, safetyMargin);
    }

    public boolean isPurelyAntiAir() {
        return type().is(
            AUnitType.Protoss_Corsair,
            AUnitType.Terran_Valkyrie,
            AUnitType.Terran_Goliath,
            AUnitType.Zerg_Scourge
        );
    }

    public boolean hasCloseRepairer() {
        AUnit repairer = repairer();

        return repairer != null
            && this.distTo(repairer) <= (isAir() ? 6 : 2)
            && (!repairer.isRepairing() || repairer.isTarget(this));
    }

    public boolean targetIsOfType(AUnitType... type) {
        return target() != null && target().is(type);
    }

    public void setProductionOrder(ProductionOrder productionOrder) {
        this.productionOrder = productionOrder;
    }

    public ProductionOrder productionOrder() {
        return this.productionOrder;
    }

    public APosition specialPosition() {
        return specialPosition;
    }

    public void setSpecialPosition(APosition specialPosition) {
        this.specialPosition = specialPosition;
    }

    public double distToTarget() {
        if (target() == null) return 999;

        return distTo(target());
    }

    public boolean distToTargetLessThan(double dist) {
        return target() != null && distTo(target()) < dist;
    }

    public boolean distToTargetMoreThan(double dist) {
        return target() != null && distTo(target()) > dist;
    }

    public boolean isSpecialMission() {
        return lastActionLessThanAgo(50, Actions.SPECIAL);
    }

    public boolean isSpecialAction() {
        return lastActionLessThanAgo(50, Actions.SPECIAL);
    }

    public boolean isWithinChoke() {
        AChoke closestChoke = nearestChoke();

        if (closestChoke == null) return false;

        return cacheBoolean.get(
            "isWithinChoke",
            1,
            () -> IsUnitWithinChoke.check(closestChoke, this)
        );
    }

    public AChoke nearestChoke() {
        return (AChoke) cache.get(
            "nearestChoke",
            77,
            () -> Chokes.nearestChoke(this, "ALL")
        );
    }

    public void setTargetUnitToAttack(AUnit target) {
        this._targetUnitToAttack = target;
    }

    public AUnit targetUnitToAttack() {
        return this._targetUnitToAttack;
    }

    public int attackWaitFrames() {
        return UnitAttackWaitFrames.stopFrames(this.type());
    }

    public boolean hasTargetted(AUnit defender) {
        return target() != null && target().equals(defender);
    }

    public double distToNearestChoke() {
        return (double) cache.get(
            "distToNearestChoke",
            77,
            () -> {
                AChoke nearestChoke = nearestChoke();
                if (nearestChoke == null) return 999.0;

                return distTo(nearestChoke);
            }
        );
    }

    public double distToNearestChokeCenter() {
        return (double) cache.get(
            "distToNearestChokeCenter",
            77,
            () -> {
                AChoke nearestChoke = nearestChoke();
                if (nearestChoke == null) return 999.0;

                return distTo(nearestChoke.center());
            }
        );
    }

    public double distToSquadCenter() {
        return squad != null ? distTo(squad.center()) : -1;
    }

    public String lastCommandName() {
        if (getLastCommandRaw() == null) return "NO_COMMAND";

        return getLastCommandRaw().getType().name();
    }

    public AFocusPoint focusPoint() {
        if (mission() == null) return null;
        return mission().focusPoint();
    }

    public boolean isLeader() {
        return squad != null && this.equals(squad.leader());
    }

    public boolean isMainBase() {
        return this.equals(Select.main());
    }

    public boolean canBeLonelyUnit() {
        return type().is(AUnitType.Terran_Vulture, AUnitType.Protoss_Dark_Templar);
    }

    public boolean squadIsRetreating() {
        return squad != null && squad.isRetreating();
    }

    private double _smallScaleEval = 0;

    public void setLastSmallScaleEval(double dEval) {
        this._smallScaleEval = dEval;
    }

    public double smallScaleEval() {
        return _smallScaleEval;
    }

    public UnderAttack underAttack() {
        return underAttack;
    }

    public boolean isSafeFromMelee() {
        if (isDragoon()) return meleeEnemiesNearCount(3.0) == 0;

        double base = baseIsSafeFromMelee();
        double distToMelee = Math.min(3.1, base + (woundPercent() / 100.0));
//        double distToMelee = 3.2;

        return meleeEnemiesNearCount(distToMelee) == 0;
    }

    private double baseIsSafeFromMelee() {
//        if (isDragoon()) return 2.4;

        return hp() >= 60 ? 1.6 : 1.8;
    }

    public boolean moreMeleeEnemiesThanOurUnits() {
        return meleeEnemiesNearCount(1.2)
            > friendsNear().melee().countInRadius(1.4, this);
    }

    public boolean isDeadMan() {
        return DeadMan.isDeadMan(this);
    }

    public boolean hasRangedEnemies(double safetyMargin) {
        return enemiesNear().ranged().inShootRangeOf(safetyMargin, this).notEmpty();
    }

    public int rangedEnemiesCount(double safetyMargin) {
        return enemiesNear().ranged().inShootRangeOf(safetyMargin, this).count();
    }

    public double shotSecondsAgo() {
        return lastAttackFrameAgo() / 30.0;
    }

    public boolean shieldHealthy() {
        return shields() >= maxShields();
    }

    public double shieldPercent() {
        return (double) (100 * shields()) / maxShields();
    }

    public double shieldWoundPercent() {
        return (double) (100 * shieldWound()) / maxShields();
    }

    public double shieldWound() {
        return 100 - ((double) (100 * shields()) / maxShields());
    }

    public boolean shieldWounded() {
        return shields() < maxShields();
    }

    public boolean almostDead() {
        if (isDragoon()) return hp() <= 25;

        return hp() <= 20;
    }

    public boolean isAlphaSquad() {
        return squad != null && squad.isAlpha();
    }

    public boolean isDancing() {
        return action().equals(Actions.MOVE_DANCE_AWAY) || action().equals(Actions.MOVE_DANCE_TO);
    }

    public boolean isHt() {
        return type().isHighTemplar();
    }

    public boolean isOvercrowded() {
        return isGroundUnit()
            && (
            friendsNear().combatUnits().inRadius(0.6, this).atLeast(4)
                || friendsNear().combatUnits().inRadius(1.5, this).atLeast(6)
        );
    }

    public boolean leaderIsRetreating() {
        return squad != null && squad.leader() != null && squad.leader().isRetreating();
    }

    public AUnit nearestBase() {
        return Select.ourBases().nearestTo(this);
    }

    public boolean isTraining(AUnitType producingThisUnit) {
        List<AUnitType> queue = trainingQueue();
        if (queue.isEmpty()) return false;

        return producingThisUnit.equals(queue.get(0));
    }

    public int hitCount() {
        return _hitCount;
    }

    public int totalHitCount() {
        return _totalHitCount;
    }

    public void increaseHitCount() {
        _hitCount++;
        _totalHitCount++;
    }

    public boolean isNotLarge() {
        return size() < 0.9;
    }

    public boolean nearestEnemyIs(AUnitType type) {
        AUnit nearestEnemy = nearestEnemy();
        return nearestEnemy != null && nearestEnemy.is(type);
    }

    public int lastCommandIssuedAgo() {
        return A.ago(_lastCommandIssued);
    }

    public void lastCommandIssuedNow(UnitCommandType command) {
        _lastCommandIssued = A.now;
        commandHistory.addMessage(
            command.name() + "/a:" + action().name() + "/" + tooltip, this
        );
    }

    public AttackState attackState() {
        if (!isOur()) ErrorLog.printMaxOncePerMinute("### attackState invoked for enemy unit");

        return attackState;
    }

    public AttackState setAttackState(AttackState attackState) {
        this.attackState = attackState;

        return this.attackState;
    }

    public void setLastBullet(ABullet bullet) {
        this._lastBullet = bullet;
    }

    public ABullet lastBullet() {
        return _lastBullet;
    }

    /**
     * Age of the last bullet fired by this unit in frames.
     */
    public int lastBulletAge() {
        if (_lastBullet == null) return -987;

        return A.ago(_lastBullet.createdAt());
    }

    public boolean isActionAttackUnit() {
        return action().equals(Actions.ATTACK_UNIT);
    }

    public boolean isActionDance() {
        return action().name().startsWith("DANCE");
    }

    public double woundOrder() {
        return (isHealthy() ? 500 : 0) - woundPercent();
    }

    public double ourToEnemyRangedUnitRatio() {
        return (double) friendsNear().ranged().countInRadius(7, this)
            / enemiesNear().ranged().countInRadius(7, this);
    }

    public boolean runOrMoveAway(AUnit from, double dist) {
        if (runningManager().runFrom(from, 3, Actions.MOVE_AVOID, false)) {
            return true;
        }

        if (moveAwayFrom(from, 3, Actions.MOVE_AVOID)) return true;

        return false;
    }

    public boolean lastTargetWasTank() {
        AUnitType lastTarget = lastTargetType();
        if (lastTarget == null) return false;

        return lastTarget.isTank();
    }

}
