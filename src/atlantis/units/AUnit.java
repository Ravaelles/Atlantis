package atlantis.units;

import atlantis.AGame;
import atlantis.combat.retreating.ARunningManager;
import atlantis.combat.missions.Mission;
import atlantis.combat.squad.Squad;
import atlantis.production.constructing.AConstructionManager;
import atlantis.production.constructing.AConstructionRequests;
import atlantis.production.constructing.ConstructionOrder;
import atlantis.debug.APainter;
import atlantis.enemy.UnitsArchive;
import atlantis.interrupt.DontInterruptStartedAttacks;
import atlantis.position.APosition;
import atlantis.position.HasPosition;
import atlantis.repair.ARepairAssignments;
import atlantis.scout.AScoutManager;
import atlantis.tech.SpellCoordinator;
import atlantis.units.actions.UnitAction;
import atlantis.units.actions.UnitActions;
import atlantis.position.PositionUtil;
import atlantis.units.select.Select;
import atlantis.util.*;
import atlantis.util.Vector;
import atlantis.wrappers.ATech;
import bwapi.*;

import java.util.*;

/**
 * Wrapper for bwapi Unit class that makes units much easier to use.<br /><br />
 * Atlantis uses wrappers for bwapi native classes which can't be extended.<br /><br />
 * <b>AUnit</b> class contains number of helper methods, but if you think some methods are missing you can
 * add them here.
 *
 * Also you can always reference original Unit class via u() method, but please avoid it as code will be very
 * hard to migrate to another bridge. I've already used 3 of them in my career so far.
 */
public class AUnit implements Comparable<AUnit>, HasPosition, AUnitOrders {

    public static final int UPDATE_UNIT_POSITION_EVERY_FRAMES = 30;

    // Mapping of native unit IDs to AUnit objects
    private static final Map<Integer, AUnit> instances = new HashMap<>();
    
    // Cached distances to other units - reduces time on calculating unit1.distanceTo(unit2)
//    public static final ACachedValue<Double> unitDistancesCached = new ACachedValue<>();

    private final Unit u;
    private Cache<Object> cache = new Cache<>();
    private Cache<Integer> cacheInt = new Cache<>();
//    private AUnitType _lastCachedType;
    private UnitAction unitAction = UnitActions.INIT;
//    private final AUnit _cachedNearestMeleeEnemy = null;
    public CappedList<Integer> _lastHitPoints = new CappedList<>(20);
    public int _lastAttackOrder;
    public int _lastAttackFrame;
    public int _lastCooldown;
    public int _lastFrameOfStartingAttack;
    public int _lastRetreat;
    public int _lastStartedRunning;
    public int _lastStoppedRunning;
    public int _lastStartedAttack;
    public AUnit _lastTargetToAttack;
    public int _lastTargetToAttackAcquired;
    public TechType _lastTech;
    public APosition _lastTechPosition;
    public AUnit _lastTechUnit;
    public int _lastUnderAttack;
    public int _lastX;
    public int _lastY;

    // =========================================================

    /**
     * Atlantis uses wrapper for BWAPI classes which aren't extended.<br />
     * <b>AUnit</b> class contains numerous helper methods, but if you think some methods are missing you can
     * create missing method here and you can reference original Unit class via u() method.
     */
    public static AUnit createFrom(Unit u) {
        if (u == null) {
            throw new RuntimeException("AUnit constructor: unit is null");
        }

        AUnit unit;
        if (instances.containsKey(u.getID())) {
            unit = instances.get(u.getID());
            if (unit != null && unit.isAlive()) {
                return unit;
            }
            instances.remove(u.getID());
        }
        
        unit = new AUnit(u);
        instances.put(u.getID(), unit);
        return unit;
    }

    public static void forgetUnitEntirely(Unit u) {
        instances.remove(u.getID());
    }

    protected AUnit(Unit u) {
        if (u == null) {
            throw new RuntimeException("AUnit constructor: unit is null");
        }

        this.u = u;
//        this.innerID = firstFreeID++;
        
        // Cached type helpers
        refreshType();

        // Repair & Heal
        this._repairableMechanically = isBuilding() || isVehicle();
        this._healable = isInfantry() || isWorker();

        // Military building
        this._isMilitaryBuildingAntiGround = isType(
                AUnitType.Terran_Bunker, AUnitType.Protoss_Photon_Cannon, AUnitType.Zerg_Sunken_Colony
        );
        this._isMilitaryBuildingAntiAir = isType(
                AUnitType.Terran_Bunker, AUnitType.Terran_Missile_Turret,
                AUnitType.Protoss_Photon_Cannon, AUnitType.Zerg_Spore_Colony
        );
    }

