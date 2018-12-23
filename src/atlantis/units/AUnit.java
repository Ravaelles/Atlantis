package atlantis.units;

import atlantis.AGame;
import atlantis.combat.micro.ARunManager;
import atlantis.combat.squad.Squad;
import atlantis.constructing.AConstructionManager;
import atlantis.constructing.ConstructionOrder;
import atlantis.enemy.AEnemyUnits;
import atlantis.information.AMap;
import atlantis.information.AOurUnitsExtraInfo;
import atlantis.position.APosition;
import atlantis.repair.ARepairManager;
import atlantis.scout.AScoutManager;
import atlantis.units.actions.UnitAction;
import atlantis.units.actions.UnitActions;
import atlantis.wrappers.ACachedValue;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.UnitCommandType;
import org.openbw.bwapi4j.type.WeaponType;
import org.openbw.bwapi4j.unit.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for BWMirror Unit class that makes units much easier to use.<br /><br />
 * Atlantis uses wrappers for BWMirror native classes which can't be extended.<br /><br />
 * <b>AUnit</b> class contains numerous helper methods, but if you think some methods are missing you can
 * createFromTileXY missing method here and you can reference original Unit class via u() method.
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AUnit extends APosition implements Comparable<Position>, AUnitOrders {

    // Mapping of native unit IDs to AUnit objects
    private static final Map<Integer, AUnit> instances = new HashMap<>();

    // Cached distances to other units - reduces time on calculating unit1.distanceTo(unit2)
    public static final ACachedValue<Double> unitDistancesCached = new ACachedValue<>();

    private UnitImpl u;
    private AUnitType _lastCachedType;
    private UnitAction unitAction;
    private int _lastTimeOrderWasIssued = -1;
    private AUnit _cachedNearestMeleeEnemy = null;

    // =========================================================

    /**
     * Atlantis uses wrapper for OpenBW Unit classes.<br />
     * <b>AUnit</b> class contains numerous helper methods, but if you think some methods are missing you can
     * createFromTileXY missing method here and you can reference original Unit class via u() method.
     */
    public static AUnit createFrom(UnitImpl u) {
        if (u == null) {
            throw new RuntimeException("AUnit constructor: unit is null");
        }

        if (instances.containsKey(u.getId())) {
            return instances.get(u.getId());
        } else {
            AUnit unit = new AUnit(u);
            instances.put(u.getId(), unit);
            return unit;
        }
    }

    private AUnit(UnitImpl u) {
        super(u.getPosition());

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

    public static AUnit createFrom(Unit u) {
        UnitImpl unitImpl = (UnitImpl) u;
        return createFrom(unitImpl);
    }

    // =========================================================

    /**
     * Returns unit type from BWMirror OR if type is Unknown (behind fog of war) it will return last cached
     * type.
     */
    public AUnitType getType() {
        AUnitType type = AUnitType.createFrom(u.getType());
        if (AUnitType.Unknown.equals(type)) {
            if (this.isOurUnit()) {
                System.err.println("Our unit (" + this + ") returned Unknown type");
            }
            return _lastCachedType;
        } else {
            _lastCachedType = type;
            return type;
        }
    }

    public void refreshType() {
        _lastCachedType = AUnitType.createFrom(u.getType());
        _isWorker = isType(AUnitType.Terran_SCV, AUnitType.Protoss_Probe, AUnitType.Zerg_Drone);
    }

    public APosition getPosition() {
        return APosition.createFromTileXY(u.getPosition());
    }

    /**
     * <b>AVOID USAGE AS MUCH AS POSSIBLE</b> outside AUnit class. AUnit class should be used always in place
     * of Unit.
     */
    public UnitImpl u() {
        return ((UnitImpl) u);
    }

    /**
     * <b>AVOID USAGE AS MUCH AS POSSIBLE</b> outside AUnit class. AUnit class should be used always in place
     * of PlayerUnit.
     */
    public PlayerUnit pu() {
        return ((PlayerUnit) u);
    }

    /**
     * This method exists only to allow reference in UnitActions class.
     */
    @Override
    public AUnit unit() {
        return this;
    }

    private static AUnit getBWMirrorUnit(Unit u) {
        for (AUnit unit : instances.values()) {
            if (unit.u.equals(u)) {
                return unit;
            }
        }
        return null;
    }

    // =========================================================
    // =========================================================
    // =========================================================

    private Squad squad = null;
    private ARunManager runManager = new ARunManager(this);
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
    public boolean moveAwayFrom(Position position, double moveDistance) {
        if (position == null || moveDistance < 0.01) {
            return false;
        }

        int dx = position.getX() - getX();
        int dy = position.getY() - getY();
        double vectorLength = Math.sqrt(dx * dx + dy * dy);
        double modifier = (moveDistance * 32) / vectorLength;
        dx = (int) (dx * modifier);
        dy = (int) (dy * modifier);

        APosition newPosition = new APosition(getX() - dx, getY() - dy).makeValid();

//        if (AtlantisRunManager.isPossibleAndReasonablePosition(
//                this, newPosition, -1, 9999, true
//        ) && move(newPosition, UnitActions.MOVE)) {
        if (ARunManager.isPossibleAndReasonablePosition(
                this.getPosition(), newPosition, moveDistance * 0.2, moveDistance * 1.5, true
        )
                && move(newPosition, UnitActions.MOVE)) {
            this.setTooltip("Move away");
            return true;
        } else {
            this.setTooltip("Can't move away");
            return false;
        }
    }

    /**
     * Returns true if any close enemy can either shoot or hit this unit.
     */
    public boolean canAnyCloseEnemyShootThisUnit() {
        return !Select.enemy().inRadius(12.5, this).canAttack(this).isEmpty();
    }

    /**
     * Returns true if any close enemy can either shoot or hit this unit.
     */
    public boolean canAnyCloseEnemyShootThisUnit(double safetyMargin) {
        return !Select.enemy().inRadius(12.5, this).canAttack(this, safetyMargin).isEmpty();
    }

    // =========================================================
    @Override
    public String toString() {
//        Position position = this.getPosition();
//        String toString = getType().getShortName();
//        toString += " #" + getId() + " at [" + position.toTilePosition() + "]";
//        return toString;
//        return "AUnit(" + u.getType().toString() + ")";
        return "AUnit(" + getType().getShortName() + " #" + getId() + ") at " + getPosition().toString();
    }

//    @Override
//    public int compareTo(Object o) {
//        int compare;
//
//        if (o instanceof AUnit) {
//            compare = ((AUnit) o).getId();
//        }
//        else {
//            compare = o.hashCode();
//        }
//
//        return Integer.compare(this.hashCode(), compare);
//    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final Unit other = (Unit) obj;
//        if (this.getId() != other.getId()) {
//            return false;
//        }

        if (obj instanceof AUnit) {
            AUnit other = (AUnit) obj;
//            return getId() == other.getId();
            return getId() == other.getId();
        } else if (obj instanceof Unit) {
            Unit other = (Unit) obj;
            return u().getId() == other.getId();
        }

        return false;
    }

    // =========================================================
    // Compare type methods
    public boolean isAlive() {
//        return getHP() > 0 && ! AtlantisEnemyUnits.isEnemyUnitDestroyed(this);
        return isExists() && (!AEnemyUnits.isEnemyUnitDestroyed(this)
                && !AOurUnitsExtraInfo.hasOurUnitBeenDestroyed(this));
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
        return getType().isBuilding();
    }

    public boolean isWorker() {
        return _isWorker;
    }

    public boolean isBunker() {
        return getType().equals(AUnitType.Terran_Bunker);
    }

    public boolean isBase() {
        return isType(AUnitType.Terran_Command_Center, AUnitType.Protoss_Nexus, AUnitType.Zerg_Hatchery,
                AUnitType.Zerg_Lair, AUnitType.Zerg_Hive);
    }

    public boolean isInfantry() {
        return getType().isOrganic();
    }

    public boolean isVehicle() {
        return getType().isMechanical();
    }

    /**
     * Returns true if given unit is considered to be "ranged" unit (not melee).
     */
    public boolean isRangedUnit() {
        return getType().isRangedUnit();
    }

    /**
     * Returns true if given unit is considered to be "melee" unit (not ranged).
     */
    public boolean isMeleeUnit() {
        return getType().isMeleeUnit();
    }

    // =========================================================
    // Auxiliary methods
    public boolean ofType(AUnitType type) {
        return getType().equals(type);
    }

    public boolean isType(AUnitType... types) {
        return getType().isType(types);
    }

    public boolean isFullyHealthy() {
        return getHitPoints() >= getMaxHitPoints();
    }

    public int getHPPercent() {
        return 100 * getHitPoints() / getMaxHitPoints();
    }

    public boolean isWounded() {
        return getHitPoints() < getMaxHP();
    }

    public boolean isExists() {
        return u().exists();
    }

    public int getHP() {
        return getHitPoints();
    }

    public int getShields() {
        return pu().getShields();
    }

    public int getMaxShields() {
        return getType().ut().maxShields();
    }

    public int getMaxHP() {
        return getMaxHitPoints() + getMaxShields();
    }

    public int getMinesCount() {
        return ((Vulture) u()).getSpiderMineCount();
    }

    public int getSpiderMinesCount() {
        return ((Vulture) u()).getSpiderMineCount();
    }

    public String getShortName() {
        return getType().getShortName();
    }

    public String getShortNamePlusId() {
        return getType().getShortName() + " #" + getId();
    }

    /**
     * Returns max shoot range (in build tiles) of this unit against land targets.
     */
    public double getWeaponRangeGround() {
        return getType().getGroundWeapon().maxRange() / 32;
    }

    /**
     * Returns max shoot range (in build tiles) of this unit against land targets.
     */
    public double getWeaponRangeAir() {
        return getType().getAirWeapon().maxRange() / 32;
    }

    /**
     * Returns max shoot range (in build tiles) of this unit against given <b>opponentUnit</b>.
     */
    public int getWeaponRangeAgainst(AUnit opponentUnit) {
        if (opponentUnit.isAirUnit()) {
            return getType().getAirWeapon().maxRange() / 32;
        } else {
            return getType().getGroundWeapon().maxRange() / 32;
        }
    }

    /**
     * Returns which unit of the same type this unit is. E.g. it can be first (0) Overlord or third (2)
     * Zergling. It compares IDs of units to return correct result.
     */
    public int getUnitIndexInBwapi() {
        int index = 0;
        for (AUnit otherUnit : Select.our().ofType(getType()).listUnits()) {
            if (otherUnit.getId() < this.getId()) {
                index++;
            }
        }
        return index;
    }

    // ===  Debugging / Painting methods ========================================

    private String tooltip;
//    private int tooltipStartInFrames;

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
//        this.tooltipStartInFrames = AGame.getTimeFrames();
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
        } else if (canShootAir && _isMilitaryBuildingAntiAir) {
            return true;
        }
        return false;
    }

    public boolean isGroundUnit() {
        return !getType().isAirUnit();
    }

    public boolean isAirUnit() {
        return getType().isAirUnit();
    }

    public boolean isSpiderMine() {
        return getType().equals(AUnitType.Terran_Vulture_Spider_Mine);
    }

    public boolean isLarvaOrEgg() {
        return getType().equals(AUnitType.Zerg_Larva) || getType().equals(AUnitType.Zerg_Egg);
    }

    public boolean isLarva() {
        return getType().equals(AUnitType.Zerg_Larva);
    }

    public boolean isEgg() {
        return getType().equals(AUnitType.Zerg_Egg);
    }

    /**
     * Not that we're racists, but spider mines and larvas aren't really units...
     */
    public boolean isNotActuallyUnit() {
        return isSpiderMine() || isLarvaOrEgg();
    }

    /**
     * Not that we're racists, but spider mines and larvas aren't really units...
     */
    public boolean isActualUnit() {
        return !isNotActuallyUnit();
    }

    // =========================================================
    // Auxiliary

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
     * Returns true if this unit is capable of attacking <b>otherUnit</b>. For example Zerglings can't attack
     * flying targets and Corsairs can't attack ground targets.
     *
     * @param includeCooldown if true, then unit will be considered able to attack only if the cooldown after
     *                        the last shot allows it
     */
    public boolean canAttackThisKindOfUnit(AUnit otherUnit, boolean includeCooldown) {
        // Enemy is GROUND unit
        if (otherUnit.isGroundUnit()) {
            return canAttackGroundUnits() && (!includeCooldown || getGroundWeaponCooldown() == 0);
        }

        // Enemy is AIR unit
        else {
            return canAttackAirUnits() && (!includeCooldown || getAirWeaponCooldown() == 0);
        }
    }

    /**
     * Returns <b>true</b> if this unit can attack <b>targetUnit</b> in terms of both min and max range
     * conditions fulfilled.
     *
     * @param safetyMargin allowed error (in tiles) applied to the max distance condition
     */
    public boolean hasRangeToAttack(AUnit targetUnit, double safetyMargin) {
        WeaponType weaponAgainstThisUnit = getWeaponAgainst(targetUnit);
        if (weaponAgainstThisUnit == WeaponType.None) {
            return false;
        }

        double dist = this.distanceTo(targetUnit);
        return dist <= (weaponAgainstThisUnit.maxRange() / 32 + safetyMargin)
                && dist >= (weaponAgainstThisUnit.minRange() / 32);
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

    // =========================================================
    // Getters & setters

    /**
     * Returns true if given unit is currently (this frame) running from an enemy.
     */
    public boolean isRunning() {
        return runManager.isRunning();
    }

    /**
     * Returns battle squad object for military units or null for non military-units (or buildings).
     */
    public Squad getSquad() {
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
    public ARunManager getRunManager() {
        return runManager;
    }

    /**
     * Returns true if unit is starting an attack or already in the attack frame animation.
     */
    public boolean isJustShooting() {
//        return isAttacking() && (isAttackFrame() || isStartingAttack());
        return (isAttackFrame() || isStartingAttack());
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
    public int getLastUnitOrderWasFramesAgo() {
        return AGame.getTimeFrames() - lastUnitOrder;
    }

    /**
     * Indicate that in this frame unit received some command (attack, move etc).
     */
    public void setLastUnitOrderNow() {
        this.lastUnitOrder = AGame.getTimeFrames();
    }

    /**
     * Returns true if unit has anti-ground weapon.
     */
    public boolean canAttackGroundUnits() {
        return getType().getGroundWeapon() != WeaponType.None;
    }

    /**
     * Returns true if unit has anti-air weapon.
     */
    public boolean canAttackAirUnits() {
        return getType().getAirWeapon() != WeaponType.None;
    }

    /**
     * Caches combat eval of this unit for the time of one frame.
     */
    public void updateCombatEval(double eval) {
        _lastTimeCombatEval = AGame.getTimeFrames();
        _lastCombatEval = eval;
    }

    public double getCombatEvalCachedValueIfNotExpired() {
        if (AGame.getTimeFrames() <= _lastTimeCombatEval) {
            return _lastCombatEval;
        } else {
            return (int) -123456;
        }
    }

    public WeaponType getAirWeapon() {
        return getType().getAirWeapon();
    }

    public WeaponType getGroundWeapon() {
        return getType().getGroundWeapon();
    }

    /**
     * Indicates that this unit should be running from given enemy unit.
     * If enemy parameter is null, it will try to determine the best run behavior.
     * If enemy is not null, it will try running straight from this unit.
     */
    public boolean runFrom(AUnit runFrom) {
        if (runFrom == null) {
            return runManager.run();
        } else {
            return runManager.runFrom(runFrom);
        }
    }

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
        return AConstructionManager.getConstructionOrderFor(this);
    }

    /**
     * Returns true if this unit belongs to the enemy.
     */
    public boolean isEnemyUnit() {
//        return isEnemy();
        int id = u().getId();
        for (AUnit unit : Select.our().listUnits()) {
            if (unit.getId() == id) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if this unit belongs to the enemy.
     */
    public boolean isEnemy() {
        return isEnemyUnit();
    }

    /**
     * Returns true if this unit belongs to us.
     */
    public boolean isOurUnit() {
        return ! isEnemyUnit();
    }

    /**
     * Returns true if this unit is neutral (minerals, geysers, critters).
     */
//    public boolean isNeutralUnit() {
//        return getPlayer().equals(AGame.getNeutralPlayer());
//    }

    /**
     * Returns true if given building is able to build add-on like Terran Machine Shop.
     */
    public boolean canHaveAddon() {
        return getType().canHaveAddon();
    }

    public int getId() {
        return u.getId();
    }

    // === Method intermediates between OpenBW and Atlantis ======================================================

    private PlayerUnitImpl playerUnitImpl() {
        return (PlayerUnitImpl) u;
    }

    public int getX() {
        return u.getX();
    }

    public int getY() {
        return u.getY();
    }

    public boolean isCompleted() {
        return playerUnitImpl().isCompleted();
    }

    public boolean exists() {
        return u.exists();
    }

    public boolean isConstructing() {
        return scvUnit().isConstructing();
    }

    public boolean hasAddon() {
        return addonableUnit().getAddon() != null;
    }

    public int getHitPoints() {
        return playerUnitImpl().getHitPoints() + playerUnitImpl().getShields();
    }

    public int getMaxHitPoints() {
        return u.getType().maxHitPoints() + getMaxShields();
    }

    public boolean isIdle() {
        return playerUnitImpl().isIdle() || playerUnitImpl().getLastCommand().equals(UnitCommandType.None);
    }

    public boolean isBusy() {
        return ! isIdle();
    }

    public boolean isVisible() {
        return u.isVisible();
    }

    public boolean isGatheringMinerals() {
        return workerUnit().isGatheringMinerals();
    }

    public boolean isGatheringGas() {
        return workerUnit().isGatheringGas();
    }

    public boolean isCarryingMinerals() {
        return workerUnit().isCarryingMinerals();
    }

    public boolean isCarryingGas() {
        return workerUnit().isCarryingGas();
    }

    public boolean isCloaked() {
        return workerUnit().isCloaked();
    }

    public boolean isBurrowed() {
        return burrowableUnit().isBurrowed();
    }

    public boolean isRepairing() {
        return scvUnit().isRepairing();
    }

    public int getGroundWeaponCooldown() {
        return groundAttackerUnit().getGroundWeaponCooldown();
    }

    public int getAirWeaponCooldown() {
        return airAttackerUnit().getAirWeaponCooldown();
    }

    public boolean isAttackFrame() {
        return playerUnitImpl().isAttackFrame();
    }

    public boolean isStartingAttack() {
        return playerUnitImpl().isStartingAttack();
    }

    public boolean isStuck() {
        return mobileUnit().isStuck();
    }

    public boolean isHoldingPosition() {
        return mobileUnit().isHoldingPosition();
    }

    public boolean isSieged() {
        return tankUnit().isSieged();
    }

    public boolean isUnsieged() {
        return ! tankUnit().isSieged();
    }

    public boolean isUnderAttack() {
        return playerUnitImpl().isUnderAttack();
    }

    public List<AUnitType> getTrainingQueue() {
        return (List<AUnitType>) AUnitType.convertToAUnitTypesCollection(trainingUnit().getTrainingQueue());
    }

    public boolean isUpgrading() {
        return researchingUnit().isUpgrading();
    }

    public List<AUnit> getLarva() {
        return (List<AUnit>) convertToAUnitCollection(hatcheryUnit().getLarva());
    }

    public AUnit getTarget() {
        return mobileUnit().getTargetUnit() != null ? AUnit.createFrom(mobileUnit().getTargetUnit()) : null;
    }

    public APosition getTargetPosition() {
        return APosition.createFromTileXY(mobileUnit().getTargetPosition());
    }

    public AUnit getOrderTarget() {
        return mobileUnit().getOrderTarget() != null ? AUnit.createFrom(mobileUnit().getOrderTarget()) : null;
    }

    public AUnit getBuildUnit() {
        return workerUnit().getBuildUnit() != null ? AUnit.createFrom(workerUnit().getBuildUnit()) : null;
    }

    public AUnitType getBuildType() {
        return workerUnit().getBuildType() != null ? AUnitType.createFrom(workerUnit().getBuildType()) : null;
    }

    public boolean isVulture() {
        return getType().isVulture();
    }

    public boolean isTank() {
        return getType().isTank();
    }

    public boolean isMorphing() {
        return ! playerUnitImpl().isCompleted();
    }

    public boolean isMoving() {
        return mobileUnit().isMoving();
    }

    public boolean isAttacking() {
        return mobileUnit().isAttacking();
    }

    /**
     * Returns true for flying Terran building.
     */
    public boolean isLifted() {
        return flyingBuildingUnit().isLifted();
    }

    /**
     * Returns true if unit is inside bunker or dropship/shuttle.
     */
    public boolean isLoaded() {
        return loadableUnit().isLoaded();
    }

    public boolean isUnderDisruptionWeb() {
        return mobileUnit().isUnderDisruptionWeb();
    }

    public boolean isUnderDarkSwarm() {
        return mobileUnit().isUnderDarkSwarm();
    }

    public boolean isUnderStorm() {
        return mobileUnit().isUnderStorm();
    }

    public int getRemainingBuildTime() {
        return buildingUnit().getRemainingBuildTime();
    }

    public int getRemainingResearchTime() {
        return researchingUnit().getResearchInProgress().getRemainingResearchTime();
    }

    public int getRemainingTrainTime() {
        return trainingUnit().getRemainingTrainTime();
    }

    public int getRemainingUpgradeTime() {
        return researchingUnit().getUpgradeInProgress().getRemainingUpgradeTime();
    }

    /**
     * Returns true if given position has land connection to given point.
     */
    public boolean hasPathTo(APosition position) {
        return AMap.hasPath(u.getPosition(), position);
    }

    public boolean isTrainingAnyUnit() {
        return trainingUnit().isTraining();
    }

    public boolean isBeingConstructed() {
        return buildingUnit().isBeingConstructed();
    }

    public boolean isInterruptible() {
        return playerUnitImpl().isInterruptible();
    }

    public UnitCommandType getLastCommand() {
        return mobileUnit().getLastCommand();
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
                || unitAction == UnitActions.RETREAT
                || unitAction == UnitActions.EXPLORE
                || unitAction == UnitActions.RUN;
    }

    public boolean isUnitActionRepair() {
        return unitAction == UnitActions.REPAIR || unitAction == UnitActions.MOVE_TO_REPAIR;
    }

    public void setUnitAction(UnitAction unitAction) {
        this.unitAction = unitAction;
    }

    // =========================================================

    public boolean isReadyToShoot() {
        return getGroundWeaponCooldown() <= 0 && getAirWeaponCooldown() <= 0;
    }

    public int getScarabCount() {
        return ((Reaver) u).getScarabCount();
    }

    public AUnitType type() {
        return getType();
    }

    public boolean isRepairerOfAnyKind() {
        return ARepairManager.isRepairerOfAnyKind(this);
    }

    public boolean isScout() {
        return AScoutManager.isScout(this);
    }

    public int getSpaceProvided() {
        return type().ut().spaceProvided();
    }

    public int getSpaceRequired() {
        return type().ut().spaceRequired();
    }

    public int getSpaceRemaining() {
        return loadableUnit().getSpaceRemaining();
    }

    public AUnit getCachedNearestMeleeEnemy() {
        return _cachedNearestMeleeEnemy;
    }

    public void setCachedNearestMeleeEnemy(AUnit _cachedNearestMeleeEnemy) {
        this._cachedNearestMeleeEnemy = _cachedNearestMeleeEnemy;
    }

}
