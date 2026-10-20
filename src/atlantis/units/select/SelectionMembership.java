package atlantis.units.select;

import atlantis.battle.cluster.Squad;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.bullets.DeadMan;
import atlantis.map.position.HasPosition;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Action;
import atlantis.units.actions.Actions;
import atlantis.units.fogged.AbstractFoggedUnit;

/**
 * Membership filters extracted from {@link Selection} (NEXT #10, slice 2).
 *
 * <p>Single-expression filters over the selection data: verbatim moves,
 * {@code Selection} keeps one-line delegates so every call site is untouched.</p>
 */
final class SelectionMembership {

    private SelectionMembership() {
    }

    static Selection moving(Selection selection)  {
        return selection.cloneByRemovingIf(
            otherUnit -> !otherUnit.isMoving(), "moving"
        );
        }

    static Selection notMoving(Selection selection)  {
        return selection.cloneByRemovingIf(
            otherUnit -> otherUnit.isMoving(), "notMoving"
        );
        }

    static Selection notShowingBackToUs(Selection selection, AUnit subject)  {
        return selection.cloneByRemovingIf(
            otherUnit -> subject.isOtherShowingBackToUs(otherUnit),
            "notShowingBackToUs:" + subject.idWithHash()
        );
        }

    static Selection facingThisUnit(Selection selection, AUnit subject)  {
        return selection.cloneByRemovingIf(
            otherUnit -> !otherUnit.isFacing(subject),
            "facingThisUnit:" + subject.idWithHash()
        );
        }

    static Selection canAttack(Selection selection, AUnit target, double safetyMargin)  {
        return selection.cloneByRemovingIf(attacker -> !attacker.canAttackTarget(
            target, true, true, false, safetyMargin
        ), "canAttack:" + target.idWithHash() + ":" + safetyMargin);
        }

/**
     * Selects only those units from current selection, which can be both <b>attacked by</b> given unit (e.g.
     * Zerglings can't attack Overlord) and are <b>in shot range</b> to the given <b>unit</b>.
     */
    static Selection canAttack(Selection selection, AUnit target, boolean checkShootingRange, boolean checkVisibility, double safetyMargin)  {
        return selection.cloneByRemovingIf(
            attacker -> !attacker.canAttackTarget(
                target, checkShootingRange, checkVisibility, false, safetyMargin
            ),
            "canAttack:" + target.idWithHash()
                + ":" + checkShootingRange
                + ":" + checkVisibility
                + ":" + safetyMargin
        );
        }

    static Selection canBeAttackedBy(Selection selection, AUnit attacker, double extraMargin)  {
        return selection.cloneByRemovingIf(target -> !attacker.canAttackTarget(
            target, true, true, false, extraMargin
        ), "canBeAttackedBy:" + attacker.idWithHash() + ":" + extraMargin);
        }

    static Selection notRetreating(Selection selection)  {
        return selection.cloneByRemovingIf(AUnit::isRetreating, "notRetreating");
        }

    static Selection notRunning(Selection selection)  {
        return selection.cloneByRemovingIf(AUnit::isRunning, "notRunning");
        }

    static Selection excludeEggsAndLarvae(Selection selection)  {
        return selection.cloneByRemovingIf(AUnit::isLarvaOrEgg, "excludeEggsAndLarvae");
        }

    static Selection excludeTanks(Selection selection)  {
        return selection.cloneByRemovingIf(AUnit::isTank, "excludeTanks");
        }

    static Selection excludeMarines(Selection selection)  {
        return selection.cloneByRemovingIf(AUnit::isMarine, "excludeMarines");
        }

    static Selection excludeOverlords(Selection selection)  {
        return selection.cloneByRemovingIf(AUnit::isOverlord, "excludeOverlords");
        }

    static Selection excludeMedics(Selection selection)  {
        return selection.cloneByRemovingIf(AUnit::isMedic, "excludeMedics");
        }

    static Selection excludeTypes(Selection selection, AUnitType... types)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.is(types)), "excludeTypes:" + CacheKey.create(types)
        );
        }

    static Selection notImmobilized(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.notImmobilized()), "notImmobilized"
        );
        }

