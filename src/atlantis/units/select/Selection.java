package atlantis.units.select;

import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.bullets.DeadMan;
import atlantis.map.path.ClosestToEnemyBase;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Units;
import atlantis.units.actions.Actions;
import atlantis.units.fogged.AbstractFoggedUnit;
import atlantis.util.cache.CacheKey;

import java.util.*;
import java.util.stream.Collectors;

public class Selection extends BaseSelection {
    protected Selection(Collection<? extends AUnit> unitsData, String initCachePath) {
        super(unitsData, initCachePath);
    }

    // =========================================================

    /**
     * Selects only units of given type(s).
     */
    public Selection ofType(AUnitType... types) {
        return cloneByRemovingIf(
            (unit -> !typeMatches(unit, types)),
            CacheKey.create(types)
        );
    }

    public Selection type(AUnitType... types) {
        return ofType(types);
    }

    /**
     * Returns whether the type in unit matches one in the haystack
     */
    private boolean typeMatches(AUnit unit, AUnitType... haystack) {
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

    public Selection add(Selection otherSelection) {
        return cloneByAdding(otherSelection.data, null);
    }

    public Selection add(Collection<? extends AUnit> otherUnits) {
        return cloneByAdding(otherUnits, null);
    }

    public Selection add(AUnit addUnit) {
        ArrayList<AUnit> list = new ArrayList<>();
        list.add(addUnit);

        return cloneByAdding(list, null);
    }

//    public Selection withEnemyFoggedUnits() {
//        return cloneByAdding(EnemyUnits.foggedUnits().data, null);
//    }

    /**
     * Returns all units that are closer than <b>maxDist</b> tiles from given <b>otherUnit</b>.
     */
    public Selection inRadius(double maxDist, AUnit unit) {
        return Select.cache.get(
            addToCachePath("inRadiusA:" + maxDist + ":" + unit.idWithHash()),
            0,
            () -> cloneByRemovingIf(
                (u -> !u.hasPosition() || u.distTo(unit) > maxDist),
                "inRadiusA:" + maxDist + ":" + unit.idWithHash()
            )
        );
    }

    /**
     * Returns all units that are closer than <b>maxDist</b> tiles from given <b>position</b>.
     */
    public Selection inRadius(double maxDist, HasPosition position) {
        return Select.cache.get(
            addToCachePath("inRadiusB:" + maxDist + ":" + position),
            0,
            () -> cloneByRemovingIf(
                (u -> u.distTo(position) > maxDist),
                "inRadiusB:" + maxDist + ":" + position
            )
        );
    }

    public Selection inGroundRadius(double maxDist, AUnit unit) {
        return Select.cache.get(
            addToCachePath("inGroundRadius:" + maxDist + ":" + unit.idWithHash()),
            0,
            () -> cloneByRemovingIf(
                (u -> u.groundDist(unit) > maxDist),
                maxDist + ":" + unit.idWithHash()
            )
        );
    }

    public Selection notInRadius(double minDist, HasPosition position) {
        return Select.cache.get(
            addToCachePath("notInRadius:" + minDist + ":" + position),
            0,
            () -> cloneByRemovingIf(
                (u -> u.distTo(position) < minDist),
                "notInRadius:" + minDist + ":" + position
            )
        );
    }

    public Selection inRadius(double maxDist, Selection selectionOfUnits) {
        return Select.cache.get(
            addToCachePath(CacheKey.create("inRadiusSelection", maxDist, selectionOfUnits)),
            1,
            () -> cloneByRemovingIf(
                (u -> selectionOfUnits.nearestTo(u).distTo(u) > maxDist),
                "selection:" + maxDist + ":" + CacheKey.create(selectionOfUnits)
            )
        );
    }

    /**
     * Returns all units that are closer than <b>maxDist</b> tiles from given <b>position</b>.
     */
    public int countInRadius(double maxDist, HasPosition unitOrPosition) {
        return Select.cacheInt.get(
            addToCachePath("countInRadius:" + maxDist + ":" + unitOrPosition),
            0,
            () -> cloneByRemovingIf(
                (u -> u.distTo(unitOrPosition) > maxDist),
                maxDist + ":" + unitOrPosition
            ).count()
        );
    }

    /**
     * Returns all units that are closer than <b>maxDist</b> tiles from given <b>position</b>.
     */
    public Selection maxGroundDist(double maxDist, AUnit unit) {
        if (unit.isAir()) {
            return this;
        }

        return Select.cache.get(
            addToCachePath("maxGroundDist:" + maxDist + ":" + unit.idWithHash()),
            0,
            () -> cloneByRemovingIf(
                (u -> u.groundDist(unit) > maxDist),
                ":" + unit.idWithHash()
            )
        );
    }

    /**
     * Returns whether the type in unit matches one in the haystack
     */
//    private boolean typeMatches(FoggedUnit unit, AUnitType... haystack) {
//        for (AUnitType type : haystack) {
//            if (unit.type().equals(type) || (unit.type().equals(AUnitType.Zerg_Egg) && unit.type().equals(type))) {
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * Selects only units of given type(s).
     */
    public int countOfType(AUnitType... types) {
        int total = 0;
        for (AUnit unit : data) {
            boolean typeMatches = false;
            for (AUnitType type : types) {
                if (unit.is(type) || (unit.is(AUnitType.Zerg_Egg) && type.equals(unit.buildType()))) {
                    typeMatches = true;
                    break;
                }
            }
            if (typeMatches) {
                total++;
            }
        }

        return total;
    }

    public Selection limit(int limit) {
        if (data.isEmpty()) return this;

        List<AUnit> newData = new ArrayList<>();
        int max = Math.min(limit, data.size());
        for (int i = 0; i < max; i++) {
            newData.add(data.get(i));
        }

        return new Selection(newData, null);
    }

    /**
     * Selects only those units which are VISIBLE ON MAP (not behind fog of war).
     */
    public Selection visibleOnMap() {
        return cloneByRemovingIf(
            (unit -> !unit.isVisibleUnitOnMap() || unit instanceof AbstractFoggedUnit),
            "visibleOnMap"
        );
    }

    /**
     * Selects only those units which are visible and not cloaked.
     */
    public Selection effVisible() {
        return cloneByRemovingIf(
            (unit -> !unit.effVisible()),
            "effVisible"
        );
    }

    public Selection effVisibleOrFoggedWithKnownPosition() {
        return cloneByRemovingIf(
            (unit -> !unit.effVisible() && !unit.isFoggedUnitWithKnownPosition()),
            "effVisibleOrFoggedWithKnownPosition"
        );
    }

    /**
     * Selects only those units which are hidden, cloaked / burrowed. Not possible to be attacked.
     */
    public Selection effUndetected() {
        return cloneByRemovingIf(
            (unit -> !unit.effUndetected()),
            "effUndetected"
        );
    }

    public Selection mobileDetectors() {
        return cloneByRemovingIf(unit -> !unit.is(
            AUnitType.Protoss_Observer,
            AUnitType.Terran_Science_Vessel,
            AUnitType.Zerg_Overlord
        ), "mobileDetectors");
    }

    public Selection detectors() {
        return cloneByRemovingIf(unit -> !unit.is(
            AUnitType.Protoss_Photon_Cannon,
            AUnitType.Protoss_Observer,
            AUnitType.Terran_Missile_Turret,
            AUnitType.Terran_Science_Vessel,
            AUnitType.Zerg_Overlord,
            AUnitType.Zerg_Spore_Colony
        ), "detectors");
    }

    public Selection tanksSieged() {
        return cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Terran_Siege_Tank_Siege_Mode)),
            "tanksSieged"
        );
    }

    public Selection guardians() {
        return cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Zerg_Guardian)),
            "guardians"
        );
    }

    public Selection mutalisks() {
        return cloneByRemovingIf(
            (unit -> !unit.isMutalisk()),
            "mutalisks"
        );
    }

    public Selection carriers() {
        return cloneByRemovingIf(
            (unit -> !unit.isCarrier()),
            "carriers"
        );
    }

    public Selection marines() {
        return cloneByRemovingIf(
            (unit -> !unit.isMarine()),
            "marines"
        );
    }

    public Selection hydras() {
        return cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Zerg_Hydralisk)),
            "hydralisks"
        );
    }

    public Selection dragoons() {
        return cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Protoss_Dragoon)),
            "dragoons"
        );
    }

    public Selection zealots() {
        return cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Protoss_Zealot)),
            "zealots"
        );
    }

    public Selection overlords() {
        return cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Zerg_Overlord)),
            "overlords"
        );
    }

    public Selection zerglings() {
        return cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Zerg_Zergling)),
            "zerglings"
        );
    }

    public Selection sunkens() {
        return cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Zerg_Sunken_Colony)),
            "sunkens"
        );
    }

    public Selection tanks() {
        return cloneByRemovingIf(unit -> !unit.isTank(), "tanks");
    }

    public Selection nonTanks() {
        return cloneByRemovingIf(unit -> unit.isTank(), "nonTanks");
    }

    public Selection groundUnits() {
        return cloneByRemovingIf(
            (unit -> !unit.isGroundUnit()),
            "groundUnits"
        );
    }

    public Selection nonBuildings() {
        return cloneByRemovingIf(
            (unit -> unit.isABuilding()),
            "nonBuildings"
        );
    }

    public Selection nonBuildingsButAllowCombatBuildings() {
        return cloneByRemovingIf(
            (unit -> unit.isABuilding() && !unit.isCombatBuilding()),
            "nonBuildingsButAllowCombatBuildings"
        );
    }

    public Selection nonWorkers() {
        return cloneByRemovingIf(
            (unit -> unit.isWorker()),
            "nonWorkers"
        );
    }

    /**
     * Selects units that are gathering minerals.
     */
    public Selection gatheringMinerals(boolean onlyNotCarryingMinerals) {
        return cloneByRemovingIf(
            (unit -> !unit.isGatheringMinerals() || (onlyNotCarryingMinerals && unit.isCarryingMinerals())),
            "gatheringMinerals:" + A.trueFalse(onlyNotCarryingMinerals)
        );
    }

    public Selection notGathering() {
        return cloneByRemovingIf(
            (unit -> unit.isGatheringMinerals() || unit.isGatheringGas()), "notGathering"
        );
    }

    public Selection notGatheringGas() {
        return cloneByRemovingIf(
            (unit -> unit.isGatheringGas()), "notGatheringGas"
        );
    }

    public Selection notSpecialAction() {
        return cloneByRemovingIf(
            (unit -> unit.lastActionLessThanAgo(100, Actions.SPECIAL)), "notSpecialAction"
        );
    }

    public Selection notStuck() {
        return cloneByRemovingIf(
            (unit -> unit.isStuck()), "notStuck"
        );
    }

    public Selection havingActiveManager(Class... activeManagers) {
        return cloneByRemovingIf(
            (unit -> !unit.isActiveManager(activeManagers)),
            "havingActiveManager:" + CacheKey.toKey(activeManagers)
        );
    }

    public Selection specialAction() {
        return cloneByRemovingIf(
            (unit -> unit.lastActionMoreThanAgo(100, Actions.SPECIAL)), "specialAction"
        );
    }

    /**
     * Selects units being infantry.
     */
    public Selection organic() {
        return cloneByRemovingIf(
            (unit -> !unit.type().isOrganic()), "organic"
        );
    }

    public Selection transports(boolean excludeOverlords) {
        return cloneByRemovingIf(
            (unit -> !unit.type().isTransport() || (excludeOverlords && !unit.type().isTransportExcludeOverlords())),
            ""
        );
    }

    /**
     * Selects bases only (including Lairs and Hives).
     */
    public Selection bases() {
        return cloneByRemovingIf(
            (unit -> !unit.type().isBase()), "bases"
        );
    }

    public Selection workers() {
        return cloneByRemovingIf(
            (unit -> !unit.isWorker()), "workers"
        );
    }

    public Selection realUnits() {
        return cloneByRemovingIf(
            (unit -> !unit.isRealUnit()), "realUnits"
        );
    }

    public Selection realUnitsAndBuildings() {
        return cloneByRemovingIf(
            (unit -> !unit.isRealUnitOrBuilding()), "realUnitsAndBuildings"
        );
    }

    public Selection realUnitsAndCombatBuildings() {
        return cloneByRemovingIf(
            (unit -> !unit.isRealUnitOrCombatBuilding()), "realUnitsAndCombatBuildings"
        );
    }

    public Selection air() {
        return cloneByRemovingIf(
            (unit -> !unit.isAir()), "air"
        );
    }

    public Selection notLifted() {
        return cloneByRemovingIf(
            (unit -> unit.isLifted()), "notLifted"
        );
    }

    public Selection lifted() {
        return cloneByRemovingIf(
            (unit -> !unit.isLifted()), "notLifted"
        );
    }

    /**
     * Selects melee units that is units which have attack range at most 1 tile.
     */
    public Selection melee() {
        return cloneByRemovingIf(
            (unit -> !unit.isMelee()), "melee"
        );
    }

    public Selection nonRanged() {
        return cloneByRemovingIf(
            (unit -> !unit.isRanged()), "nonRanged"
        );
    }

    /**
     * Selects only units that do not currently have max hit points.
     */
    public Selection ranged() {
        return cloneByRemovingIf(unit -> !unit.isRanged(), "ranged");
    }

    public Selection wounded() {
        return cloneByRemovingIf(
            (unit -> !unit.isWounded()), "wounded"
        );
    }

    public Selection crucialUnits() {
        return cloneByRemovingIf(
            (unit -> !unit.isCrucialUnit()), "crucialUnits");
    }

    public Selection criticallyWounded() {
        return cloneByRemovingIf(
            (unit -> unit.hp() >= 20), "criticallyWounded");
    }

    public Selection havingSeriousShieldWound() {
        return cloneByRemovingIf(
            (unit -> unit.shieldWound() >= 40), "havingSeriousShieldWound");
    }

    public Selection notAttacking() {
        return cloneByRemovingIf(
            (unit -> unit.isAttacking()), "notAttacking");
    }

    public Selection terranInfantryWithoutMedics() {
        return cloneByRemovingIf(
            (unit -> !unit.isTerranInfantryWithoutMedics()), "terranInfantryWithoutMedics"
        );
    }

    public Selection tankSupport() {
        return cloneByRemovingIf(
            (unit -> !unit.isTerranInfantryWithoutMedics() && !unit.isVulture() && !unit.isGoliath()),
            "tankSupport"
        );
    }

    public Selection havingTargeted(AUnit targetUnit) {
        return cloneByRemovingIf(
            (unit -> (unit.target() == null || !unit.target().equals(targetUnit))),
            "havingTargeted:" + targetUnit.idWithHash()
        );
    }

    public Selection havingTargetedBuildings() {
        return cloneByRemovingIf(
            (unit -> (unit.target() == null || !unit.target().isABuilding())),
            "havingTargetedBuildings"
        );
    }

    public Selection havingAnyTarget() {
        return cloneByRemovingIf(
            (unit -> (unit.target() == null)),
            "havingAnyTarget"
        );
    }

    /**
     * Selects only buildings.
     */
    public Selection buildings() {
        return cloneByRemovingIf(
            (unit -> !unit.type().isABuilding() && !unit.type().isAddon()), "buildings"
        );
    }

    /**
     * Fogged units may have no position if e.g. unit moved and we don't know where it is.
     */
    public Selection havingPosition() {
        return cloneByRemovingIf(u -> !u.hasPosition(), "havingPosition");
    }

    public Selection beingVisibleUnitOrNotVisibleFoggedUnit() {
        return cloneByRemovingIf(
            u -> u.u() != null || ((u instanceof AbstractFoggedUnit) && !u.position().isPositionVisible()),
            "beingVisibleUnitOrNotVisibleFoggedUnit"
        );
    }

    public int countRunning(int maxStartedRunningAgo) {
        return cloneByRemovingIf(
            u -> (!u.isRunning() || u.lastStartedRunningMoreThanAgo(maxStartedRunningAgo)),
            "countRunning:" + maxStartedRunningAgo
        ).count();
    }

    public int countRetreating() {
        return cloneByRemovingIf(
            u -> !u.isRetreating(),
            "countRetreating"
        ).count();
    }

    public Selection cloakable() {
        return cloneByRemovingIf(u -> !u.type().is(
            AUnitType.Protoss_Dark_Templar,
            AUnitType.Zerg_Lurker
        ), "cloakable");
    }

    public Selection notDeadMan() {
        return cloneByRemovingIf(u -> DeadMan.isDeadMan(u), "notDeadMan");
    }

    public Selection havingWeapon() {
        return cloneByRemovingIf(u -> !u.hasAnyWeapon(), "havingWeapon");
    }

    public Selection havingAntiGroundWeapon() {
        return cloneByRemovingIf(u -> !u.canAttackGroundUnits(), "havingAntiGroundWeapon");
    }

    public Selection notHavingAntiAirWeapon() {
        return cloneByRemovingIf(u -> u.canAttackAirUnits(), "notHavingAntiAirWeapon");
    }

    public Selection havingAntiAirWeapon() {
        return cloneByRemovingIf(u -> !u.canAttackAirUnits(), "havingAntiAirWeapon");
    }

    public Selection havingSmallerRange(AUnit than) {
        return cloneByRemovingIf(
            u -> !u.canAttackTarget(than)
//                || than.type().equals(u.type())
                || u.weaponRangeAgainst(than) < than.weaponRangeAgainst(u),
            "havingSmallerRange:" + than.type().id()
        );
    }

    public Selection notPurelyAntiAir() {
        return cloneByRemovingIf(u -> !u.isPurelyAntiAir(), "notPurelyAntiAir");
    }

    public int totalHp() {
        return data.stream()
            .map(AUnit::hp)
            .reduce(0, Integer::sum);
    }

    public Selection combatUnits() {
        return cloneByRemovingIf(
            (unit -> !unit.isCompleted() || !unit.type().isCombatUnit()), "combatUnits"
        );
    }

    /**
     * Selects military buildings like Photon Cannon, Bunker, Spore Colony, Sunken Colony
     */
    public Selection combatBuildings(boolean includeCreepColonies) {
        return cloneByRemovingIf(
            (unit -> includeCreepColonies ? !unit.type().isCombatBuildingOrCreepColony() : !unit.type().isCombatBuilding()),
            "combatBuildings:" + A.trueFalse(includeCreepColonies)
        );
    }

    public Selection combatBuildingsAntiLand() {
        return cloneByRemovingIf(
            (unit -> !unit.is(AUnitType.Protoss_Photon_Cannon, AUnitType.Terran_Bunker, AUnitType.Zerg_Sunken_Colony)),
            "combatBuildingsAntiLand"
        );
    }

    public Selection combatBuildingsAntiAir() {
        return cloneByRemovingIf(
            (unit -> !unit.is(
                AUnitType.Protoss_Photon_Cannon,
                AUnitType.Terran_Bunker,
                AUnitType.Terran_Missile_Turret,
                AUnitType.Zerg_Spore_Colony
            )),
            "combatBuildingsAntiAir"
        );
    }

    public Selection combatBuildingsAnti(AUnit unit) {
        return unit.isGroundUnit() ? combatBuildingsAntiLand() : combatBuildingsAntiAir();
    }

    public Selection onlyCompleted() {
        return cloneByRemovingIf(
            (unit -> !unit.isCompleted()), "onlyCompleted"
        );
    }

    /**
     * Selects only those Terran vehicles/buildings that can be repaired so it has to be:<br />
     * - mechanical<br />
     * - not 100% healthy<br />
     */
    public Selection repairable(boolean checkIfWounded) {
        return cloneByRemovingIf(
            (unit -> !unit.isCompleted()
                || (!unit.type().isMechanical() && (!unit.isABuilding() || !unit.isTerran()))
                || (checkIfWounded && !unit.isWounded())
            ),
            "repairable:" + A.trueFalse(checkIfWounded)
        );
    }

    public boolean onlyMelee() {
        return units().onlyMelee();
    }

    public boolean onlyRanged() {
        return units().onlyRanged();
    }

    public boolean mostlyRanged() {
        return count() * 0.7 <= ranged().count();
    }

    public boolean onlyAir() {
        return units().onlyAir();
    }

    public boolean onlyOfType(AUnitType type) {
        return count() > 0 && count() == countOfType(type);
    }

    public boolean mostlyOfType(AUnitType type, int percentThreshold) {
        return count() > 0 && (count() * percentThreshold >= countOfType(type) * 100);
    }

    public Selection burrowed() {
        return cloneByRemovingIf(
            (unit -> !unit.isBurrowed()), "burrowed"
        );
    }

    public Selection burrowing() {
        return cloneByRemovingIf(
            (unit -> !unit.isBurrowing()), "burrowing"
        );
    }

    public Selection loaded() {
        return cloneByRemovingIf(
            (unit -> !unit.isLoaded()), "loaded"
        );
    }

    public Selection notLoaded() {
        return cloneByRemovingIf(
            (AUnit::isLoaded), "notLoaded");
    }

    /**
     * Selects these units (makes sense only for workers) who aren't assigned to repair any other unit.
     */
    public Selection notRepairing() {
        return cloneByRemovingIf(
            (unit -> unit.isRepairing() || RepairAssignments.isRepairerOfAnyKind(unit)), "notRepairing"
        );
    }

    public Selection repairing() {
        return cloneByRemovingIf(
            (unit -> unit.isRepairing() || RepairAssignments.isRepairerOfAnyKind(unit)), "repairing"
        );
    }

    public Selection builders() {
        return cloneByRemovingIf(
            (unit -> !unit.isBuilder()), "builders"
        );
    }

    public Selection notProtectors() {
        return cloneByRemovingIf(
            (AUnit::isProtector), "notProtectors"
        );
    }

    public Selection protectors() {
        return cloneByRemovingIf(
            (u -> !u.isProtector()), "protectors"
        );
    }

    public Selection producing(AUnitType producingThisUnit) {
        return cloneByRemovingIf(
            (u -> u.trainingQueue().isEmpty() || !u.isTraining(producingThisUnit)),
            "producing:" + producingThisUnit.id()
        );
    }

    public Selection healthy() {
        return cloneByRemovingIf(
            (AUnit::isWounded), "healthy"
        );
    }

    /**
     * Selects these transport/bunker units which have still enough room inside.
     */
    public Selection havingSpaceFree(int spaceRequired) {
        return cloneByRemovingIf(
            (unit -> unit.spaceRemaining() < spaceRequired), "havingSpaceFree:" + spaceRequired);
    }

    public Selection havingCooldownMin(int minCooldown) {
        return cloneByRemovingIf(
            (unit -> unit.cooldown() < minCooldown), "havingCooldownMin:" + minCooldown
        );
    }

    public Selection havingCooldownMax(int maxCooldown) {
        return cloneByRemovingIf(
            (unit -> unit.cooldown() > maxCooldown), "havingCooldownMax:" + maxCooldown
        );
    }

    public Selection havingEnergy(int minEnergy) {
        return cloneByRemovingIf(
            (unit -> !unit.energy(minEnergy)), "havingEnergy:" + minEnergy
        );
    }

    public Selection notHavingHp(int maxHp) {
        return cloneByRemovingIf(
            (unit -> unit.hpMoreThan(maxHp)), "notHavingHp:" + maxHp
        );
    }

    public Selection notBeingHealed() {
        return cloneByRemovingIf(
            (unit -> unit.isBeingHealed()), "notBeingHealed"
        );
    }

    public Selection havingAtLeastHp(int minHp) {
        return cloneByRemovingIf(
            (unit -> unit.hpLessThan(minHp)), "havingAtLeastHp:" + minHp
        );
    }

    public Selection nonStasisedOrLockedDown() {
        return cloneByRemovingIf(
            (unit -> unit.isStasised() || unit.isLockedDown()), "nonStasisedOrLockedDown"
        );
    }

    public Selection mechanical() {
        return cloneByRemovingIf(
            (unit -> !unit.isMechanical()), "mechanical"
        );
    }

    public Selection medics() {
        return cloneByRemovingIf(
            (unit -> !unit.isMedic()), "medics"
        );
    }

    public Selection observers() {
        return cloneByRemovingIf(
            (unit -> !unit.isObserver()), "observers"
        );
    }

    public Selection bunkers() {
        return cloneByRemovingIf(
            (unit -> !unit.isBunker()), "bunkers"
        );
    }

    public Selection cannons() {
        return cloneByRemovingIf(
            (unit -> !unit.isCannon()), "cannons"
        );
    }

    public Selection lurkers() {
        return cloneByRemovingIf(
            (unit -> !unit.isLurker()), "lurkers"
        );
    }

    public Selection reavers() {
        return cloneByRemovingIf(
            (unit -> !unit.isReaver()), "reavers"
        );
    }

    /**
     * Selects these units (makes sense only for workers) who aren't assigned to construct anything.
     */
    public Selection notConstructing() {
        return cloneByRemovingIf(
            (unit -> unit.isConstructing() || unit.isMorphing() || unit.isBuilder()), "notConstructing"
        );
    }

    public Selection free() {
        return cloneByRemovingIf(
            (u -> u.isBusy() || u.isLifted() || u.isResearching()), "free"
        );
    }

    public Selection withAddon() {
        return cloneByRemovingIf(
            (u -> !u.hasAddon()), "withAddon"
        );
    }

    public Selection withoutAddon() {
        return cloneByRemovingIf(
            (u -> u.hasAddon()), "withoutAddon"
        );
    }

    /**
     * Selects these units which are not scouts.
     */
    public Selection notScout() {
        return cloneByRemovingIf(
            (u -> u.isScout() || u.isFlyingScout()), "notScout");
    }

    /**
     * Selects these units which are not carrynig nor minerals, nor gas.
     */
    public Selection notCarrying() {
        return cloneByRemovingIf(
            (unit -> unit.isCarryingGas() || unit.isCarryingMinerals()), "notCarrying"
        );
    }

    public Selection notImmobilized() {
        return cloneByRemovingIf(
            (unit -> !unit.notImmobilized()), "notImmobilized"
        );
    }

    public Selection excludeTypes(AUnitType... types) {
        return cloneByRemovingIf(
            (unit -> unit.is(types)), "excludeTypes:" + CacheKey.create(types)
        );
    }

    public Selection excludeMedics() {
        return cloneByRemovingIf(AUnit::isMedic, "excludeMedics");
    }

    public Selection excludeOverlords() {
        return cloneByRemovingIf(AUnit::isOverlord, "excludeOverlords");
    }

    public Selection excludeMarines() {
        return cloneByRemovingIf(AUnit::isMarine, "excludeMarines");
    }

    public Selection excludeTanks() {
        return cloneByRemovingIf(AUnit::isTank, "excludeTanks");
    }

    public Selection excludeEggsAndLarvae() {
        return cloneByRemovingIf(AUnit::isLarvaOrEgg, "excludeEggsAndLarvae");
    }

    public Selection notRunning() {
        return cloneByRemovingIf(AUnit::isRunning, "notRunning");
    }

    public Selection farFromAntiAirBuildings(double minDistToBuilding) {
        return cloneByRemovingIf((unit -> {
            AUnit nearestAntiAirBuilding = EnemyUnits.discovered().combatBuildingsAntiAir().nearestTo(unit);
            return nearestAntiAirBuilding != null && nearestAntiAirBuilding.distTo(unit) < minDistToBuilding;
        }), "farFromAntiAirBuildings:" + minDistToBuilding);
    }

    public Selection hasPathFrom(HasPosition fromPosition) {
        return cloneByRemovingIf(
            (unit -> !unit.hasPathTo(fromPosition)), "hasPathFrom:" + fromPosition
        );
    }

    public Selection canBeAttackedBy(AUnit attacker, double extraMargin) {
        return cloneByRemovingIf(target -> !attacker.canAttackTarget(
            target, true, true, false, extraMargin
        ), "canBeAttackedBy:" + attacker.idWithHash() + ":" + extraMargin);
    }

    /**
     * Selects only those units from current selection, which can be both <b>attacked by</b> given unit (e.g.
     * Zerglings can't attack Overlord) and are <b>in shot range</b> to the given <b>unit</b>.
     */
    public Selection canAttack(AUnit target, boolean checkShootingRange, boolean checkVisibility, double safetyMargin) {
        return cloneByRemovingIf(
            attacker -> !attacker.canAttackTarget(
                target, checkShootingRange, checkVisibility, false, safetyMargin
            ),
            "canAttack:" + target.idWithHash()
                + ":" + checkShootingRange
                + ":" + checkVisibility
                + ":" + safetyMargin
        );
    }

    public Selection canAttack(AUnit target, double safetyMargin) {
        return cloneByRemovingIf(attacker -> !attacker.canAttackTarget(
            target, true, true, false, safetyMargin
        ), "canAttack:" + target.idWithHash() + ":" + safetyMargin);
    }


    public Selection inShootRangeOf(AUnit attacker) {
        return canBeAttackedBy(attacker, 0);
    }

    public Selection inShootRangeOf(double shootingRangeBonus, AUnit attacker) {
        return canBeAttackedBy(attacker, shootingRangeBonus);
    }

    public Selection facing(AUnit target) {
        return cloneByRemovingIf(
            attacker -> !attacker.isFacing(target),
            "facing:" + target.idWithHash()
        );
    }

    public Selection notShowingBackToUs(AUnit target) {
        return cloneByRemovingIf(
            attacker -> attacker.isOtherUnitShowingBackToUs(target),
            "notShowingBackToUs:" + target.idWithHash()
        );
    }

    // =========================================================
    // Localization-related methods

    /**
     * Returns closest unit to given <b>position</b> from all units in the current selection.
     */
    public AUnit nearestTo(HasPosition position) {
        if (data.isEmpty() || position == null) return null;
        if (data.size() == 1) return data.get(0);

        sortDataByDistanceTo(position, true);

        return data.isEmpty() ? null : data.get(0);
    }

    public double distToNearest(HasPosition position) {
        AUnit nearest = nearestTo(position);
        if (nearest == null) return 9999;

        return nearest.distTo(position);
    }

    public AUnit groundNearestTo(HasPosition position) {
        if (data.isEmpty() || position == null) return null;

        if (data.size() == 1) return data.get(0);

        sortDataByGroundDistanceTo(position, true);

        return data.isEmpty() ? null : data.get(0);
    }

    public AUnit groundFarthestTo(HasPosition position) {
        if (data.isEmpty() || position == null) return null;

        if (data.size() == 1) return data.get(0);

        sortDataByGroundDistanceTo(position, false);

        return data.isEmpty() ? null : data.get(0);
    }

    public boolean nearestToDistLess(HasPosition position, double maxDist) {
        AUnit nearest = nearestTo(position);
        return nearest != null && nearest.distTo(position) <= maxDist;
    }

    public boolean nearestToDistMore(HasPosition position, double maxDist) {
        AUnit nearest = nearestTo(position);
        return nearest != null && nearest.distTo(position) >= maxDist;
    }

    public AUnit mostDistantTo(HasPosition position) {
        if (data.isEmpty() || position == null) {
            return null;
        }
        if (data.size() == 1) {
            return data.get(0);
        }

        sortDataByDistanceTo(position, false);

        return data.isEmpty() ? null : (AUnit) data.get(0);
    }

    public AUnit mostDistantToBase() {
        return mostDistantTo(Select.mainOrAnyBuilding());
    }

    public AUnit nearestToBase() {
        return nearestTo(Select.mainOrAnyBuilding());
    }

    public AUnit closestToEnemyBase() {
        return ClosestToEnemyBase.from(this);
    }

    /**
     * From all units currently in selection, returns closest unit to given <b>position</b>.
     */
    public AUnit nearestToOrNull(HasPosition position, double maxLength) {
        if (data.isEmpty() || position == null) {
            return null;
        }

//        Position position;
//        if (positionOrUnit instanceof APosition) {
//            position = (APosition) positionOrUnit;
//        } else if (positionOrUnit instanceof Position) {
//            position = (Position) positionOrUnit;
//        } else {
//            position = ((AUnit) positionOrUnit).getPosition();
//        }

        sortDataByDistanceTo(position, true);
        AUnit nearestUnit = (AUnit) data.get(0);

        if (nearestUnit != null && nearestUnit.distTo(position) < maxLength) {
            return nearestUnit;
        }
        else {
            return null;
        }
    }

    public AUnit mostWounded() {
        if (data.isEmpty()) {
            return null;
        }

//        sortByHealth();
        sortByWound();

//        print("Most wounded: " + data.get(0).type());

        return data.get(0);
    }

    public AUnit mostWoundedOrNearest(HasPosition nearestTo) {
        if (data.isEmpty()) {
            return null;
        }

//        sortByHealth();
        sortByWound();
        AUnit first = data.get(0);

        if (first.isWounded()) return first;

        return nearestTo(nearestTo);
    }

    // =========================================================
    // Special retrieve

    /**
     * Returns <b>true</b> if current selection contains at least one unit.
     */
    public boolean anyExists() {
        return !data.isEmpty();
    }

    /**
     * Returns first unit that matches previous conditions or null.
     */
    public AUnit first() {
        return data.isEmpty() ? null : (AUnit) data.get(0);
    }

    public AUnit second() {
        return data.size() < 2 ? null : (AUnit) data.get(1);
    }

    public AUnit randomWithSeed(int seed) {
        if (data.isEmpty()) return null;
        if (data.size() == 1) return data.get(0);

        Random rand = new Random(seed);
        return data.get(rand.nextInt(data.size()));
    }

    /**
     * Returns first unit that matches previous conditions or null if no units match conditions.
     */
    public AUnit last() {
        return data.isEmpty() ? null : (AUnit) data.get(data.size() - 1);
    }

    /**
     * Returns random unit that matches previous conditions or null if no units matched all conditions.
     */
    public AUnit random() {
        return (AUnit) A.getRandomElement(data);
    }

    // === High-level of abstraction ===========================

    public AUnit lowestHealth() {
        int lowestHealth = 99999;
        AUnit lowestHealthUnit = null;

        for (Iterator it = data.iterator(); it.hasNext(); ) {
            AUnit unit = (AUnit) it.next();
            if (unit.hp() < lowestHealth) {
                lowestHealth = unit.hp();
                lowestHealthUnit = unit;
            }
        }

        return lowestHealthUnit;
    }

    public boolean areAllBusy() {
        for (Iterator<AUnit> it = (Iterator) data.iterator(); it.hasNext(); ) {
            AUnit unit = it.next();
            if (!unit.isBusy()) return false;
        }

        return true;
    }

    // === Operations on set of units ==========================

    /**
     * @return all units except for the given one
     */
    public Selection exclude(AUnit unitToExclude) {
        if (unitToExclude == null) return new Selection(new ArrayList<>(data), null);

        List<AUnit> newData = new ArrayList<>(data);
        newData.remove(unitToExclude);
        return new Selection(newData, "exclude:" + unitToExclude.id());
    }

    public Selection exclude(Collection unitsToExclude) {
        List<AUnit> newData = new ArrayList<>(data);
        newData.removeAll(unitsToExclude);
        return new Selection(newData, null);
    }

    public Selection exclude(Selection otherSelection) {
        return exclude(otherSelection.list());
    }

    /**
     * Reverse the order in which units are returned.
     */
    public Selection reverse() {
        Collections.reverse(data);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("Units (" + data.size() + "):\n");

        for (AUnit unit : data) {
            string.append("   - ").append(unit.type()).append(" (ID:").append(unit.id()).append(")\n");
        }

        return string.toString();
    }

    // =========================================================
    // Get results

    /**
     * Selects result as an iterable collection (list).
     */
    public List<AUnit> list() {
        return data;
    }

    /**
     * Returns result as an <b>Units</b> object, which contains multiple useful methods to handle set of
     * units.
     */
    public Units units() {
        return new Units(this.data);
    }

    /**
     * Returns number of units matching all previous conditions.
     */
    public int count() {
        return data.size();
    }

    public boolean atLeast(double min) {
        return data.size() >= min;
    }

    public boolean atMost(double max) {
        return data.size() <= max;
    }

    /**
     * Returns true if there are no units that fullfilled all previous conditions.
     */
    public boolean isEmpty() {
        return data.size() == 0;
    }

    public boolean empty() {
        return data.size() == 0;
    }

    public boolean isNotEmpty() {
        return data.size() > 0;
    }

    public boolean notEmpty() {
        return data.size() > 0;
    }

    /**
     * Returns number of units matching all previous conditions.
     */
    public int size() {
        return data.size();
    }

    public List<AUnit> sortDataByGroundDistanceTo(final HasPosition position, final boolean nearestFirst) {
//        if (position == null) {
//            return null;
//        }

        if (data.size() != 1) {
            Collections.sort(data, new Comparator<HasPosition>() {
                @Override
                public int compare(HasPosition p1, HasPosition p2) {
                    if (!(p1 instanceof HasPosition)) {
                        throw new RuntimeException("Invalid comparison: " + p1);
                    }
                    if (!(p2 instanceof HasPosition)) {
                        throw new RuntimeException("Invalid comparison: " + p2);
                    }

                    double distance1 = p1.groundDist(position);
                    double distance2 = p2.groundDist(position);

                    return nearestFirst ? Double.compare(distance1, distance2) : Double.compare(distance2, distance1);
                }
            });
        }

        return data;
    }

    public List<AUnit> sortDataByDistanceTo(final HasPosition position, final boolean nearestFirst) {
//        if (position == null) {
//            return null;
//        }

        if (data.size() != 1) {
            Collections.sort(data, new Comparator<HasPosition>() {
                @Override
                public int compare(HasPosition p1, HasPosition p2) {
                    if (!(p1 instanceof HasPosition)) {
                        throw new RuntimeException("Invalid comparison: " + p1);
                    }
                    if (!(p2 instanceof HasPosition)) {
                        throw new RuntimeException("Invalid comparison: " + p2);
                    }

                    double distance1 = p1.distTo(position);
                    double distance2 = p2.distTo(position);

                    return nearestFirst ? Double.compare(distance1, distance2) : Double.compare(distance2, distance1);
                }
            });
        }

        return data;
    }

    public List<AUnit> sortDataByDistanceTo(final AUnit unit, final boolean nearestFirst) {
        if (data.size() != 1) {
            Collections.sort(data, new Comparator<AUnit>() {
                @Override
                public int compare(AUnit p1, AUnit p2) {
                    if (!(p1 instanceof HasPosition)) {
                        throw new RuntimeException("Invalid comparison: " + p1);
                    }
                    if (!(p2 instanceof HasPosition)) {
                        throw new RuntimeException("Invalid comparison: " + p2);
                    }

                    double distance1 = unit.distTo(p1);
                    double distance2 = unit.distTo(p2);

                    return nearestFirst ? Double.compare(distance1, distance2) : Double.compare(distance2, distance1);
                }
            });
        }

        return data;
    }

