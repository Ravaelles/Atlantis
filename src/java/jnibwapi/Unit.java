package jnibwapi;

import java.util.ArrayList;
import java.util.List;

import jnibwapi.types.OrderType;
import jnibwapi.types.OrderType.OrderTypes;
import jnibwapi.types.TechType;
import jnibwapi.types.TechType.TechTypes;
import jnibwapi.types.UnitCommandType;
import jnibwapi.types.UnitCommandType.UnitCommandTypes;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.types.UpgradeType;
import jnibwapi.types.UpgradeType.UpgradeTypes;
import atlantis.Atlantis;
import atlantis.AtlantisGame;
import atlantis.combat.group.Group;
import atlantis.combat.micro.AtlantisRunning;

/**
 * Represents a StarCraft unit.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/Unit
 */
public class Unit extends Position implements Cloneable {

	public static final int numAttributes = 123;
	public static final double TO_DEGREES = 180.0 / Math.PI;
	public static final double fixedScale = 100.0;

	private final JNIBWAPI bwapi;

	private int ID;
	private int replayID;
	private int playerID;
	private int typeID;
	// private int unitX; // @AtlantisChange
	// private int unitY; // @AtlantisChange
	// private int tileX; // @AtlantisChange
	// private int tileY; // @AtlantisChange
	private double angle;
	private double velocityX;
	private double velocityY;
	private int hitPoints;
	private int shield;
	private int energy;
	private int resources;
	private int resourceGroup;
	private int lastCommandFrame;
	private int lastCommandID;
	private int lastAttackingPlayerID;
	private int initialTypeID;
	private int initialX;
	private int initialY;
	private int initialTileX;
	private int initialTileY;
	private int initialHitPoints;
	private int initialResources;
	private int killCount;
	private int acidSporeCount;
	private int interceptorCount;
	private int scarabCount;
	private int spiderMineCount;
	private int groundWeaponCooldown;
	private int airWeaponCooldown;
	private int spellCooldown;
	private int defenseMatrixPoints;
	private int defenseMatrixTimer;
	private int ensnareTimer;
	private int irradiateTimer;
	private int lockdownTimer;
	private int maelstromTimer;
	private int orderTimer;
	private int plagueTimer;
	private int removeTimer;
	private int stasisTimer;
	private int stimTimer;
	private int buildTypeID;
	private int trainingQueueSize;
	private int researchingTechID;
	private int upgradingUpgradeID;
	private int remainingBuildTimer;
	private int remainingTrainTime;
	private int remainingResearchTime;
	private int remainingUpgradeTime;
	private int buildUnitID;
	private int targetUnitID;
	private int targetX;
	private int targetY;
	private int orderID;
	private int orderTargetID;
	private int secondaryOrderID;
	private int rallyX;
	private int rallyY;
	private int rallyUnitID;
	private int addOnID;
	private int nydusExitUnitID;
	private int transportID;
	private int loadedUnitsCount;
	private int carrierUnitID;
	private int hatcheryUnitID;
	private int larvaCount;
	private int powerUpUnitID;
	private boolean exists;
	private boolean nukeReady;
	private boolean accelerating;
	private boolean attacking;
	private boolean attackFrame;
	private boolean beingConstructed;
	private boolean beingGathered;
	private boolean beingHealed;
	private boolean blind;
	private boolean braking;
	private boolean burrowed;
	private boolean carryingGas;
	private boolean carryingMinerals;
	private boolean cloaked;
	private boolean completed;
	private boolean constructing;
	private boolean defenseMatrixed;
	private boolean detected;
	private boolean ensnared;
	private boolean following;
	private boolean gatheringGas;
	private boolean gatheringMinerals;
	private boolean hallucination;
	private boolean holdingPosition;
	private boolean idle;
	private boolean interruptable;
	private boolean invincible;
	private boolean irradiated;
	private boolean lifted;
	private boolean loaded;
	private boolean lockedDown;
	private boolean maelstrommed;
	private boolean morphing;
	private boolean moving;
	private boolean parasited;
	private boolean patrolling;
	private boolean plagued;
	private boolean repairing;
	private boolean selected;
	private boolean sieged;
	private boolean startingAttack;
	private boolean stasised;
	private boolean stimmed;
	private boolean stuck;
	private boolean training;
	private boolean underAttack;
	private boolean underDarkSwarm;
	private boolean underDisruptionWeb;
	private boolean underStorm;
	private boolean unpowered;
	private boolean upgrading;
	private boolean visible;

