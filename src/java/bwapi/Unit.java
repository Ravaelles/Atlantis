package bwapi;

import atlantis.Atlantis;
import atlantis.AtlantisGame;
import atlantis.combat.group.Group;
import atlantis.combat.micro.AtlantisRunning;
import atlantis.constructing.AtlantisConstructingManager;
import atlantis.constructing.ConstructionOrder;
import atlantis.debug.tooltip.Tooltip;
import atlantis.debug.tooltip.TooltipManager;
import atlantis.enemy.AtlantisEnemyUnits;
import atlantis.util.PositionUtil;
import atlantis.wrappers.Select;
import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import bwapi.PositionedObject;

/**
 * The Unit class is used to get information about individual units as well as issue orders to units. Each
 * unit in the game has a unique Unit object, and Unit objects are not deleted until the end of the match (so
 * you don't need to worry about unit pointers becoming invalid). Every Unit in the game is either accessible
 * or inaccessible. To determine if an AI can access a particular unit, BWAPI checks to see if
 * Flag::CompleteMapInformation is enabled. So there are two cases to consider - either the flag is enabled,
 * or it is disabled: If Flag::CompleteMapInformation is disabled, then a unit is accessible if and only if it
 * is visible. Note Some properties of visible enemy units will not be made available to the AI (such as the
 * contents of visible enemy dropships). If a unit is not visible, UnitInterface::exists will return false,
 * regardless of whether or not the unit exists. This is because absolutely no state information on invisible
 * enemy units is made available to the AI. To determine if an enemy unit has been destroyed, the AI must
 * watch for AIModule::onUnitDestroy messages from BWAPI, which is only called for visible units which get
 * destroyed. If Flag::CompleteMapInformation is enabled, then all units that exist in the game are
 * accessible, and UnitInterface::exists is accurate for all units. Similarly AIModule::onUnitDestroy messages
 * are generated for all units that get destroyed, not just visible ones. If a Unit is not accessible, then
 * only the getInitial__ functions will be available to the AI. However for units that were owned by the
 * player, getPlayer and getType will continue to work for units that have been destroyed.
 */
public class Unit extends PositionedObject implements Comparable<Unit> {

    /**
     * Retrieves a unique identifier for this unit. Returns An integer containing the unit's identifier. See
     * also getReplayID
     */
    public int getID() {
        return getID_native(pointer);
    }

    /**
     * Checks if the Unit exists in the view of the BWAPI player. This is used primarily to check if BWAPI has
     * access to a specific unit, or if the unit is alive. This function is more general and would be
     * synonymous to an isAlive function if such a function were necessary. Return values true If the unit
     * exists on the map and is visible according to BWAPI. false If the unit is not accessible or the unit is
     * dead. In the event that this function returns false, there are two cases to consider: You own the unit.
     * This means the unit is dead. Another player owns the unit. This could either mean that you don't have
     * access to the unit or that the unit has died. You can specifically identify dead units by polling
     * onUnitDestroy. See also isVisible, isCompleted
     */
    public boolean exists() {
        return exists_native(pointer);
    }

    /**
     * Retrieves the unit identifier for this unit as seen in replay data. Note This is only available if
     * Flag::CompleteMapInformation is enabled. Returns An integer containing the replay unit identifier. See
     * also getID
     */
    public int getReplayID() {
        return getReplayID_native(pointer);
    }

    /**
     * Retrieves the player that owns this unit. Return values Game::neutral() If the unit is a neutral unit
     * or inaccessible. Returns The owning Player interface object.
     */
    public Player getPlayer() {
        return getPlayer_native(pointer);
    }

    /**
     * Retrieves the unit's type. Return values UnitType::Unknown if this unit is inaccessible or cannot be
     * determined. Returns A UnitType objects representing the unit's type. See also getInitialType
     */
    public UnitType getType() {
        return getType_native(pointer);
    }

    /**
     * Retrieves the unit's position from the upper left corner of the map in pixels. The position returned is
     * roughly the center if the unit. Note The unit bounds are defined as this value plus/minus the values of
     * UnitType::dimensionLeft, UnitType::dimensionUp, UnitType::dimensionRight, and UnitType::dimensionDown,
     * which is conveniently expressed in UnitInterface::getLeft, UnitInterface::getTop,
     * UnitInterface::getRight, and UnitInterface::getBottom respectively. Return values Positions::Unknown if
     * this unit is inaccessible. Returns Position object representing the unit's current position. See also
     * getTilePosition, getInitialPosition, getLeft, getTop
     */
    public Position getPosition() {
        return getPosition_native(pointer);
    }

    /**
     * Retrieves the unit's build position from the upper left corner of the map in tiles. Note : This tile
     * position is the tile that is at the top left corner of the structure. Return values
     * TilePositions::Unknown if this unit is inaccessible. Returns TilePosition object representing the
     * unit's current tile position. See also getPosition, getInitialTilePosition
     */
    public TilePosition getTilePosition() {
        return getTilePosition_native(pointer);
    }

    /**
     * Retrieves the unit's facing direction in radians. Note A value of 0.0 means the unit is facing east.
     * Returns A double with the angle measure in radians.
     */
    public double getAngle() {
        return getAngle_native(pointer);
    }

    /**
     * Retrieves the x component of the unit's velocity, measured in pixels per frame. Returns A double that
     * represents the velocity's x component. See also getVelocityY
     */
    public double getVelocityX() {
        return getVelocityX_native(pointer);
    }

    /**
     * Retrieves the y component of the unit's velocity, measured in pixels per frame. Returns A double that
     * represents the velocity's y component. See also getVelocityX
     */
    public double getVelocityY() {
        return getVelocityY_native(pointer);
    }

    /**
     * Retrieves the Region that the center of the unit is in. Return values nullptr If the unit is
     * inaccessible. Returns The Region object that contains this unit. Example Unitset myUnits =
     * Broodwar->self()->getUnits(); for ( auto u = myUnits.begin(); u != myUnits.end(); ++u ) { if (
     * u->isFlying() && u->isUnderAttack() ) // implies exists and isCompleted { Region r = u->getRegion(); if
     * ( r ) u->move(r->getClosestInaccessibleRegion()); // Retreat to inaccessible region } } Note If this
     * function returns a successful state, then the following function calls will also return a successful
     * state: exists
     */
    public Region getRegion() {
        return getRegion_native(pointer);
    }

    /**
     * Retrieves the X coordinate of the unit's left boundary, measured in pixels from the left side of the
     * map. Returns An integer representing the position of the left side of the unit. See also getTop,
     * getRight, getBottom
     */
    public int getLeft() {
        return getLeft_native(pointer);
    }

    /**
     * Retrieves the Y coordinate of the unit's top boundary, measured in pixels from the top of the map.
     * Returns An integer representing the position of the top side of the unit. See also getLeft, getRight,
     * getBottom
     */
    public int getTop() {
        return getTop_native(pointer);
    }

    /**
     * Retrieves the X coordinate of the unit's right boundary, measured in pixels from the left side of the
     * map. Returns An integer representing the position of the right side of the unit. See also getLeft,
     * getTop, getBottom
     */
    public int getRight() {
        return getRight_native(pointer);
    }

    /**
     * Retrieves the Y coordinate of the unit's bottom boundary, measured in pixels from the top of the map.
     * Returns An integer representing the position of the bottom side of the unit. See also getLeft, getTop,
     * getRight
     */
    public int getBottom() {
        return getBottom_native(pointer);
    }

    /**
     * Retrieves the unit's current Hit Points (HP) as seen in the game. Returns An integer representing the
     * amount of hit points a unit currently has. Note In Starcraft, a unit usually dies when its HP reaches
     * 0. It is possible however, to have abnormal HP values in the Use Map Settings game type and as the
     * result of a hack over Battle.net. Such values include units that have 0 HP (can't be killed
     * conventionally) or even negative HP (death in one hit). See also UnitType::maxHitPoints, getShields,
     * getInitialHitPoints
     */
    public int getHitPoints() {
        return getHitPoints_native(pointer);
    }

    /**
     * Retrieves the unit's current Shield Points (Shields) as seen in the game. Returns An integer
     * representing the amount of shield points a unit currently has. See also UnitType::maxShields,
     * getHitPoints
     */
    public int getShields() {
        return getShields_native(pointer);
    }

    /**
     * Retrieves the unit's current Energy Points (Energy) as seen in the game. Returns An integer
     * representing the amount of energy points a unit currently has. Note Energy is required in order for
     * units to use abilities. See also UnitType::maxEnergy
     */
    public int getEnergy() {
        return getEnergy_native(pointer);
    }

    /**
     * Retrieves the resource amount from a resource container, such as a Mineral Field and Vespene Geyser. If
     * the unit is inaccessible, then the last known resource amount is returned. Returns An integer
     * representing the last known amount of resources remaining in this resource. See also
     * getInitialResources
     */
    public int getResources() {
        return getResources_native(pointer);
    }

    /**
     * Retrieves a grouping index from a resource container. Other resource containers of the same value are
     * considered part of one expansion location (group of resources that are close together). Note This
     * grouping method is explicitly determined by Starcraft itself and is used only by the internal AI.
     * Returns An integer with an identifier between 0 and 250 that determine which resources are grouped
     * together to form an expansion.
     */
    public int getResourceGroup() {
        return getResourceGroup_native(pointer);
    }

    /**
     * Retrieves the distance between this unit and a target. Note Distance is calculated from the edge of
     * this unit, using Starcraft's own distance algorithm. Parameters target A Position or a Unit to
     * calculate the distance to. If it is a unit, then it will calculate the distance to the edge of the
     * target unit. Returns An integer representation of the number of pixels between this unit and the
     * target.
     */
    public int getDistance(Position target) {
        return getDistance_native(pointer, target);
    }

    public int getDistance(Unit target) {
        return getDistance_native(pointer, target);
    }

    public int getDistance(PositionOrUnit target) {
        return getDistance_native(pointer, target);
    }

    /**
     * Using data provided by Starcraft, checks if there is a path available from this unit to the given
     * target. Note This function only takes into account the terrain data, and does not include buildings
     * when determining if a path is available. However, the complexity of this function is constant ( O(1) ),
     * and no extensive calculations are necessary. If the current unit is an air unit, then this function
     * will always return true. Parameters target A Position or a Unit that is used to determine if this unit
     * has a path to the target. Return values true If there is a path between this unit and the target. false
     * If the target is on a different piece of land than this one (such as an island).
     */
    public boolean hasPath(Position target) {
        return hasPath_native(pointer, target);
    }

    public boolean hasPath(Unit target) {
        return hasPath_native(pointer, target);
    }

    public boolean hasPath(PositionOrUnit target) {
        return hasPath_native(pointer, target);
    }

    /**
     * Retrieves the frame number that sent the last successful command. Note This value is comparable to
     * Game::getFrameCount. Returns The frame number that sent the last successfully processed command to
     * BWAPI. See also Game::getFrameCount, getLastCommand
     */
    public int getLastCommandFrame() {
        return getLastCommandFrame_native(pointer);
    }

    /**
     * Retrieves the last successful command that was sent to BWAPI. Returns A UnitCommand object containing
     * information about the command that was processed. See also getLastCommandFrame
     */
    public UnitCommand getLastCommand() {
        return getLastCommand_native(pointer);
    }

    /**
     * Retrieves the Player that last attacked this unit. Returns Player interface object representing the
     * player that last attacked this unit. Return values nullptr If this unit was not attacked. Note If this
     * function returns a successful state, then the following function calls will also return a successful
     * state: exists()
     */
    public Player getLastAttackingPlayer() {
        return getLastAttackingPlayer_native(pointer);
    }

    /**
     * Retrieves the initial type of the unit. This is the type that the unit starts as in the beginning of
     * the game. This is used to access the types of static neutral units such as mineral fields when they are
     * not visible. Returns UnitType of this unit as it was when it was created. Return values
     * UnitType::Unknown if this unit was not a static neutral unit in the beginning of the game.
     */
    public UnitType getInitialType() {
        return getInitialType_native(pointer);
    }

    /**
     * Retrieves the initial position of this unit. This is the position that the unit starts at in the
     * beginning of the game. This is used to access the positions of static neutral units such as mineral
     * fields when they are not visible. Returns Position indicating the unit's initial position when it was
     * created. Return values Positions::Unknown if this unit was not a static neutral unit in the beginning
     * of the game.
     */
    public Position getInitialPosition() {
        return getInitialPosition_native(pointer);
    }

    /**
     * Retrieves the initial build tile position of this unit. This is the tile position that the unit starts
     * at in the beginning of the game. This is used to access the tile positions of static neutral units such
     * as mineral fields when they are not visible. The build tile position corresponds to the upper left
     * corner of the unit. Returns TilePosition indicating the unit's initial tile position when it was
     * created. Return values TilePositions::Unknown if this unit was not a static neutral unit in the
     * beginning of the game.
     */
    public TilePosition getInitialTilePosition() {
        return getInitialTilePosition_native(pointer);
    }

    /**
     * Retrieves the amount of hit points that this unit started off with at the beginning of the game. The
     * unit must be neutral. Returns Number of hit points that this unit started with. Return values 0 if this
     * unit was not a neutral unit at the beginning of the game. Note : It is possible for the unit's initial
     * hit points to differ from the maximum hit points. See also Game::getStaticNeutralUnits
     */
    public int getInitialHitPoints() {
        return getInitialHitPoints_native(pointer);
    }

    /**
     * Retrieves the amount of resources contained in the unit at the beginning of the game. The unit must be
     * a neutral resource container. Returns Amount of resources that this unit started with. Return values 0
     * if this unit was not a neutral unit at the beginning of the game, or if this unit does not contain
     * resources. It is possible that the unit simply contains 0 resources. See also
     * Game::getStaticNeutralUnits
     */
    public int getInitialResources() {
        return getInitialResources_native(pointer);
    }

    /**
     * Retrieves the number of units that this unit has killed in total. Note The maximum amount of recorded
     * kills per unit is 255. Returns integer indicating this unit's kill count.
     */
    public int getKillCount() {
        return getKillCount_native(pointer);
    }

    /**
     * Retrieves the number of acid spores that this unit is inflicted with. Returns Number of acid spores on
     * this unit.
     */
    public int getAcidSporeCount() {
        return getAcidSporeCount_native(pointer);
    }

    /**
     * Retrieves the number of interceptors that this unit manages. This function is only for the Carrier.
     * Returns Number of interceptors in this unit.
     */
    public int getInterceptorCount() {
        return getInterceptorCount_native(pointer);
    }

    /**
     * Retrieves the number of scarabs that this unit has for use. This function is only for the Reaver.
     * Returns Number of scarabs this unit has ready.
     */
    public int getScarabCount() {
        return getScarabCount_native(pointer);
    }

    /**
     * Retrieves the amount of Spider Mines this unit has available. This function is only for the Vulture.
     * Returns Number of spider mines available for placement.
     */
    public int getSpiderMineCount() {
        return getSpiderMineCount_native(pointer);
    }

    /**
     * Retrieves the unit's ground weapon cooldown. This value decreases every frame, until it reaches 0. When
     * the value is 0, this indicates that the unit is capable of using its ground weapon, otherwise it must
     * wait until it reaches 0. Note This value will vary, because Starcraft adds an additional random value
     * between (-1) and (+2) to the unit's weapon cooldown. Returns Number of frames needed for the unit's
     * ground weapon to become available again.
     */
    public int getGroundWeaponCooldown() {
        return getGroundWeaponCooldown_native(pointer);
    }

    /**
     * Retrieves the unit's air weapon cooldown. This value decreases every frame, until it reaches 0. When
     * the value is 0, this indicates that the unit is capable of using its air weapon, otherwise it must wait
     * until it reaches 0. Note This value will vary, because Starcraft adds an additional random value
     * between (-1) and (+2) to the unit's weapon cooldown. Returns Number of frames needed for the unit's air
     * weapon to become available again.
     */
    public int getAirWeaponCooldown() {
        return getAirWeaponCooldown_native(pointer);
    }

    /**
     * Retrieves the unit's ability cooldown. This value decreases every frame, until it reaches 0. When the
     * value is 0, this indicates that the unit is capable of using one of its special abilities, otherwise it
     * must wait until it reaches 0. Note This value will vary, because Starcraft adds an additional random
     * value between (-1) and (+2) to the unit's ability cooldown. Returns Number of frames needed for the
     * unit's abilities to become available again.
     */
    public int getSpellCooldown() {
        return getSpellCooldown_native(pointer);
    }

    /**
     * Retrieves the amount of hit points remaining on the Defensive Matrix created by a Science Vessel. The
     * Defensive Matrix ability starts with 250 hit points when it is used. Returns Number of hit points
     * remaining on this unit's Defensive Matrix. See also getDefenseMatrixTimer, isDefenseMatrixed
     */
    public int getDefenseMatrixPoints() {
        return getDefenseMatrixPoints_native(pointer);
    }

    /**
     * Retrieves the time, in frames, that the Defensive Matrix will remain active on the current unit.
     * Returns Number of frames remaining until the effect is removed. See also getDefenseMatrixPoints,
     * isDefenseMatrixed
     */
    public int getDefenseMatrixTimer() {
        return getDefenseMatrixTimer_native(pointer);
    }

    /**
     * Retrieves the time, in frames, that Ensnare will remain active on the current unit. Returns Number of
     * frames remaining until the effect is removed. See also isEnsnared
     */
    public int getEnsnareTimer() {
        return getEnsnareTimer_native(pointer);
    }

    /**
     * Retrieves the time, in frames, that Irradiate will remain active on the current unit. Returns Number of
     * frames remaining until the effect is removed. See also isIrradiated
     */
    public int getIrradiateTimer() {
        return getIrradiateTimer_native(pointer);
    }

    /**
     * Retrieves the time, in frames, that Lockdown will remain active on the current unit. Returns Number of
     * frames remaining until the effect is removed. See also isLockdowned
     */
    public int getLockdownTimer() {
        return getLockdownTimer_native(pointer);
    }

    /**
     * Retrieves the time, in frames, that Maelstrom will remain active on the current unit. Returns Number of
     * frames remaining until the effect is removed. See also isMaelstrommed
     */
    public int getMaelstromTimer() {
        return getMaelstromTimer_native(pointer);
    }

    /**
     * Retrieves an internal timer used for the primary order. Its use is specific to the order type that is
     * currently assigned to the unit. Returns A value used as a timer for the primary order. See also
     * getOrder
     */
    public int getOrderTimer() {
        return getOrderTimer_native(pointer);
    }

    /**
     * Retrieves the time, in frames, that Plague will remain active on the current unit. Returns Number of
     * frames remaining until the effect is removed. See also isPlagued
     */
    public int getPlagueTimer() {
        return getPlagueTimer_native(pointer);
    }

    /**
     * Retrieves the time, in frames, until this temporary unit is destroyed or removed. This is used to
     * determine the remaining time for the following units that were created by abilities: Hallucination
     * broodling Dark Swarm Disruption Web Scanner Sweep Once this value reaches 0, the unit is destroyed.
     */
    public int getRemoveTimer() {
        return getRemoveTimer_native(pointer);
    }

    /**
     * Retrieves the time, in frames, that Stasis Field will remain active on the current unit. Returns Number
     * of frames remaining until the effect is removed. See also isPlagued
     */
    public int getStasisTimer() {
        return getStasisTimer_native(pointer);
    }

    /**
     * Retrieves the time, in frames, that Stim Packs will remain active on the current unit. Returns Number
     * of frames remaining until the effect is removed. See also isPlagued
     */
    public int getStimTimer() {
        return getStimTimer_native(pointer);
    }

    /**
     * Retrieves the building type that a worker (SCV, Probe, Drone) is about to construct. If the unit is
     * morphing or is an incomplete structure, then this returns the UnitType that it will become when it has
     * completed morphing/constructing. Returns UnitType indicating the type that a worker (SCV, Probe, Drone)
     * is about to construct, or an incomplete unit will be when completed.
     */
    public UnitType getBuildType() {
        return getBuildType_native(pointer);
    }

    /**
     * Retrieves the list of units queued up to be trained. Returns a UnitType::set containing all the types
     * that are in this factory's training queue. See also train, cancelTrain, isTraining
     */
    public List<UnitType> getTrainingQueue() {
        return getTrainingQueue_native(pointer);
    }

    /**
     * Retrieves the technology that this unit is currently researching. Returns TechType indicating the
     * technology being researched by this unit. Return values TechTypes::None if this unit is not researching
     * anything. See also research, cancelResearch, isResearching, getRemainingResearchTime
     */
    public TechType getTech() {
        return getTech_native(pointer);
    }

    /**
     * Retrieves the upgrade that this unit is currently upgrading. Returns UpgradeType indicating the upgrade
     * in progress by this unit. Return values UpgradeTypes::None if this unit is not upgrading anything. See
     * also upgrade, cancelUpgrade, isUpgrading, getRemainingUpgradeTime
     */
    public UpgradeType getUpgrade() {
        return getUpgrade_native(pointer);
    }

    /**
     * Retrieves the remaining build time for a unit or structure that is being trained or constructed.
     * Returns Number of frames remaining until the unit's completion.
     */
    public int getRemainingBuildTime() {
        return getRemainingBuildTime_native(pointer);
    }