    // =========================================================

    /**
     * Returns unit type from bridge OR if type is Unknown (behind fog of war) it will return last cached type.
     */
    public AUnitType type() {
        return (AUnitType) cache.get(
                "type",
                isOur() ? -1 : 3,
                () -> {
                    AUnitType type = AUnitType.createFrom(u.getType());
                    if (type.isUnknown()) {
                        if (this.isOur()) {
                            System.err.println("Our unit (" + u.getType() + ") returned Unknown type");
                        }
                        // This is expected - invisible units return Unknown type
                        else {
//                            System.err.println("Enemy unit type is Unknown...");
//                            System.err.println(u.getType());
//                            System.err.println(u.getHitPoints());
                        }
                    }
//                    System.out.println(this.u + " // " + type.ut().name());
                    return type;
                }
        );
    }
    
    public void refreshType() {
        cache.forgetAll();
        _isWorker = isType(AUnitType.Terran_SCV, AUnitType.Protoss_Probe, AUnitType.Zerg_Drone);
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AUnit aUnit = (AUnit) o;
        return id() == aUnit.id();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id());
    }

    // =========================================================
    // =========================================================
    // =========================================================

    private Squad squad = null;
    private final ARunningManager runningManager = new ARunningManager(this);
    private int lastUnitOrder = 0;

    private boolean _repairableMechanically = false;
    private boolean _healable = false;
    private boolean _isMilitaryBuildingAntiGround = false;
    private boolean _isMilitaryBuildingAntiAir = false;
    private boolean _isWorker;
    private double _lastCombatEval;
    private int _lastTimeCombatEval = 0;

    // =========================================================
    // Important methods
    
    /**
     * Unit will move by given distance (in build tiles) from given position.
     */
    public boolean moveAwayFrom(HasPosition position, double moveDistance, String tooltip) {
        if (position == null || moveDistance < 0.01) {
            return false;
        }
        
        int dx = position.x() - x();
        int dy = position.y() - y();
        double vectorLength = Math.sqrt(dx * dx + dy * dy);
        double modifier = (moveDistance * 32) / vectorLength;
        dx = (int) (dx * modifier);
        dy = (int) (dy * modifier);

        APosition newPosition = new APosition(x() - dx, y() - dy).makeValid();

//        if (AtlantisRunManager.isPossibleAndReasonablePosition(
//                this, newPosition, -1, 9999, true
//        ) && move(newPosition, UnitActions.MOVE)) {
        if (
                runningManager().isPossibleAndReasonablePosition(this.position(), newPosition, false)
                && move(newPosition, UnitActions.MOVE, "Move away")
        ) {
            this.setTooltip(tooltip);
            return true;
        }

//        else {
//            System.out.println("CANT = " + position.distanceTo(newPosition));
            APainter.paintLine(position.position(), newPosition.position(), Color.Teal);
            this.setTooltip("Cant move away");
            return move(newPosition, UnitActions.MOVE, "Force move");
//            return false;
//        }
    }
    
    // =========================================================
    @Override
    public String toString() {
//        Position position = this.getPosition();
//        String toString = type().shortName();
//        toString += " #" + getID() + " at [" + position.toTilePosition() + "]";
//        return toString;
//        return "AUnit(" + u.getType().toString() + ")";
        return "AUnit(" + type().shortName()+ " #" + getID() + ") at " + position().toString();
    }

    @Override
    public int compareTo(AUnit otherUnit) {
        return Integer.compare(this.hashCode(), otherUnit.hashCode());
    }