	public Unit(int ID, JNIBWAPI bwapi) {
		super(-666, -666); // @AtlantisChange
		this.ID = ID;
		this.bwapi = bwapi;
		atlantisInit(); // @AtlantisChange
	}

	public void setDestroyed() {
		exists = false;
	}

	public void update(int[] data, int index) {
		index++; // ID = data[index++];
		replayID = data[index++];
		playerID = data[index++];
		typeID = data[index++];

		// unitX = data[index++];
		// unitY = data[index++];

		setPixelX(data[index++]); // @AtlantisChange
		setPixelY(data[index++]); // @AtlantisChange
		index++; // @AtlantisChange
		index++; // @AtlantisChange

		// tileX = data[index++];
		// tileY = data[index++];

		angle = data[index++] / TO_DEGREES;
		velocityX = data[index++] / fixedScale;
		velocityY = data[index++] / fixedScale;
		hitPoints = data[index++];
		shield = data[index++];
		energy = data[index++];
		resources = data[index++];
		resourceGroup = data[index++];
		lastCommandFrame = data[index++];
		lastCommandID = data[index++];
		lastAttackingPlayerID = data[index++];
		initialTypeID = data[index++];
		initialX = data[index++];
		initialY = data[index++];
		initialTileX = data[index++];
		initialTileY = data[index++];
		initialHitPoints = data[index++];
		initialResources = data[index++];
		killCount = data[index++];
		acidSporeCount = data[index++];
		interceptorCount = data[index++];
		scarabCount = data[index++];
		spiderMineCount = data[index++];
		groundWeaponCooldown = data[index++];
		airWeaponCooldown = data[index++];
		spellCooldown = data[index++];
		defenseMatrixPoints = data[index++];
		defenseMatrixTimer = data[index++];
		ensnareTimer = data[index++];
		irradiateTimer = data[index++];
		lockdownTimer = data[index++];
		maelstromTimer = data[index++];
		orderTimer = data[index++];
		plagueTimer = data[index++];
		removeTimer = data[index++];
		stasisTimer = data[index++];
		stimTimer = data[index++];
		buildTypeID = data[index++];
		trainingQueueSize = data[index++];
		researchingTechID = data[index++];
		upgradingUpgradeID = data[index++];
		remainingBuildTimer = data[index++];
		remainingTrainTime = data[index++];
		remainingResearchTime = data[index++];
		remainingUpgradeTime = data[index++];
		buildUnitID = data[index++];
		targetUnitID = data[index++];
		targetX = data[index++];
		targetY = data[index++];
		orderID = data[index++];
		orderTargetID = data[index++];
		secondaryOrderID = data[index++];
		rallyX = data[index++];
		rallyY = data[index++];
		rallyUnitID = data[index++];
		addOnID = data[index++];
		nydusExitUnitID = data[index++];
		transportID = data[index++];
		loadedUnitsCount = data[index++];
		carrierUnitID = data[index++];
		hatcheryUnitID = data[index++];
		larvaCount = data[index++];
		powerUpUnitID = data[index++];
		exists = data[index++] == 1;
		nukeReady = data[index++] == 1;
		accelerating = data[index++] == 1;
		attacking = data[index++] == 1;
		attackFrame = data[index++] == 1;
		beingConstructed = data[index++] == 1;
		beingGathered = data[index++] == 1;
		beingHealed = data[index++] == 1;
		blind = data[index++] == 1;
		braking = data[index++] == 1;
		burrowed = data[index++] == 1;
		carryingGas = data[index++] == 1;
		carryingMinerals = data[index++] == 1;
		cloaked = data[index++] == 1;
		completed = data[index++] == 1;
		constructing = data[index++] == 1;
		defenseMatrixed = data[index++] == 1;
		detected = data[index++] == 1;
		ensnared = data[index++] == 1;
		following = data[index++] == 1;
		gatheringGas = data[index++] == 1;
		gatheringMinerals = data[index++] == 1;
		hallucination = data[index++] == 1;
		holdingPosition = data[index++] == 1;
		idle = data[index++] == 1;
		interruptable = data[index++] == 1;
		invincible = data[index++] == 1;
		irradiated = data[index++] == 1;
		lifted = data[index++] == 1;
		loaded = data[index++] == 1;
		lockedDown = data[index++] == 1;
		maelstrommed = data[index++] == 1;
		morphing = data[index++] == 1;
		moving = data[index++] == 1;
		parasited = data[index++] == 1;
		patrolling = data[index++] == 1;
		plagued = data[index++] == 1;
		repairing = data[index++] == 1;
		selected = data[index++] == 1;
		sieged = data[index++] == 1;
		startingAttack = data[index++] == 1;
		stasised = data[index++] == 1;
		stimmed = data[index++] == 1;
		stuck = data[index++] == 1;
		training = data[index++] == 1;
		underAttack = data[index++] == 1;
		underDarkSwarm = data[index++] == 1;
		underDisruptionWeb = data[index++] == 1;
		underStorm = data[index++] == 1;
		unpowered = data[index++] == 1;
		upgrading = data[index++] == 1;
		visible = data[index++] == 1;
	}