    /**
     * Retrieves the remaining time, in frames, of the unit that is currently being trained. Note If the unit
     * is a Hatchery, Lair, or Hive, this retrieves the amount of time until the next larva spawns. Returns
     * Number of frames remaining until the current training unit becomes completed, or the number of frames
     * remaining until the next larva spawns. Return values 0 If the unit is not training or has three larvae.
     * See also train, getTrainingQueue
     */
    public int getRemainingTrainTime() {
        return getRemainingTrainTime_native(pointer);
    }

    /**
     * Retrieves the amount of time until the unit is done researching its currently assigned TechType.
     * Returns The remaining research time, in frames, for the current technology being researched by this
     * unit. Return values 0 If the unit is not researching anything. See also research, cancelResearch,
     * isResearching, getTech
     */
    public int getRemainingResearchTime() {
        return getRemainingResearchTime_native(pointer);
    }

    /**
     * Retrieves the amount of time until the unit is done upgrading its current upgrade. Returns The
     * remaining upgrade time, in frames, for the current upgrade. Return values 0 If the unit is not
     * upgrading anything. See also upgrade, cancelUpgrade, isUpgrading, getUpgrade
     */
    public int getRemainingUpgradeTime() {
        return getRemainingUpgradeTime_native(pointer);
    }

    /**
     * Retrieves the corresponding paired unit for SCVs and Terran structures. For example, if this unit is a
     * Factory under construction, this function will return the SCV that is constructing it. If this unit is
     * a SCV, then it will return the structure it is currently constructing. Returns Paired build unit that
     * is either constructing this unit, or being constructed by this unit. Return values nullptr If there is
     * no unit constructing this one, or this unit is not constructing another unit.
     */
    public Unit getBuildUnit() {
        return getBuildUnit_native(pointer);
    }

    /**
     * Generally returns the appropriate target unit after issuing an order that accepts a target unit (i.e.
     * attack, repair, gather, etc.). To get a target that has been acquired automatically without issuing an
     * order, use getOrderTarget. Returns Unit that is currently being targeted by this unit. See also
     * getOrderTarget
     */
    public Unit getTarget() {
        return getTarget_native(pointer);
    }

    /**
     * Retrieves the target position the unit is moving to, provided a valid path to the target position
     * exists. Returns Target position of a movement action.
     */
    public Position getTargetPosition() {
        return getTargetPosition_native(pointer);
    }

    /**
     * Retrieves the primary Order that the unit is assigned. Primary orders are distinct actions such as
     * Orders::AttackUnit and Orders::PlayerGuard. Returns The primary Order that the unit is executing.
     */
    public Order getOrder() {
        return getOrder_native(pointer);
    }

    /**
     * Retrieves the secondary Order that the unit is assigned. Secondary orders are run in the background as
     * a sub-order. An example would be Orders::TrainFighter, because a Carrier can move and train fighters at
     * the same time. Returns The secondary Order that the unit is executing.
     */
    public Order getSecondaryOrder() {
        return getSecondaryOrder_native(pointer);
    }

    /**
     * Retrieves the unit's primary order target. This is usually set when the low level unit AI acquires a
     * new target automatically. For example if an enemy Probe comes in range of your Marine, the Marine will
     * start attacking it, and getOrderTarget will be set in this case, but not getTarget. Returns The Unit
     * that this unit is currently targetting. See also getTarget, getOrder
     */
    public Unit getOrderTarget() {
        return getOrderTarget_native(pointer);
    }

    /**
     * Retrieves the target position for the unit's order. For example, when Orders::Move is assigned,
     * getTargetPosition returns the end of the unit's path, but this returns the location that the unit is
     * trying to move to. Returns Position that this unit is currently targetting. See also getTargetPosition,
     * getOrder
     */
    public Position getOrderTargetPosition() {
        return getOrderTargetPosition_native(pointer);
    }

    /**
     * Retrieves the position the structure is rallying units to once they are completed. Returns Position
     * that a completed unit coming from this structure will travel to. Return values Positions::None If this
     * building does not produce units. Note If getRallyUnit is valid, then this value is ignored. See also
     * setRallyPoint, getRallyUnit
     */
    public Position getRallyPosition() {
        return getRallyPosition_native(pointer);
    }

    /**
     * Retrieves the unit the structure is rallying units to once they are completed. Units will then follow
     * the targetted unit. Returns Unit that a completed unit coming from this structure will travel to.
     * Return values nullptr If the structure is not rallied to a unit or it does not produce units. Note A
     * rallied unit takes precedence over a rallied position. That is if the return value is valid(non-null),
     * then getRallyPosition is ignored. See also setRallyPoint, getRallyPosition
     */
    public Unit getRallyUnit() {
        return getRallyUnit_native(pointer);
    }

    /**
     * Retrieves the add-on that is attached to this unit. Returns Unit interface that represents the add-on
     * that is attached to this unit. Return values nullptr if this unit does not have an add-on.
     */
    public Unit getAddon() {
        return getAddon_native(pointer);
    }

    /**
     * Retrieves the Nydus Canal that is attached to this one. Every Nydus Canal can place a "Nydus Exit"
     * which, when connected, can be travelled through by Zerg units. Returns Unit interface representing the
     * Nydus Canal connected to this one. Return values nullptr if the unit is not a Nydus Canal, is not
     * owned, or has not placed a Nydus Exit.
     */
    public Unit getNydusExit() {
        return getNydusExit_native(pointer);
    }

    /**
     * Retrieves the power-up that the worker unit is holding. Power-ups are special units such as the Flag in
     * the Capture The Flag game type, which can be picked up by worker units. Note If your bot is strictly
     * melee/1v1, then this method is not necessary. Returns The Unit interface object that represents the
     * power-up. Return values nullptr If the unit is not carrying anything. Example BWAPI::Unitset myUnits =
     * BWAPI::Broodwar->self()getUnits(); for ( auto u = myUnits.begin(); u != myUnits.end(); ++u ) { // If we
     * are carrying a flag if ( u->getPowerUp() && u->getPowerUp()->getType() == BWAPI::UnitType::Powerup_Flag
     * ) u->move( u->getClosestUnit(BWAPI::Filter::IsFlagBeacon && BWAPI::Filter::IsOwned) ); // return it to
     * our flag beacon to score } Note If this function returns a successful state, then the following
     * function calls will also return a successful state: getType().isWorker(), isCompleted()
     */
    public Unit getPowerUp() {
        return getPowerUp_native(pointer);
    }

    /**
     * Retrieves the Transport(Dropship, Shuttle, Overlord ) or Bunker unit that has this unit loaded inside
     * of it. Returns Unit interface object representing the Transport(Dropship, Shuttle, Overlord )
     * containing this unit. Return values nullptr if this unit is not in a Transport(Dropship, Shuttle,
     * Overlord ).
     */
    public Unit getTransport() {
        return getTransport_native(pointer);
    }

    /**
     * Retrieves the set of units that are contained within this Bunker or Transport(Dropship, Shuttle,
     * Overlord ). Returns A Unitset object containing all of the units that are loaded inside of the current
     * unit.
     */
    public List<Unit> getLoadedUnits() {
        return getLoadedUnits_native(pointer);
    }

    /**
     * Retrieves the remaining unit-space available for Bunkers and Transports(Dropships, Shuttles, Overlords
     * ). Returns The number of spots available to transport a unit. See also getLoadedUnits
     */
    public int getSpaceRemaining() {
        return getSpaceRemaining_native(pointer);
    }

    /**
     * Retrieves the parent Carrier that owns this Interceptor. Returns The parent Carrier unit that has
     * ownership of this one. Return values nullptr if the current unit is not an Interceptor.
     */
    public Unit getCarrier() {
        return getCarrier_native(pointer);
    }

    /**
     * Retrieves the set of Interceptors controlled by this unit. This is intended for Carriers. Returns
     * Unitset containing Interceptor units owned by this one.
     */
    public List<Unit> getInterceptors() {
        return getInterceptors_native(pointer);
    }

    /**
     * Retrieves the parent Hatchery, Lair, or Hive that owns this particular unit. This is intended for
     * Larvae. Returns Hatchery unit that has ownership of this larva. Return values nullptr if the current
     * unit is not a Larva or has no parent.
     */
    public Unit getHatchery() {
        return getHatchery_native(pointer);
    }

    /**
     * Retrieves the set of Larvae that were spawned by this unit. Only Hatcheries, Lairs, and Hives are
     * capable of spawning Larvae. This is like clicking the "Select Larva" button and getting the selection
     * of Larvae. Returns Unitset containing Larva units owned by this unit. The set will be empty if there
     * are none.
     */
    public List<Unit> getLarva() {
        return getLarva_native(pointer);
    }

    /**
     * Retrieves the set of all units in a given radius of the current unit. Takes into account this unit's
     * dimensions. Can optionally specify a filter that is composed using BWAPI Filter semantics to include
     * only specific units (such as only ground units, etc.) Parameters radius The radius, in pixels, to
     * search for units. pred (optional) The composed function predicate to include only specific (desired)
     * units in the set. Defaults to nullptr, which means no filter. Returns A Unitset containing the set of
     * units that match the given criteria. Example usage: // Get main building closest to start location.
     * BWAPI::Unit pMain = BWAPI::Broodwar->getClosestUnit( BWAPI::Broodwar->self()->getStartLocation(),
     * BWAPI::Filter::IsResourceDepot ); if ( pMain ) // check if pMain is valid { // Get sets of resources
     * and workers BWAPI::Unitset myResources = pMain->getUnitsInRadius(1024, BWAPI::Filter::IsMineralField);
     * if ( !myResources.empty() ) // check if we have resources nearby { BWAPI::Unitset myWorkers =
     * pMain->getUnitsInRadius(512, BWAPI::Filter::IsWorker && BWAPI::Filter::IsIdle && BWAPI::Filter::IsOwned
     * ); while ( !myWorkers.empty() ) // make sure we command all nearby idle workers, if any { for ( auto u
     * = myResources.begin(); u != myResources.end() && !myWorkers.empty(); ++u ) {
     * myWorkers.back()->gather(*u); myWorkers.pop_back(); } } } // myResources not empty } // pMain !=
     * nullptr See also getClosestUnit, getUnitsInWeaponRange, Game::getUnitsInRadius,
     * Game::getUnitsInRectangle
     */
    public List<Unit> getUnitsInRadius(int radius) {
        return getUnitsInRadius_native(pointer, radius);
    }

    /**
     * Obtains the set of units within weapon range of this unit. Parameters weapon The weapon type to use as
     * a filter for distance and units that can be hit by it. pred (optional) A predicate used as an
     * additional filter. If omitted, no additional filter is used. See also getUnitsInRadius, getClosestUnit,
     * Game::getUnitsInRadius, Game::getUnitsInRectangle
     */
    public List<Unit> getUnitsInWeaponRange(WeaponType weapon) {
        return getUnitsInWeaponRange_native(pointer, weapon);
    }

    /**
     * Checks if the current unit is housing a Nuke. This is only available for Nuclear Silos. Returns true if
     * this unit has a Nuke ready, and false if there is no Nuke.
     */
    public boolean hasNuke() {
        return hasNuke_native(pointer);
    }

    /**
     * Checks if the current unit is accelerating. Returns true if this unit is accelerating, and false
     * otherwise
     */
    public boolean isAccelerating() {
        return isAccelerating_native(pointer);
    }

    /**
     * Checks if this unit is currently attacking something. Returns true if this unit is attacking another
     * unit, and false if it is not.
     */
    public boolean isAttacking() {
        return isAttacking_native(pointer);
    }

    /**
     * Checks if this unit is currently playing an attack animation. Issuing commands while this returns true
     * may interrupt the unit's next attack sequence. Returns true if this unit is currently running an attack
     * frame, and false if interrupting the unit is feasible. Note This function is only available to some
     * unit types, specifically those that play special animations when they attack.
     */
    public boolean isAttackFrame() {
        return isAttackFrame_native(pointer);
    }

    /**
     * Checks if the current unit is being constructed. This is mostly applicable to Terran structures which
     * require an SCV to be constructing a structure. Return values true if this is either a Protoss
     * structure, Zerg structure, or Terran structure being constructed by an attached SCV. false if this is
     * either completed, not a structure, or has no SCV constructing it See also build, cancelConstruction,
     * haltConstruction, isConstructing
     */
    public boolean isBeingConstructed() {
        return isBeingConstructed_native(pointer);
    }

    /**
     * Checks this Mineral Field or Refinery is currently being gathered from. Returns true if this unit is a
     * resource container and being harvested by a worker, and false otherwise
     */
    public boolean isBeingGathered() {
        return isBeingGathered_native(pointer);
    }

    /**
     * Checks if this unit is currently being healed by a Medic or repaired by a SCV. Returns true if this
     * unit is being healed, and false otherwise.
     */
    public boolean isBeingHealed() {
        return isBeingHealed_native(pointer);
    }

    /**
     * Checks if this unit is currently blinded by a Medic 's Optical Flare ability. Blinded units have
     * reduced sight range and cannot detect other units. Returns true if this unit is blind, and false
     * otherwise
     */
    public boolean isBlind() {
        return isBlind_native(pointer);
    }

    /**
     * Checks if the current unit is slowing down to come to a stop. Returns true if this unit is breaking,
     * false if it has stopped or is still moving at full speed.
     */
    public boolean isBraking() {
        return isBraking_native(pointer);
    }

    /**
     * Checks if the current unit is burrowed, either using the Burrow ability, or is an armed Spider Mine.
     * Returns true if this unit is burrowed, and false otherwise See also burrow, unburrow
     */
    public boolean isBurrowed() {
        return isBurrowed_native(pointer);
    }

    /**
     * Checks if this worker unit is carrying some vespene gas. Returns true if this is a worker unit carrying
     * vespene gas, and false if it is either not a worker, or not carrying gas. Example BWAPI::Unitset
     * myUnits = BWAPI::Broodwar->self()->getUnits(); for ( auto u = myUnits.begin(); u != myUnits.end(); ++u
     * ) { if ( u->isIdle() && (u->isCarryingGas() || u->isCarryingMinerals()) ) u->returnCargo(); } Note If
     * this function returns a successful state, then the following function calls will also return a
     * successful state: isCompleted(), getType().isWorker() See also returnCargo, isGatheringGas,
     * isCarryingMinerals
     */
    public boolean isCarryingGas() {
        return isCarryingGas_native(pointer);
    }

    /**
     * Checks if this worker unit is carrying some minerals. Returns true if this is a worker unit carrying
     * minerals, and false if it is either not a worker, or not carrying minerals. Example BWAPI::Unitset
     * myUnits = BWAPI::Broodwar->self()->getUnits(); for ( auto u = myUnits.begin(); u != myUnits.end(); ++u
     * ) { if ( u->isIdle() && (u->isCarryingGas() || u->isCarryingMinerals()) ) u->returnCargo(); } Note If
     * this function returns a successful state, then the following function calls will also return a
     * successful state: isCompleted(), getType().isWorker() See also returnCargo, isGatheringMinerals,
     * isCarryingMinerals
     */
    public boolean isCarryingMinerals() {
        return isCarryingMinerals_native(pointer);
    }

    /**
     * Checks if this unit is currently cloaked. Returns true if this unit is cloaked, and false if it is
     * visible. See also cloak, decloak
     */
    public boolean isCloaked() {
        return isCloaked_native(pointer);
    }

    /**
     * Checks if this unit has finished being constructed, trained, morphed, or warped in, and can now receive
     * orders. Returns true if this unit is completed, and false if it is under construction or inaccessible.
     */
    public boolean isCompleted() {
        return isCompleted_native(pointer);
    }

    /**
     * Checks if a unit is either constructing something or moving to construct something. Returns true when a
     * unit has been issued an order to build a structure and is moving to the build location, or is currently
     * constructing something. See also isBeingConstructed, build, cancelConstruction, haltConstruction
     */
    public boolean isConstructing() {
        return isConstructing_native(pointer);
    }

    /**
     * Checks if this unit has the Defensive Matrix effect. Returns true if the Defensive Matrix ability was
     * used on this unit, and false otherwise.
     */
    public boolean isDefenseMatrixed() {
        return isDefenseMatrixed_native(pointer);
    }

    /**
     * Checks if this unit is visible or revealed by a detector unit. If this is false and isVisible is true,
     * then the unit is only partially visible and requires a detector in order to be targetted. Returns true
     * if this unit is detected, and false if it needs a detector unit nearby in order to see it. Note If this
     * function returns a successful state, then the following function calls will also return a successful
     * state: isVisible
     */
    public boolean isDetected() {
        return isDetected_native(pointer);
    }

    /**
     * Checks if the Queen ability Ensnare has been used on this unit. Returns true if the unit is ensnared,
     * and false if it is not
     */
    public boolean isEnsnared() {
        return isEnsnared_native(pointer);
    }

    /**
     * This macro function checks if this unit is in the air. That is, the unit is either a flyer or a flying
     * building. Returns true if this unit is in the air, and false if it is on the ground See also
     * UnitType::isFlyer, UnitInterface::isLifted
     */
    public boolean isFlying() {
        return isFlying_native(pointer);
    }

    /**
     * Checks if this unit is following another unit. When a unit is following another unit, it simply moves
     * where the other unit does, and does not attack enemies when it is following. Returns true if this unit
     * is following another unit, and false if it is not Note If this function returns a successful state,
     * then the following function calls will also return a successful state: isCompleted See also follow,
     * getTarget
     */
    public boolean isFollowing() {
        return isFollowing_native(pointer);
    }

    /**
     * Checks if this unit is currently gathering gas. That is, the unit is either moving to a refinery,
     * waiting to enter a refinery, harvesting from the refinery, or returning gas to a resource depot.
     * Returns true if this unit is harvesting gas, and false if it is not Note If this function returns a
     * successful state, then the following function calls will also return a successful state: isCompleted,
     * getType().isWorker() See also isCarryingGas
     */
    public boolean isGatheringGas() {
        return isGatheringGas_native(pointer);
    }

    /**
     * Checks if this unit is currently harvesting minerals. That is, the unit is either moving to a Mineral
     * Field, waiting to mine, mining minerals, or returning minerals to a resource depot. Returns true if
     * this unit is gathering minerals, and false if it is not Note If this function returns a successful
     * state, then the following function calls will also return a successful state: isCompleted,
     * getType().isWorker() See also isCarryingMinerals
     */
    public boolean isGatheringMinerals() {
        return isGatheringMinerals_native(pointer);
    }

    /**
     * Checks if this unit is a hallucination. Hallucinations are created by the High Templar using the
     * Hallucination ability. Enemy hallucinations are unknown if Flag::CompleteMapInformation is disabled.
     * Hallucinations have a time limit until they are destroyed (see UnitInterface::getRemoveTimer). Returns
     * true if the unit is a hallucination and false otherwise. See also getRemoveTimer
     */
    public boolean isHallucination() {
        return isHallucination_native(pointer);
    }

    /**
     * Checks if the unit is currently holding position. A unit that is holding position will attack other
     * units, but will not chase after them. Returns true if this unit is holding position, and false if it is
     * not. See also holdPosition
     */
    public boolean isHoldingPosition() {
        return isHoldingPosition_native(pointer);
    }

    /**
     * Checks if this unit is running an idle order. This function is particularly useful when checking for
     * units that aren't doing any tasks that you assigned. A unit is considered idle if it is not doing any
     * of the following: Training Constructing Morphing Researching Upgrading In addition to running one of
     * the following orders: Orders::PlayerGuard: Player unit idle. Orders::Guard: Generic unit idle.
     * Orders::Stop Orders::PickupIdle Orders::Nothing: Structure/generic idle. Orders::Medic: Medic idle.
     * Orders::Carrier: Carrier idle. Orders::Reaver: Reaver idle. Orders::Critter: Critter idle.
     * Orders::Neutral: Neutral unit idle. Orders::TowerGuard: Turret structure idle. Orders::Burrowed:
     * Burrowed unit idle. Orders::NukeTrain Orders::Larva: Larva idle. BWAPI::Unitset myUnits =
     * BWAPI::Broodwar->self()->getUnits(); for ( auto u = myUnits.begin(); u != myUnits.end(); ++u ) { //
     * Order idle worker to gather from closest mineral field if ( u->getType().isWorker() && u->isIdle() )
     * u->gather( u->getClosestUnit( BWAPI::Filter::IsMineralField ) ); } Returns true if this unit is idle,
     * and false if this unit is performing any action, such as moving or attacking Note If this function
     * returns a successful state, then the following function calls will also return a successful state:
     * isCompleted See also UnitInterface::stop
     */
    public boolean isIdle() {
        return isIdle_native(pointer);
    }

    /**
     * Checks if the unit can be interrupted. Returns true if this unit can be interrupted, or false if this
     * unit is uninterruptable
     */
    public boolean isInterruptible() {
        return isInterruptible_native(pointer);
    }

    /**
     * Checks the invincibility state for this unit. Returns true if this unit is currently invulnerable, and
     * false if it is vulnerable
     */
    public boolean isInvincible() {
        return isInvincible_native(pointer);
    }

    /**
     * Checks if the target unit can immediately be attacked by this unit in the current frame. Parameters
     * target The target unit to use in this check. Returns true if target is within weapon range of this
     * unit's appropriate weapon, and false otherwise. Return values false if target is invalid, inaccessible,
     * too close, too far, or this unit does not have a weapon that can attack target.
     */
    public boolean isInWeaponRange(Unit target) {
        return isInWeaponRange_native(pointer, target);
    }