/**
     * Selects these units which are not carrynig nor minerals, nor gas.
     */
    static Selection notCarrying(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.isCarryingGas() || unit.isCarryingMinerals()), "notCarrying"
        );
        }

/**
     * Selects these units which are not scouts.
     */
    static Selection notScout(Selection selection)  {
        return selection.cloneByRemovingIf(
            (u -> u.isScout() || u.isFlyingScout()), "notScout");
        }

    static Selection withoutAddon(Selection selection)  {
        return selection.cloneByRemovingIf(
            (u -> u.hasAddon()), "withoutAddon"
        );
        }

    static Selection withAddon(Selection selection)  {
        return selection.cloneByRemovingIf(
            (u -> !u.hasAddon()), "withAddon"
        );
        }

    static Selection free(Selection selection)  {
        return selection.cloneByRemovingIf(
            (u -> u.isBusy() || u.isLifted() || u.isResearching()), "free"
        );
        }

/**
     * Selects these units (makes sense only for workers) who aren't assigned to construct anything.
     */
    static Selection notConstructing(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.isConstructing() || unit.construction() != null || unit.isMorphing() || unit.isBuilder()),
            "notConstructing"
        );
        }

    /**
     * Selects workers that are not assigned to any construction. Narrower
     * than notConstructing: a builder is an assignment, not a moment -
     * a Protoss probe walking to its site constructs nothing yet and a
     * Terran SCV constructs while assigned, so "currently constructing"
     * and "has a building to raise" are different questions with
     * different consequences (a scout picked as builder never builds;
     * a builder picked as scout never scouts).
     */
    static Selection notBuilder(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.isBuilder()), "notBuilder"
        );
        }

    static Selection reavers(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isReaver()), "reavers"
        );
        }

    static Selection lurkers(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isLurker()), "lurkers"
        );
        }

    static Selection scarabs(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.type().isScarab()), "scarabs"
        );
        }

    static Selection dts(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isDarkTemplar()), "dts"
        );
        }

    static Selection forge(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isForge()), "forge"
        );
        }

    static Selection cannons(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isCannon()), "cannons"
        );
        }

    static Selection bunkers(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isBunker()), "bunkers"
        );
        }

    static Selection observatories(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isObservatory()), "observatories"
        );
        }

    static Selection observers(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isObserver()), "observers"
        );
        }

    static Selection medics(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isMedic()), "medics"
        );
        }

    static Selection mechanical(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isMechanical()), "mechanical"
        );
        }

    static Selection nonStasisedOrLockedDown(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.isStasised() || unit.isLockedDown()), "nonStasisedOrLockedDown"
        );
        }

    static Selection havingAtLeastHp(Selection selection, int minHp)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.hpLessThan(minHp)), "havingAtLeastHp:" + minHp
        );
        }

    static Selection notBeingHealed(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.isBeingHealed()), "notBeingHealed"
        );
        }

    static Selection notHavingHp(Selection selection, int maxHp)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.hpMoreThan(maxHp)), "notHavingHp:" + maxHp
        );
        }

    static Selection havingEnergy(Selection selection, int minEnergy)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.energy(minEnergy)), "havingEnergy:" + minEnergy
        );
        }

    static Selection havingCooldownMax(Selection selection, int maxCooldown)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.cooldown() > maxCooldown), "havingCooldownMax:" + maxCooldown
        );
        }

    static Selection havingCooldownMin(Selection selection, int minCooldown)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.cooldown() < minCooldown), "havingCooldownMin:" + minCooldown
        );
        }