//    public int compareTo(Object o) {
//        int compare;
//
//        if (o instanceof AUnit) {
//            compare = ((AUnit) o).getID();
//        } else {
//            compare = o.hashCode();
//        }
//
//        return Integer.compare(this.hashCode(), compare);
//    }

    // =========================================================
    // Compare type methods
    public boolean isAlive() {
        if (isOur()) {
            return hp() > 0;
        }

        return hp() > 0 || !UnitsArchive.isDestroyed(id());

//        if (isOur()) {
//            return hp() > 0 && !AOurUnitsExtraInfo.hasOurUnitBeenDestroyed(this);
//        }
//
//        return !AEnemyUnits.isEnemyUnitDestroyed(this);
    }

    public boolean canBeHealed() {
        return _repairableMechanically || _healable;
    }

    public boolean isRepairableMechanically() {
        return _repairableMechanically;
    }

    public boolean isHealable() {
        return _healable;
    }

    /**
     * Returns true if given unit is OF TYPE BUILDING.
     */
    public boolean isBuilding() {
        return type().isBuilding() || type().isAddon();
    }

    public boolean isWorker() {
        return _isWorker;
    }

    public boolean isBunker() {
        return type().equals(AUnitType.Terran_Bunker);
    }

    public boolean isBase() {
        return isType(AUnitType.Terran_Command_Center, AUnitType.Protoss_Nexus, AUnitType.Zerg_Hatchery,
                AUnitType.Zerg_Lair, AUnitType.Zerg_Hive);
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
                () -> type().isRangedUnit()
        );
    }

    /**
     * Returns true if given unit is considered to be "melee" unit (not ranged).
     */
    public boolean isMelee() {
        return (boolean) cache.get(
                "isMelee",
                -1,
                () -> type().isMeleeUnit()
        );
    }

    // =========================================================
    // Auxiliary methods
    public boolean ofType(AUnitType type) {
        return type().equals(type);
    }

    public boolean isType(AUnitType... types) {
        return type().is(types);
    }

    public boolean isFullyHealthy() {
        return hp() >= maxHp();
    }

    public int hpPercent() {
        return 100 * hp() / maxHp();
    }

    public double woundPercent() {
        return 100 - 100.0 * hp() / maxHp();
    }

    public boolean isWounded() {
        return hp() < getMaxHP();
    }

    public boolean isExists() {
        return u().exists();
    }

    public int getShields() {
        return u().getShields();
    }

    public int getMaxShields() {
        return type().ut().maxShields();
    }

    public int getMaxHP() {
        return maxHp() + getMaxShields();
    }

    public int getMinesCount() {
        return u().getSpiderMineCount();
    }

    public int getSpiderMinesCount() {
        return u().getSpiderMineCount();
    }

    public String shortName() {
        return type().shortName();
    }

    public String shortNamePlusId() {
        return type().shortName() + " #" + getID();
    }

    public boolean isInWeaponRangeByGame(AUnit target) {
        return u.isInWeaponRange(target.u);
    }

    /**
     * Returns max shoot range (in build tiles) of this unit against land targets.
     */
    public double groundWeaponRange() {
        return type().getGroundWeapon().maxRange() / 32;
    }

    /**
     * Returns max shoot range (in build tiles) of this unit against land targets.
     */
    public double getGroundWeaponMinRange() {
        return type().getGroundWeapon().minRange() / 32;
    }

    /**
     * Returns max shoot range (in build tiles) of this unit against land targets.
     */
    public double airWeaponRange() {
        return type().getAirWeapon().maxRange() / 32;
    }

    /**
     * Returns max shoot range (in build tiles) of this unit against given <b>opponentUnit</b>.
     */
    public int getWeaponRangeAgainst(AUnit opponentUnit) {
        return opponentUnit.type().weaponRangeAgainst(this);
    }

    /**
     * Returns which unit of the same type this unit is. E.g. it can be first (0) Overlord or third (2)
     * Zergling. It compares IDs of units to return correct result.
     */
    public int getUnitIndexInBwapi() {
        int index = 0;
        for (AUnit otherUnit : Select.our().ofType(type()).listUnits()) {
            if (otherUnit.getID() < this.getID()) {
                index++;
            }
        }
        return index;
    }

    // ===  Debugging / Painting methods ========================================
    
    private String tooltip;