    /**
     * Checks if this unit is irradiated by a Science Vessel 's Irradiate ability. Returns true if this unit
     * is irradiated, and false otherwise Example usage: BWAPI::Unitset myUnits =
     * BWAPI::Broodwar->self()->getUnits(); for ( auto u = myUnits.begin(); u != myUnits.end(); ++u ) { if (
     * u->isIrradiated() && u->getIrradiateTimer > 50 &&
     * BWAPI::Broodwar->self()->hasResearched(BWAPI::TechTypes::Restoration) ) { BWAPI::Unit medic =
     * u->getClosestUnit( BWAPI::Filter::GetType == BWAPI::UnitType::Terran_Medic && BWAPI::Filter::Energy >=
     * BWAPI::TechTypes::Restoration.energyCost() ); if ( medic )
     * medic->useTech(BWAPI::TechTypes::Restoration, *u); } } See also getIrradiateTimer
     */
    public boolean isIrradiated() {
        return isIrradiated_native(pointer);
    }

    /**
     * Checks if this unit is a Terran building and lifted off the ground. This function generally implies
     * this->getType().isBuilding() and this->isCompleted() both return true. Returns true if this unit is a
     * Terran structure lifted off the ground. Note If this function returns a successful state, then the
     * following function calls will also return a successful state: isCompleted, getType().isFlyingBuilding()
     * See also isFlying
     */
    public boolean isLifted() {
        return isLifted_native(pointer);
    }

    /**
     * Checks if this unit is currently loaded into another unit such as a Transport(Dropship, Shuttle,
     * Overlord ). Returns true if this unit is loaded in another one, and false otherwise Note If this
     * function returns a successful state, then the following function calls will also return a successful
     * state: isCompleted See also load, unload, unloadAll
     */
    public boolean isLoaded() {
        return isLoaded_native(pointer);
    }

    /**
     * Checks if this unit is currently locked by a Ghost. Returns true if this unit is locked down, and false
     * otherwise See also getLockdownTimer
     */
    public boolean isLockedDown() {
        return isLockedDown_native(pointer);
    }

    /**
     * Checks if this unit has been maelstrommed by a Dark Archon. Returns true if this unit is maelstrommed,
     * and false otherwise See also getMaelstromTimer
     */
    public boolean isMaelstrommed() {
        return isMaelstrommed_native(pointer);
    }

    /**
     * Finds out if the current unit is morphing or not. Zerg units and structures often have the ability to
     * morph into different types of units. This function allows you to identify when this process is
     * occurring. Return values true if the unit is currently morphing. false if the unit is not morphing See
     * also morph, cancelMorph, getBuildType, getRemainingBuildTime
     */
    public boolean isMorphing() {
        return isMorphing_native(pointer);
    }

    /**
     * Checks if this unit is currently moving. Returns true if this unit is moving, and false if it is not
     * See also stop
     */
    public boolean isMoving() {
        return isMoving_native(pointer);
    }

    /**
     * Checks if this unit has been parasited by some other player. Returns true if this unit is inflicted
     * with Parasite, and false if it is clean
     */
    public boolean isParasited() {
        return isParasited_native(pointer);
    }

    /**
     * Checks if this unit is patrolling between two positions. Returns true if this unit is patrolling and
     * false if it is not See also patrol
     */
    public boolean isPatrolling() {
        return isPatrolling_native(pointer);
    }

    /**
     * Checks if this unit has been been plagued by a Defiler. Returns true if this unit is inflicted with
     * Plague and is taking damage, and false if it is clean See also getPlagueTimer
     */
    public boolean isPlagued() {
        return isPlagued_native(pointer);
    }

    /**
     * Checks if this unit is repairing or moving to repair another unit. This is only applicable to SCVs.
     * Returns true if this unit is currently repairing or moving to repair another unit, and false if it is
     * not
     */
    public boolean isRepairing() {
        return isRepairing_native(pointer);
    }

    /**
     * Checks if this unit is a structure that is currently researching a technology. See TechTypes for a
     * complete list of technologies in Broodwar. Returns true if this structure is researching a technology,
     * false otherwise See also research, cancelResearch, getTech, getRemainingResearchTime, Note If this
     * function returns a successful state, then the following function calls will also return a successful
     * state: exists(), isCompleted(), getType().isBuilding()
     */
    public boolean isResearching() {
        return isResearching_native(pointer);
    }

    /**
     * Checks if this unit has been selected in the user interface. This function is only available if the
     * flag Flag::UserInput is enabled. Returns true if this unit is currently selected, and false if this
     * unit is not selected See also Game::getSelectedUnits
     */
    public boolean isSelected() {
        return isSelected_native(pointer);
    }

    /**
     * Checks if this unit is currently sieged. This is only applicable to Siege Tanks. Returns true if the
     * unit is in siege mode, and false if it is either not in siege mode or not a Siege Tank See also siege,
     * unsiege
     */
    public boolean isSieged() {
        return isSieged_native(pointer);
    }

    /**
     * Checks if the unit is starting to attack. Returns true if this unit is starting an attack. See also
     * attack, getGroundWeaponCooldown, getAirWeaponCooldown
     */
    public boolean isStartingAttack() {
        return isStartingAttack_native(pointer);
    }

    /**
     * Checks if this unit is inflicted with Stasis Field by an Arbiter. Returns true if this unit is locked
     * in a Stasis Field and is unable to move, and false if it is free. Note This function does not
     * necessarily imply that the unit is invincible, since there is a feature in the Use Map Settings game
     * type that allows stasised units to be vulnerable. See also getStasisTimer
     */
    public boolean isStasised() {
        return isStasised_native(pointer);
    }

    /**
     * Checks if this unit is currently under the influence of a Stim Packs. Returns true if this unit has
     * used a stim pack, false otherwise See also getStimTimer
     */
    public boolean isStimmed() {
        return isStimmed_native(pointer);
    }

    /**
     * Checks if this unit is currently trying to resolve a collision by randomly moving around. Returns true
     * if this unit is currently stuck and trying to resolve a collision, and false if this unit is free
     */
    public boolean isStuck() {
        return isStuck_native(pointer);
    }

    /**
     * Checks if this unit is training a new unit. For example, a Barracks training a Marine. Note It is
     * possible for a unit to remain in the training queue with no progress. In that case, this function will
     * return false because of supply or unit count limitations. Returns true if this unit is currently
     * training another unit, and false otherwise. See also train, getTrainingQueue, cancelTrain,
     * getRemainingTrainTime
     */
    public boolean isTraining() {
        return isTraining_native(pointer);
    }

    /**
     * Checks if the current unit is being attacked. Has a small delay before this returns false again when
     * the unit is no longer being attacked. Returns true if this unit has been attacked within the past few
     * frames, and false if it has not
     */
    public boolean isUnderAttack() {
        return isUnderAttack_native(pointer);
    }

    /**
     * Checks if this unit is under the cover of a Dark Swarm. Returns true if this unit is protected by a
     * Dark Swarm, and false if it is not
     */
    public boolean isUnderDarkSwarm() {
        return isUnderDarkSwarm_native(pointer);
    }

    /**
     * Checks if this unit is currently being affected by a Disruption Web. Returns true if this unit is under
     * the effects of Disruption Web.
     */
    public boolean isUnderDisruptionWeb() {
        return isUnderDisruptionWeb_native(pointer);
    }

    /**
     * Checks if this unit is currently taking damage from a Psionic Storm. Returns true if this unit is
     * losing hit points from a Psionic Storm, and false otherwise.
     */
    public boolean isUnderStorm() {
        return isUnderStorm_native(pointer);
    }

    /**
     * Checks if this unit has power. Most structures are powered by default, but Protoss structures require a
     * Pylon to be powered and functional. Returns true if this unit has power or is inaccessible, and false
     * if this unit is unpowered. Since 4.0.1 Beta (previously isUnpowered)
     */
    public boolean isPowered() {
        return isPowered_native(pointer);
    }

    /**
     * Checks if this unit is a structure that is currently upgrading an upgrade. See UpgradeTypes for a full
     * list of upgrades in Broodwar. Returns true if this structure is upgrading, false otherwise See also
     * upgrade, cancelUpgrade, getUpgrade, getRemainingUpgradeTime Note If this function returns a successful
     * state, then the following function calls will also return a successful state: exists(), isCompleted(),
     * getType().isBuilding()
     */
    public boolean isUpgrading() {
        return isUpgrading_native(pointer);
    }

    /**
     * Checks if this unit is visible. Parameters player (optional) The player to check visibility for. If
     * this parameter is omitted, then the BWAPI player obtained from Game::self will be used. Returns true if
     * this unit is visible to the specified player, and false if it is not. Note If the
     * Flag::CompleteMapInformation flag is enabled, existing units hidden by the fog of war will be
     * accessible, but isVisible will still return false. See also exists
     */
    public boolean isVisible() {
        return isVisible_native(pointer);
    }

    public boolean isVisible(Player player) {
        return isVisible_native(pointer, player);
    }

    /**
     * Performs some cheap checks to attempt to quickly detect whether the unit is unable to be targetted as
     * the target unit of an unspecified command. Return values true if BWAPI was unable to determine whether
     * the unit can be a target. false if an error occurred and the unit can not be a target. See also
     * Game::getLastError, UnitInterface::canTargetUnit
     */
    public boolean isTargetable() {
        return isTargetable_native(pointer);
    }

    /**
     * This function issues a command to the unit(s), however it is used for interfacing only, and is
     * recommended to use one of the more specific command functions when writing an AI. Parameters command A
     * UnitCommand containing command parameters such as the type, position, target, etc. Returns true if the
     * command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There
     * is a small chance for a command to fail after it has been passed to Broodwar. See also
     * UnitCommandTypes, Game::getLastError, UnitInterface::canIssueCommand
     */
    public boolean issueCommand(UnitCommand command) {
        return issueCommand_native(pointer, command);
    }

    /**
     * Orders the unit(s) to attack move to the specified position or attack the specified unit. Parameters
     * target A Position or a Unit to designate as the target. If a Position is used, the unit will perform an
     * Attack Move command. shiftQueueCommand (optional) If this value is true, then the order will be queued
     * instead of immediately executed. If this value is omitted, then the order will be executed immediately
     * by default. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the
     * command would fail. Note There is a small chance for a command to fail after it has been passed to
     * Broodwar. A Medic will use Heal Move instead of attack. See also Game::getLastError,
     * UnitInterface::canAttack
     */
    public boolean attack(Position target) {
        return attack_native(pointer, target);
    }

    public boolean attack(Unit target) {
        return attack_native(pointer, target);
    }

    public boolean attack(PositionOrUnit target) {
        return attack_native(pointer, target);
    }

    public boolean attack(Position target, boolean shiftQueueCommand) {
        return attack_native(pointer, target, shiftQueueCommand);
    }

    public boolean attack(Unit target, boolean shiftQueueCommand) {
        return attack_native(pointer, target, shiftQueueCommand);
    }

    public boolean attack(PositionOrUnit target, boolean shiftQueueCommand) {
        return attack_native(pointer, target, shiftQueueCommand);
    }

    /**
     * Orders the worker unit(s) to construct a structure at a target position. Parameters type The UnitType
     * to build. target A TilePosition to specify the build location, specifically the upper-left corner of
     * the location. If the target is not specified, then the function call will be redirected to the train
     * command. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the
     * command would fail. Note There is a small chance for a command to fail after it has been passed to
     * Broodwar. You must have sufficient resources and meet the necessary requirements in order to build a
     * structure. See also Game::getLastError, UnitInterface::train, UnitInterface::cancelConstruction,
     * UnitInterface::canBuild
     */
    public boolean build(UnitType type) {
        return build_native(pointer, type);
    }

    public boolean build(UnitType type, TilePosition target) {
        return build_native(pointer, type, target);
    }

    /**
     * Orders the Terran structure(s) to construct an add-on. Parameters type The add-on UnitType to
     * construct. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the
     * command would fail. Note There is a small chance for a command to fail after it has been passed to
     * Broodwar. You must have sufficient resources and meet the necessary requirements in order to build a
     * structure. See also Game::getLastError, UnitInterface::build, UnitInterface::cancelAddon,
     * UnitInterface::canBuildAddon
     */
    public boolean buildAddon(UnitType type) {
        return buildAddon_native(pointer, type);
    }

    /**
     * Orders the unit(s) to add a UnitType to its training queue, or morphs into the UnitType if it is Zerg.
     * Parameters type The UnitType to train. Returns true if the command was passed to Broodwar, and false if
     * BWAPI determined that the command would fail. Note There is a small chance for a command to fail after
     * it has been passed to Broodwar. You must have sufficient resources, supply, and meet the necessary
     * requirements in order to train a unit. This command is also used for training Interceptors and Scarabs.
     * If you call this using a Hatchery, Lair, or Hive, then it will automatically pass the command to one of
     * its Larvae. See also Game::getLastError, UnitInterface::build, UnitInterface::morph,
     * UnitInterface::cancelTrain, UnitInterface::isTraining, UnitInterface::canTrain
     */
    public boolean train() {
        return train_native(pointer);
    }

    public boolean train(UnitType type) {
        return train_native(pointer, type);
    }

    /**
     * Orders the unit(s) to morph into a different UnitType. Parameters type The UnitType to morph into.
     * Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command
     * would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar.
     * See also Game::getLastError, UnitInterface::build, UnitInterface::morph, UnitInterface::canMorph
     */
    public boolean morph(UnitType type) {
        return morph_native(pointer, type);
    }

    /**
     * Orders the unit to research the given tech type. Parameters tech The TechType to research. Returns true
     * if the command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note
     * There is a small chance for a command to fail after it has been passed to Broodwar. See also
     * cancelResearch, isResearching, getRemainingResearchTime, getTech, canResearch
     */
    public boolean research(TechType tech) {
        return research_native(pointer, tech);
    }

    /**
     * Orders the unit to upgrade the given upgrade type. Parameters upgrade The UpgradeType to upgrade.
     * Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command
     * would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar.
     * See also cancelUpgrade, isUpgrading, getRemainingUpgradeTime, getUpgrade, canUpgrade
     */
    public boolean upgrade(UpgradeType upgrade) {
        return upgrade_native(pointer, upgrade);
    }

    /**
     * Orders the unit to set its rally position to the specified position or unit. Parameters target The
     * target position or target unit that this structure will rally to. Returns true if the command was
     * passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small
     * chance for a command to fail after it has been passed to Broodwar. See also getRallyPosition,
     * getRallyUnit, canSetRallyPoint, canSetRallyPosition, canSetRallyUnit
     */
    public boolean setRallyPoint(Position target) {
        return setRallyPoint_native(pointer, target);
    }

    public boolean setRallyPoint(Unit target) {
        return setRallyPoint_native(pointer, target);
    }

    public boolean setRallyPoint(PositionOrUnit target) {
        return setRallyPoint_native(pointer, target);
    }

    /**
     * Orders the unit to move from its current position to the specified position. Parameters target The
     * target position to move to. shiftQueueCommand (optional) If this value is true, then the order will be
     * queued instead of immediately executed. If this value is omitted, then the order will be executed
     * immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI
     * determined that the command would fail. Note There is a small chance for a command to fail after it has
     * been passed to Broodwar. See also isMoving, canMove
     */
    public boolean move(Position target) {
        return move_native(pointer, target);
    }

    public boolean move(Position target, boolean shiftQueueCommand) {
        return move_native(pointer, target, shiftQueueCommand);
    }

    /**
     * Orders the unit to patrol between its current position and the specified position. While patrolling,
     * units will attack and chase enemy units that they encounter, and then return to its patrol route.
     * Medics will automatically heal units and then return to their patrol route. Parameters target The
     * position to patrol to. shiftQueueCommand (optional) If this value is true, then the order will be
     * queued instead of immediately executed. If this value is omitted, then the order will be executed
     * immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI
     * determined that the command would fail. Note There is a small chance for a command to fail after it has
     * been passed to Broodwar. See also isPatrolling, canPatrol
     */
    public boolean patrol(Position target) {
        return patrol_native(pointer, target);
    }

    public boolean patrol(Position target, boolean shiftQueueCommand) {
        return patrol_native(pointer, target, shiftQueueCommand);
    }

    /**
     * Orders the unit to hold its position. Parameters shiftQueueCommand (optional) If this value is true,
     * then the order will be queued instead of immediately executed. If this value is omitted, then the order
     * will be executed immediately by default. Returns true if the command was passed to Broodwar, and false
     * if BWAPI determined that the command would fail. Note There is a small chance for a command to fail
     * after it has been passed to Broodwar. See also canHoldPosition, isHoldingPosition
     */
    public boolean holdPosition() {
        return holdPosition_native(pointer);
    }

    public boolean holdPosition(boolean shiftQueueCommand) {
        return holdPosition_native(pointer, shiftQueueCommand);
    }

    /**
     * Orders the unit to stop. Parameters shiftQueueCommand (optional) If this value is true, then the order
     * will be queued instead of immediately executed. If this value is omitted, then the order will be
     * executed immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI
     * determined that the command would fail. Note There is a small chance for a command to fail after it has
     * been passed to Broodwar. See also canStop, isIdle
     */
    public boolean stop() {
        return stop_native(pointer);
    }

    public boolean stop(boolean shiftQueueCommand) {
        return stop_native(pointer, shiftQueueCommand);
    }

    /**
     * Orders the unit to follow the specified unit. Units that are following other units will not perform any
     * other actions such as attacking. They will ignore attackers. Parameters target The target unit to start
     * following. shiftQueueCommand (optional) If this value is true, then the order will be queued instead of
     * immediately executed. If this value is omitted, then the order will be executed immediately by default.
     * Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command
     * would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar.
     * See also isFollowing, canFollow, getOrderTarget
     */
    public boolean follow(Unit target) {
        return follow_native(pointer, target);
    }

    public boolean follow(Unit target, boolean shiftQueueCommand) {
        return follow_native(pointer, target, shiftQueueCommand);
    }

    /**
     * Orders the unit to gather the specified unit (must be mineral or refinery type). Parameters target The
     * target unit to gather from. shiftQueueCommand (optional) If this value is true, then the order will be
     * queued instead of immediately executed. If this value is omitted, then the order will be executed
     * immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI
     * determined that the command would fail. Note There is a small chance for a command to fail after it has
     * been passed to Broodwar. See also isGatheringGas, isGatheringMinerals, canGather
     */
    public boolean gather(Unit target) {
        return gather_native(pointer, target);
    }

    public boolean gather(Unit target, boolean shiftQueueCommand) {
        return gather_native(pointer, target, shiftQueueCommand);
    }

    /**
     * Orders the unit to return its cargo to a nearby resource depot such as a Command Center. Only workers
     * that are carrying minerals or gas can be ordered to return cargo. Parameters shiftQueueCommand
     * (optional) If this value is true, then the order will be queued instead of immediately executed. If
     * this value is omitted, then the order will be executed immediately by default. Returns true if the
     * command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There
     * is a small chance for a command to fail after it has been passed to Broodwar. See also isCarryingGas,
     * isCarryingMinerals, canReturnCargo
     */
    public boolean returnCargo() {
        return returnCargo_native(pointer);
    }

    public boolean returnCargo(boolean shiftQueueCommand) {
        return returnCargo_native(pointer, shiftQueueCommand);
    }

    /**
     * Orders the unit to repair the specified unit. Only Terran SCVs can be ordered to repair, and the target
     * must be a mechanical Terran unit or building. Parameters target The unit to repair. shiftQueueCommand
     * (optional) If this value is true, then the order will be queued instead of immediately executed. If
     * this value is omitted, then the order will be executed immediately by default. Returns true if the
     * command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There
     * is a small chance for a command to fail after it has been passed to Broodwar. See also isRepairing,
     * canRepair
     */
    public boolean repair(Unit target) {
        return repair_native(pointer, target);
    }

    public boolean repair(Unit target, boolean shiftQueueCommand) {
        return repair_native(pointer, target, shiftQueueCommand);
    }

    /**
     * Orders the unit to burrow. Either the unit must be a Lurker, or the unit must be a Zerg ground unit
     * that is capable of Burrowing, and Burrow technology must be researched. Returns true if the command was
     * passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small
     * chance for a command to fail after it has been passed to Broodwar. See also unburrow, isBurrowed,
     * canBurrow
     */
    public boolean burrow() {
        return burrow_native(pointer);
    }

    /**
     * Orders a burrowed unit to unburrow. Returns true if the command was passed to Broodwar, and false if
     * BWAPI determined that the command would fail. Note There is a small chance for a command to fail after
     * it has been passed to Broodwar. See also burrow, isBurrowed, canUnburrow
     */
    public boolean unburrow() {
        return unburrow_native(pointer);
    }

    /**
     * Orders the unit to cloak. Returns true if the command was passed to Broodwar, and false if BWAPI
     * determined that the command would fail. Note There is a small chance for a command to fail after it has
     * been passed to Broodwar. See also decloak, isCloaked, canCloak
     */
    public boolean cloak() {
        return cloak_native(pointer);
    }

    /**
     * Orders a cloaked unit to decloak. Returns true if the command was passed to Broodwar, and false if
     * BWAPI determined that the command would fail. Note There is a small chance for a command to fail after
     * it has been passed to Broodwar. See also cloak, isCloaked, canDecloak
     */
    public boolean decloak() {
        return decloak_native(pointer);
    }

    /**
     * Orders the unit to siege. Only works for Siege Tanks. Returns true if the command was passed to
     * Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a
     * command to fail after it has been passed to Broodwar. See also unsiege, isSieged, canSiege
     */
    public boolean siege() {
        return siege_native(pointer);
    }