/**
     * Selects these transport/bunker units which have still enough room inside.
     */
    static Selection havingSpaceFree(Selection selection, int spaceRequired)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.spaceRemaining() < spaceRequired), "havingSpaceFree:" + spaceRequired);
        }

    static Selection healthy(Selection selection)  {
        return selection.cloneByRemovingIf(
            (AUnit::isWounded), "healthy"
        );
        }

    static Selection producing(Selection selection, AUnitType producingThisUnit)  {
        return selection.cloneByRemovingIf(
            (u -> u.trainingQueue().isEmpty() || !u.isTraining(producingThisUnit)),
            "producing:" + producingThisUnit.id()
        );
        }

    static Selection protectors(Selection selection)  {
        return selection.cloneByRemovingIf(
            (u -> !u.isProtector()), "protectors"
        );
        }

    static Selection notProtectors(Selection selection)  {
        return selection.cloneByRemovingIf(
            (AUnit::isProtector), "notProtectors"
        );
        }

    static Selection nonBuilders(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.isBuilder()), "nonBuilders"
        );
        }

    static Selection builders(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isBuilder()), "builders"
        );
        }

    static Selection notLoaded(Selection selection)  {
        return selection.cloneByRemovingIf(
            (AUnit::isLoaded), "notLoaded");
        }

    static Selection loaded(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isLoaded()), "loaded"
        );
        }

    static Selection burrowing(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isBurrowing()), "burrowing"
        );
        }

    static Selection burrowed(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isBurrowed()), "burrowed"
        );
        }

/**
     * Selects only those Terran vehicles/buildings that can be repaired so it has to be:<br />
     * - mechanical<br />
     * - not 100% healthy<br />
     */
    static Selection repairable(Selection selection, boolean checkIfWounded)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isCompleted()
                || (!unit.type().isMechanical() && (!unit.isABuilding() || !unit.isTerran()))
                || (checkIfWounded && !unit.isWounded())
            ),
            "repairable:" + A.trueFalse(checkIfWounded)
        );
        }

    static Selection notCompleted(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.isCompleted()), "notCompleted"
        );
        }

    static Selection onlyCompletedOrAlmost(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isCompletedOrAlmost()), "onlyCompletedOrAlmost"
        );
        }

    static Selection onlyCompleted(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isCompleted()), "onlyCompleted"
        );
        }

    static Selection combatBuildingsAntiAir(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.is(
                AUnitType.Protoss_Photon_Cannon,
                AUnitType.Terran_Bunker,
                AUnitType.Terran_Missile_Turret,
                AUnitType.Zerg_Spore_Colony
            )),
            "combatBuildingsAntiAir"
        );
        }

    static Selection combatBuildingsAntiLand(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Protoss_Photon_Cannon, AUnitType.Terran_Bunker, AUnitType.Zerg_Sunken_Colony)),
            "combatBuildingsAntiLand"
        );
        }

/**
     * Selects military buildings like Photon Cannon, Bunker, Spore Colony, Sunken Colony
     */
    static Selection combatBuildings(Selection selection, boolean includeCreepColonies)  {
        return selection.cloneByRemovingIf(
            (unit -> includeCreepColonies
                ? !unit.type().isCombatBuildingOrCreepColony()
                : !unit.type().isCombatBuildingWithoutCreepColonies()),
            "combatBuildings:" + A.trueFalse(includeCreepColonies)
        );
        }

    static Selection combatUnits(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isCompleted() || !unit.type().isCombatUnit()), "combatUnits"
        );
        }

    static Selection inSquad(Selection selection, Squad squad)  {
        return selection.cloneByRemovingIf(
            (unit -> !squad.equals(unit.squad())), "inSquad:" + squad.name()
        );
        }

    static Selection notPurelyAntiAir(Selection selection)  {
        return selection.cloneByRemovingIf(u -> !u.isPurelyAntiAir(), "notPurelyAntiAir");
        }

    static Selection havingSmallerRange(Selection selection, AUnit than)  {
        return selection.cloneByRemovingIf(
            u -> !u.canAttackTarget(than)
//                || than.type().equals(u.type())
                || u.weaponRangeAgainst(than) < than.weaponRangeAgainst(u),
            "havingSmallerRange:" + than.type().id()
        );
        }

    static Selection havingAntiAirWeapon(Selection selection)  {
        return selection.cloneByRemovingIf(u -> !u.canAttackAirUnits(), "havingAntiAirWeapon");
        }

    static Selection notHavingAntiAirWeapon(Selection selection)  {
        return selection.cloneByRemovingIf(u -> u.canAttackAirUnits(), "notHavingAntiAirWeapon");
        }

    static Selection havingAntiGroundWeapon(Selection selection)  {
        return selection.cloneByRemovingIf(u -> !u.canAttackGroundUnits(), "havingAntiGroundWeapon");
        }

    static Selection havingWeapon(Selection selection)  {
        return selection.cloneByRemovingIf(u -> !u.hasAnyWeapon(), "havingWeapon");
        }

    static Selection notDeadMan(Selection selection)  {
        return selection.cloneByRemovingIf(u -> DeadMan.isDeadMan(u), "notDeadMan");
        }

    static Selection cloakable(Selection selection)  {
        return selection.cloneByRemovingIf(u -> !u.type().is(
            AUnitType.Protoss_Dark_Templar,
            AUnitType.Zerg_Lurker
        ), "cloakable");
        }

    static int countRetreating(Selection selection)  {
        return selection.cloneByRemovingIf(
            u -> !u.isRetreating(),
            "countRetreating"
        ).count();
        }

    static int countRunning(Selection selection, int maxStartedRunningAgo)  {
        return selection.cloneByRemovingIf(
            u -> (!u.isRunning() || u.lastStartedRunningMoreThanAgo(maxStartedRunningAgo)),
            "countRunning:" + maxStartedRunningAgo
        ).count();
        }

    static Selection beingVisibleUnitOrNotVisibleFoggedUnit(Selection selection)  {
        return selection.cloneByRemovingIf(
            u -> u.u() != null || ((u instanceof AbstractFoggedUnit) && !u.position().isPositionVisible()),
            "beingVisibleUnitOrNotVisibleFoggedUnit"
        );
        }