	@Override
	public Unit clone() {
		/*
		 * Safe to use clone for this class because it has only primitive fields and a reference to BWAPI, which should
		 * be shallow-copied. Beware when using equals or == with cloned Units as they will be considered equal (and not
		 * ==) regardless of any changes in their properties over time.
		 */
		try {
			return (Unit) super.clone();
		} catch (CloneNotSupportedException e) {
			// Should never happen, as this implements Cloneable and extends
			// Object
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns the edge-to-edge distance between the current unit and the target unit.
	 */
	public double getDistance(Unit target) {
		if (!isExists() || target == null || !target.isExists())
			return Integer.MAX_VALUE;

		if (this == target)
			return 0;

		int xDist = getLeftPixelBoundary() - (target.getRightPixelBoundary() + 1);
		if (xDist < 0) {
			xDist = target.getLeftPixelBoundary() - (getRightPixelBoundary() + 1);
			if (xDist < 0) {
				xDist = 0;
			}
		}
		int yDist = getTopPixelBoundary() - (target.getBottomPixelBoundary() + 1);
		if (yDist < 0) {
			yDist = target.getTopPixelBoundary() - (getBottomPixelBoundary() + 1);
			if (yDist < 0) {
				yDist = 0;
			}
		}
		return new Position(0, 0).distanceTo(new Position(xDist, yDist));
	}

	/**
	 * Returns the distance from the edge of the current unit to the target position.
	 */
	public double getDistance(Position target) {
		if (!isExists())
			return Integer.MAX_VALUE;
		int xDist = getLeftPixelBoundary() - (target.getPX() + 1);
		if (xDist < 0) {
			xDist = target.getPX() - (getRightPixelBoundary() + 1);
			if (xDist < 0) {
				xDist = 0;
			}
		}
		int yDist = getTopPixelBoundary() - (target.getPY() + 1);
		if (yDist < 0) {
			yDist = target.getPY() - (getBottomPixelBoundary() + 1);
			if (yDist < 0) {
				yDist = 0;
			}
		}
		return new Position(0, 0).distanceTo(new Position(xDist, yDist));
	}

	/** The top left corner of the unit's collision boundary. */
	public Position getTopLeft() {
		return new Position(getLeftPixelBoundary(), getTopPixelBoundary());
	}

	/** The bottom right corner of the unit's collision boundary. */
	public Position getBottomRight() {
		return new Position(getRightPixelBoundary(), getBottomPixelBoundary());
	}

	public int getLeftPixelBoundary() {
		return getPX() - getType().getDimensionLeft();
	}

	public int getTopPixelBoundary() {
		return getPY() - getType().getDimensionUp();
	}

	public int getRightPixelBoundary() {
		return getPX() + getType().getDimensionRight();
	}

	public int getBottomPixelBoundary() {
		return getPY() + getType().getDimensionDown();
	}

	// ------------------------------ FIELD ACCESSOR METHODS
	// ------------------------------ //

	public int getID() {
		return ID;
	}

	public int getReplayID() {
		return replayID;
	}

	@Deprecated
	public int getPlayerID() {
		return playerID;
	}

	public Player getPlayer() {
		return bwapi.getPlayer(playerID);
	}

	@Deprecated
	public int getTypeID() {
		return typeID;
	}

	public UnitType getType() {
		return UnitTypes.getUnitType(typeID);
	}

	// /** Gives the position of the <b>center</b> of the unit. */
	// public Position getPosition() {
	// // return new Position(unitX, unitY); // @AtlantisChange
	// return this;
	// }

	// /** @deprecated use {@link #getPosition()} */
	// @Deprecated
	// public int getX() {
	// return unitX;
	// }
	//
	// /** @deprecated use {@link #getPosition()} */
	// @Deprecated
	// public int getY() {
	// return unitY;
	// }

	// /**
	// * Returns the position of the top-left build tile occupied by the unit. Most useful for buildings. Always
	// * above-left of {@link #getPosition()} and above-left or equal to {@link #getTopLeft()}
	// */
	// public Position getTilePosition() {
	// return new Position(tileX, tileY, PosType.BUILD);
	// }

	public double getAngle() {
		return angle;
	}

	public double getVelocityX() {
		return velocityX;
	}

	public double getVelocityY() {
		return velocityY;
	}

	public int getHitPoints() {
		return hitPoints;
	}

	public int getShields() {
		return shield;
	}

	public int getEnergy() {
		return energy;
	}

	public int getResources() {
		return resources;
	}

	public int getResourceGroup() {
		return resourceGroup;
	}

	public int getLastCommandFrame() {
		return lastCommandFrame;
	}

	@Deprecated
	public int getLastCommandID() {
		return lastCommandID;
	}

	public UnitCommandType getLastCommand() {
		return UnitCommandTypes.getUnitCommandType(lastCommandID);
	}

	@Deprecated
	public int getLastAttackingPlayerID() {
		return lastAttackingPlayerID;
	}

	public Player getLastAttackingPlayer() {
		return bwapi.getPlayer(lastAttackingPlayerID);
	}

	@Deprecated
	public int getInitialTypeID() {
		return initialTypeID;
	}

	public UnitType getInitialType() {
		return UnitTypes.getUnitType(initialTypeID);
	}

	/** @deprecated use {@link #getInitialPosition()} */
	@Deprecated
	public int getInitialX() {
		return initialX;
	}

	/** @deprecated use {@link #getInitialPosition()} */
	@Deprecated
	public int getInitialY() {
		return initialY;
	}

	/** @deprecated use {@link #getInitialPosition()} */
	@Deprecated
	public int getInitialTileX() {
		return initialTileX;
	}

	/** @deprecated use {@link #getInitialPosition()} */
	@Deprecated
	public int getInitialTileY() {
		return initialTileY;
	}

	public Position getInitialPosition() {
		return new Position(initialX, initialY);
	}

	public int getInitialHitPoints() {
		return initialHitPoints;
	}

	public int getInitialResources() {
		return initialResources;
	}

	public int getKillCount() {
		return killCount;
	}

	public int getAcidSporeCount() {
		return acidSporeCount;
	}

	public int getInterceptorCount() {
		return interceptorCount;
	}

	public List<Unit> getInterceptors() {
		List<Unit> interceptors = new ArrayList<>(8);
		for (int id : bwapi.getInterceptors(ID)) {
			interceptors.add(bwapi.getUnit(id));
		}
		return interceptors;
	}

	public int getScarabCount() {
		return scarabCount;
	}

	public int getSpiderMineCount() {
		return spiderMineCount;
	}

	public int getGroundWeaponCooldown() {
		return groundWeaponCooldown;
	}

	public int getAirWeaponCooldown() {
		return airWeaponCooldown;
	}

	public int getSpellCooldown() {
		return spellCooldown;
	}

	public int getDefenseMatrixPoints() {
		return defenseMatrixPoints;
	}

	public int getDefenseMatrixTimer() {
		return defenseMatrixTimer;
	}

	public int getEnsnareTimer() {
		return ensnareTimer;
	}

	public int getIrradiateTimer() {
		return irradiateTimer;
	}

	public int getLockdownTimer() {
		return lockdownTimer;
	}

	public int getMaelstromTimer() {
		return maelstromTimer;
	}

	public int getOrderTimer() {
		return orderTimer;
	}

	public int getPlagueTimer() {
		return plagueTimer;
	}

	public int getRemoveTimer() {
		return removeTimer;
	}

	public int getStasisTimer() {
		return stasisTimer;
	}

	public int getStimTimer() {
		return stimTimer;
	}

	@Deprecated
	public int getBuildTypeID() {
		return buildTypeID;
	}

	public UnitType getBuildType() {
		return UnitTypes.getUnitType(buildTypeID);
	}

	public int getTrainingQueueSize() {
		return trainingQueueSize;
	}

	@Deprecated
	public int getResearchingTechID() {
		return researchingTechID;
	}

	public TechType getTech() {
		return TechTypes.getTechType(researchingTechID);
	}

	@Deprecated
	public int getUpgradingUpgradeID() {
		return upgradingUpgradeID;
	}

	public UpgradeType getUpgrade() {
		return UpgradeTypes.getUpgradeType(upgradingUpgradeID);
	}

	public int getRemainingBuildTimer() {
		return remainingBuildTimer;
	}

	public int getRemainingTrainTime() {
		return remainingTrainTime;
	}

	public int getRemainingResearchTime() {
		return remainingResearchTime;
	}

	public int getRemainingUpgradeTime() {
		return remainingUpgradeTime;
	}

	@Deprecated
	public int getBuildUnitID() {
		return buildUnitID;
	}

	public Unit getBuildUnit() {
		return bwapi.getUnit(buildUnitID);
	}

	@Deprecated
	public int getTargetUnitID() {
		return targetUnitID;
	}

	public Unit getTarget() {
		return bwapi.getUnit(targetUnitID);
	}

	@Deprecated
	public int getTargetX() {
		return targetX;
	}

	@Deprecated
	public int getTargetY() {
		return targetY;
	}

	public Position getTargetPosition() {
		return new Position(targetX, targetY);
	}

	@Deprecated
	public int getOrderID() {
		return orderID;
	}

	public OrderType getOrder() {
		return OrderTypes.getOrderType(orderID);
	}

	@Deprecated
	public int getOrderTargetUnitID() {
		return orderTargetID;
	}

	public Unit getOrderTarget() {
		return bwapi.getUnit(orderTargetID);
	}

	@Deprecated
	public int getSecondaryOrderID() {
		return secondaryOrderID;
	}

	public OrderType getSecondaryOrder() {
		return OrderTypes.getOrderType(secondaryOrderID);
	}

	@Deprecated
	public int getRallyX() {
		return rallyX;
	}

	@Deprecated
	public int getRallyY() {
		return rallyY;
	}

	public Position getRallyPosition() {
		return new Position(rallyX, rallyY);
	}

	@Deprecated
	public int getRallyUnitID() {
		return rallyUnitID;
	}

	public Unit getRallyUnit() {
		return bwapi.getUnit(rallyUnitID);
	}

	@Deprecated
	public int getAddOnUnitID() {
		return addOnID;
	}

	public Unit getAddon() {
		return bwapi.getUnit(addOnID);
	}

	@Deprecated
	public int getNydusExitUnitID() {
		return nydusExitUnitID;
	}

	public Unit getNydusExit() {
		return bwapi.getUnit(nydusExitUnitID);
	}

	@Deprecated
	public int getTransportUnitID() {
		return transportID;
	}

	public Unit getTransport() {
		return bwapi.getUnit(transportID);
	}

	@Deprecated
	public int getLoadedUnitsCount() {
		return loadedUnitsCount;
	}

	public List<Unit> getLoadedUnits() {
		List<Unit> units = new ArrayList<Unit>();
		for (int id : bwapi.getLoadedUnits(ID)) {
			units.add(bwapi.getUnit(id));
		}
		return units;
	}

	@Deprecated
	public int getCarrierUnitID() {
		return carrierUnitID;
	}

	public Unit getCarrier() {
		return bwapi.getUnit(carrierUnitID);
	}

	@Deprecated
	public int getHatcheryUnitID() {
		return hatcheryUnitID;
	}

	public Unit getHatchery() {
		return bwapi.getUnit(hatcheryUnitID);
	}

	@Deprecated
	public int getLarvaCount() {
		return larvaCount;
	}

	public List<Unit> getLarva() {
		List<Unit> larva = new ArrayList<>(3);
		for (int id : bwapi.getLarva(ID)) {
			larva.add(bwapi.getUnit(id));
		}
		return larva;
	}

	@Deprecated
	public int getPowerUpUnitID() {
		return powerUpUnitID;
	}

	public Unit getPowerUp() {
		return bwapi.getUnit(powerUpUnitID);
	}

	public boolean isExists() {
		return exists;
	}

	public boolean isNukeReady() {
		return nukeReady;
	}

	public boolean isAccelerating() {
		return accelerating;
	}

	public boolean isAttacking() {
		return attacking;
	}

	public boolean isAttackFrame() {
		return attackFrame;
	}

	public boolean isBeingConstructed() {
		return beingConstructed;
	}

	public boolean isBeingGathered() {
		return beingGathered;
	}

	public boolean isBeingHealed() {
		return beingHealed;
	}

	public boolean isBlind() {
		return blind;
	}

	public boolean isBraking() {
		return braking;
	}

	public boolean isBurrowed() {
		return burrowed;
	}

	public boolean isCarryingGas() {
		return carryingGas;
	}

	public boolean isCarryingMinerals() {
		return carryingMinerals;
	}

	public boolean isCloaked() {
		return cloaked;
	}

	public boolean isCompleted() {
		return completed;
	}

	public boolean isConstructing() {
		return constructing;
	}

	public boolean isDefenseMatrixed() {
		return defenseMatrixed;
	}

	public boolean isDetected() {
		return detected;
	}

	public boolean isEnsnared() {
		return ensnared;
	}

	public boolean isFollowing() {
		return following;
	}

	public boolean isGatheringGas() {
		return gatheringGas;
	}

	public boolean isGatheringMinerals() {
		return gatheringMinerals;
	}

	public boolean isHallucination() {
		return hallucination;
	}

	public boolean isHoldingPosition() {
		return holdingPosition;
	}

	public boolean isIdle() {
		return idle;
	}

	public boolean isInterruptable() {
		return interruptable;
	}

	public boolean isInvincible() {
		return invincible;
	}

	public boolean isIrradiated() {
		return irradiated;
	}

	public boolean isLifted() {
		return lifted;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public boolean isLockedDown() {
		return lockedDown;
	}

	public boolean isMaelstrommed() {
		return maelstrommed;
	}

	public boolean isMorphing() {
		return morphing;
	}

	public boolean isMoving() {
		return moving;
	}

	public boolean isParasited() {
		return parasited;
	}

	public boolean isPatrolling() {
		return patrolling;
	}

	public boolean isPlagued() {
		return plagued;
	}

	public boolean isRepairing() {
		return repairing;
	}

	public boolean isSelected() {
		return selected;
	}

	public boolean isSieged() {
		return sieged;
	}

	public boolean isStartingAttack() {
		return startingAttack;
	}

	public boolean isStasised() {
		return stasised;
	}

	public boolean isStimmed() {
		return stimmed;
	}

	public boolean isStuck() {
		return stuck;
	}

	public boolean isTraining() {
		return training;
	}

	public boolean isUnderAttack() {
		return underAttack;
	}

	public boolean isUnderDarkSwarm() {
		return underDarkSwarm;
	}

	public boolean isUnderDisruptionWeb() {
		return underDisruptionWeb;
	}

	public boolean isUnderStorm() {
		return underStorm;
	}

	public boolean isUnpowered() {
		return unpowered;
	}

	public boolean isUpgrading() {
		return upgrading;
	}

	public boolean isVisible() {
		return visible;
	}

	// ------------------------------ UNIT COMMANDS
	// ------------------------------ //
	public boolean attack(Position p, boolean queued) {

		// @AtlantisChange
		// Do not execute the same command twice
		if (isAttacking() && getTargetPosition() != null && getTargetPosition().equals(p)) {
			return false; // Ignore this command request
		}

		setTooltip("Attack position");

		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Attack_Move, p, queued));
	}

	public boolean attackUnit(Unit target, boolean queued) {

		// @AtlantisChange
		// Do not execute the same command twice
		if (isAttacking() && getTarget() != null && getTarget().equals(target)) {
			return false; // Ignore this command request
		}

		setTooltip("Attack " + target.getShortName());

		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Attack_Unit, target, queued));
	}

	public boolean build(Position p, UnitType type) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Build, p, type.getID()));
	}

	public boolean buildAddon(UnitType type) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Build_Addon, type.getID()));
	}

	public boolean train(UnitType type) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Train, type.getID()));
	}

	public boolean morph(UnitType type) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Morph, type.getID()));
	}

	public boolean research(TechType tech) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Research, tech.getID()));
	}

	public boolean upgrade(UpgradeType upgrade) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Upgrade, upgrade.getID()));
	}

	public boolean setRallyPoint(Position p) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Set_Rally_Position, p));
	}

	public boolean setRallyPoint(Unit target) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Set_Rally_Unit, target));
	}

	public boolean move(Position p, boolean queued) {

		// @AtlantisChange
		// Do not execute the same command twice
		if (isMoving() && getTargetPosition() != null && getTargetPosition().equals(p)) {
			return false; // Ignore this command request
		}

		setTooltip("Move");

		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Move, p, queued));
	}

	public boolean patrol(Position p, boolean queued) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Patrol, p, queued));
	}

	public boolean holdPosition(boolean queued) {

		// @AtlantisChange
		// Do not execute the same command twice
		if (isHoldingPosition()) {
			return false; // Ignore this command request
		}

		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Hold_Position, queued));
	}

	public boolean stop(boolean queued) {

		// @AtlantisChange
		// Do not execute the same command twice
		if (isIdle()) {
			return false; // Ignore this command request
		}

		setTooltip("Stop");

		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Stop, queued));
	}

	public boolean follow(Unit target, boolean queued) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Follow, target, queued));
	}

	public boolean gather(Unit target, boolean queued) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Gather, target, queued));
	}

	public boolean returnCargo(boolean queued) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Return_Cargo, queued));
	}

	public boolean repair(Unit target, boolean queued) {

		// @AtlantisChange
		// Do not execute the same command twice
		if (isRepairing()) {
			return false; // Ignore this command request
		}

		setTooltip("Repair");

		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Repair, target, queued));
	}

	public boolean burrow() {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Burrow));
	}

	public boolean unburrow() {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Unburrow));
	}

	public boolean cloak() {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Cloak));
	}

	public boolean decloak() {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Decloak));
	}

	public boolean siege() {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Siege));
	}

	public boolean unsiege() {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Unsiege));
	}

	public boolean lift() {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Lift));
	}

	public boolean land(Position p) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Land, p));
	}

	public boolean load(Unit target, boolean queued) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Load, target, queued));
	}

	public boolean unload(Unit target) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Unload, target));
	}

	public boolean unloadAll(boolean queued) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Unload_All, queued));
	}

	public boolean unloadAll(Position p, boolean queued) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Unload_All_Position, p, queued));
	}

	public boolean rightClick(Position p, boolean queued) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Right_Click_Position, p, queued));
	}

	public boolean rightClick(Unit target, boolean queued) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Right_Click_Unit, target, queued));
	}

	public boolean haltConstruction() {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Halt_Construction));
	}

	public boolean cancelConstruction() {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Cancel_Construction));
	}

	public boolean cancelAddon() {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Cancel_Addon));
	}

	public boolean cancelTrain(int slot) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Cancel_Train_Slot, slot));
	}

	/** Remove the last unit from the training queue. */
	public boolean cancelTrain() {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Cancel_Train, -2));
	}

	public boolean cancelMorph() {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Cancel_Morph));
	}

	public boolean cancelResearch() {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Cancel_Research));
	}

	public boolean cancelUpgrade() {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Cancel_Upgrade));
	}

	public boolean useTech(TechType tech) {
		UnitCommandType uct = UnitCommandTypes.Use_Tech;
		if (tech == TechTypes.Burrowing) {
			if (isBurrowed())
				uct = UnitCommandTypes.Unburrow;
			else
				uct = UnitCommandTypes.Burrow;
		} else if (tech == TechTypes.Cloaking_Field || tech == TechTypes.Personnel_Cloaking) {
			if (isCloaked())
				uct = UnitCommandTypes.Decloak;
			else
				uct = UnitCommandTypes.Cloak;
		} else if (tech == TechTypes.Tank_Siege_Mode) {
			if (isSieged())
				uct = UnitCommandTypes.Unsiege;
			else
				uct = UnitCommandTypes.Siege;
		}
		return bwapi.issueCommand(new UnitCommand(this, uct, tech.getID()));
	}

	public boolean useTech(TechType tech, Position p) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Use_Tech_Position, p, tech.getID()));
	}

	public boolean useTech(TechType tech, Unit target) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Use_Tech_Unit, target, tech.getID()));
	}

	public boolean placeCOP(Position p) {
		return bwapi.issueCommand(new UnitCommand(this, UnitCommandTypes.Place_COP, p));
	}

	// ------------------------------ HASHCODE & EQUALS
	// ------------------------------ //

	@Override
	public int hashCode() {
		return ID;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Unit other = (Unit) obj;
		if (ID != other.ID)
			return false;
		return true;
	}

	// =========================================================
	// ===== Start of ATLANTIS CODE ============================
	// =========================================================

	private Group group = null;
	private AtlantisRunning running = new AtlantisRunning(this);

	private boolean cached_repairableMechanically = false;
	private boolean cached_healable = false;

	// =========================================================
	// Atlantis constructor

	private void atlantisInit() {
		cached_repairableMechanically = isBuilding() || isVehicle();
		cached_healable = isInfantry() || isWorker();
	}

	// =========================================================
	// Important methods

	/**
	 * Unit will move by given distance (in build tiles) from given position.
	 */
	public void moveAwayFrom(Position position, double moveDistance) {
		int dx = position.getPX() - getPX();
		int dy = position.getPY() - getPY();
		double vectorLength = Math.sqrt(dx * dx + dy * dy);
		double modifier = (moveDistance * 32) / vectorLength;
		dx = (int) (dx * modifier);
		dy = (int) (dy * modifier);

		Position newPosition = new Position(getPX() - dx, getPY() - dy, PosType.PIXEL);

		move(newPosition, false);
		setTooltip("Move away");
	}

	public static Unit getByID(int unitID) {
		for (Unit unit : Atlantis.getBwapi().getAllUnits()) {
			if (unit.getID() == unitID) {
				return unit;
			}
		}

		return null;
	}

	@Override
	public String toString() {
		// Position position = getPosition();
		Position position = this;
		String toString = getType().getName();
		toString = toString.replace("Terran ", "").replace("Protoss ", "").replace("Zerg ", "")
				.replace("Resource ", "");
		toString += " #" + ID + " at [" + position.getBX() + "," + position.getBY() + "]";
		return toString;
	}

	// =========================================================
	// Generic methods

	public boolean isAlive() {
		return exists;
	}

	public boolean canBeHealed() {
		return cached_repairableMechanically || cached_healable;
	}

	public boolean isRepairableMechanically() {
		return cached_repairableMechanically;
	}

	public boolean isHealable() {
		return cached_healable;
	}

	public boolean isBuilding() {
		return getType().isBuilding();
	}

	public boolean isWorker() {
		return getType().isWorker();
	}

	public boolean isBase() {
		return isType(UnitTypes.Terran_Command_Center, UnitTypes.Protoss_Nexus, UnitTypes.Zerg_Hatchery,
				UnitTypes.Zerg_Lair, UnitTypes.Zerg_Hive);
	}

	public boolean isInfantry() {
		return getType().isOrganic();
	}

	public boolean isVehicle() {
		return getType().isMechanical();
	}

	private boolean isAirUnit() {
		return getType().isFlyer();
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

	public boolean isType(UnitType type) {
		return getType().equals(type);
	}

	public boolean isType(UnitType... types) {
		return getType().isType(types);
	}

	public boolean isFullyHealthy() {
		return getHitPoints() >= getType().getMaxHitPoints();
	}

	public int getHPPercent() {
		return 100 * getHitPoints() / getType().getMaxHitPoints();
	}

	public boolean isWounded() {
		return getHitPoints() < getMaxHP();
	}

	public int getHP() {
		return hitPoints;
	}

	public int getMaxHP() {
		return getType().getMaxHitPoints();
	}

	public String getShortName() {
		return getType().getShortName();
	}

	/**
	 * Returns max shoot range (in build tiles) of this unit against land targets.
	 */
	public double getShootRangeGround() {
		return getType().getGroundWeapon().getMaxRange() / 32;
	}

	/**
	 * Returns max shoot range (in build tiles) of this unit against land targets.
	 */
	public double getShootRangeAir() {
		return getType().getAirWeapon().getMaxRange() / 32;
	}

	/**
	 * Returns max shoot range (in build tiles) of this unit against given <b>opponentUnit</b>.
	 */
	public double getShootRangeAgainst(Unit opponentUnit) {
		if (opponentUnit.isAirUnit()) {
			return getType().getAirWeapon().getMaxRange() / 32;
		} else {
			return getType().getGroundWeapon().getMaxRange() / 32;
		}
	}

	/**
	 * Indicates that this unit should be running from given enemy unit.
	 */
	public void runFrom(Unit nearestEnemy) {
		getRunning().runFrom(nearestEnemy);
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

	public boolean isSpiderMine() {
		return getType().equals(UnitTypes.Terran_Vulture_Spider_Mine);
	}

	// =========================================================
	// Getters & setters

	/**
	 * Returns true if given unit is currently (this frame) running from an enemy.
	 */
	public boolean isRunning() {
		return running.isRunning();
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	/**
	 * Returns AtlantisRunning object for this unit.
	 */
	public AtlantisRunning getRunning() {
		return running;
	}

}