    /**
     * Orders the unit to unsiege. Only works for sieged Siege Tanks. Returns true if the command was passed
     * to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance
     * for a command to fail after it has been passed to Broodwar. See also siege, isSieged, canUnsiege
     */
    public boolean unsiege() {
        return unsiege_native(pointer);
    }

    /**
     * Orders the unit to lift. Only works for liftable Terran structures. Returns true if the command was
     * passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small
     * chance for a command to fail after it has been passed to Broodwar. See also land, isLifted, canLift
     */
    public boolean lift() {
        return lift_native(pointer);
    }

    /**
     * Orders the unit to land. Only works for Terran structures that are currently lifted. Parameters target
     * The tile position to land this structure at. Returns true if the command was passed to Broodwar, and
     * false if BWAPI determined that the command would fail. Note There is a small chance for a command to
     * fail after it has been passed to Broodwar. See also lift, isLifted, canLand
     */
    public boolean land(TilePosition target) {
        return land_native(pointer, target);
    }

    /**
     * Orders the unit to load the target unit. Only works if this unit is a Transport(Dropship, Shuttle,
     * Overlord ) or Bunker type. Parameters target The target unit to load into this Transport(Dropship,
     * Shuttle, Overlord ) or Bunker. shiftQueueCommand (optional) If this value is true, then the order will
     * be queued instead of immediately executed. If this value is omitted, then the order will be executed
     * immediately by default. Returns true if the command was passed to Broodwar, and false if BWAPI
     * determined that the command would fail. Note There is a small chance for a command to fail after it has
     * been passed to Broodwar. See also unload, unloadAll, getLoadedUnits, isLoaded
     */
    public boolean load(Unit target) {
        return load_native(pointer, target);
    }

    public boolean load(Unit target, boolean shiftQueueCommand) {
        return load_native(pointer, target, shiftQueueCommand);
    }

    /**
     * Orders the unit to unload the target unit. Only works for Transports(Dropships, Shuttles, Overlords )
     * and Bunkers. Parameters target Unloads the target unit from this Transport(Dropship, Shuttle, Overlord
     * ) or Bunker. Returns true if the command was passed to Broodwar, and false if BWAPI determined that the
     * command would fail. Note There is a small chance for a command to fail after it has been passed to
     * Broodwar. See also load, unloadAll, getLoadedUnits, isLoaded, canUnload, canUnloadAtPosition
     */
    public boolean unload(Unit target) {
        return unload_native(pointer, target);
    }

    /**
     * Orders the unit to unload all loaded units at the unit's current position. Only works for
     * Transports(Dropships, Shuttles, Overlords ) and Bunkers. Parameters shiftQueueCommand (optional) If
     * this value is true, then the order will be queued instead of immediately executed. If this value is
     * omitted, then the order will be executed immediately by default. Returns true if the command was passed
     * to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance
     * for a command to fail after it has been passed to Broodwar. See also load, unload, getLoadedUnits,
     * isLoaded, canUnloadAll, canUnloadAtPosition
     */
    public boolean unloadAll() {
        return unloadAll_native(pointer);
    }

    public boolean unloadAll(boolean shiftQueueCommand) {
        return unloadAll_native(pointer, shiftQueueCommand);
    }

    /**
     * Orders the unit to unload all loaded units at the unit's current position. Only works for
     * Transports(Dropships, Shuttles, Overlords ) and Bunkers. Parameters shiftQueueCommand (optional) If
     * this value is true, then the order will be queued instead of immediately executed. If this value is
     * omitted, then the order will be executed immediately by default. Returns true if the command was passed
     * to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance
     * for a command to fail after it has been passed to Broodwar. See also load, unload, getLoadedUnits,
     * isLoaded, canUnloadAll, canUnloadAtPosition
     */
    public boolean unloadAll(Position target) {
        return unloadAll_native(pointer, target);
    }

    public boolean unloadAll(Position target, boolean shiftQueueCommand) {
        return unloadAll_native(pointer, target, shiftQueueCommand);
    }

    /**
     * Works like the right click in the GUI. Parameters target The target position or target unit to right
     * click. shiftQueueCommand (optional) If this value is true, then the order will be queued instead of
     * immediately executed. If this value is omitted, then the order will be executed immediately by default.
     * Returns true if the command was passed to Broodwar, and false if BWAPI determined that the command
     * would fail. Note There is a small chance for a command to fail after it has been passed to Broodwar.
     * See also canRightClick, canRightClickPosition, canRightClickUnit
     */
    public boolean rightClick(Position target) {
        return rightClick_native(pointer, target);
    }

    public boolean rightClick(Unit target) {
        return rightClick_native(pointer, target);
    }

    public boolean rightClick(PositionOrUnit target) {
        return rightClick_native(pointer, target);
    }

    public boolean rightClick(Position target, boolean shiftQueueCommand) {
        return rightClick_native(pointer, target, shiftQueueCommand);
    }

    public boolean rightClick(Unit target, boolean shiftQueueCommand) {
        return rightClick_native(pointer, target, shiftQueueCommand);
    }

    public boolean rightClick(PositionOrUnit target, boolean shiftQueueCommand) {
        return rightClick_native(pointer, target, shiftQueueCommand);
    }

    /**
     * Orders a SCV to stop constructing a structure. This leaves the structure in an incomplete state until
     * it is either cancelled, razed, or completed by another SCV. Returns true if the command was passed to
     * Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance for a
     * command to fail after it has been passed to Broodwar. See also isConstructing, canHaltConstruction
     */
    public boolean haltConstruction() {
        return haltConstruction_native(pointer);
    }

    /**
     * Orders this unit to cancel and refund itself from begin constructed. Returns true if the command was
     * passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small
     * chance for a command to fail after it has been passed to Broodwar. See also isBeingConstructed, build,
     * canCancelConstruction
     */
    public boolean cancelConstruction() {
        return cancelConstruction_native(pointer);
    }

    /**
     * Orders this unit to cancel and refund an add-on that is being constructed. Returns true if the command
     * was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a
     * small chance for a command to fail after it has been passed to Broodwar. See also canCancelAddon,
     * buildAddon
     */
    public boolean cancelAddon() {
        return cancelAddon_native(pointer);
    }

    /**
     * Orders the unit to remove the specified unit from its training queue. Parameters slot (optional)
     * Identifies the slot that will be cancelled. If the specified value is at least 0, then the unit in the
     * corresponding slot from the list provided by getTrainingQueue will be cancelled. If the value is either
     * omitted or -2, then the last slot is cancelled. Note The value of slot is passed directly to Broodwar.
     * Other negative values have no effect. See also train, cancelTrain, isTraining, getTrainingQueue,
     * canCancelTrain, canCancelTrainSlot
     */
    public boolean cancelTrain() {
        return cancelTrain_native(pointer);
    }

    public boolean cancelTrain(int slot) {
        return cancelTrain_native(pointer, slot);
    }

    /**
     * Orders this unit to cancel and refund a unit that is morphing. Returns true if the command was passed
     * to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small chance
     * for a command to fail after it has been passed to Broodwar. See also morph, isMorphing, canCancelMorph
     */
    public boolean cancelMorph() {
        return cancelMorph_native(pointer);
    }

    /**
     * Orders this unit to cancel and refund a research that is in progress. Returns true if the command was
     * passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small
     * chance for a command to fail after it has been passed to Broodwar. See also research, isResearching,
     * getTech, canCancelResearch
     */
    public boolean cancelResearch() {
        return cancelResearch_native(pointer);
    }

    /**
     * Orders this unit to cancel and refund an upgrade that is in progress. Returns true if the command was
     * passed to Broodwar, and false if BWAPI determined that the command would fail. Note There is a small
     * chance for a command to fail after it has been passed to Broodwar. See also upgrade, isUpgrading,
     * getUpgrade, canCancelUpgrade
     */
    public boolean cancelUpgrade() {
        return cancelUpgrade_native(pointer);
    }

    /**
     * Orders the unit to use a technology. Parameters tech The technology type to use. target (optional) If
     * specified, indicates the target location or unit to use the tech on. If unspecified, causes the tech to
     * be used without a target (i.e. Stim Packs). Returns true if the command was passed to Broodwar, and
     * false if BWAPI determined that the command would fail. See also canUseTechWithOrWithoutTarget,
     * canUseTech, canUseTechWithoutTarget, canUseTechUnit, canUseTechPosition, TechTypes
     */
    public boolean useTech(TechType tech) {
        return useTech_native(pointer, tech);
    }

    public boolean useTech(TechType tech, Position target) {
        return useTech_native(pointer, tech, target);
    }

    public boolean useTech(TechType tech, Unit target) {
        return useTech_native(pointer, tech, target);
    }

    public boolean useTech(TechType tech, PositionOrUnit target) {
        return useTech_native(pointer, tech, target);
    }

    /**
     * Moves a Flag Beacon to a different location. This is only used for Capture The Flag or Use Map Settings
     * game types. Parameters target The target tile position to place the Flag Beacon. Returns true if the
     * command was passed to Broodwar, and false if BWAPI determined that the command would fail. Note There
     * is a small chance for a command to fail after it has been passed to Broodwar. This command is only
     * available for the first 10 minutes of the game, as in Broodwar. See also canPlaceCOP
     */
    public boolean placeCOP(TilePosition target) {
        return placeCOP_native(pointer, target);
    }

