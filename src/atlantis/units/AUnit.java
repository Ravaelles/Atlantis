package atlantis.units;

import atlantis.AtlantisGame;
import atlantis.combat.group.Group;
import atlantis.combat.micro.AtlantisRunning;
import atlantis.constructing.AtlantisConstructingManager;
import atlantis.constructing.ConstructionOrder;
import atlantis.debug.tooltip.TooltipManager;
import atlantis.enemy.AtlantisEnemyUnits;
import atlantis.wrappers.APositionedObject;
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitCommand;
import bwapi.UnitCommandType;
import bwapi.UnitType;
import bwapi.WeaponType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Wrapper for BWMirror Unit class that makes it much easier to use.
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AUnit extends APositionedObject implements Comparable<AUnit>, UnitActions {
    
    private static final HashMap<Unit, AUnit> instances = new HashMap<>();
//    private static final List<AUnit> instances = new ArrayList<>();
    
    private Unit u;
//    private AUnitType type;
    
    // =========================================================

    private AUnit(Unit u) {
        if (u == null) {
            throw new RuntimeException("AUnit constructor: unit is null");
        }
        this.u = u;
//        this.type = AUnitType.createFrom(u.getType());
        
        atlantisInit();
    }

    public static AUnit createFrom(Unit u) {
        if (u == null) {
            throw new RuntimeException("AUnit constructor: unit is null");
        }

        if (instances.containsKey(u)) {
            return instances.get(u);
        }
        else {
            AUnit unit = new AUnit(u);
            instances.put(u, unit);
            return unit;
        }
//        AUnit unit;
//        if ((unit = getBWMirrorUnit(u)) != null) {
//            return unit;
//        }
//        else {
//            unit = new AUnit(u);
//            instances.add(unit);
//            return unit;
//        }
    }
    
    // =========================================================
    
    /**
     * Units can change its type and we have to manually call this method to updated cached unit's type.
     */
//    public void updateType() {
//        if (type.isLarva()) {
//            System.out.println("Update " + this + " to " + u.getType());
//        }
////        this.type = AUnitType.createFrom(u.getType());
//    }
    
    public AUnitType getType() {
//        return type;
        return AUnitType.createFrom(u.getType());
    }
    
    @Override
    public Position getPosition() {
        return u.getPosition();
    }
    
    /**
     * <b>AVOID USAGE AS MUCH AS POSSIBLE</b> outside AUnit class.
     * AUnit class should be used always in place of Unit.
     */
    @Override
    public Unit u() {
        return u;
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

    private static int firstFreeID = 1;

    private int innerID;
    private Group group = null;
    private AtlantisRunning running = new AtlantisRunning(this);
    private int lastUnitAction = 0;

    private boolean _repairableMechanically = false;
    private boolean _healable = false;
    private boolean _isMilitaryBuildingAntiGround = false;
    private boolean _isMilitaryBuildingAntiAir = false;
    private double _lastCombatEval;
    private int _lastTimeCombatEval = 0;

    // =========================================================
    // Atlantis constructor
    private void atlantisInit() {
        innerID = firstFreeID++;

        // Repair & Heal
        _repairableMechanically = isBuilding() || isVehicle();
        _healable = isInfantry() || isWorker();

        // Military building
        _isMilitaryBuildingAntiGround = isType(
                AUnitType.Terran_Bunker, AUnitType.Protoss_Photon_Cannon, AUnitType.Zerg_Sunken_Colony
        );
        _isMilitaryBuildingAntiAir = isType(
                AUnitType.Terran_Bunker, AUnitType.Terran_Missile_Turret,
                AUnitType.Protoss_Photon_Cannon, AUnitType.Zerg_Spore_Colony
        );
    }

    // =========================================================
    // Important methods
    /**
     * Unit will move by given distance (in build tiles) from given position.
     */
    public void moveAwayFrom(Position position, double moveDistance) {
        int dx = position.getX() - getX();
        int dy = position.getY() - getY();
        double vectorLength = Math.sqrt(dx * dx + dy * dy);
        double modifier = (moveDistance * 32) / vectorLength;
        dx = (int) (dx * modifier);
        dy = (int) (dy * modifier);

        Position newPosition = new Position(getX() - dx, getY() - dy);

        move(newPosition);
        TooltipManager.setTooltip(this, "Run");
    }
    
    // =========================================================
    @Override
    public String toString() {
//        Position position = this.getPosition();
//        String toString = getType().getShortName();
//        toString += " #" + getID() + " at [" + position.toTilePosition() + "]";
//        return toString;
//        return "AUnit(" + u.getType().toString() + ")";
        return "AUnit(" + getType().toString() + " #" + innerID + ")";
    }
    
    @Override
    public int compareTo(AUnit o) {
        return Integer.compare(this.getID(), ((AUnit) o).getID());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.getID();
        return hash;
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
//        if (this.getID() != other.getID()) {
//            return false;
//        }

        if (obj instanceof AUnit) {
            AUnit other = (AUnit) obj;
//            return getID() == other.getID();
            return u == other.u;
        }
//        else if (obj instanceof Unit) {
//            Unit other = (Unit) obj;
//            return getID() == other.getID();
//        }

        return true;
    }

    // =========================================================
    // Compare type methods
    
    public boolean isAlive() {
        return !AtlantisEnemyUnits.isEnemyUnitDestroyed(this);
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
        return isType(AUnitType.Terran_SCV, AUnitType.Protoss_Probe, AUnitType.Zerg_Drone);
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

    public int getHP() {
        return getHitPoints();
    }

    public int getMaxHP() {
        return getMaxHitPoints();
    }

    public String getShortName() {
        return getType().getShortName();
    }

    /**
     * Has separate name not to mistake attackUnit with attackPosition.
     */
    public boolean attackUnit(AUnit target) {
        return u.attack(target.u);
    }
    
    /**
     * Returns max shoot range (in build tiles) of this unit against land targets.
     */
    public double getShootRangeGround() {
        return getType().getGroundWeapon().maxRange() / 32;
    }

    /**
     * Returns max shoot range (in build tiles) of this unit against land targets.
     */
    public double getShootRangeAir() {
        return getType().getAirWeapon().maxRange() / 32;
    }

    /**
     * Returns max shoot range (in build tiles) of this unit against given <b>opponentUnit</b>.
     */
    public double getShootRangeAgainst(AUnit opponentUnit) {
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
            if (otherUnit.getID() < this.getID()) {
                index++;
            }
        }
        return index;
    }

    // =========================================================
    // Debugging / Painting methods
    
    private String tooltip;
    private int tooltipStartInFrames;

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
        this.tooltipStartInFrames = AtlantisGame.getTimeFrames();
    }

    public String getTooltip() {
        if (AtlantisGame.getTimeFrames() - tooltipStartInFrames > 30) {
            String tooltipToReturn = this.tooltip;
            this.tooltip = null;
            return tooltipToReturn;
        } else {
            return tooltip;
        }
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

    // =========================================================
    // RANGE and ATTACK methods
    
    /**
     * Returns true if this unit is capable of attacking <b>otherUnit</b>. For example Zerglings can't attack
     * flying targets and Corsairs can't attack ground targets.
     *
     * @param includeCooldown if true, then unit will be considered able to attack only if the cooldown after
     * the last shot allows it
     */
    public boolean canAttackThisKindOfUnit(AUnit otherUnit, boolean includeCooldown) {

        // Enemy is GROUND unit
        if (otherUnit.isGroundUnit()) {
            return canAttackGroundUnits() && (!includeCooldown || getGroundWeaponCooldown() == 0);
        } // Enemy is AIR unit
        else {
            return canAttackAirUnits() && (!includeCooldown || getAirWeaponCooldown() == 0);
        }
    }

/**
     * Returns <b>true</b> if this unit can attack <b>targetUnit</b> in terms of both min and max range 
     * conditions fulfilled.
     * @param safetyMargin allowed error (in tiles) applied to the max distance condition
     */
    public boolean hasRangeToAttack(AUnit targetUnit, double safetyMargin) {
        WeaponType weaponAgainstThisUnit = getWeaponAgainst(targetUnit);
        double dist = this.distanceTo(targetUnit);
        return weaponAgainstThisUnit != WeaponType.None 
                && weaponAgainstThisUnit.maxRange() <= (dist + safetyMargin) 
                && weaponAgainstThisUnit.minRange() >= dist;
    }
    
    /**
     * Returns weapon that would be used to attack given target. 
     * If no such weapon, then WeaponTypes.None will be returned.
     */
    public WeaponType getWeaponAgainst(AUnit target) {
        if (target.isGroundUnit()) {
            return getGroundWeapon();
        }
        else {
            return getAirWeapon();
        }
    }
    
    // =========================================================
    // Getters & setters
    /**
     * Returns true if given unit is currently (this frame) running from an enemy.
     */
    public boolean isRunning() {
        return running.isRunning(this);
    }

    /**
     * Returns battle group object for military units or null for non military-units (or buildings).
     */
    public Group getGroup() {
        return group;
    }

    /**
     * Assign battle group object for military units.
     */
    public void setGroup(Group group) {
        this.group = group;
    }

    /**
     * Returns AtlantisRunning object for this unit.
     */
    public AtlantisRunning getRunning() {
        return running;
    }

    /**
     * Returns true if unit is starting an attack or already in the attack frame animation.
     */
    public boolean isJustShooting() {
        return isAttackFrame() || isStartingAttack();
    }

    /**
     * Returns the frames counter (time) when the unit had been issued any command.
     */
    public int getLastUnitActionTime() {
        return lastUnitAction;
    }

    /**
     * Returns the frames counter (time) since the unit had been issued any command.
     */
    public int getLastUnitActionWasFramesAgo() {
        return AtlantisGame.getTimeFrames() - lastUnitAction;
    }

    /**
     * Indicate that in this frame unit received some command (attack, move etc).
     */
    public void setLastUnitActionNow() {
        this.lastUnitAction = AtlantisGame.getTimeFrames();
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
        _lastTimeCombatEval = AtlantisGame.getTimeFrames();
        _lastCombatEval = eval;
    }

    public double getCombatEvalCachedValueIfNotExpired() {
        if (AtlantisGame.getTimeFrames() <= _lastTimeCombatEval) {
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
     */
    public boolean runFrom(AUnit nearestEnemy) {
        if (nearestEnemy == null) {
            nearestEnemy = Select.enemyRealUnits().nearestTo(this.getPosition());
        }

        if (nearestEnemy == null) {
            return false;
        } else {
            return running.runFrom(this, nearestEnemy);
        }
    }
    
    /**
     * Returns <b>true</b> if this unit is supposed to "build" something. It will return true even if the unit
     * wasn't issued yet actual build order, but we've created ConstructionOrder and assigned it as a builder,
     * so it will return true.
     */
    public boolean isBuilder() {
        return AtlantisConstructingManager.isBuilder(this);
    }

    /**
     * If this unit is supposed to build something it will return ConstructionOrder object assigned to the
     * construction.
     */
    public ConstructionOrder getConstructionOrder() {
        return AtlantisConstructingManager.getConstructionOrderFor(this);
    }

    /**
     * Returns true if this unit belongs to the enemy.
     */
    public boolean isEnemyUnit() {
//        return getPlayer().isEnemy(AtlantisGame.getPlayerUs());
        return getPlayer().isEnemy(AtlantisGame.getPlayerUs());
    }

    /**
     * Returns true if this unit belongs to the enemy.
     */
    public boolean isEnemy() {
        return getPlayer().isEnemy(AtlantisGame.getPlayerUs());
    }
    
    /**
     * Returns true if this unit belongs to us.
     */
    public boolean isOurUnit() {
        return getPlayer().equals(AtlantisGame.getPlayerUs());
    }

    /**
     * Returns true if this unit is neutral (minerals, geysers, critters).
     */
    public boolean isNeutralUnit() {
        return getPlayer().equals(AtlantisGame.getNeutralPlayer());
    }    
    
    public int getID() {
//        return u.getID();
        return innerID;
    }

    // =========================================================
    // Method intermediates between BWMirror and Atlantis
    
    public Player getPlayer() {
        return u.getPlayer();
    }

    public int getX() {
        return u.getX();
    }

    public int getY() {
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

    public int getHitPoints() {
        return u.getHitPoints();
    }

    public int getMaxHitPoints() {
        return u.getType().maxHitPoints();
    }

    public boolean isIdle() {
        return u.isIdle() || u.getLastCommand().getUnitCommandType().equals(UnitCommandType.None);
    }

    public boolean isVisible() {
        return u.isVisible();
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

    public boolean isRepairing() {
        return u.isRepairing();
    }

    public int getGroundWeaponCooldown() {
        return u.getGroundWeaponCooldown();
    }

    public int getAirWeaponCooldown() {
        return u.getAirWeaponCooldown();
    }

    public boolean isAttackFrame() {
        return u.isAttackFrame();
    }

    public boolean isStartingAttack() {
        return u.isStartingAttack();
    }

    public List<UnitType> getTrainingQueue() {
        return u.getTrainingQueue();
    }

    public boolean isUpgrading() {
        return u.isUpgrading();
    }

    public List<Unit> getLarva() {
        return u.getLarva();
    }

    public AUnit getTarget() {
        return u.getTarget() != null ? AUnit.createFrom(u.getTarget()) : null;
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

    public boolean isMorphing() {
        return u.isMorphing();
    }

    public boolean isMoving() {
        return u.isMoving();
    }

    public boolean isAttacking() {
        return u.isAttacking();
    }

    public boolean hasPathTo(Position point) {
        return u.hasPath(point);
    }

    public boolean isTraining() {
        return u.isTraining();
    }

    public UnitCommand getLastCommand() {
        return u.getLastCommand();
    }

}