//    private int tooltipStartInFrames;

    public AUnit setTooltip(String tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public String getTooltip() {
//        if (AGame.getTimeFrames() - tooltipStartInFrames > 30) {
//            String tooltipToReturn = this.tooltip;
//            this.tooltip = null;
//            return tooltipToReturn;
//        } else {
        return tooltip;
//        }
    }

    public void removeTooltip() {
        this.tooltip = null;
    }

    public boolean hasTooltip() {
        return this.tooltip != null;
    }

    // =========================================================
    // Very specific auxiliary methods
    /**
     * Returns true if given unit is one of buildings like Bunker, Photon Cannon etc. For more details, you
     * have to specify at least one <b>true</b> to the params.
     */
    public boolean isMilitaryBuilding(boolean canShootGround, boolean canShootAir) {
        if (!isBuilding()) {
            return false;
        }
        if (canShootGround && _isMilitaryBuildingAntiGround) {
            return true;
        } else return canShootAir && _isMilitaryBuildingAntiAir;
    }

    public boolean isGroundUnit() {
        return !type().isAirUnit();
    }

    public boolean isAirUnit() {
        return type().isAirUnit();
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
     * Not that we're racists, but spider mines and larvas aren't really units...
     */
    public boolean isNotRealUnit() {
        return type().isNotRealUnit();
    }

    /**
     * Not that we're racists, but buildings, spider mines and larvas aren't really units...
     */
    public boolean isRealUnit() {
        return !type().isNotRealUnit();
    }

    // =========================================================
    // Auxiliary

    public double distTo(AUnit otherUnit) {
        return PositionUtil.distanceTo(this, otherUnit);
    }

    /**
     * Returns real ground distance to given point (not the air shortcut over impassable terrain).
     */
    public double groundDistance(AUnit otherUnit) {
        return PositionUtil.groundDistanceTo(this.position(), otherUnit.position());
    }

    public double distTo(Object o) {
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
        } else if (collection instanceof List) {
            List<AUnit> result = new ArrayList<>();
            for (Object key : (List) collection) {
                Unit u = (Unit) key;
                AUnit unit = createFrom(u);
                result.add(unit);
            }
            return result;
        } else {
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
        return canAttackTarget(target, true, true, false, 0);
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

    public boolean canAttackTarget(
            AUnit target,
            boolean checkShootingRange,
            boolean checkVisibility,
            boolean includeCooldown,
            double safetyMargin
    ) {
        // Target CLOAKED
        if (checkVisibility && target.effCloaked()) {
            return false;
        }

        // Target is GROUND unit
        if (target.isGroundUnit() && (!canAttackGroundUnits() || (includeCooldown && cooldownRemaining() >= 4))) {
            return false;
        }

        // Target is AIR unit
        if (target.isAirUnit() && (!target.canAttackAirUnits() || (includeCooldown && cooldownRemaining() >= 4))) {
            return false;
        }

        // Shooting RANGE
        if (checkShootingRange && !this.hasWeaponRange(target, safetyMargin)) {
            return false;
        }

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
        return this.u.isInWeaponRange(targetUnit.u);
    }

    public boolean hasWeaponRange(AUnit targetUnit, double safetyMargin) {
        WeaponType weaponAgainstThisUnit = getWeaponAgainst(targetUnit);
        if (weaponAgainstThisUnit == WeaponType.None) {
            return false;
        }

        double dist = this.position().distTo(targetUnit);
        return (weaponAgainstThisUnit.minRange() / 32) <= dist && dist <= (weaponAgainstThisUnit.maxRange() / 32 + safetyMargin);
    }

    public boolean hasGroundWeaponRange(APosition position, double safetyMargin) {
        double weaponRange = groundWeaponRange();
        if (weaponRange <= 0) {
            return false;
        }

        double dist = this.position().distTo(position);
        return dist <= (weaponRange + safetyMargin);
    }

    /**
     * Returns weapon that would be used to attack given target. If no such weapon, then WeaponTypes.None will
     * be returned.
     */
    public WeaponType getWeaponAgainst(AUnit target) {
        if (target.isGroundUnit()) {
            return getGroundWeapon();
        } else {
            return getAirWeapon();
        }
    }

    public boolean distToLessThan(AUnit target, double maxDist) {
        if (target == null) {
            return false;
        }

        return distTo(target) <= maxDist;
    }

    public boolean distToMoreThan(AUnit target, double minDist) {
        if (target == null) {
            return false;
        }

        return distTo(target) >= minDist;
    }

    // === Getters ============================================= & setters
    /**
     * Returns true if given unit is currently (this frame) running from an enemy.
     */
    public boolean isRunning() {
        return UnitActions.RUN.equals(getUnitAction()) && runningManager.isRunning();
    }

    public boolean isLastOrderFramesAgo(int minFramesAgo) {
        return AGame.getTimeFrames() - lastUnitOrder >= minFramesAgo;
    }

    /**
     * Returns battle squad object for military units or null for non military-units (or buildings).
     */
    public Squad squad() {
//        if (squad == null) {
//            System.err.println("still squad in unit was fuckin null");
//            squad = AtlantisSquadManager.getAlphaSquad();
//        }
        return squad;
    }

    /**
     * Assign battle squad object for military units.
     */
    public void setSquad(Squad squad) {
        this.squad = squad;
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
    public boolean isJustShooting() {
        return DontInterruptStartedAttacks.shouldNotInterrupt(unit());
    }

    /**
     * Returns the frames counter (time) when the unit had been issued any command.
     */
    public int getLastUnitOrderTime() {
        return lastUnitOrder;
    }

    /**
     * Returns the frames counter (time) since the unit had been issued any command.
     */
    public int getLastOrderFramesAgo() {
        return AGame.getTimeFrames() - lastUnitOrder;
    }

    /**
     * Indicate that in this frame unit received some command (attack, move etc).
     * @return
     */
    public AUnit setLastUnitOrderNow() {
        this.lastUnitOrder = AGame.getTimeFrames();
        return this;
    }

    /**
     * Returns true if unit has anti-ground weapon.
     */
    public boolean canAttackGroundUnits() {
        return (boolean) cache.get(
                "canAttackGroundUnits",
                -1,
                () -> type().getGroundWeapon() != WeaponType.None && type().getGroundWeapon().damageAmount() > 0
                        || type().isReaver()
        );
    }

    /**
     * Returns true if unit has anti-air weapon.
     */
    public boolean canAttackAirUnits() {
        return (boolean) cache.get(
                "canAttackAirUnits",
                -1,
                () -> type().getAirWeapon() != WeaponType.None && type().getAirWeapon().damageAmount() > 0
        );
    }

    /**
     * Caches combat eval of this unit for the time of one frame.
     */
//    public void updateCombatEval(double eval) {
//        _lastTimeCombatEval = AGame.getTimeFrames();
//        _lastCombatEval = eval;
//    }

//    public double getCombatEvalCachedValueIfNotExpired() {
//        if (AGame.getTimeFrames() <= _lastTimeCombatEval) {
//            return _lastCombatEval;
//        } else {
//            return -123456;
//        }
//    }

    public WeaponType getAirWeapon() {
        return type().getAirWeapon();
    }

    public WeaponType getGroundWeapon() {
        return type().getGroundWeapon();
    }

    /**
     * Returns number of frames unit has to wait between the shots.
     * E.g. for Dragoon this value will be always 30.
     */
    public int cooldownAbsolute() {
        if (canAttackGroundUnits()) {
            return getGroundWeapon().damageCooldown();
        }
        if (canAttackAirUnits()) {
            return getAirWeapon().damageCooldown();
        }
        return 0;
    }

    /**
     * Returns number of frames unit STILL has to wait before it can shoot again.
     * E.g. for Dragoon this value will vary between 0 and 30 inclusive.
     */
    public int cooldownRemaining() {
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
        return AConstructionManager.isBuilder(this);
    }

    /**
     * If this unit is supposed to build something it will return ConstructionOrder object assigned to the
     * construction.
     */
    public ConstructionOrder getConstructionOrder() {
        return AConstructionRequests.getConstructionOrderFor(this);
    }

    /**
     * Returns true if this unit belongs to the enemy.
     */
    public boolean isEnemyUnit() {
//        return getPlayer().isEnemy(AGame.getPlayerUs());
        return getPlayer().isEnemy(AGame.getPlayerUs());
    }

    /**
     * Returns true if this unit belongs to the enemy.
     */
    public boolean isEnemy() {
        return (boolean) cache.get(
                "isEnemy",
                60,
                () -> getPlayer().isEnemy(AGame.getPlayerUs())
        );
    }

    /**
     * Returns true if this unit belongs to us.
     */
    public boolean isOur() {
        return getPlayer().equals(AGame.getPlayerUs());
    }

    /**
     * Returns true if this unit is neutral (minerals, geysers, critters).
     */
    public boolean isNeutral() {
        return getPlayer().equals(AGame.getNeutralPlayer());
    }

    /**
     * Returns true if given building is able to build add-on like Terran Machine Shop.
     */
    public boolean canHaveAddon() {
        return type().canHaveAddon();
    }
    
    public int getID() {
        return u.getID();
    }

    public String getIDWithHash() {
        return "#" + u.getID();
    }

    public int id() {
        return u.getID();
    }

    public String idWithHash() {
        return "#" + u.getID();
    }

    // =========================================================
    // Method intermediates between BWMirror and Atlantis
    public Player getPlayer() {
        return u.getPlayer();
    }

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
        return u.exists();
    }

    public boolean isConstructing() {
        return u.isConstructing();
    }

    public boolean hasAddon() {
        return u().getAddon() != null;
    }
    
    public int hp() {
        return u.getHitPoints() + getShields();
    }

    public int maxHp() {
        return (int) cache.get(
                "getMaxHitPoints",
                -1,
                () -> {
                    int hp = u.getType().maxHitPoints() + getMaxShields();
                    if (hp == 0) {
                        System.err.println("Max HP = 0 for");
                        System.err.println(this);
                    }

                    return hp;
                }
        );
    }

    public boolean isIdle() {
        return u.isIdle() || (u.getLastCommand() == null || u.getLastCommand().getType().equals(UnitCommandType.None));
    }

    public boolean isBusy() {
        return !isIdle();
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
    public boolean isVisibleOnMap() {
        return u.isVisible();
    }

    public boolean effVisible() {
        return !effCloaked();
    }

    /**
     * Unit is effectvely cloaked and we can't attack it. Need to detect it first.
     */
    public boolean effCloaked() {
        if ((!isCloaked() && !isBurrowed()) || ensnared() || plagued()) {
            return false;
        }

        return hp() == 0;
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

    public boolean notVisible() {
        return !u.isVisible();
    }

    public boolean isMiningOrExtractingGas() {
        return isGatheringMinerals() || isGatheringGas();
    }

    public boolean isGatheringMinerals() {
        return u.isGatheringMinerals();
    }

    public boolean isGatheringGas() {
        return u.isGatheringGas();
    }

    public boolean isCarryingMinerals() {
        return u.isCarryingMinerals();
    }

    public boolean isCarryingGas() {
        return u.isCarryingGas();
    }

    public boolean isCloaked() {
        return u.isCloaked() || u.isBurrowed();
    }

    public boolean isBurrowed() {
        return u.isBurrowed();
    }

    public boolean isRepairing() {
        return u.isRepairing();
    }

    public int groundWeaponCooldown() {
        return u.getGroundWeaponCooldown();
    }

    public int airWeaponCooldown() {
        return u.getAirWeaponCooldown();
    }

    public boolean isAttackFrame() {
        return u.isAttackFrame();
    }

    public boolean isStartingAttack() {
        return u.isStartingAttack();
    }

    public boolean isStopped() {
        return u.getLastCommand() == null;
    }

    public boolean isStuck() {
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

        if (_lastHitPoints.size() < inLastFrames) {
            return false;
        }

        return hp() < _lastHitPoints.get(inLastFrames - 1);
    }

    public boolean isUnderAttack() {
        // In-game solutions sucks ass badly
//        return u.isUnderAttack();

        return isUnderAttack(1);
    }

    public List<AUnitType> getTrainingQueue() {
        return (List<AUnitType>) AUnitType.convertToAUnitTypesCollection(u.getTrainingQueue());
    }

    public boolean isUpgrading() {
        return u.isUpgrading();
    }

    public List<AUnit> getLarva() {
        return (List<AUnit>) convertToAUnitCollection(u.getLarva());
    }

    public AUnit getTarget() {
        if (u.getTarget() != null) {
            return AUnit.createFrom(u.getTarget());
        }

        return getOrderTarget();
    }

    public APosition getTargetPosition() {
        return APosition.create(u.getTargetPosition());
    }

    public AUnit getOrderTarget() {
        return u.getOrderTarget() != null ? AUnit.createFrom(u.getOrderTarget()) : null;
    }

    public AUnit getBuildUnit() {
        return u.getBuildUnit() != null ? AUnit.createFrom(u.getBuildUnit()) : null;
    }

    public AUnitType getBuildType() {
        return u.getBuildType() != null ? AUnitType.createFrom(u.getBuildType()) : null;
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

    public boolean isAttackingOrMovingToAttack() {
        return u.isAttacking() || (
            getUnitAction() != null && getUnitAction().isAttacking() && getTarget() != null && getTarget().isAlive()
        );
    }

    /**
     * Returns true for flying Terran building.
     */
    public boolean isLifted() {
        return u.isLifted();
    }

    /**
     * Returns true if unit is inside bunker or dropship/shuttle.
     */
    public boolean isLoaded() {
        return u.isLoaded();
    }
    
    public boolean isUnderDisruptionWeb() {
        return u().isUnderDisruptionWeb();
    }
    
    public boolean isUnderDarkSwarm() {
        return u().isUnderDarkSwarm();
    }
    
    public boolean isUnderStorm() {
        return u().isUnderStorm();
    }
    
    public int getRemainingBuildTime() {
        return u().getRemainingBuildTime();
    }
    
    public int getRemainingResearchTime() {
        return u().getRemainingResearchTime();
    }
    
    public int getRemainingTrainTime() {
        return u().getRemainingTrainTime();
    }

    public int getTotalTrainTime() {
        return type().getTotalTrainTime();
    }

    public int getRemainingUpgradeTime() {
        return u().getRemainingUpgradeTime();
    }

    /**
     * Returns true if given position has land connection to given point.
     */
    public boolean hasPathTo(APosition point) {
        return u.hasPath(point);
    }

    public boolean hasPathTo(AUnit unit) {
        return u.hasPath(unit.position());
    }

    public boolean isTrainingAnyUnit() {
        return u.isTraining();
    }

    public boolean isBeingConstructed() {
        return u.isBeingConstructed();
    }

    public boolean isInterruptible() {
        return u.isInterruptible();
    }
    
    public UnitCommand getLastCommand() {
        return u.getLastCommand();
    }

    public boolean isCommand(UnitCommandType command) {
        return u.getLastCommand() != null && u.getLastCommand().getType().equals(command);
    }

    public UnitAction getUnitAction() {
        return unitAction;
    }
    
    // === Unit actions ========================================
    
    public boolean isUnitAction(UnitAction constant) {
        return unitAction == constant;
    }
    
    public boolean isUnitActionAttack() {
        return unitAction == UnitActions.ATTACK_POSITION || unitAction == UnitActions.ATTACK_UNIT
                 || unitAction == UnitActions.MOVE_TO_ENGAGE;
    }
    
    public boolean isUnitActionMove() {
        return unitAction == UnitActions.MOVE || unitAction == UnitActions.MOVE_TO_ENGAGE
                || unitAction == UnitActions.MOVE_TO_BUILD || unitAction == UnitActions.MOVE_TO_REPAIR
                || unitAction == UnitActions.MOVE_TO_FOCUS
                || unitAction == UnitActions.RETREAT
                || unitAction == UnitActions.EXPLORE
                || unitAction == UnitActions.RUN;
    }
    
    public boolean isUnitActionRepair() {
        return unitAction == UnitActions.REPAIR || unitAction == UnitActions.MOVE_TO_REPAIR;
    }
    
    public AUnit setUnitAction(UnitAction unitAction) {
        this.unitAction = unitAction;
        cacheUnitActionTimestamp(unitAction);
        return this;
    }

    public AUnit setUnitAction(UnitAction unitAction, TechType tech, APosition usedAt) {
        this._lastTech = tech;
        this._lastTechPosition = usedAt;
        SpellCoordinator.newSpellAt(usedAt, tech);

        return setUnitAction(unitAction);
    }

    public AUnit setUnitAction(UnitAction unitAction, TechType tech, AUnit usedOn) {
        this._lastTech = tech;
        this._lastTechUnit = usedOn;

        if (ATech.isOffensiveSpell(tech)) {
            SpellCoordinator.newSpellAt(usedOn.position(), tech);
        }

        return setUnitAction(unitAction);
    }

    private void cacheUnitActionTimestamp(UnitAction unitAction) {
        if (unitAction == null) {
            return;
        }
        cacheInt.set(
                "_last" + unitAction.name(),
                -1,
                A.now()
        );
    }

    public boolean lastActionMoreThanAgo(int framesAgo, UnitAction unitAction) {
        return lastActionAgo(unitAction) >= framesAgo;
    }

    public boolean lastActionLessThanAgo(int framesAgo, UnitAction unitAction) {
        return lastActionAgo(unitAction) <= framesAgo;
    }

    public int lastActionAgo(UnitAction unitAction) {
        Integer time = cacheInt.get("_last" + unitAction.name());

//        if (!cacheInt.isEmpty()) {
//            cacheInt.print("lastActionAgo", true);
//        }

        if (time == null) {
            return 999 + A.now();
        }
        return A.now() - time;
    }

    public int lastActionFrame(UnitAction unitAction) {
        Integer time = cacheInt.get("_last" + unitAction.name());

        if (time == null) {
            return 0;
        }
        return time;
    }

    // =========================================================

//    public boolean shouldApplyAntiGlitch() {
////        return (isAttacking() || isAttackFrame());
//        return getLastUnitOrderWasFramesAgo() >= 40 || isMoving() && getLastUnitOrderWasFramesAgo() >= 10;
//    }

    public boolean noCooldown() {
        return groundWeaponCooldown() <= 0 || airWeaponCooldown() <= 0;
    }
    
    public int scarabCount() {
        return u().getScarabCount();
    }

    public boolean isRepairerOfAnyKind() {
        return ARepairAssignments.isRepairerOfAnyKind(this);
    }

    public boolean isScout() {
        return AScoutManager.isScout(this);
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

    public boolean lastAttackOrderLessThanAgo(int framesAgo) {
        return A.ago(_lastAttackOrder) <= framesAgo;
    }

    public boolean lastAttackOrderMoreThanAgo(int framesAgo) {
        return A.ago(_lastAttackOrder) <= framesAgo;
    }

    public int lastAttackFrameAgo() {
        return A.ago(_lastAttackFrame);
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

    public int lastRetreatedAgo() {
        return A.ago(_lastRetreat);
    }

    public int lastStartedRunningAgo() {
        return A.ago(_lastStartedRunning);
    }

    public boolean lastStartedRunningMoreThanAgo(int framesAgo) {
        return A.ago(_lastStartedRunning) >= framesAgo;
    }

    public boolean lastStoppedRunningLessThanAgo(int framesAgo) {
        return A.ago(_lastStoppedRunning) <= framesAgo;
    }

    public boolean hasNotMovedInAWhile() {
        return x() == _lastX && y() == _lastY;
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

    public boolean isOtherUnitFacingThisUnit(AUnit otherUnit) {
        Vector positionDifference = Vectors.fromPositionsBetween(this, otherUnit);
        Vector otherUnitLookingVector = Vectors.vectorFromAngle(otherUnit.getAngle(), positionDifference.length());

//        if (isFirstCombatUnit()) {
//            System.out.println("### ARE PARALLEL = " + (positionDifference.isParallelTo(otherUnitLookingVector)));
//            System.out.println(positionDifference + " // " + positionDifference.toAngle());
//            System.out.println(otherUnitLookingVector + " // " + otherUnitLookingVector.toAngle());
//        }

        return positionDifference.isParallelTo(otherUnitLookingVector);
    }

    public boolean isFirstCombatUnit() {
        return getID() == Select.ourCombatUnits().first().getID();
    }

    public Mission micro() {
        return squad().mission();
    }

    public int squadSize() {
        return squad().size();
    }

    public int energy() {
        return u.getEnergy();
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

    public boolean hasBiggerRangeThan(AUnit enemy) {
        if (isGroundUnit()) {
            return groundWeaponRange() > enemy.groundWeaponRange();
        }
        return airWeaponRange() > enemy.airWeaponRange();
    }

    public boolean hasBiggerRangeThan(Units enemies) {
        if (isGroundUnit()) {
            return enemies.stream().noneMatch(u -> u.groundWeaponRange() > this.groundWeaponRange());
        }
        else {
            return enemies.stream().noneMatch(u -> u.groundWeaponRange() > this.airWeaponRange());
        }
    }

    public boolean hasNothingInQueue() {
        return getTrainingQueue().size() <= 1;
    }

    public boolean canCloak() {
        return type().isCloakable() && !isCloaked();
    }

    public boolean is(AUnitType ...types) {
        return type().is(types);
    }

    public boolean isTargettedBy(AUnit attacker) {
        return this.equals(attacker.getTarget());
    }

//    public boolean inActOfShooting() {
//        return lastStartedAttackLessThanAgo(8);
//    }

    public boolean isArchon() {
        return is(AUnitType.Protoss_Archon);
    }

    public boolean isUltralisk() {
        return is(AUnitType.Zerg_Ultralisk);
    }

    public List<AUnit> loadedUnits() {
        List<AUnit> loaded = new ArrayList<>();
        for (Unit unit : u.getLoadedUnits()) {
            loaded.add(AUnit.createFrom(unit));
        }
        return loaded;
    }

    public int lastTechUsedAgo() {
        return lastActionAgo(UnitActions.USING_TECH);
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

    public boolean isUnitUnableToDoAnyDamage() {
        return type().isUnitUnableToDoAnyDamage() || (type().isReaver() && scarabCount() == 0);
    }

    public boolean recentlyAcquiredTargetToAttack() {
        if (getTarget() == null) {
            return false;
        }

//        if (!isAttackingOrMovingToAttack()) {
//            return false;
//        }

//        System.out.println(unit().idWithHash() + " got target " + lastTargetToAttackAcquiredAgo() + " ago");

//        int targetAcquiredAgo = A.atMostFramesAgo(_lastTargetToAttackAcquired, (int) (cooldownAbsolute() / 1.3));
        int targetAcquiredAgo = lastTargetToAttackAcquiredAgo();

        return getTarget().isAlive()
                && (
                    (targetAcquiredAgo <= 45 && unit().woundPercent() <= 5 && !lastUnderAttackMoreThanAgo(30 * 10))
                    || targetAcquiredAgo <= cooldownAbsolute() / 1.1
                );
    }

    public int lastTargetToAttackAcquiredAgo() {
        return A.ago(_lastTargetToAttackAcquired);
    }

    public boolean isAirUnitAntiAir() {
        return type().isAirUnitAntiAir();
    }

    public boolean isSquadScout() {
        return squad() != null && equals(squad().getSquadScout());
    }

    public boolean isNotAttackableByRangedDueToSpell() {
        return isUnderDarkSwarm();
    }

//    public boolean isDepleted() {
//        return u.getAcidSporeCount();
//    }

    //    public boolean isFacingTheSameDirection(AUnit otherUnit) {
//        return Math.abs(getAngle() - otherUnit.getAngle()) <= 0.3;
//    }
}