    /**
     * Checks whether the unit is able to execute the given command. If you are calling this function
     * repeatedly (e.g. to generate a collection of valid commands), you can avoid repeating the same kinds of
     * checks by specifying false for some of the optional boolean arguments. Make sure that the state hasn't
     * changed since the check was done though (eg a new frame/event, or a command issued). Also see the more
     * specific functions. Parameters command A UnitCommand to check. checkCanUseTechPositionOnPositions Only
     * used if the command type is UnitCommandTypes::Enum::Use_Tech_Position. A boolean for whether to perform
     * cheap checks for whether the unit is unable to target any positions using the command's TechType (i.e.
     * regardless of what the other command parameters are). You can set this to false if you know this check
     * has already just been performed. checkCanUseTechUnitOnUnits Only used if the command type is
     * UnitCommandTypes::Enum::Use_Tech_Unit. A boolean for whether to perform cheap checks for whether the
     * unit is unable to target any units using the command's TechType (i.e. regardless of what the other
     * command parameters are). You can set this to false if you know this check has already just been
     * performed. checkCanBuildUnitType Only used if the command type is UnitCommandTypes::Build. A boolean
     * for whether to perform cheap checks for whether the unit is unable to build the specified UnitType
     * (i.e. regardless of what the other command parameters are). You can set this to false if you know this
     * check has already just been performed. checkCanTargetUnit Only used for command types that can target a
     * unit. A boolean for whether to perform UnitInterface::canTargetUnit as a check. You can set this to
     * false if you know this check has already just been performed. checkCanIssueCommandType A boolean for
     * whether to perform UnitInterface::canIssueCommandType as a check. You can set this to false if you know
     * this check has already just been performed. checkCommandibility A boolean for whether to perform
     * UnitInterface::canCommand as a check. You can set this to false if you know this check has already just
     * been performed. Return values true if BWAPI determined that the command is valid. false if an error
     * occurred and the command is invalid. See also UnitCommandTypes, Game::getLastError,
     * UnitInterface::canCommand, UnitInterface::canIssueCommandType, UnitInterface::canTargetUnit
     */
    public boolean canIssueCommand(UnitCommand command, boolean checkCanUseTechPositionOnPositions, boolean checkCanUseTechUnitOnUnits, boolean checkCanBuildUnitType, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canIssueCommand_native(pointer, command, checkCanUseTechPositionOnPositions, checkCanUseTechUnitOnUnits, checkCanBuildUnitType, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canIssueCommand(UnitCommand command, boolean checkCanUseTechPositionOnPositions, boolean checkCanUseTechUnitOnUnits, boolean checkCanBuildUnitType, boolean checkCanTargetUnit) {
        return canIssueCommand_native(pointer, command, checkCanUseTechPositionOnPositions, checkCanUseTechUnitOnUnits, checkCanBuildUnitType, checkCanTargetUnit);
    }

    public boolean canIssueCommand(UnitCommand command, boolean checkCanUseTechPositionOnPositions, boolean checkCanUseTechUnitOnUnits, boolean checkCanBuildUnitType) {
        return canIssueCommand_native(pointer, command, checkCanUseTechPositionOnPositions, checkCanUseTechUnitOnUnits, checkCanBuildUnitType);
    }

    public boolean canIssueCommand(UnitCommand command, boolean checkCanUseTechPositionOnPositions, boolean checkCanUseTechUnitOnUnits) {
        return canIssueCommand_native(pointer, command, checkCanUseTechPositionOnPositions, checkCanUseTechUnitOnUnits);
    }

    public boolean canIssueCommand(UnitCommand command, boolean checkCanUseTechPositionOnPositions) {
        return canIssueCommand_native(pointer, command, checkCanUseTechPositionOnPositions);
    }

    public boolean canIssueCommand(UnitCommand command) {
        return canIssueCommand_native(pointer, command);
    }

    public boolean canIssueCommand(UnitCommand command, boolean checkCanUseTechPositionOnPositions, boolean checkCanUseTechUnitOnUnits, boolean checkCanBuildUnitType, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canIssueCommand_native(pointer, command, checkCanUseTechPositionOnPositions, checkCanUseTechUnitOnUnits, checkCanBuildUnitType, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute the given command as part of a Unitset (even if none of the
     * units in the Unitset are able to execute the command individually). The reason this function exists is
     * because some commands are valid for an individual unit but not for those individuals as a group (e.g.
     * buildings, critters) and some commands are only valid for a unit if it is commanded as part of a unit
     * group, e.g.: attackMove/attackUnit for a Unitset, some of which can't attack, e.g. High Templar. This
     * is supported simply for consistency with BW's behaviour - you could issue move command(s) individually
     * instead. attackMove/move/patrol/rightClickPosition for air unit(s) + e.g. Larva, as part of the air
     * stacking technique. This is supported simply for consistency with BW's behaviour - you could issue
     * move/patrol/rightClickPosition command(s) for them individually instead. Note BWAPI allows the
     * following special cases to command a unit individually (rather than only allowing it to be commanded as
     * part of a Unitset). These commands are not available to a user in BW when commanding units
     * individually, but BWAPI allows them for convenience: attackMove for Medic, which is equivalent to Heal
     * Move. holdPosition for burrowed Lurker, for ambushes. stop for Larva, to move it to a different side of
     * the Hatchery / Lair / Hive (e.g. so that Drones morphed later morph nearer to minerals/gas). See also
     * UnitCommandTypes, Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::canCommandGrouped,
     * UnitInterface::canIssueCommandTypeGrouped, UnitInterface::canTargetUnit
     */
    public boolean canIssueCommandGrouped(UnitCommand command, boolean checkCanUseTechPositionOnPositions, boolean checkCanUseTechUnitOnUnits, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped) {
        return canIssueCommandGrouped_native(pointer, command, checkCanUseTechPositionOnPositions, checkCanUseTechUnitOnUnits, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibilityGrouped);
    }

    public boolean canIssueCommandGrouped(UnitCommand command, boolean checkCanUseTechPositionOnPositions, boolean checkCanUseTechUnitOnUnits, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canIssueCommandGrouped_native(pointer, command, checkCanUseTechPositionOnPositions, checkCanUseTechUnitOnUnits, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canIssueCommandGrouped(UnitCommand command, boolean checkCanUseTechPositionOnPositions, boolean checkCanUseTechUnitOnUnits, boolean checkCanTargetUnit) {
        return canIssueCommandGrouped_native(pointer, command, checkCanUseTechPositionOnPositions, checkCanUseTechUnitOnUnits, checkCanTargetUnit);
    }

    public boolean canIssueCommandGrouped(UnitCommand command, boolean checkCanUseTechPositionOnPositions, boolean checkCanUseTechUnitOnUnits) {
        return canIssueCommandGrouped_native(pointer, command, checkCanUseTechPositionOnPositions, checkCanUseTechUnitOnUnits);
    }

    public boolean canIssueCommandGrouped(UnitCommand command, boolean checkCanUseTechPositionOnPositions) {
        return canIssueCommandGrouped_native(pointer, command, checkCanUseTechPositionOnPositions);
    }

    public boolean canIssueCommandGrouped(UnitCommand command) {
        return canIssueCommandGrouped_native(pointer, command);
    }

    public boolean canIssueCommandGrouped(UnitCommand command, boolean checkCanUseTechPositionOnPositions, boolean checkCanUseTechUnitOnUnits, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped, boolean checkCommandibility) {
        return canIssueCommandGrouped_native(pointer, command, checkCanUseTechPositionOnPositions, checkCanUseTechUnitOnUnits, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibilityGrouped, checkCommandibility);
    }

    /**
     * Performs some cheap checks to attempt to quickly detect whether the unit is unable to execute any
     * commands (eg the unit is stasised). Return values true if BWAPI was unable to determine whether the
     * unit can be commanded. false if an error occurred and the unit can not be commanded. See also
     * Game::getLastError, UnitInterface::canIssueCommand
     */
    public boolean canCommand() {
        return canCommand_native(pointer);
    }

    /**
     * Performs some cheap checks to attempt to quickly detect whether the unit is unable to execute any
     * commands as part of a Unitset (eg buildings, critters). Return values true if BWAPI was unable to
     * determine whether the unit can be commanded grouped. false if an error occurred and the unit can not be
     * commanded grouped. See also Game::getLastError, UnitInterface::canIssueCommandGrouped,
     * UnitInterface::canIssueCommand
     */
    public boolean canCommandGrouped() {
        return canCommandGrouped_native(pointer);
    }

    public boolean canCommandGrouped(boolean checkCommandibility) {
        return canCommandGrouped_native(pointer, checkCommandibility);
    }

    /**
     * Performs some cheap checks to attempt to quickly detect whether the unit is unable to execute the given
     * command type (i.e. regardless of what other possible command parameters could be). Parameters ct A
     * UnitCommandType. checkCommandibility A boolean for whether to perform UnitInterface::canCommand as a
     * check. You can set this to false if you know this check has already just been performed. Return values
     * true if BWAPI was unable to determine whether the command type is invalid. false if an error occurred
     * and the command type is invalid. See also UnitCommandTypes, Game::getLastError,
     * UnitInterface::canIssueCommand
     */
    public boolean canIssueCommandType(UnitCommandType ct) {
        return canIssueCommandType_native(pointer, ct);
    }

    public boolean canIssueCommandType(UnitCommandType ct, boolean checkCommandibility) {
        return canIssueCommandType_native(pointer, ct, checkCommandibility);
    }

    /**
     * Performs some cheap checks to attempt to quickly detect whether the unit is unable to execute the given
     * command type (i.e. regardless of what other possible command parameters could be) as part of a Unitset.
     * Parameters ct A UnitCommandType. checkCommandibilityGrouped A boolean for whether to perform
     * UnitInterface::canCommandGrouped as a check. You can set this to false if you know this check has
     * already just been performed. checkCommandibility A boolean for whether to perform
     * UnitInterface::canCommand as a check. You can set this to false if you know this check has already just
     * been performed. Return values true if BWAPI was unable to determine whether the command type is
     * invalid. false if an error occurred and the command type is invalid. See also UnitCommandTypes,
     * Game::getLastError, UnitInterface::canIssueCommandGrouped
     */
    public boolean canIssueCommandTypeGrouped(UnitCommandType ct, boolean checkCommandibilityGrouped) {
        return canIssueCommandTypeGrouped_native(pointer, ct, checkCommandibilityGrouped);
    }

    public boolean canIssueCommandTypeGrouped(UnitCommandType ct) {
        return canIssueCommandTypeGrouped_native(pointer, ct);
    }

    public boolean canIssueCommandTypeGrouped(UnitCommandType ct, boolean checkCommandibilityGrouped, boolean checkCommandibility) {
        return canIssueCommandTypeGrouped_native(pointer, ct, checkCommandibilityGrouped, checkCommandibility);
    }

    /**
     * Performs some cheap checks to attempt to quickly detect whether the unit is unable to use the given
     * unit as the target unit of an unspecified command. Parameters targetUnit A target unit for an
     * unspecified command. checkCommandibility A boolean for whether to perform UnitInterface::canCommand as
     * a check. You can set this to false if you know this check has already just been performed. Return
     * values true if BWAPI was unable to determine whether the unit can target the given unit. false if an
     * error occurred and the unit can not target the given unit. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::isTargetable
     */
    public boolean canTargetUnit(Unit targetUnit) {
        return canTargetUnit_native(pointer, targetUnit);
    }

    public boolean canTargetUnit(Unit targetUnit, boolean checkCommandibility) {
        return canTargetUnit_native(pointer, targetUnit, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute an attack command to attack-move or attack a unit.
     * See also Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::attack,
     * UnitInterface::canAttackMove, UnitInterface::canAttackUnit
     */
    public boolean canAttack() {
        return canAttack_native(pointer);
    }

    public boolean canAttack(boolean checkCommandibility) {
        return canAttack_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute an attack command to attack-move or attack a unit.
     * See also Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::attack,
     * UnitInterface::canAttackMove, UnitInterface::canAttackUnit
     */
    public boolean canAttack(Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canAttack_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canAttack(Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canAttack_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canAttack(PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canAttack_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canAttack(Position target, boolean checkCanTargetUnit) {
        return canAttack_native(pointer, target, checkCanTargetUnit);
    }

    public boolean canAttack(Unit target, boolean checkCanTargetUnit) {
        return canAttack_native(pointer, target, checkCanTargetUnit);
    }

    public boolean canAttack(PositionOrUnit target, boolean checkCanTargetUnit) {
        return canAttack_native(pointer, target, checkCanTargetUnit);
    }

    public boolean canAttack(Position target) {
        return canAttack_native(pointer, target);
    }

    public boolean canAttack(Unit target) {
        return canAttack_native(pointer, target);
    }

    public boolean canAttack(PositionOrUnit target) {
        return canAttack_native(pointer, target);
    }

    public boolean canAttack(Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canAttack_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibility);
    }

    public boolean canAttack(Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canAttack_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibility);
    }

    public boolean canAttack(PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canAttack_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute an attack command to attack-move or attack a unit,
     * as part of a Unitset. See also Game::getLastError, UnitInterface::canIssueCommandGrouped,
     * UnitInterface::canAttack
     */
    public boolean canAttackGrouped(boolean checkCommandibilityGrouped) {
        return canAttackGrouped_native(pointer, checkCommandibilityGrouped);
    }

    public boolean canAttackGrouped() {
        return canAttackGrouped_native(pointer);
    }

    public boolean canAttackGrouped(boolean checkCommandibilityGrouped, boolean checkCommandibility) {
        return canAttackGrouped_native(pointer, checkCommandibilityGrouped, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute an attack command to attack-move or attack a unit,
     * as part of a Unitset. See also Game::getLastError, UnitInterface::canIssueCommandGrouped,
     * UnitInterface::canAttack
     */
    public boolean canAttackGrouped(Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped) {
        return canAttackGrouped_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibilityGrouped);
    }

    public boolean canAttackGrouped(Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped) {
        return canAttackGrouped_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibilityGrouped);
    }

    public boolean canAttackGrouped(PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped) {
        return canAttackGrouped_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibilityGrouped);
    }

    public boolean canAttackGrouped(Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canAttackGrouped_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canAttackGrouped(Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canAttackGrouped_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canAttackGrouped(PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canAttackGrouped_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canAttackGrouped(Position target, boolean checkCanTargetUnit) {
        return canAttackGrouped_native(pointer, target, checkCanTargetUnit);
    }

    public boolean canAttackGrouped(Unit target, boolean checkCanTargetUnit) {
        return canAttackGrouped_native(pointer, target, checkCanTargetUnit);
    }

    public boolean canAttackGrouped(PositionOrUnit target, boolean checkCanTargetUnit) {
        return canAttackGrouped_native(pointer, target, checkCanTargetUnit);
    }

    public boolean canAttackGrouped(Position target) {
        return canAttackGrouped_native(pointer, target);
    }

    public boolean canAttackGrouped(Unit target) {
        return canAttackGrouped_native(pointer, target);
    }

    public boolean canAttackGrouped(PositionOrUnit target) {
        return canAttackGrouped_native(pointer, target);
    }

    public boolean canAttackGrouped(Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped, boolean checkCommandibility) {
        return canAttackGrouped_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibilityGrouped, checkCommandibility);
    }

    public boolean canAttackGrouped(Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped, boolean checkCommandibility) {
        return canAttackGrouped_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibilityGrouped, checkCommandibility);
    }

    public boolean canAttackGrouped(PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped, boolean checkCommandibility) {
        return canAttackGrouped_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibilityGrouped, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute an attack command to attack-move. See also
     * Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::attack
     */
    public boolean canAttackMove() {
        return canAttackMove_native(pointer);
    }

    public boolean canAttackMove(boolean checkCommandibility) {
        return canAttackMove_native(pointer, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute an attack command to attack-move, as part of a Unitset. See
     * also Game::getLastError, UnitInterface::canIssueCommandGrouped, UnitInterface::canAttackMove
     */
    public boolean canAttackMoveGrouped(boolean checkCommandibilityGrouped) {
        return canAttackMoveGrouped_native(pointer, checkCommandibilityGrouped);
    }

    public boolean canAttackMoveGrouped() {
        return canAttackMoveGrouped_native(pointer);
    }

    public boolean canAttackMoveGrouped(boolean checkCommandibilityGrouped, boolean checkCommandibility) {
        return canAttackMoveGrouped_native(pointer, checkCommandibilityGrouped, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute an attack command to attack a unit. See also
     * Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::attack
     */
    public boolean canAttackUnit() {
        return canAttackUnit_native(pointer);
    }

    public boolean canAttackUnit(boolean checkCommandibility) {
        return canAttackUnit_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute an attack command to attack a unit. See also
     * Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::attack
     */
    public boolean canAttackUnit(Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canAttackUnit_native(pointer, targetUnit, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canAttackUnit(Unit targetUnit, boolean checkCanTargetUnit) {
        return canAttackUnit_native(pointer, targetUnit, checkCanTargetUnit);
    }

    public boolean canAttackUnit(Unit targetUnit) {
        return canAttackUnit_native(pointer, targetUnit);
    }

    public boolean canAttackUnit(Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canAttackUnit_native(pointer, targetUnit, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute an attack command to attack a unit, as part of a
     * Unitset. See also Game::getLastError, UnitInterface::canIssueCommandGrouped,
     * UnitInterface::canAttackUnit
     */
    public boolean canAttackUnitGrouped(boolean checkCommandibilityGrouped) {
        return canAttackUnitGrouped_native(pointer, checkCommandibilityGrouped);
    }

    public boolean canAttackUnitGrouped() {
        return canAttackUnitGrouped_native(pointer);
    }

    public boolean canAttackUnitGrouped(boolean checkCommandibilityGrouped, boolean checkCommandibility) {
        return canAttackUnitGrouped_native(pointer, checkCommandibilityGrouped, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute an attack command to attack a unit, as part of a
     * Unitset. See also Game::getLastError, UnitInterface::canIssueCommandGrouped,
     * UnitInterface::canAttackUnit
     */
    public boolean canAttackUnitGrouped(Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped) {
        return canAttackUnitGrouped_native(pointer, targetUnit, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibilityGrouped);
    }

    public boolean canAttackUnitGrouped(Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canAttackUnitGrouped_native(pointer, targetUnit, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canAttackUnitGrouped(Unit targetUnit, boolean checkCanTargetUnit) {
        return canAttackUnitGrouped_native(pointer, targetUnit, checkCanTargetUnit);
    }

    public boolean canAttackUnitGrouped(Unit targetUnit) {
        return canAttackUnitGrouped_native(pointer, targetUnit);
    }

    public boolean canAttackUnitGrouped(Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped, boolean checkCommandibility) {
        return canAttackUnitGrouped_native(pointer, targetUnit, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibilityGrouped, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a build command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::build
     */
    public boolean canBuild() {
        return canBuild_native(pointer);
    }

    public boolean canBuild(boolean checkCommandibility) {
        return canBuild_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a build command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::build
     */
    public boolean canBuild(UnitType uType, boolean checkCanIssueCommandType) {
        return canBuild_native(pointer, uType, checkCanIssueCommandType);
    }

    public boolean canBuild(UnitType uType) {
        return canBuild_native(pointer, uType);
    }

    public boolean canBuild(UnitType uType, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canBuild_native(pointer, uType, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a build command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::build
     */
    public boolean canBuild(UnitType uType, TilePosition tilePos, boolean checkTargetUnitType, boolean checkCanIssueCommandType) {
        return canBuild_native(pointer, uType, tilePos, checkTargetUnitType, checkCanIssueCommandType);
    }

    public boolean canBuild(UnitType uType, TilePosition tilePos, boolean checkTargetUnitType) {
        return canBuild_native(pointer, uType, tilePos, checkTargetUnitType);
    }

    public boolean canBuild(UnitType uType, TilePosition tilePos) {
        return canBuild_native(pointer, uType, tilePos);
    }

    public boolean canBuild(UnitType uType, TilePosition tilePos, boolean checkTargetUnitType, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canBuild_native(pointer, uType, tilePos, checkTargetUnitType, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a buildAddon command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::buildAddon
     */
    public boolean canBuildAddon() {
        return canBuildAddon_native(pointer);
    }

    public boolean canBuildAddon(boolean checkCommandibility) {
        return canBuildAddon_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a buildAddon command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::buildAddon
     */
    public boolean canBuildAddon(UnitType uType, boolean checkCanIssueCommandType) {
        return canBuildAddon_native(pointer, uType, checkCanIssueCommandType);
    }

    public boolean canBuildAddon(UnitType uType) {
        return canBuildAddon_native(pointer, uType);
    }

    public boolean canBuildAddon(UnitType uType, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canBuildAddon_native(pointer, uType, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a train command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::train
     */
    public boolean canTrain() {
        return canTrain_native(pointer);
    }

    public boolean canTrain(boolean checkCommandibility) {
        return canTrain_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a train command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::train
     */
    public boolean canTrain(UnitType uType, boolean checkCanIssueCommandType) {
        return canTrain_native(pointer, uType, checkCanIssueCommandType);
    }

    public boolean canTrain(UnitType uType) {
        return canTrain_native(pointer, uType);
    }

    public boolean canTrain(UnitType uType, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canTrain_native(pointer, uType, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a morph command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::morph
     */
    public boolean canMorph() {
        return canMorph_native(pointer);
    }

    public boolean canMorph(boolean checkCommandibility) {
        return canMorph_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a morph command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::morph
     */
    public boolean canMorph(UnitType uType, boolean checkCanIssueCommandType) {
        return canMorph_native(pointer, uType, checkCanIssueCommandType);
    }

    public boolean canMorph(UnitType uType) {
        return canMorph_native(pointer, uType);
    }

    public boolean canMorph(UnitType uType, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canMorph_native(pointer, uType, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a research command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::research
     */
    public boolean canResearch() {
        return canResearch_native(pointer);
    }

    public boolean canResearch(boolean checkCommandibility) {
        return canResearch_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a research command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::research
     */
    public boolean canResearch(TechType type) {
        return canResearch_native(pointer, type);
    }

    public boolean canResearch(TechType type, boolean checkCanIssueCommandType) {
        return canResearch_native(pointer, type, checkCanIssueCommandType);
    }

    /**
     * Cheap checks for whether the unit is able to execute an upgrade command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::upgrade
     */
    public boolean canUpgrade() {
        return canUpgrade_native(pointer);
    }

    public boolean canUpgrade(boolean checkCommandibility) {
        return canUpgrade_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute an upgrade command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::upgrade
     */
    public boolean canUpgrade(UpgradeType type) {
        return canUpgrade_native(pointer, type);
    }

    public boolean canUpgrade(UpgradeType type, boolean checkCanIssueCommandType) {
        return canUpgrade_native(pointer, type, checkCanIssueCommandType);
    }

    /**
     * Cheap checks for whether the unit is able to execute a setRallyPoint command to a position or unit. See
     * also Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::setRallyPoint,
     * UnitInterface::canSetRallyPosition, UnitInterface::canSetRallyUnit.
     */
    public boolean canSetRallyPoint() {
        return canSetRallyPoint_native(pointer);
    }

    public boolean canSetRallyPoint(boolean checkCommandibility) {
        return canSetRallyPoint_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a setRallyPoint command to a position or unit. See
     * also Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::setRallyPoint,
     * UnitInterface::canSetRallyPosition, UnitInterface::canSetRallyUnit.
     */
    public boolean canSetRallyPoint(Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canSetRallyPoint_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canSetRallyPoint(Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canSetRallyPoint_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canSetRallyPoint(PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canSetRallyPoint_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canSetRallyPoint(Position target, boolean checkCanTargetUnit) {
        return canSetRallyPoint_native(pointer, target, checkCanTargetUnit);
    }

    public boolean canSetRallyPoint(Unit target, boolean checkCanTargetUnit) {
        return canSetRallyPoint_native(pointer, target, checkCanTargetUnit);
    }

    public boolean canSetRallyPoint(PositionOrUnit target, boolean checkCanTargetUnit) {
        return canSetRallyPoint_native(pointer, target, checkCanTargetUnit);
    }

    public boolean canSetRallyPoint(Position target) {
        return canSetRallyPoint_native(pointer, target);
    }

    public boolean canSetRallyPoint(Unit target) {
        return canSetRallyPoint_native(pointer, target);
    }

    public boolean canSetRallyPoint(PositionOrUnit target) {
        return canSetRallyPoint_native(pointer, target);
    }

    public boolean canSetRallyPoint(Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canSetRallyPoint_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibility);
    }

    public boolean canSetRallyPoint(Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canSetRallyPoint_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibility);
    }

    public boolean canSetRallyPoint(PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canSetRallyPoint_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a setRallyPoint command to a position. See also
     * Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::setRallyPoint
     */
    public boolean canSetRallyPosition() {
        return canSetRallyPosition_native(pointer);
    }

    public boolean canSetRallyPosition(boolean checkCommandibility) {
        return canSetRallyPosition_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a setRallyPoint command to a unit. See also
     * Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::setRallyPoint
     */
    public boolean canSetRallyUnit() {
        return canSetRallyUnit_native(pointer);
    }

    public boolean canSetRallyUnit(boolean checkCommandibility) {
        return canSetRallyUnit_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a setRallyPoint command to a unit. See also
     * Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::setRallyPoint
     */
    public boolean canSetRallyUnit(Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canSetRallyUnit_native(pointer, targetUnit, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canSetRallyUnit(Unit targetUnit, boolean checkCanTargetUnit) {
        return canSetRallyUnit_native(pointer, targetUnit, checkCanTargetUnit);
    }

    public boolean canSetRallyUnit(Unit targetUnit) {
        return canSetRallyUnit_native(pointer, targetUnit);
    }

    public boolean canSetRallyUnit(Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canSetRallyUnit_native(pointer, targetUnit, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a move command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::move
     */
    public boolean canMove() {
        return canMove_native(pointer);
    }

    public boolean canMove(boolean checkCommandibility) {
        return canMove_native(pointer, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a move command, as part of a Unitset. See also
     * Game::getLastError, UnitInterface::canIssueCommandGrouped, UnitInterface::canMove
     */
    public boolean canMoveGrouped(boolean checkCommandibilityGrouped) {
        return canMoveGrouped_native(pointer, checkCommandibilityGrouped);
    }

    public boolean canMoveGrouped() {
        return canMoveGrouped_native(pointer);
    }

    public boolean canMoveGrouped(boolean checkCommandibilityGrouped, boolean checkCommandibility) {
        return canMoveGrouped_native(pointer, checkCommandibilityGrouped, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a patrol command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::patrol
     */
    public boolean canPatrol() {
        return canPatrol_native(pointer);
    }

    public boolean canPatrol(boolean checkCommandibility) {
        return canPatrol_native(pointer, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a patrol command, as part of a Unitset. See also
     * Game::getLastError, UnitInterface::canIssueCommandGrouped, UnitInterface::canPatrol
     */
    public boolean canPatrolGrouped(boolean checkCommandibilityGrouped) {
        return canPatrolGrouped_native(pointer, checkCommandibilityGrouped);
    }

    public boolean canPatrolGrouped() {
        return canPatrolGrouped_native(pointer);
    }

    public boolean canPatrolGrouped(boolean checkCommandibilityGrouped, boolean checkCommandibility) {
        return canPatrolGrouped_native(pointer, checkCommandibilityGrouped, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a follow command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::follow
     */
    public boolean canFollow() {
        return canFollow_native(pointer);
    }

    public boolean canFollow(boolean checkCommandibility) {
        return canFollow_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a follow command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::follow
     */
    public boolean canFollow(Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canFollow_native(pointer, targetUnit, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canFollow(Unit targetUnit, boolean checkCanTargetUnit) {
        return canFollow_native(pointer, targetUnit, checkCanTargetUnit);
    }

    public boolean canFollow(Unit targetUnit) {
        return canFollow_native(pointer, targetUnit);
    }

    public boolean canFollow(Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canFollow_native(pointer, targetUnit, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a gather command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::gather
     */
    public boolean canGather() {
        return canGather_native(pointer);
    }

    public boolean canGather(boolean checkCommandibility) {
        return canGather_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a gather command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::gather
     */
    public boolean canGather(Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canGather_native(pointer, targetUnit, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canGather(Unit targetUnit, boolean checkCanTargetUnit) {
        return canGather_native(pointer, targetUnit, checkCanTargetUnit);
    }

    public boolean canGather(Unit targetUnit) {
        return canGather_native(pointer, targetUnit);
    }

    public boolean canGather(Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canGather_native(pointer, targetUnit, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a returnCargo command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::returnCargo
     */
    public boolean canReturnCargo() {
        return canReturnCargo_native(pointer);
    }

    public boolean canReturnCargo(boolean checkCommandibility) {
        return canReturnCargo_native(pointer, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a holdPosition command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::holdPosition
     */
    public boolean canHoldPosition() {
        return canHoldPosition_native(pointer);
    }

    public boolean canHoldPosition(boolean checkCommandibility) {
        return canHoldPosition_native(pointer, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a stop command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::stop
     */
    public boolean canStop() {
        return canStop_native(pointer);
    }

    public boolean canStop(boolean checkCommandibility) {
        return canStop_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a repair command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::repair
     */
    public boolean canRepair() {
        return canRepair_native(pointer);
    }

    public boolean canRepair(boolean checkCommandibility) {
        return canRepair_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a repair command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::repair
     */
    public boolean canRepair(Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canRepair_native(pointer, targetUnit, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canRepair(Unit targetUnit, boolean checkCanTargetUnit) {
        return canRepair_native(pointer, targetUnit, checkCanTargetUnit);
    }

    public boolean canRepair(Unit targetUnit) {
        return canRepair_native(pointer, targetUnit);
    }

    public boolean canRepair(Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canRepair_native(pointer, targetUnit, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a burrow command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::burrow
     */
    public boolean canBurrow() {
        return canBurrow_native(pointer);
    }

    public boolean canBurrow(boolean checkCommandibility) {
        return canBurrow_native(pointer, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute an unburrow command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::unburrow
     */
    public boolean canUnburrow() {
        return canUnburrow_native(pointer);
    }

    public boolean canUnburrow(boolean checkCommandibility) {
        return canUnburrow_native(pointer, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a cloak command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::cloak
     */
    public boolean canCloak() {
        return canCloak_native(pointer);
    }

    public boolean canCloak(boolean checkCommandibility) {
        return canCloak_native(pointer, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a decloak command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::decloak
     */
    public boolean canDecloak() {
        return canDecloak_native(pointer);
    }

    public boolean canDecloak(boolean checkCommandibility) {
        return canDecloak_native(pointer, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a siege command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::siege
     */
    public boolean canSiege() {
        return canSiege_native(pointer);
    }

    public boolean canSiege(boolean checkCommandibility) {
        return canSiege_native(pointer, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute an unsiege command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::unsiege
     */
    public boolean canUnsiege() {
        return canUnsiege_native(pointer);
    }

    public boolean canUnsiege(boolean checkCommandibility) {
        return canUnsiege_native(pointer, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a lift command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::lift
     */
    public boolean canLift() {
        return canLift_native(pointer);
    }

    public boolean canLift(boolean checkCommandibility) {
        return canLift_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a land command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::land
     */
    public boolean canLand() {
        return canLand_native(pointer);
    }

    public boolean canLand(boolean checkCommandibility) {
        return canLand_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a land command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::land
     */
    public boolean canLand(TilePosition target, boolean checkCanIssueCommandType) {
        return canLand_native(pointer, target, checkCanIssueCommandType);
    }

    public boolean canLand(TilePosition target) {
        return canLand_native(pointer, target);
    }

    public boolean canLand(TilePosition target, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canLand_native(pointer, target, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a load command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::load
     */
    public boolean canLoad() {
        return canLoad_native(pointer);
    }

    public boolean canLoad(boolean checkCommandibility) {
        return canLoad_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a load command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::load
     */
    public boolean canLoad(Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canLoad_native(pointer, targetUnit, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canLoad(Unit targetUnit, boolean checkCanTargetUnit) {
        return canLoad_native(pointer, targetUnit, checkCanTargetUnit);
    }

    public boolean canLoad(Unit targetUnit) {
        return canLoad_native(pointer, targetUnit);
    }

    public boolean canLoad(Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canLoad_native(pointer, targetUnit, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute an unload command or unloadAll at current position
     * command or unloadAll at a different position command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::unload, UnitInterface::unloadAll
     */
    public boolean canUnloadWithOrWithoutTarget() {
        return canUnloadWithOrWithoutTarget_native(pointer);
    }

    public boolean canUnloadWithOrWithoutTarget(boolean checkCommandibility) {
        return canUnloadWithOrWithoutTarget_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute an unload command or unloadAll at current position
     * command or unloadAll at a different position command, for a given position. See also
     * Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::unload, UnitInterface::unloadAll
     */
    public boolean canUnloadAtPosition(Position targDropPos, boolean checkCanIssueCommandType) {
        return canUnloadAtPosition_native(pointer, targDropPos, checkCanIssueCommandType);
    }

    public boolean canUnloadAtPosition(Position targDropPos) {
        return canUnloadAtPosition_native(pointer, targDropPos);
    }

    public boolean canUnloadAtPosition(Position targDropPos, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canUnloadAtPosition_native(pointer, targDropPos, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute an unload command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::unload
     */
    public boolean canUnload() {
        return canUnload_native(pointer);
    }

    public boolean canUnload(boolean checkCommandibility) {
        return canUnload_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute an unload command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::unload
     */
    public boolean canUnload(Unit targetUnit, boolean checkCanTargetUnit, boolean checkPosition, boolean checkCanIssueCommandType) {
        return canUnload_native(pointer, targetUnit, checkCanTargetUnit, checkPosition, checkCanIssueCommandType);
    }

    public boolean canUnload(Unit targetUnit, boolean checkCanTargetUnit, boolean checkPosition) {
        return canUnload_native(pointer, targetUnit, checkCanTargetUnit, checkPosition);
    }

    public boolean canUnload(Unit targetUnit, boolean checkCanTargetUnit) {
        return canUnload_native(pointer, targetUnit, checkCanTargetUnit);
    }

    public boolean canUnload(Unit targetUnit) {
        return canUnload_native(pointer, targetUnit);
    }

    public boolean canUnload(Unit targetUnit, boolean checkCanTargetUnit, boolean checkPosition, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canUnload_native(pointer, targetUnit, checkCanTargetUnit, checkPosition, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute an unloadAll command for the current position. See also
     * Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::unloadAll
     */
    public boolean canUnloadAll() {
        return canUnloadAll_native(pointer);
    }

    public boolean canUnloadAll(boolean checkCommandibility) {
        return canUnloadAll_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute an unloadAll command for a different position. See
     * also Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::unloadAll
     */
    public boolean canUnloadAllPosition() {
        return canUnloadAllPosition_native(pointer);
    }

    public boolean canUnloadAllPosition(boolean checkCommandibility) {
        return canUnloadAllPosition_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute an unloadAll command for a different position. See
     * also Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::unloadAll
     */
    public boolean canUnloadAllPosition(Position targDropPos, boolean checkCanIssueCommandType) {
        return canUnloadAllPosition_native(pointer, targDropPos, checkCanIssueCommandType);
    }

    public boolean canUnloadAllPosition(Position targDropPos) {
        return canUnloadAllPosition_native(pointer, targDropPos);
    }

    public boolean canUnloadAllPosition(Position targDropPos, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canUnloadAllPosition_native(pointer, targDropPos, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a rightClick command to a position or unit. See
     * also Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::rightClick,
     * UnitInterface::canRightClickPosition, UnitInterface::canRightClickUnit.
     */
    public boolean canRightClick() {
        return canRightClick_native(pointer);
    }

    public boolean canRightClick(boolean checkCommandibility) {
        return canRightClick_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a rightClick command to a position or unit. See
     * also Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::rightClick,
     * UnitInterface::canRightClickPosition, UnitInterface::canRightClickUnit.
     */
    public boolean canRightClick(Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canRightClick_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canRightClick(Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canRightClick_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canRightClick(PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canRightClick_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canRightClick(Position target, boolean checkCanTargetUnit) {
        return canRightClick_native(pointer, target, checkCanTargetUnit);
    }

    public boolean canRightClick(Unit target, boolean checkCanTargetUnit) {
        return canRightClick_native(pointer, target, checkCanTargetUnit);
    }

    public boolean canRightClick(PositionOrUnit target, boolean checkCanTargetUnit) {
        return canRightClick_native(pointer, target, checkCanTargetUnit);
    }

    public boolean canRightClick(Position target) {
        return canRightClick_native(pointer, target);
    }

    public boolean canRightClick(Unit target) {
        return canRightClick_native(pointer, target);
    }

    public boolean canRightClick(PositionOrUnit target) {
        return canRightClick_native(pointer, target);
    }

    public boolean canRightClick(Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canRightClick_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibility);
    }

    public boolean canRightClick(Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canRightClick_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibility);
    }

    public boolean canRightClick(PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canRightClick_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a rightClick command to a position or unit, as
     * part of a Unitset. See also Game::getLastError, UnitInterface::canIssueCommandGrouped,
     * UnitInterface::canRightClickUnit
     */
    public boolean canRightClickGrouped(boolean checkCommandibilityGrouped) {
        return canRightClickGrouped_native(pointer, checkCommandibilityGrouped);
    }

    public boolean canRightClickGrouped() {
        return canRightClickGrouped_native(pointer);
    }

    public boolean canRightClickGrouped(boolean checkCommandibilityGrouped, boolean checkCommandibility) {
        return canRightClickGrouped_native(pointer, checkCommandibilityGrouped, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a rightClick command to a position or unit, as
     * part of a Unitset. See also Game::getLastError, UnitInterface::canIssueCommandGrouped,
     * UnitInterface::canRightClickUnit
     */
    public boolean canRightClickGrouped(Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped) {
        return canRightClickGrouped_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibilityGrouped);
    }

    public boolean canRightClickGrouped(Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped) {
        return canRightClickGrouped_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibilityGrouped);
    }

    public boolean canRightClickGrouped(PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped) {
        return canRightClickGrouped_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibilityGrouped);
    }

    public boolean canRightClickGrouped(Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canRightClickGrouped_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canRightClickGrouped(Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canRightClickGrouped_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canRightClickGrouped(PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canRightClickGrouped_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canRightClickGrouped(Position target, boolean checkCanTargetUnit) {
        return canRightClickGrouped_native(pointer, target, checkCanTargetUnit);
    }

    public boolean canRightClickGrouped(Unit target, boolean checkCanTargetUnit) {
        return canRightClickGrouped_native(pointer, target, checkCanTargetUnit);
    }

    public boolean canRightClickGrouped(PositionOrUnit target, boolean checkCanTargetUnit) {
        return canRightClickGrouped_native(pointer, target, checkCanTargetUnit);
    }

    public boolean canRightClickGrouped(Position target) {
        return canRightClickGrouped_native(pointer, target);
    }

    public boolean canRightClickGrouped(Unit target) {
        return canRightClickGrouped_native(pointer, target);
    }

    public boolean canRightClickGrouped(PositionOrUnit target) {
        return canRightClickGrouped_native(pointer, target);
    }

    public boolean canRightClickGrouped(Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped, boolean checkCommandibility) {
        return canRightClickGrouped_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibilityGrouped, checkCommandibility);
    }

    public boolean canRightClickGrouped(Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped, boolean checkCommandibility) {
        return canRightClickGrouped_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibilityGrouped, checkCommandibility);
    }

    public boolean canRightClickGrouped(PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped, boolean checkCommandibility) {
        return canRightClickGrouped_native(pointer, target, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibilityGrouped, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a rightClick command for a position. See also
     * Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::rightClick
     */
    public boolean canRightClickPosition() {
        return canRightClickPosition_native(pointer);
    }

    public boolean canRightClickPosition(boolean checkCommandibility) {
        return canRightClickPosition_native(pointer, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a rightClick command for a position, as part of a Unitset.
     * See also Game::getLastError, UnitInterface::canIssueCommandGrouped, UnitInterface::canRightClick
     */
    public boolean canRightClickPositionGrouped(boolean checkCommandibilityGrouped) {
        return canRightClickPositionGrouped_native(pointer, checkCommandibilityGrouped);
    }

    public boolean canRightClickPositionGrouped() {
        return canRightClickPositionGrouped_native(pointer);
    }

    public boolean canRightClickPositionGrouped(boolean checkCommandibilityGrouped, boolean checkCommandibility) {
        return canRightClickPositionGrouped_native(pointer, checkCommandibilityGrouped, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a rightClick command to a unit. See also
     * Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::rightClick
     */
    public boolean canRightClickUnit() {
        return canRightClickUnit_native(pointer);
    }

    public boolean canRightClickUnit(boolean checkCommandibility) {
        return canRightClickUnit_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a rightClick command to a unit. See also
     * Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::rightClick
     */
    public boolean canRightClickUnit(Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canRightClickUnit_native(pointer, targetUnit, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canRightClickUnit(Unit targetUnit, boolean checkCanTargetUnit) {
        return canRightClickUnit_native(pointer, targetUnit, checkCanTargetUnit);
    }

    public boolean canRightClickUnit(Unit targetUnit) {
        return canRightClickUnit_native(pointer, targetUnit);
    }

    public boolean canRightClickUnit(Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canRightClickUnit_native(pointer, targetUnit, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a rightClick command to a unit, as part of a
     * Unitset. See also Game::getLastError, UnitInterface::canIssueCommandGrouped,
     * UnitInterface::canRightClickUnit
     */
    public boolean canRightClickUnitGrouped(boolean checkCommandibilityGrouped) {
        return canRightClickUnitGrouped_native(pointer, checkCommandibilityGrouped);
    }

    public boolean canRightClickUnitGrouped() {
        return canRightClickUnitGrouped_native(pointer);
    }

    public boolean canRightClickUnitGrouped(boolean checkCommandibilityGrouped, boolean checkCommandibility) {
        return canRightClickUnitGrouped_native(pointer, checkCommandibilityGrouped, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a rightClick command to a unit, as part of a
     * Unitset. See also Game::getLastError, UnitInterface::canIssueCommandGrouped,
     * UnitInterface::canRightClickUnit
     */
    public boolean canRightClickUnitGrouped(Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped) {
        return canRightClickUnitGrouped_native(pointer, targetUnit, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibilityGrouped);
    }

    public boolean canRightClickUnitGrouped(Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType) {
        return canRightClickUnitGrouped_native(pointer, targetUnit, checkCanTargetUnit, checkCanIssueCommandType);
    }

    public boolean canRightClickUnitGrouped(Unit targetUnit, boolean checkCanTargetUnit) {
        return canRightClickUnitGrouped_native(pointer, targetUnit, checkCanTargetUnit);
    }

    public boolean canRightClickUnitGrouped(Unit targetUnit) {
        return canRightClickUnitGrouped_native(pointer, targetUnit);
    }

    public boolean canRightClickUnitGrouped(Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped, boolean checkCommandibility) {
        return canRightClickUnitGrouped_native(pointer, targetUnit, checkCanTargetUnit, checkCanIssueCommandType, checkCommandibilityGrouped, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a haltConstruction command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::haltConstruction
     */
    public boolean canHaltConstruction() {
        return canHaltConstruction_native(pointer);
    }

    public boolean canHaltConstruction(boolean checkCommandibility) {
        return canHaltConstruction_native(pointer, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a cancelConstruction command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::cancelConstruction
     */
    public boolean canCancelConstruction() {
        return canCancelConstruction_native(pointer);
    }

    public boolean canCancelConstruction(boolean checkCommandibility) {
        return canCancelConstruction_native(pointer, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a cancelAddon command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::cancelAddon
     */
    public boolean canCancelAddon() {
        return canCancelAddon_native(pointer);
    }

    public boolean canCancelAddon(boolean checkCommandibility) {
        return canCancelAddon_native(pointer, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a cancelTrain command for any slot. See also
     * Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::cancelTrain
     */
    public boolean canCancelTrain() {
        return canCancelTrain_native(pointer);
    }

    public boolean canCancelTrain(boolean checkCommandibility) {
        return canCancelTrain_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a cancelTrain command for an unspecified slot. See
     * also Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::cancelTrain
     */
    public boolean canCancelTrainSlot() {
        return canCancelTrainSlot_native(pointer);
    }

    public boolean canCancelTrainSlot(boolean checkCommandibility) {
        return canCancelTrainSlot_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a cancelTrain command for an unspecified slot. See
     * also Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::cancelTrain
     */
    public boolean canCancelTrainSlot(int slot, boolean checkCanIssueCommandType) {
        return canCancelTrainSlot_native(pointer, slot, checkCanIssueCommandType);
    }

    public boolean canCancelTrainSlot(int slot) {
        return canCancelTrainSlot_native(pointer, slot);
    }

    public boolean canCancelTrainSlot(int slot, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canCancelTrainSlot_native(pointer, slot, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a cancelMorph command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::cancelMorph
     */
    public boolean canCancelMorph() {
        return canCancelMorph_native(pointer);
    }

    public boolean canCancelMorph(boolean checkCommandibility) {
        return canCancelMorph_native(pointer, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a cancelResearch command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::cancelResearch
     */
    public boolean canCancelResearch() {
        return canCancelResearch_native(pointer);
    }

    public boolean canCancelResearch(boolean checkCommandibility) {
        return canCancelResearch_native(pointer, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a cancelUpgrade command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::cancelUpgrade
     */
    public boolean canCancelUpgrade() {
        return canCancelUpgrade_native(pointer);
    }

    public boolean canCancelUpgrade(boolean checkCommandibility) {
        return canCancelUpgrade_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a useTech command without a target or or a useTech
     * command with a target position or a useTech command with a target unit. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::useTech
     */
    public boolean canUseTechWithOrWithoutTarget() {
        return canUseTechWithOrWithoutTarget_native(pointer);
    }

    public boolean canUseTechWithOrWithoutTarget(boolean checkCommandibility) {
        return canUseTechWithOrWithoutTarget_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a useTech command without a target or or a useTech
     * command with a target position or a useTech command with a target unit. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::useTech
     */
    public boolean canUseTechWithOrWithoutTarget(TechType tech, boolean checkCanIssueCommandType) {
        return canUseTechWithOrWithoutTarget_native(pointer, tech, checkCanIssueCommandType);
    }

    public boolean canUseTechWithOrWithoutTarget(TechType tech) {
        return canUseTechWithOrWithoutTarget_native(pointer, tech);
    }

    public boolean canUseTechWithOrWithoutTarget(TechType tech, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canUseTechWithOrWithoutTarget_native(pointer, tech, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a useTech command for a specified position or unit (only
     * specify nullptr if the TechType does not target another position/unit). See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::useTech, UnitInterface::canUseTechWithoutTarget,
     * UnitInterface::canUseTechUnit, UnitInterface::canUseTechPosition
     */
    public boolean canUseTech(TechType tech, Position target, boolean checkCanTargetUnit, boolean checkTargetsType, boolean checkCanIssueCommandType) {
        return canUseTech_native(pointer, tech, target, checkCanTargetUnit, checkTargetsType, checkCanIssueCommandType);
    }

    public boolean canUseTech(TechType tech, Unit target, boolean checkCanTargetUnit, boolean checkTargetsType, boolean checkCanIssueCommandType) {
        return canUseTech_native(pointer, tech, target, checkCanTargetUnit, checkTargetsType, checkCanIssueCommandType);
    }

    public boolean canUseTech(TechType tech, PositionOrUnit target, boolean checkCanTargetUnit, boolean checkTargetsType, boolean checkCanIssueCommandType) {
        return canUseTech_native(pointer, tech, target, checkCanTargetUnit, checkTargetsType, checkCanIssueCommandType);
    }

    public boolean canUseTech(TechType tech, Position target, boolean checkCanTargetUnit, boolean checkTargetsType) {
        return canUseTech_native(pointer, tech, target, checkCanTargetUnit, checkTargetsType);
    }

    public boolean canUseTech(TechType tech, Unit target, boolean checkCanTargetUnit, boolean checkTargetsType) {
        return canUseTech_native(pointer, tech, target, checkCanTargetUnit, checkTargetsType);
    }

    public boolean canUseTech(TechType tech, PositionOrUnit target, boolean checkCanTargetUnit, boolean checkTargetsType) {
        return canUseTech_native(pointer, tech, target, checkCanTargetUnit, checkTargetsType);
    }

    public boolean canUseTech(TechType tech, Position target, boolean checkCanTargetUnit) {
        return canUseTech_native(pointer, tech, target, checkCanTargetUnit);
    }

    public boolean canUseTech(TechType tech, Unit target, boolean checkCanTargetUnit) {
        return canUseTech_native(pointer, tech, target, checkCanTargetUnit);
    }

    public boolean canUseTech(TechType tech, PositionOrUnit target, boolean checkCanTargetUnit) {
        return canUseTech_native(pointer, tech, target, checkCanTargetUnit);
    }

    public boolean canUseTech(TechType tech, Position target) {
        return canUseTech_native(pointer, tech, target);
    }

    public boolean canUseTech(TechType tech, Unit target) {
        return canUseTech_native(pointer, tech, target);
    }

    public boolean canUseTech(TechType tech, PositionOrUnit target) {
        return canUseTech_native(pointer, tech, target);
    }

    public boolean canUseTech(TechType tech) {
        return canUseTech_native(pointer, tech);
    }

    public boolean canUseTech(TechType tech, Position target, boolean checkCanTargetUnit, boolean checkTargetsType, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canUseTech_native(pointer, tech, target, checkCanTargetUnit, checkTargetsType, checkCanIssueCommandType, checkCommandibility);
    }

    public boolean canUseTech(TechType tech, Unit target, boolean checkCanTargetUnit, boolean checkTargetsType, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canUseTech_native(pointer, tech, target, checkCanTargetUnit, checkTargetsType, checkCanIssueCommandType, checkCommandibility);
    }

    public boolean canUseTech(TechType tech, PositionOrUnit target, boolean checkCanTargetUnit, boolean checkTargetsType, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canUseTech_native(pointer, tech, target, checkCanTargetUnit, checkTargetsType, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a useTech command without a target. See also
     * Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::useTech
     */
    public boolean canUseTechWithoutTarget(TechType tech, boolean checkCanIssueCommandType) {
        return canUseTechWithoutTarget_native(pointer, tech, checkCanIssueCommandType);
    }

    public boolean canUseTechWithoutTarget(TechType tech) {
        return canUseTechWithoutTarget_native(pointer, tech);
    }

    public boolean canUseTechWithoutTarget(TechType tech, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canUseTechWithoutTarget_native(pointer, tech, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a useTech command with an unspecified target unit.
     * See also Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::useTech
     */
    public boolean canUseTechUnit(TechType tech, boolean checkCanIssueCommandType) {
        return canUseTechUnit_native(pointer, tech, checkCanIssueCommandType);
    }

    public boolean canUseTechUnit(TechType tech) {
        return canUseTechUnit_native(pointer, tech);
    }

    public boolean canUseTechUnit(TechType tech, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canUseTechUnit_native(pointer, tech, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a useTech command with an unspecified target unit.
     * See also Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::useTech
     */
    public boolean canUseTechUnit(TechType tech, Unit targetUnit, boolean checkCanTargetUnit, boolean checkTargetsUnits, boolean checkCanIssueCommandType) {
        return canUseTechUnit_native(pointer, tech, targetUnit, checkCanTargetUnit, checkTargetsUnits, checkCanIssueCommandType);
    }

    public boolean canUseTechUnit(TechType tech, Unit targetUnit, boolean checkCanTargetUnit, boolean checkTargetsUnits) {
        return canUseTechUnit_native(pointer, tech, targetUnit, checkCanTargetUnit, checkTargetsUnits);
    }

    public boolean canUseTechUnit(TechType tech, Unit targetUnit, boolean checkCanTargetUnit) {
        return canUseTechUnit_native(pointer, tech, targetUnit, checkCanTargetUnit);
    }

    public boolean canUseTechUnit(TechType tech, Unit targetUnit) {
        return canUseTechUnit_native(pointer, tech, targetUnit);
    }

    public boolean canUseTechUnit(TechType tech, Unit targetUnit, boolean checkCanTargetUnit, boolean checkTargetsUnits, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canUseTechUnit_native(pointer, tech, targetUnit, checkCanTargetUnit, checkTargetsUnits, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a useTech command with an unspecified target position. See
     * also Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::useTech
     */
    public boolean canUseTechPosition(TechType tech, boolean checkCanIssueCommandType) {
        return canUseTechPosition_native(pointer, tech, checkCanIssueCommandType);
    }

    public boolean canUseTechPosition(TechType tech) {
        return canUseTechPosition_native(pointer, tech);
    }

    public boolean canUseTechPosition(TechType tech, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canUseTechPosition_native(pointer, tech, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Checks whether the unit is able to execute a useTech command with an unspecified target position. See
     * also Game::getLastError, UnitInterface::canIssueCommand, UnitInterface::useTech
     */
    public boolean canUseTechPosition(TechType tech, Position target, boolean checkTargetsPositions, boolean checkCanIssueCommandType) {
        return canUseTechPosition_native(pointer, tech, target, checkTargetsPositions, checkCanIssueCommandType);
    }

    public boolean canUseTechPosition(TechType tech, Position target, boolean checkTargetsPositions) {
        return canUseTechPosition_native(pointer, tech, target, checkTargetsPositions);
    }

    public boolean canUseTechPosition(TechType tech, Position target) {
        return canUseTechPosition_native(pointer, tech, target);
    }

    public boolean canUseTechPosition(TechType tech, Position target, boolean checkTargetsPositions, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canUseTechPosition_native(pointer, tech, target, checkTargetsPositions, checkCanIssueCommandType, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a placeCOP command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::placeCOP
     */
    public boolean canPlaceCOP() {
        return canPlaceCOP_native(pointer);
    }

    public boolean canPlaceCOP(boolean checkCommandibility) {
        return canPlaceCOP_native(pointer, checkCommandibility);
    }

    /**
     * Cheap checks for whether the unit is able to execute a placeCOP command. See also Game::getLastError,
     * UnitInterface::canIssueCommand, UnitInterface::placeCOP
     */
    public boolean canPlaceCOP(TilePosition target, boolean checkCanIssueCommandType) {
        return canPlaceCOP_native(pointer, target, checkCanIssueCommandType);
    }

    public boolean canPlaceCOP(TilePosition target) {
        return canPlaceCOP_native(pointer, target);
    }

    public boolean canPlaceCOP(TilePosition target, boolean checkCanIssueCommandType, boolean checkCommandibility) {
        return canPlaceCOP_native(pointer, target, checkCanIssueCommandType, checkCommandibility);
    }

    private static Map<Long, Unit> instances = new HashMap<Long, Unit>();

    public Unit(long pointer) {
        this.pointer = pointer;
//        if (!isNeutralUnit()) {
        atlantisInit(); // @AtlantisChange
//        }
    }

    private static Unit get(long pointer) {
        if (pointer == 0) {
            return null;
        }
        Unit instance = instances.get(pointer);
        if (instance == null) {
            instance = new Unit(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native int getID_native(long pointer);

    private native boolean exists_native(long pointer);

    private native int getReplayID_native(long pointer);

    private native Player getPlayer_native(long pointer);

    private native UnitType getType_native(long pointer);

    private native Position getPosition_native(long pointer);

    private native TilePosition getTilePosition_native(long pointer);

    private native double getAngle_native(long pointer);

    private native double getVelocityX_native(long pointer);

    private native double getVelocityY_native(long pointer);

    private native Region getRegion_native(long pointer);

    private native int getLeft_native(long pointer);

    private native int getTop_native(long pointer);

    private native int getRight_native(long pointer);

    private native int getBottom_native(long pointer);

    private native int getHitPoints_native(long pointer);

    private native int getShields_native(long pointer);

    private native int getEnergy_native(long pointer);

    private native int getResources_native(long pointer);

    private native int getResourceGroup_native(long pointer);

    private native int getDistance_native(long pointer, Position target);

    private native int getDistance_native(long pointer, Unit target);

    private native int getDistance_native(long pointer, PositionOrUnit target);

    private native boolean hasPath_native(long pointer, Position target);

    private native boolean hasPath_native(long pointer, Unit target);

    private native boolean hasPath_native(long pointer, PositionOrUnit target);

    private native int getLastCommandFrame_native(long pointer);

    private native UnitCommand getLastCommand_native(long pointer);

    private native Player getLastAttackingPlayer_native(long pointer);

    private native UnitType getInitialType_native(long pointer);

    private native Position getInitialPosition_native(long pointer);

    private native TilePosition getInitialTilePosition_native(long pointer);

    private native int getInitialHitPoints_native(long pointer);

    private native int getInitialResources_native(long pointer);

    private native int getKillCount_native(long pointer);

    private native int getAcidSporeCount_native(long pointer);

    private native int getInterceptorCount_native(long pointer);

    private native int getScarabCount_native(long pointer);

    private native int getSpiderMineCount_native(long pointer);

    private native int getGroundWeaponCooldown_native(long pointer);

    private native int getAirWeaponCooldown_native(long pointer);

    private native int getSpellCooldown_native(long pointer);

    private native int getDefenseMatrixPoints_native(long pointer);

    private native int getDefenseMatrixTimer_native(long pointer);

    private native int getEnsnareTimer_native(long pointer);

    private native int getIrradiateTimer_native(long pointer);

    private native int getLockdownTimer_native(long pointer);

    private native int getMaelstromTimer_native(long pointer);

    private native int getOrderTimer_native(long pointer);

    private native int getPlagueTimer_native(long pointer);

    private native int getRemoveTimer_native(long pointer);

    private native int getStasisTimer_native(long pointer);

    private native int getStimTimer_native(long pointer);

    private native UnitType getBuildType_native(long pointer);

    private native List<UnitType> getTrainingQueue_native(long pointer);

    private native TechType getTech_native(long pointer);

    private native UpgradeType getUpgrade_native(long pointer);

    private native int getRemainingBuildTime_native(long pointer);

    private native int getRemainingTrainTime_native(long pointer);

    private native int getRemainingResearchTime_native(long pointer);

    private native int getRemainingUpgradeTime_native(long pointer);

    private native Unit getBuildUnit_native(long pointer);

    private native Unit getTarget_native(long pointer);

    private native Position getTargetPosition_native(long pointer);

    private native Order getOrder_native(long pointer);

    private native Order getSecondaryOrder_native(long pointer);

    private native Unit getOrderTarget_native(long pointer);

    private native Position getOrderTargetPosition_native(long pointer);

    private native Position getRallyPosition_native(long pointer);

    private native Unit getRallyUnit_native(long pointer);

    private native Unit getAddon_native(long pointer);

    private native Unit getNydusExit_native(long pointer);

    private native Unit getPowerUp_native(long pointer);

    private native Unit getTransport_native(long pointer);

    private native List<Unit> getLoadedUnits_native(long pointer);

    private native int getSpaceRemaining_native(long pointer);

    private native Unit getCarrier_native(long pointer);

    private native List<Unit> getInterceptors_native(long pointer);

    private native Unit getHatchery_native(long pointer);

    private native List<Unit> getLarva_native(long pointer);

    private native List<Unit> getUnitsInRadius_native(long pointer, int radius);

    private native List<Unit> getUnitsInWeaponRange_native(long pointer, WeaponType weapon);

    private native boolean hasNuke_native(long pointer);

    private native boolean isAccelerating_native(long pointer);

    private native boolean isAttacking_native(long pointer);

    private native boolean isAttackFrame_native(long pointer);

    private native boolean isBeingConstructed_native(long pointer);

    private native boolean isBeingGathered_native(long pointer);

    private native boolean isBeingHealed_native(long pointer);

    private native boolean isBlind_native(long pointer);

    private native boolean isBraking_native(long pointer);

    private native boolean isBurrowed_native(long pointer);

    private native boolean isCarryingGas_native(long pointer);

    private native boolean isCarryingMinerals_native(long pointer);

    private native boolean isCloaked_native(long pointer);

    private native boolean isCompleted_native(long pointer);

    private native boolean isConstructing_native(long pointer);

    private native boolean isDefenseMatrixed_native(long pointer);

    private native boolean isDetected_native(long pointer);

    private native boolean isEnsnared_native(long pointer);

    private native boolean isFlying_native(long pointer);

    private native boolean isFollowing_native(long pointer);

    private native boolean isGatheringGas_native(long pointer);

    private native boolean isGatheringMinerals_native(long pointer);

    private native boolean isHallucination_native(long pointer);

    private native boolean isHoldingPosition_native(long pointer);

    private native boolean isIdle_native(long pointer);

    private native boolean isInterruptible_native(long pointer);

    private native boolean isInvincible_native(long pointer);

    private native boolean isInWeaponRange_native(long pointer, Unit target);

    private native boolean isIrradiated_native(long pointer);

    private native boolean isLifted_native(long pointer);

    private native boolean isLoaded_native(long pointer);

    private native boolean isLockedDown_native(long pointer);

    private native boolean isMaelstrommed_native(long pointer);

    private native boolean isMorphing_native(long pointer);

    private native boolean isMoving_native(long pointer);

    private native boolean isParasited_native(long pointer);

    private native boolean isPatrolling_native(long pointer);

    private native boolean isPlagued_native(long pointer);

    private native boolean isRepairing_native(long pointer);

    private native boolean isResearching_native(long pointer);

    private native boolean isSelected_native(long pointer);

    private native boolean isSieged_native(long pointer);

    private native boolean isStartingAttack_native(long pointer);

    private native boolean isStasised_native(long pointer);

    private native boolean isStimmed_native(long pointer);

    private native boolean isStuck_native(long pointer);

    private native boolean isTraining_native(long pointer);

    private native boolean isUnderAttack_native(long pointer);

    private native boolean isUnderDarkSwarm_native(long pointer);

    private native boolean isUnderDisruptionWeb_native(long pointer);

    private native boolean isUnderStorm_native(long pointer);

    private native boolean isPowered_native(long pointer);

    private native boolean isUpgrading_native(long pointer);

    private native boolean isVisible_native(long pointer);

    private native boolean isVisible_native(long pointer, Player player);

    private native boolean isTargetable_native(long pointer);

    private native boolean issueCommand_native(long pointer, UnitCommand command);

    private native boolean attack_native(long pointer, Position target);

    private native boolean attack_native(long pointer, Unit target);

    private native boolean attack_native(long pointer, PositionOrUnit target);

    private native boolean attack_native(long pointer, Position target, boolean shiftQueueCommand);

    private native boolean attack_native(long pointer, Unit target, boolean shiftQueueCommand);

    private native boolean attack_native(long pointer, PositionOrUnit target, boolean shiftQueueCommand);

    private native boolean build_native(long pointer, UnitType type);

    private native boolean build_native(long pointer, UnitType type, TilePosition target);

    private native boolean buildAddon_native(long pointer, UnitType type);

    private native boolean train_native(long pointer);

    private native boolean train_native(long pointer, UnitType type);

    private native boolean morph_native(long pointer, UnitType type);

    private native boolean research_native(long pointer, TechType tech);

    private native boolean upgrade_native(long pointer, UpgradeType upgrade);

    private native boolean setRallyPoint_native(long pointer, Position target);

    private native boolean setRallyPoint_native(long pointer, Unit target);

    private native boolean setRallyPoint_native(long pointer, PositionOrUnit target);

    private native boolean move_native(long pointer, Position target);

    private native boolean move_native(long pointer, Position target, boolean shiftQueueCommand);

    private native boolean patrol_native(long pointer, Position target);

    private native boolean patrol_native(long pointer, Position target, boolean shiftQueueCommand);

    private native boolean holdPosition_native(long pointer);

    private native boolean holdPosition_native(long pointer, boolean shiftQueueCommand);

    private native boolean stop_native(long pointer);

    private native boolean stop_native(long pointer, boolean shiftQueueCommand);

    private native boolean follow_native(long pointer, Unit target);

    private native boolean follow_native(long pointer, Unit target, boolean shiftQueueCommand);

    private native boolean gather_native(long pointer, Unit target);

    private native boolean gather_native(long pointer, Unit target, boolean shiftQueueCommand);

    private native boolean returnCargo_native(long pointer);

    private native boolean returnCargo_native(long pointer, boolean shiftQueueCommand);

    private native boolean repair_native(long pointer, Unit target);

    private native boolean repair_native(long pointer, Unit target, boolean shiftQueueCommand);

    private native boolean burrow_native(long pointer);

    private native boolean unburrow_native(long pointer);

    private native boolean cloak_native(long pointer);

    private native boolean decloak_native(long pointer);

    private native boolean siege_native(long pointer);

    private native boolean unsiege_native(long pointer);

    private native boolean lift_native(long pointer);

    private native boolean land_native(long pointer, TilePosition target);

    private native boolean load_native(long pointer, Unit target);

    private native boolean load_native(long pointer, Unit target, boolean shiftQueueCommand);

    private native boolean unload_native(long pointer, Unit target);

    private native boolean unloadAll_native(long pointer);

    private native boolean unloadAll_native(long pointer, boolean shiftQueueCommand);

    private native boolean unloadAll_native(long pointer, Position target);

    private native boolean unloadAll_native(long pointer, Position target, boolean shiftQueueCommand);

    private native boolean rightClick_native(long pointer, Position target);

    private native boolean rightClick_native(long pointer, Unit target);

    private native boolean rightClick_native(long pointer, PositionOrUnit target);

    private native boolean rightClick_native(long pointer, Position target, boolean shiftQueueCommand);

    private native boolean rightClick_native(long pointer, Unit target, boolean shiftQueueCommand);

    private native boolean rightClick_native(long pointer, PositionOrUnit target, boolean shiftQueueCommand);

    private native boolean haltConstruction_native(long pointer);

    private native boolean cancelConstruction_native(long pointer);

    private native boolean cancelAddon_native(long pointer);

    private native boolean cancelTrain_native(long pointer);

    private native boolean cancelTrain_native(long pointer, int slot);

    private native boolean cancelMorph_native(long pointer);

    private native boolean cancelResearch_native(long pointer);

    private native boolean cancelUpgrade_native(long pointer);

    private native boolean useTech_native(long pointer, TechType tech);

    private native boolean useTech_native(long pointer, TechType tech, Position target);

    private native boolean useTech_native(long pointer, TechType tech, Unit target);

    private native boolean useTech_native(long pointer, TechType tech, PositionOrUnit target);

    private native boolean placeCOP_native(long pointer, TilePosition target);

    private native boolean canIssueCommand_native(long pointer, UnitCommand command, boolean checkCanUseTechPositionOnPositions, boolean checkCanUseTechUnitOnUnits, boolean checkCanBuildUnitType, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canIssueCommand_native(long pointer, UnitCommand command, boolean checkCanUseTechPositionOnPositions, boolean checkCanUseTechUnitOnUnits, boolean checkCanBuildUnitType, boolean checkCanTargetUnit);

    private native boolean canIssueCommand_native(long pointer, UnitCommand command, boolean checkCanUseTechPositionOnPositions, boolean checkCanUseTechUnitOnUnits, boolean checkCanBuildUnitType);

    private native boolean canIssueCommand_native(long pointer, UnitCommand command, boolean checkCanUseTechPositionOnPositions, boolean checkCanUseTechUnitOnUnits);

    private native boolean canIssueCommand_native(long pointer, UnitCommand command, boolean checkCanUseTechPositionOnPositions);

    private native boolean canIssueCommand_native(long pointer, UnitCommand command);

    private native boolean canIssueCommand_native(long pointer, UnitCommand command, boolean checkCanUseTechPositionOnPositions, boolean checkCanUseTechUnitOnUnits, boolean checkCanBuildUnitType, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canIssueCommandGrouped_native(long pointer, UnitCommand command, boolean checkCanUseTechPositionOnPositions, boolean checkCanUseTechUnitOnUnits, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped);

    private native boolean canIssueCommandGrouped_native(long pointer, UnitCommand command, boolean checkCanUseTechPositionOnPositions, boolean checkCanUseTechUnitOnUnits, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canIssueCommandGrouped_native(long pointer, UnitCommand command, boolean checkCanUseTechPositionOnPositions, boolean checkCanUseTechUnitOnUnits, boolean checkCanTargetUnit);

    private native boolean canIssueCommandGrouped_native(long pointer, UnitCommand command, boolean checkCanUseTechPositionOnPositions, boolean checkCanUseTechUnitOnUnits);

    private native boolean canIssueCommandGrouped_native(long pointer, UnitCommand command, boolean checkCanUseTechPositionOnPositions);

    private native boolean canIssueCommandGrouped_native(long pointer, UnitCommand command);

    private native boolean canIssueCommandGrouped_native(long pointer, UnitCommand command, boolean checkCanUseTechPositionOnPositions, boolean checkCanUseTechUnitOnUnits, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped, boolean checkCommandibility);

    private native boolean canCommand_native(long pointer);

    private native boolean canCommandGrouped_native(long pointer);

    private native boolean canCommandGrouped_native(long pointer, boolean checkCommandibility);

    private native boolean canIssueCommandType_native(long pointer, UnitCommandType ct);

    private native boolean canIssueCommandType_native(long pointer, UnitCommandType ct, boolean checkCommandibility);

    private native boolean canIssueCommandTypeGrouped_native(long pointer, UnitCommandType ct, boolean checkCommandibilityGrouped);

    private native boolean canIssueCommandTypeGrouped_native(long pointer, UnitCommandType ct);

    private native boolean canIssueCommandTypeGrouped_native(long pointer, UnitCommandType ct, boolean checkCommandibilityGrouped, boolean checkCommandibility);

    private native boolean canTargetUnit_native(long pointer, Unit targetUnit);

    private native boolean canTargetUnit_native(long pointer, Unit targetUnit, boolean checkCommandibility);

    private native boolean canAttack_native(long pointer);

    private native boolean canAttack_native(long pointer, boolean checkCommandibility);

    private native boolean canAttack_native(long pointer, Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canAttack_native(long pointer, Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canAttack_native(long pointer, PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canAttack_native(long pointer, Position target, boolean checkCanTargetUnit);

    private native boolean canAttack_native(long pointer, Unit target, boolean checkCanTargetUnit);

    private native boolean canAttack_native(long pointer, PositionOrUnit target, boolean checkCanTargetUnit);

    private native boolean canAttack_native(long pointer, Position target);

    private native boolean canAttack_native(long pointer, Unit target);

    private native boolean canAttack_native(long pointer, PositionOrUnit target);

    private native boolean canAttack_native(long pointer, Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canAttack_native(long pointer, Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canAttack_native(long pointer, PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canAttackGrouped_native(long pointer, boolean checkCommandibilityGrouped);

    private native boolean canAttackGrouped_native(long pointer);

    private native boolean canAttackGrouped_native(long pointer, boolean checkCommandibilityGrouped, boolean checkCommandibility);

    private native boolean canAttackGrouped_native(long pointer, Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped);

    private native boolean canAttackGrouped_native(long pointer, Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped);

    private native boolean canAttackGrouped_native(long pointer, PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped);

    private native boolean canAttackGrouped_native(long pointer, Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canAttackGrouped_native(long pointer, Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canAttackGrouped_native(long pointer, PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canAttackGrouped_native(long pointer, Position target, boolean checkCanTargetUnit);

    private native boolean canAttackGrouped_native(long pointer, Unit target, boolean checkCanTargetUnit);

    private native boolean canAttackGrouped_native(long pointer, PositionOrUnit target, boolean checkCanTargetUnit);

    private native boolean canAttackGrouped_native(long pointer, Position target);

    private native boolean canAttackGrouped_native(long pointer, Unit target);

    private native boolean canAttackGrouped_native(long pointer, PositionOrUnit target);

    private native boolean canAttackGrouped_native(long pointer, Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped, boolean checkCommandibility);

    private native boolean canAttackGrouped_native(long pointer, Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped, boolean checkCommandibility);

    private native boolean canAttackGrouped_native(long pointer, PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped, boolean checkCommandibility);

    private native boolean canAttackMove_native(long pointer);

    private native boolean canAttackMove_native(long pointer, boolean checkCommandibility);

    private native boolean canAttackMoveGrouped_native(long pointer, boolean checkCommandibilityGrouped);

    private native boolean canAttackMoveGrouped_native(long pointer);

    private native boolean canAttackMoveGrouped_native(long pointer, boolean checkCommandibilityGrouped, boolean checkCommandibility);

    private native boolean canAttackUnit_native(long pointer);

    private native boolean canAttackUnit_native(long pointer, boolean checkCommandibility);

    private native boolean canAttackUnit_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canAttackUnit_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit);

    private native boolean canAttackUnit_native(long pointer, Unit targetUnit);

    private native boolean canAttackUnit_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canAttackUnitGrouped_native(long pointer, boolean checkCommandibilityGrouped);

    private native boolean canAttackUnitGrouped_native(long pointer);

    private native boolean canAttackUnitGrouped_native(long pointer, boolean checkCommandibilityGrouped, boolean checkCommandibility);

    private native boolean canAttackUnitGrouped_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped);

    private native boolean canAttackUnitGrouped_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canAttackUnitGrouped_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit);

    private native boolean canAttackUnitGrouped_native(long pointer, Unit targetUnit);

    private native boolean canAttackUnitGrouped_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped, boolean checkCommandibility);

    private native boolean canBuild_native(long pointer);

    private native boolean canBuild_native(long pointer, boolean checkCommandibility);

    private native boolean canBuild_native(long pointer, UnitType uType, boolean checkCanIssueCommandType);

    private native boolean canBuild_native(long pointer, UnitType uType);

    private native boolean canBuild_native(long pointer, UnitType uType, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canBuild_native(long pointer, UnitType uType, TilePosition tilePos, boolean checkTargetUnitType, boolean checkCanIssueCommandType);

    private native boolean canBuild_native(long pointer, UnitType uType, TilePosition tilePos, boolean checkTargetUnitType);

    private native boolean canBuild_native(long pointer, UnitType uType, TilePosition tilePos);

    private native boolean canBuild_native(long pointer, UnitType uType, TilePosition tilePos, boolean checkTargetUnitType, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canBuildAddon_native(long pointer);

    private native boolean canBuildAddon_native(long pointer, boolean checkCommandibility);

    private native boolean canBuildAddon_native(long pointer, UnitType uType, boolean checkCanIssueCommandType);

    private native boolean canBuildAddon_native(long pointer, UnitType uType);

    private native boolean canBuildAddon_native(long pointer, UnitType uType, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canTrain_native(long pointer);

    private native boolean canTrain_native(long pointer, boolean checkCommandibility);

    private native boolean canTrain_native(long pointer, UnitType uType, boolean checkCanIssueCommandType);

    private native boolean canTrain_native(long pointer, UnitType uType);

    private native boolean canTrain_native(long pointer, UnitType uType, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canMorph_native(long pointer);

    private native boolean canMorph_native(long pointer, boolean checkCommandibility);

    private native boolean canMorph_native(long pointer, UnitType uType, boolean checkCanIssueCommandType);

    private native boolean canMorph_native(long pointer, UnitType uType);

    private native boolean canMorph_native(long pointer, UnitType uType, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canResearch_native(long pointer);

    private native boolean canResearch_native(long pointer, boolean checkCommandibility);

    private native boolean canResearch_native(long pointer, TechType type);

    private native boolean canResearch_native(long pointer, TechType type, boolean checkCanIssueCommandType);

    private native boolean canUpgrade_native(long pointer);

    private native boolean canUpgrade_native(long pointer, boolean checkCommandibility);

    private native boolean canUpgrade_native(long pointer, UpgradeType type);

    private native boolean canUpgrade_native(long pointer, UpgradeType type, boolean checkCanIssueCommandType);

    private native boolean canSetRallyPoint_native(long pointer);

    private native boolean canSetRallyPoint_native(long pointer, boolean checkCommandibility);

    private native boolean canSetRallyPoint_native(long pointer, Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canSetRallyPoint_native(long pointer, Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canSetRallyPoint_native(long pointer, PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canSetRallyPoint_native(long pointer, Position target, boolean checkCanTargetUnit);

    private native boolean canSetRallyPoint_native(long pointer, Unit target, boolean checkCanTargetUnit);

    private native boolean canSetRallyPoint_native(long pointer, PositionOrUnit target, boolean checkCanTargetUnit);

    private native boolean canSetRallyPoint_native(long pointer, Position target);

    private native boolean canSetRallyPoint_native(long pointer, Unit target);

    private native boolean canSetRallyPoint_native(long pointer, PositionOrUnit target);

    private native boolean canSetRallyPoint_native(long pointer, Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canSetRallyPoint_native(long pointer, Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canSetRallyPoint_native(long pointer, PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canSetRallyPosition_native(long pointer);

    private native boolean canSetRallyPosition_native(long pointer, boolean checkCommandibility);

    private native boolean canSetRallyUnit_native(long pointer);

    private native boolean canSetRallyUnit_native(long pointer, boolean checkCommandibility);

    private native boolean canSetRallyUnit_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canSetRallyUnit_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit);

    private native boolean canSetRallyUnit_native(long pointer, Unit targetUnit);

    private native boolean canSetRallyUnit_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canMove_native(long pointer);

    private native boolean canMove_native(long pointer, boolean checkCommandibility);

    private native boolean canMoveGrouped_native(long pointer, boolean checkCommandibilityGrouped);

    private native boolean canMoveGrouped_native(long pointer);

    private native boolean canMoveGrouped_native(long pointer, boolean checkCommandibilityGrouped, boolean checkCommandibility);

    private native boolean canPatrol_native(long pointer);

    private native boolean canPatrol_native(long pointer, boolean checkCommandibility);

    private native boolean canPatrolGrouped_native(long pointer, boolean checkCommandibilityGrouped);

    private native boolean canPatrolGrouped_native(long pointer);

    private native boolean canPatrolGrouped_native(long pointer, boolean checkCommandibilityGrouped, boolean checkCommandibility);

    private native boolean canFollow_native(long pointer);

    private native boolean canFollow_native(long pointer, boolean checkCommandibility);

    private native boolean canFollow_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canFollow_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit);

    private native boolean canFollow_native(long pointer, Unit targetUnit);

    private native boolean canFollow_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canGather_native(long pointer);

    private native boolean canGather_native(long pointer, boolean checkCommandibility);

    private native boolean canGather_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canGather_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit);

    private native boolean canGather_native(long pointer, Unit targetUnit);

    private native boolean canGather_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canReturnCargo_native(long pointer);

    private native boolean canReturnCargo_native(long pointer, boolean checkCommandibility);

    private native boolean canHoldPosition_native(long pointer);

    private native boolean canHoldPosition_native(long pointer, boolean checkCommandibility);

    private native boolean canStop_native(long pointer);

    private native boolean canStop_native(long pointer, boolean checkCommandibility);

    private native boolean canRepair_native(long pointer);

    private native boolean canRepair_native(long pointer, boolean checkCommandibility);

    private native boolean canRepair_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canRepair_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit);

    private native boolean canRepair_native(long pointer, Unit targetUnit);

    private native boolean canRepair_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canBurrow_native(long pointer);

    private native boolean canBurrow_native(long pointer, boolean checkCommandibility);

    private native boolean canUnburrow_native(long pointer);

    private native boolean canUnburrow_native(long pointer, boolean checkCommandibility);

    private native boolean canCloak_native(long pointer);

    private native boolean canCloak_native(long pointer, boolean checkCommandibility);

    private native boolean canDecloak_native(long pointer);

    private native boolean canDecloak_native(long pointer, boolean checkCommandibility);

    private native boolean canSiege_native(long pointer);

    private native boolean canSiege_native(long pointer, boolean checkCommandibility);

    private native boolean canUnsiege_native(long pointer);

    private native boolean canUnsiege_native(long pointer, boolean checkCommandibility);

    private native boolean canLift_native(long pointer);

    private native boolean canLift_native(long pointer, boolean checkCommandibility);

    private native boolean canLand_native(long pointer);

    private native boolean canLand_native(long pointer, boolean checkCommandibility);

    private native boolean canLand_native(long pointer, TilePosition target, boolean checkCanIssueCommandType);

    private native boolean canLand_native(long pointer, TilePosition target);

    private native boolean canLand_native(long pointer, TilePosition target, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canLoad_native(long pointer);

    private native boolean canLoad_native(long pointer, boolean checkCommandibility);

    private native boolean canLoad_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canLoad_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit);

    private native boolean canLoad_native(long pointer, Unit targetUnit);

    private native boolean canLoad_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canUnloadWithOrWithoutTarget_native(long pointer);

    private native boolean canUnloadWithOrWithoutTarget_native(long pointer, boolean checkCommandibility);

    private native boolean canUnloadAtPosition_native(long pointer, Position targDropPos, boolean checkCanIssueCommandType);

    private native boolean canUnloadAtPosition_native(long pointer, Position targDropPos);

    private native boolean canUnloadAtPosition_native(long pointer, Position targDropPos, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canUnload_native(long pointer);

    private native boolean canUnload_native(long pointer, boolean checkCommandibility);

    private native boolean canUnload_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkPosition, boolean checkCanIssueCommandType);

    private native boolean canUnload_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkPosition);

    private native boolean canUnload_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit);

    private native boolean canUnload_native(long pointer, Unit targetUnit);

    private native boolean canUnload_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkPosition, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canUnloadAll_native(long pointer);

    private native boolean canUnloadAll_native(long pointer, boolean checkCommandibility);

    private native boolean canUnloadAllPosition_native(long pointer);

    private native boolean canUnloadAllPosition_native(long pointer, boolean checkCommandibility);

    private native boolean canUnloadAllPosition_native(long pointer, Position targDropPos, boolean checkCanIssueCommandType);

    private native boolean canUnloadAllPosition_native(long pointer, Position targDropPos);

    private native boolean canUnloadAllPosition_native(long pointer, Position targDropPos, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canRightClick_native(long pointer);

    private native boolean canRightClick_native(long pointer, boolean checkCommandibility);

    private native boolean canRightClick_native(long pointer, Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canRightClick_native(long pointer, Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canRightClick_native(long pointer, PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canRightClick_native(long pointer, Position target, boolean checkCanTargetUnit);

    private native boolean canRightClick_native(long pointer, Unit target, boolean checkCanTargetUnit);

    private native boolean canRightClick_native(long pointer, PositionOrUnit target, boolean checkCanTargetUnit);

    private native boolean canRightClick_native(long pointer, Position target);

    private native boolean canRightClick_native(long pointer, Unit target);

    private native boolean canRightClick_native(long pointer, PositionOrUnit target);

    private native boolean canRightClick_native(long pointer, Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canRightClick_native(long pointer, Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canRightClick_native(long pointer, PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canRightClickGrouped_native(long pointer, boolean checkCommandibilityGrouped);

    private native boolean canRightClickGrouped_native(long pointer);

    private native boolean canRightClickGrouped_native(long pointer, boolean checkCommandibilityGrouped, boolean checkCommandibility);

    private native boolean canRightClickGrouped_native(long pointer, Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped);

    private native boolean canRightClickGrouped_native(long pointer, Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped);

    private native boolean canRightClickGrouped_native(long pointer, PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped);

    private native boolean canRightClickGrouped_native(long pointer, Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canRightClickGrouped_native(long pointer, Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canRightClickGrouped_native(long pointer, PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canRightClickGrouped_native(long pointer, Position target, boolean checkCanTargetUnit);

    private native boolean canRightClickGrouped_native(long pointer, Unit target, boolean checkCanTargetUnit);

    private native boolean canRightClickGrouped_native(long pointer, PositionOrUnit target, boolean checkCanTargetUnit);

    private native boolean canRightClickGrouped_native(long pointer, Position target);

    private native boolean canRightClickGrouped_native(long pointer, Unit target);

    private native boolean canRightClickGrouped_native(long pointer, PositionOrUnit target);

    private native boolean canRightClickGrouped_native(long pointer, Position target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped, boolean checkCommandibility);

    private native boolean canRightClickGrouped_native(long pointer, Unit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped, boolean checkCommandibility);

    private native boolean canRightClickGrouped_native(long pointer, PositionOrUnit target, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped, boolean checkCommandibility);

    private native boolean canRightClickPosition_native(long pointer);

    private native boolean canRightClickPosition_native(long pointer, boolean checkCommandibility);

    private native boolean canRightClickPositionGrouped_native(long pointer, boolean checkCommandibilityGrouped);

    private native boolean canRightClickPositionGrouped_native(long pointer);

    private native boolean canRightClickPositionGrouped_native(long pointer, boolean checkCommandibilityGrouped, boolean checkCommandibility);

    private native boolean canRightClickUnit_native(long pointer);

    private native boolean canRightClickUnit_native(long pointer, boolean checkCommandibility);

    private native boolean canRightClickUnit_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canRightClickUnit_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit);

    private native boolean canRightClickUnit_native(long pointer, Unit targetUnit);

    private native boolean canRightClickUnit_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canRightClickUnitGrouped_native(long pointer, boolean checkCommandibilityGrouped);

    private native boolean canRightClickUnitGrouped_native(long pointer);

    private native boolean canRightClickUnitGrouped_native(long pointer, boolean checkCommandibilityGrouped, boolean checkCommandibility);

    private native boolean canRightClickUnitGrouped_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped);

    private native boolean canRightClickUnitGrouped_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType);

    private native boolean canRightClickUnitGrouped_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit);

    private native boolean canRightClickUnitGrouped_native(long pointer, Unit targetUnit);

    private native boolean canRightClickUnitGrouped_native(long pointer, Unit targetUnit, boolean checkCanTargetUnit, boolean checkCanIssueCommandType, boolean checkCommandibilityGrouped, boolean checkCommandibility);

    private native boolean canHaltConstruction_native(long pointer);

    private native boolean canHaltConstruction_native(long pointer, boolean checkCommandibility);

    private native boolean canCancelConstruction_native(long pointer);

    private native boolean canCancelConstruction_native(long pointer, boolean checkCommandibility);

    private native boolean canCancelAddon_native(long pointer);

    private native boolean canCancelAddon_native(long pointer, boolean checkCommandibility);

    private native boolean canCancelTrain_native(long pointer);

    private native boolean canCancelTrain_native(long pointer, boolean checkCommandibility);

    private native boolean canCancelTrainSlot_native(long pointer);

    private native boolean canCancelTrainSlot_native(long pointer, boolean checkCommandibility);

    private native boolean canCancelTrainSlot_native(long pointer, int slot, boolean checkCanIssueCommandType);

    private native boolean canCancelTrainSlot_native(long pointer, int slot);

    private native boolean canCancelTrainSlot_native(long pointer, int slot, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canCancelMorph_native(long pointer);

    private native boolean canCancelMorph_native(long pointer, boolean checkCommandibility);

    private native boolean canCancelResearch_native(long pointer);

    private native boolean canCancelResearch_native(long pointer, boolean checkCommandibility);

    private native boolean canCancelUpgrade_native(long pointer);

    private native boolean canCancelUpgrade_native(long pointer, boolean checkCommandibility);

    private native boolean canUseTechWithOrWithoutTarget_native(long pointer);

    private native boolean canUseTechWithOrWithoutTarget_native(long pointer, boolean checkCommandibility);

    private native boolean canUseTechWithOrWithoutTarget_native(long pointer, TechType tech, boolean checkCanIssueCommandType);

    private native boolean canUseTechWithOrWithoutTarget_native(long pointer, TechType tech);

    private native boolean canUseTechWithOrWithoutTarget_native(long pointer, TechType tech, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canUseTech_native(long pointer, TechType tech, Position target, boolean checkCanTargetUnit, boolean checkTargetsType, boolean checkCanIssueCommandType);

    private native boolean canUseTech_native(long pointer, TechType tech, Unit target, boolean checkCanTargetUnit, boolean checkTargetsType, boolean checkCanIssueCommandType);

    private native boolean canUseTech_native(long pointer, TechType tech, PositionOrUnit target, boolean checkCanTargetUnit, boolean checkTargetsType, boolean checkCanIssueCommandType);

    private native boolean canUseTech_native(long pointer, TechType tech, Position target, boolean checkCanTargetUnit, boolean checkTargetsType);

    private native boolean canUseTech_native(long pointer, TechType tech, Unit target, boolean checkCanTargetUnit, boolean checkTargetsType);

    private native boolean canUseTech_native(long pointer, TechType tech, PositionOrUnit target, boolean checkCanTargetUnit, boolean checkTargetsType);

    private native boolean canUseTech_native(long pointer, TechType tech, Position target, boolean checkCanTargetUnit);

    private native boolean canUseTech_native(long pointer, TechType tech, Unit target, boolean checkCanTargetUnit);

    private native boolean canUseTech_native(long pointer, TechType tech, PositionOrUnit target, boolean checkCanTargetUnit);

    private native boolean canUseTech_native(long pointer, TechType tech, Position target);

    private native boolean canUseTech_native(long pointer, TechType tech, Unit target);

    private native boolean canUseTech_native(long pointer, TechType tech, PositionOrUnit target);

    private native boolean canUseTech_native(long pointer, TechType tech);

    private native boolean canUseTech_native(long pointer, TechType tech, Position target, boolean checkCanTargetUnit, boolean checkTargetsType, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canUseTech_native(long pointer, TechType tech, Unit target, boolean checkCanTargetUnit, boolean checkTargetsType, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canUseTech_native(long pointer, TechType tech, PositionOrUnit target, boolean checkCanTargetUnit, boolean checkTargetsType, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canUseTechWithoutTarget_native(long pointer, TechType tech, boolean checkCanIssueCommandType);

    private native boolean canUseTechWithoutTarget_native(long pointer, TechType tech);

    private native boolean canUseTechWithoutTarget_native(long pointer, TechType tech, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canUseTechUnit_native(long pointer, TechType tech, boolean checkCanIssueCommandType);

    private native boolean canUseTechUnit_native(long pointer, TechType tech);

    private native boolean canUseTechUnit_native(long pointer, TechType tech, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canUseTechUnit_native(long pointer, TechType tech, Unit targetUnit, boolean checkCanTargetUnit, boolean checkTargetsUnits, boolean checkCanIssueCommandType);

    private native boolean canUseTechUnit_native(long pointer, TechType tech, Unit targetUnit, boolean checkCanTargetUnit, boolean checkTargetsUnits);

    private native boolean canUseTechUnit_native(long pointer, TechType tech, Unit targetUnit, boolean checkCanTargetUnit);

    private native boolean canUseTechUnit_native(long pointer, TechType tech, Unit targetUnit);

    private native boolean canUseTechUnit_native(long pointer, TechType tech, Unit targetUnit, boolean checkCanTargetUnit, boolean checkTargetsUnits, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canUseTechPosition_native(long pointer, TechType tech, boolean checkCanIssueCommandType);

    private native boolean canUseTechPosition_native(long pointer, TechType tech);

    private native boolean canUseTechPosition_native(long pointer, TechType tech, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canUseTechPosition_native(long pointer, TechType tech, Position target, boolean checkTargetsPositions, boolean checkCanIssueCommandType);

    private native boolean canUseTechPosition_native(long pointer, TechType tech, Position target, boolean checkTargetsPositions);

    private native boolean canUseTechPosition_native(long pointer, TechType tech, Position target);

    private native boolean canUseTechPosition_native(long pointer, TechType tech, Position target, boolean checkTargetsPositions, boolean checkCanIssueCommandType, boolean checkCommandibility);

    private native boolean canPlaceCOP_native(long pointer);

    private native boolean canPlaceCOP_native(long pointer, boolean checkCommandibility);

    private native boolean canPlaceCOP_native(long pointer, TilePosition target, boolean checkCanIssueCommandType);

    private native boolean canPlaceCOP_native(long pointer, TilePosition target);

    private native boolean canPlaceCOP_native(long pointer, TilePosition target, boolean checkCanIssueCommandType, boolean checkCommandibility);

    // =========================================================
    // ===== Start of ATLANTIS CODE ============================
    // =========================================================
    
//    private static int firstFreeID = 1;
//    private int atlantisID;
    
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

        // Repair & Heal
        _repairableMechanically = isBuilding() || isVehicle();
        _healable = isInfantry() || isWorker();

        // Military building
        _isMilitaryBuildingAntiGround = isType(
                UnitType.Terran_Bunker, UnitType.Protoss_Photon_Cannon, UnitType.Zerg_Sunken_Colony
        );
        _isMilitaryBuildingAntiAir = isType(
                UnitType.Terran_Bunker, UnitType.Terran_Missile_Turret,
                UnitType.Protoss_Photon_Cannon, UnitType.Zerg_Spore_Colony
        );
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

        Position newPosition = new Position(getPX() - dx, getPY() - dy);

        move(newPosition, false);
        TooltipManager.setTooltip(this, "Run");
    }

    /**
     * Returns distance in tiles (1 tile = 32 pixels) to the target.
     */
    public double distanceTo(Object target) {
        if (target instanceof Unit) {
            return (double) getDistance((Unit) target) / 32;
        }
        else {
            return (double) getDistance((Position) target) / 32;
        }
    }
    
    // =========================================================
    @Override
    public String toString() {
        // Position position = getPosition();
        Position position = this;
        String toString = getType().getShortName();
        toString += " #" + getID() + " at [" + position.getTileX() + "," + position.getTileY() + "]";
        return toString;
    }
    
    @Override
    public int compareTo(Unit o) {
        return Integer.compare(this.getID(), ((Unit) o).getID());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Unit other = (Unit) obj;
        if (this.getID() != other.getID()) {
            return false;
        }
        return true;
    }

//    @Override
//    public int compareTo(Object o) {
//        if (!(o instanceof Unit)) {
//            return -1;
//        }
//        return Integer.compare(this.getID(), ((Unit) o).getID());
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = 7;
//        hash = 53 * hash + (int) (getID() ^ (getID() >>> 32));
//        return hash;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) {
//            return true;
//        }
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final Unit other = (Unit) obj;
//        if (this.getID() != other.getID()) {
//            return false;
//        }
//        return true;
//    }

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
        return getType().isWorker();
    }

    public boolean isBunker() {
        return getType().equals(UnitType.Terran_Bunker);
    }

    public boolean isBase() {
        return isType(UnitType.Terran_Command_Center, UnitType.Protoss_Nexus, UnitType.Zerg_Hatchery,
                UnitType.Zerg_Lair, UnitType.Zerg_Hive);
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
    
    public boolean isType(UnitType type) {
        return getType().equals(type);
    }

    public boolean ofType(UnitType type) {
        return getType().equals(type);
    }

    public boolean isType(UnitType... types) {
        return getType().isType(types);
    }

    public boolean isFullyHealthy() {
        return getHitPoints() >= getType().maxHitPoints();
    }

    public int getHPPercent() {
        return 100 * getHitPoints() / getType().maxHitPoints();
    }

    public boolean isWounded() {
        return getHitPoints() < getMaxHP();
    }

    public int getHP() {
        return getHitPoints();
    }

    public int getMaxHP() {
        return getType().maxHitPoints();
    }

    public String getShortName() {
        return getType().getShortName();
    }

    /**
     * Has separate name not to mistake attackUnit with attackPosition.
     */
    public void attackUnit(Unit target) {
        this.attack(target);
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
    public double getShootRangeAgainst(Unit opponentUnit) {
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
    public int getUnitIndex() {
        int index = 0;
        for (Unit otherUnit : Select.our().ofType(getType()).listUnits()) {
            if (otherUnit.getID() < this.getID()) {
                index++;
            }
        }
        return index;
    }

    // =========================================================
    // Debugging / Painting methods
//    private String tooltip;
//    private int tooltipStartInFrames;
//
//    public void setTooltip(String tooltip) {
//        this.tooltip = tooltip;
//        this.tooltipStartInFrames = AtlantisGame.getTimeFrames();
//    }
//
//    public String getTooltip() {
//        if (AtlantisGame.getTimeFrames() - tooltipStartInFrames > 30) {
//            String tooltipToReturn = this.tooltip;
//            this.tooltip = null;
//            return tooltipToReturn;
//        } else {
//            return tooltip;
//        }
//    }
//
//    public void removeTooltip() {
//        this.tooltip = null;
//    }
//
//    public boolean hasTooltip() {
//        return this.tooltip != null;
//    }

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
        return !getType().isFlyer();
    }

    public boolean isAirUnit() {
        return getType().isFlyer();
    }

    public boolean isSpiderMine() {
        return getType().equals(UnitType.Terran_Vulture_Spider_Mine);
    }

    public boolean isLarvaOrEgg() {
        return getType().equals(UnitType.Zerg_Larva) || getType().equals(UnitType.Zerg_Egg);
    }

    public boolean isLarva() {
        return getType().equals(UnitType.Zerg_Larva);
    }

    public boolean isEgg() {
        return getType().equals(UnitType.Zerg_Egg);
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
    public boolean canAttackThisKindOfUnit(Unit otherUnit, boolean includeCooldown) {

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
    public boolean hasRangeToAttack(Unit targetUnit, double safetyMargin) {
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
    public WeaponType getWeaponAgainst(Unit target) {
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
    public boolean runFrom(Unit nearestEnemy) {
        if (nearestEnemy == null) {
            nearestEnemy = Select.enemyRealUnits().nearestTo(this);
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
        return getPlayer().isEnemy();
    }

    /**
     * Returns true if this unit belongs to the enemy.
     */
    public boolean isEnemy() {
        return getPlayer().isEnemy();
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
    
}