/**
     * Fogged units may have no position if e.g. unit moved and we don't know where it is.
     */
    static Selection havingPosition(Selection selection)  {
        return selection.cloneByRemovingIf(u -> !u.hasPosition(), "havingPosition");
        }

/**
     * Selects only buildings.
     */
    static Selection buildings(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.type().isABuilding() && !unit.type().isAddon()), "buildings"
        );
        }

    static Selection havingAnyTarget(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> (unit.target() == null)),
            "havingAnyTarget"
        );
        }

    static Selection havingTargetedBuildings(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> (unit.target() == null || !unit.target().isABuilding())),
            "havingTargetedBuildings"
        );
        }

    static Selection havingTargeted(Selection selection, AUnit targetUnit)  {
        return selection.cloneByRemovingIf(
            (unit -> (unit.target() == null || !unit.target().equals(targetUnit))),
            "havingTargeted:" + targetUnit.id()
        );
        }

/**
     * Units whose attack against us is under way but whose shot has not left yet.
     *
     * <p>This means STARTING as well as PENDING, exactly like every other place
     * in the code that asks "is this unit busy shooting" ({@code AttackState
     * .startingOrPending()}). Filtering out STARTING made a unit that had
     * already been ordered to shoot look harmless for one more frame, which is
     * precisely what {@code Bullets.against()} and the dead-man logic use this
     * for.</p>
     */
    static Selection attackStatePending(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.attackState().startingOrPending()),
            "attackStatePending"
        );
        }

    static Selection tankSupport(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isTerranInfantryWithoutMedics() && !unit.isVulture() && !unit.isGoliath()),
            "tankSupport"
        );
        }

    static Selection terranInfantryWithoutMedics(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isTerranInfantryWithoutMedics()), "terranInfantryWithoutMedics"
        );
        }

    static Selection attacking(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isAttacking()), "attacking");
        }

    static Selection notAttacking(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.isAttacking()), "notAttacking");
        }

    static Selection havingSeriousShieldWound(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.shieldWound() >= 40), "havingSeriousShieldWound");
        }

    static Selection criticallyWounded(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.hp() >= 20), "criticallyWounded");
        }

    static Selection crucialUnits(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isCrucialUnit()), "crucialUnits");
        }

    static Selection wounded(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isWounded()), "wounded"
        );
        }

/**
     * Selects only units that do not currently have max hit points.
     */
    static Selection ranged(Selection selection)  {
        return selection.cloneByRemovingIf(unit -> !unit.isRanged(), "ranged");
        }

    static Selection nonRanged(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isRanged()), "nonRanged"
        );
        }