//    public Selection limit(int n) {
//        if (data.isEmpty()) {
//            return new Selection(new ArrayList<>(), "");
//        }
//
//        return new Selection(
//            data.subList(0, Math.min(n, data.size())),
//            currentCachePath + ":limit:" + n
//        );
//    }

//    public List<AUnit> sortDataByDistanceTo(final AUnit unit, final boolean nearestFirst) {
//        if (data.size() != 1) {
//            Collections.sort(data, new Comparator<AUnit>() {
//                @Override
//                public int compare(AUnit p1, AUnit p2) {
//                    if (!(p1 instanceof HasPosition)) {
//                        throw new RuntimeException("Invalid comparison: " + p1);
//                    }
//                    if (!(p2 instanceof HasPosition)) {
//                        throw new RuntimeException("Invalid comparison: " + p2);
//                    }
//
//                    double distance1 = unit.distTo(p1);
//                    double distance2 = unit.distTo(p2);
//
//                    return nearestFirst ? Double.compare(distance1, distance2) : Double.compare(distance2, distance1);
//                }
//            });
//        }
//
//        return data;
//    }

    public Selection sortByNearestTo(final AUnit unit) {
        if (data.isEmpty()) {
            return new Selection(new ArrayList<>(), "");
        }

        if (data.size() != 1) {
            data.sort(new Comparator<AUnit>() {
                @Override
                public int compare(AUnit p1, AUnit p2) {
                    if (!(p1 instanceof HasPosition)) {
                        throw new RuntimeException("Invalid comparison: " + p1);
                    }
                    if (!(p2 instanceof HasPosition)) {
                        throw new RuntimeException("Invalid comparison: " + p2);
                    }

                    double distance1 = unit.distTo(p1);
                    double distance2 = unit.distTo(p2);

                    return Double.compare(distance1, distance2);
                }
            });
        }

        return this;
    }

    public Selection sortByHealth() {
        if (data.isEmpty()) {
            return new Selection(new ArrayList<>(), "");
        }

        if (data.size() != 1) {
            data.sort(Comparator.comparingDouble(AUnit::hpPercent));
        }

        return this;
    }

    public Selection sortByWound() {
        if (data.isEmpty()) {
            return new Selection(new ArrayList<>(), "");
        }

        if (data.size() != 1) {
            data.sort(Comparator.comparingDouble(AUnit::woundOrder));
        }

        return this;
    }

//    public Selection sortByIdAsc() {
//        if (data.isEmpty()) {
//            return new Selection(new ArrayList<>(), "");
//        }
//
//        data.sort(Comparator.comparingInt(AUnit::id));
//
//        return this;
//    }

    public APosition center() {
        return units().average();
    }

    public Selection removeDuplicates() {
        List<AUnit> newData = data.stream().distinct().collect(Collectors.toList());
        return new Selection(newData, currentCachePath + ":removeDuplicates");
    }

    public Selection print() {
        return print(null);
    }

    public Selection print(String message) {
        System.out.println("=== " + (message != null ? message : currentCachePath) + " (" + size() + ") ===");
        for (AUnit unit : data) {
            System.out.println(unit.toString());

        }
        System.out.println();
        return this;
    }

    public String unitIds() {
        StringBuilder result = new StringBuilder("ids(");

        for (AUnit unit : list()) {
            result.append(unit.id()).append(",");
        }

        return result.append(")").toString();
    }
}