/**
     * Selects melee units that is units which have attack range at most 1 tile.
     */
    static Selection melee(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isMelee()), "melee"
        );
        }

    static Selection lifted(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isLifted()), "notLifted"
        );
        }

    static Selection notUnpowered(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isPowered() && unit.isProtoss() && unit.isABuilding()), "notUnpowered"
        );
        }

    static Selection notLifted(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.isLifted()), "notLifted"
        );
        }

    static Selection air(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isAir()), "air"
        );
        }

    static Selection realUnitsAndCombatBuildings(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isRealUnitOrCombatBuilding()), "realUnitsAndCombatBuildings"
        );
        }

    static Selection realUnitsAndBuildings(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isRealUnitOrBuilding()), "realUnitsAndBuildings"
        );
        }

    static Selection realUnits(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isRealUnit()), "realUnits"
        );
        }

    static Selection workers(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isWorker()), "workers"
        );
        }

/**
     * Selects bases only (including Lairs and Hives).
     */
    static Selection bases(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.type().isBase()), "bases"
        );
        }

    static Selection transports(Selection selection, boolean excludeOverlords)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.type().isTransport() || (excludeOverlords && !unit.type().isTransportExcludeOverlords())),
            ""
        );
        }

/**
     * Selects units being infantry.
     */
    static Selection organic(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.type().isOrganic()), "organic"
        );
        }

    static Selection specialAction(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.lastActionMoreThanAgo(100, Actions.SPECIAL)), "specialAction"
        );
        }

    static Selection havingActiveManager(Selection selection, Class... activeManagers)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isActiveManager(activeManagers)),
            "havingActiveManager:" + CacheKey.toKey(activeManagers)
        );
        }

    static Selection notStuck(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.isStuck()), "notStuck"
        );
        }

    static Selection notSpecialAction(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.lastActionLessThanAgo(100, Actions.SPECIAL)), "notSpecialAction"
        );
        }

    static Selection notGatheringGas(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.isGatheringGas()), "notGatheringGas"
        );
        }

    static Selection notGathering(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.isGatheringMinerals() || unit.isGatheringGas()), "notGathering"
        );
        }

/**
     * Selects units that are gathering minerals.
     */
    static Selection gatheringMinerals(Selection selection, boolean onlyNotCarryingMinerals)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isGatheringMinerals() || (onlyNotCarryingMinerals && unit.isCarryingMinerals())),
            "gatheringMinerals:" + A.trueFalse(onlyNotCarryingMinerals)
        );
        }

    static Selection nonWorkers(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.isWorker()),
            "nonWorkers"
        );
        }

    static Selection nonBuildingsButAllowCombatBuildings(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.isABuilding() && !unit.isCombatBuilding()),
            "nonBuildingsButAllowCombatBuildings"
        );
        }

    static Selection nonBuildings(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.isABuilding()),
            "nonBuildings"
        );
        }

    static Selection groundUnits(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isGroundUnit()),
            "groundUnits"
        );
        }

    static Selection nonReavers(Selection selection)  {
        return selection.cloneByRemovingIf(unit -> unit.isReaver(), "nonReavers");
        }

    static Selection nonTanks(Selection selection)  {
        return selection.cloneByRemovingIf(unit -> unit.isTank(), "nonTanks");
        }

    static Selection tanks(Selection selection)  {
        return selection.cloneByRemovingIf(unit -> !unit.isTank(), "tanks");
        }

    static Selection creepColonies(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Zerg_Creep_Colony)),
            "creepColonies"
        );
        }

    static Selection sporeColonies(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Zerg_Spore_Colony)),
            "sporeColonies"
        );
        }

    static Selection sunkens(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Zerg_Sunken_Colony)),
            "sunkens"
        );
        }

    static Selection zerglings(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Zerg_Zergling)),
            "zerglings"
        );
        }

    static Selection scienceVessels(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Terran_Science_Vessel)),
            "scienceVessels"
        );
        }

    static Selection overlords(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Zerg_Overlord)),
            "overlords"
        );
        }

    static Selection firebats(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Terran_Firebat)),
            "firebats"
        );
        }

    static Selection zealots(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Protoss_Zealot)),
            "zealots"
        );
        }

    static Selection vultures(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Terran_Vulture)),
            "vultures"
        );
        }

    static Selection turrets(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Terran_Missile_Turret)),
            "turrets"
        );
        }

    static Selection dragoons(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Protoss_Dragoon)),
            "dragoons"
        );
        }

    static Selection minerals(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.type().isMineralField()),
            "minerals"
        );
        }

    static Selection hydras(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Zerg_Hydralisk)),
            "hydralisks"
        );
        }

    static Selection marines(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isMarine()),
            "marines"
        );
        }

    static Selection carriers(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isCarrier()),
            "carriers"
        );
        }

    static Selection mutalisks(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isMutalisk()),
            "mutalisks"
        );
        }

    static Selection guardians(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Zerg_Guardian)),
            "guardians"
        );
        }

    static Selection tanksUnsieged(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Terran_Siege_Tank_Tank_Mode)),
            "tanksUnsieged"
        );
        }

    static Selection tanksSieged(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Terran_Siege_Tank_Siege_Mode)),
            "tanksSieged"
        );
        }

    static Selection detectors(Selection selection)  {
        return selection.cloneByRemovingIf(unit -> !unit.isDetector(), "detectors");
        }

    static Selection mobileDetectors(Selection selection)  {
        return selection.cloneByRemovingIf(unit -> !unit.is(
            AUnitType.Protoss_Observer,
            AUnitType.Terran_Science_Vessel,
            AUnitType.Zerg_Overlord
        ), "mobileDetectors");
        }

/**
     * Selects only those units which are hidden, cloaked / burrowed. Not possible to be attacked.
     */
    static Selection effUndetected(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.effUndetected()),
            "effUndetected"
        );
        }

    static Selection effVisibleOrFoggedWithKnownPosition(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.effVisible() && !unit.isFoggedUnitWithKnownPosition()),
            "effVisibleOrFoggedWithKnownPosition"
        );
        }

/**
     * Selects only those units which are visible and not cloaked.
     */
    static Selection effVisible(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.effVisible()),
            "effVisible"
        );
        }

    static Selection lastSeenAtLeastMinuteAgo(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isVisibleUnitOnMap()
                && unit.lastPositionUpdatedAgo() >= 30 * 60
            ),
            "lastSeenAtLeastMinuteAgo"
        );
        }

    static Selection lastActionMoreThanAgo(Selection selection, int framesAgo, Action action)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.lastActionMoreThanAgo(framesAgo, action)),
            "lastActionMoreThanAgo:" + framesAgo + ":" + action.name()
        );
        }

    static Selection lastActionLessThanAgo(Selection selection, int framesAgo, Action action)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.lastActionLessThanAgo(framesAgo, action)),
            "lastActionLessThanAgo:" + framesAgo + ":" + action.name()
        );
        }

    static Selection lastSeenRecently(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isVisibleUnitOnMap()
                && unit.lastPositionUpdatedAgo() >= 30 * 7
            ),
            "lastSeenRecently"
        );
        }

    static Selection underAttackLessThanAgo(Selection selection, int framesAgo)  {
        return selection.cloneByRemovingIf(
            (unit -> unit.lastUnderAttackAgo() > framesAgo
            ),
            "underAttackLessThanAgo:" + framesAgo
        );
        }

    static Selection visibleOnMapOrCombatBuilding(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isVisibleUnitOnMap() && !unit.isCombatBuilding()),
            "visibleOnMapOrCombatBuilding"
        );
        }

/**
     * Selects only those units which are VISIBLE ON MAP (not behind fog of war).
     */
    static Selection visibleOnMap(Selection selection)  {
        return selection.cloneByRemovingIf(
            (unit -> !unit.isVisibleUnitOnMap() || unit instanceof AbstractFoggedUnit),
            "visibleOnMap"
        );
        }

/**
     * Selects only units of given type(s).
     */
    static Selection ofType(Selection selection, AUnitType... types)  {
        return selection.cloneByRemovingIf(
            (unit -> !typeMatches(unit, types)),
            CacheKey.create(types)
        );
        }

    static boolean typeMatches(AUnit unit, AUnitType... haystack) {
        for (AUnitType type : haystack) {
            if (
                type != null && unit.is(type)
                    || (unit != null && type != null && unit.is(AUnitType.Zerg_Egg) && type.equals(unit.buildType()))
            ) {
                return true;
            }
        }
        return false;
    }
}
