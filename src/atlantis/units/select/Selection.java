package atlantis.units.select;

import atlantis.battle.eval.estimate.Estimate;
import atlantis.battle.Squad;
import atlantis.game.A;
import atlantis.game.ARandom;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.bullets.DeadMan;
import atlantis.map.path.ClosestToEnemyBase;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Units;
import atlantis.units.actions.Action;
import atlantis.units.actions.Actions;
import atlantis.units.fogged.AbstractFoggedUnit;
import atlantis.units.select.CacheKey;
import atlantis.util.log.ErrorLog;

import java.util.*;
import java.util.stream.Collectors;

public class Selection extends BaseSelection {
    protected Selection(Collection<? extends AUnit> unitsData, String initCachePath) {
        super(unitsData, initCachePath);
    }

    // =========================================================

    public Selection ofType(AUnitType... types)  {
        return SelectionMembership.ofType(this, types);
    }

    public Selection type(AUnitType... types) {
        return ofType(types);
    }

    /**
     * Returns whether the type in unit matches one in the haystack
     */
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
        return SelectionRadius.inRadius(this, maxDist, unit);
    }

    /**
     * Returns all units that are closer than <b>maxDist</b> tiles from given <b>position</b>.
     */
    public Selection inRadius(double maxDist, HasPosition position) {
        return SelectionRadius.inRadius(this, maxDist, position);
    }

    public Selection inGroundRadius(double maxDist, AUnit unit) {
        return SelectionRadius.inGroundRadius(this, maxDist, unit);
    }

    public Selection notInRadius(double minDist, HasPosition position) {
        return SelectionRadius.notInRadius(this, minDist, position);
    }

    public Selection inRadius(double maxDist, Selection selectionOfUnits) {
        return SelectionRadius.inRadius(this, maxDist, selectionOfUnits);
    }

    /**
     * Returns all units that are closer than <b>maxDist</b> tiles from given <b>position</b>.
     */
    public int countInRadius(double maxDist, HasPosition unitOrPosition) {
        return SelectionRadius.countInRadius(this, maxDist, unitOrPosition);
    }

    /**
     * Returns all units that are closer than <b>maxDist</b> tiles from given <b>position</b>.
     */
    public Selection maxGroundDist(double maxDist, AUnit unit) {
        return SelectionRadius.maxGroundDist(this, maxDist, unit);
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

    public Selection visibleOnMap()  {
        return SelectionMembership.visibleOnMap(this);
    }

    public Selection visibleOnMapOrCombatBuilding()  {
        return SelectionMembership.visibleOnMapOrCombatBuilding(this);
    }

    public Selection underAttackLessThanAgo(int framesAgo)  {
        return SelectionMembership.underAttackLessThanAgo(this, framesAgo);
    }

    public Selection lastSeenRecently()  {
        return SelectionMembership.lastSeenRecently(this);
    }

    public Selection lastActionLessThanAgo(int framesAgo, Action action)  {
        return SelectionMembership.lastActionLessThanAgo(this, framesAgo, action);
    }

    public Selection lastActionMoreThanAgo(int framesAgo, Action action)  {
        return SelectionMembership.lastActionMoreThanAgo(this, framesAgo, action);
    }

    public Selection lastSeenAtLeastMinuteAgo()  {
        return SelectionMembership.lastSeenAtLeastMinuteAgo(this);
    }

    public Selection regionsMatch(HasPosition position) {
        if (position == null) return null;

        return cloneByRemovingIf(
            (unit -> !unit.regionsMatch(position)),
            "regionsMatch:" + position.toString()
        );
    }

    public Selection effVisible()  {
        return SelectionMembership.effVisible(this);
    }

    public Selection effVisibleOrFoggedWithKnownPosition()  {
        return SelectionMembership.effVisibleOrFoggedWithKnownPosition(this);
    }

    public Selection effUndetected()  {
        return SelectionMembership.effUndetected(this);
    }

    public Selection mobileDetectors()  {
        return SelectionMembership.mobileDetectors(this);
    }

    public Selection detectors()  {
        return SelectionMembership.detectors(this);
    }

    public Selection tanksSieged()  {
        return SelectionMembership.tanksSieged(this);
    }

    public Selection tanksUnsieged()  {
        return SelectionMembership.tanksUnsieged(this);
    }

    public Selection guardians()  {
        return SelectionMembership.guardians(this);
    }

    public Selection mutalisks()  {
        return SelectionMembership.mutalisks(this);
    }

    public Selection carriers()  {
        return SelectionMembership.carriers(this);
    }

    public Selection marines()  {
        return SelectionMembership.marines(this);
    }

    public Selection hydras()  {
        return SelectionMembership.hydras(this);
    }

    public Selection minerals()  {
        return SelectionMembership.minerals(this);
    }

    public Selection dragoons()  {
        return SelectionMembership.dragoons(this);
    }

    public Selection turrets()  {
        return SelectionMembership.turrets(this);
    }

    public Selection vultures()  {
        return SelectionMembership.vultures(this);
    }

    public Selection zealots()  {
        return SelectionMembership.zealots(this);
    }

    public Selection firebats()  {
        return SelectionMembership.firebats(this);
    }

    public Selection overlords()  {
        return SelectionMembership.overlords(this);
    }

    public Selection scienceVessels()  {
        return SelectionMembership.scienceVessels(this);
    }

    public Selection zerglings()  {
        return SelectionMembership.zerglings(this);
    }

    public Selection sunkens()  {
        return SelectionMembership.sunkens(this);
    }

    public Selection sporeColonies()  {
        return SelectionMembership.sporeColonies(this);
    }

    public Selection creepColonies()  {
        return SelectionMembership.creepColonies(this);
    }

    public Selection tanks()  {
        return SelectionMembership.tanks(this);
    }

    public Selection nonTanks()  {
        return SelectionMembership.nonTanks(this);
    }

    public Selection nonReavers()  {
        return SelectionMembership.nonReavers(this);
    }

    public Selection groundUnits()  {
        return SelectionMembership.groundUnits(this);
    }

    public Selection nonBuildings()  {
        return SelectionMembership.nonBuildings(this);
    }

    public Selection nonBuildingsButAllowCombatBuildings()  {
        return SelectionMembership.nonBuildingsButAllowCombatBuildings(this);
    }

    public Selection nonWorkers()  {
        return SelectionMembership.nonWorkers(this);
    }

    public Selection gatheringMinerals(boolean onlyNotCarryingMinerals)  {
        return SelectionMembership.gatheringMinerals(this, onlyNotCarryingMinerals);
    }

    public Selection notGathering()  {
        return SelectionMembership.notGathering(this);
    }

    public Selection notGatheringGas()  {
        return SelectionMembership.notGatheringGas(this);
    }

    public Selection notSpecialAction()  {
        return SelectionMembership.notSpecialAction(this);
    }

    public Selection notStuck()  {
        return SelectionMembership.notStuck(this);
    }

    public Selection havingActiveManager(Class... activeManagers)  {
        return SelectionMembership.havingActiveManager(this, activeManagers);
    }

    public Selection specialAction()  {
        return SelectionMembership.specialAction(this);
    }

    public Selection organic()  {
        return SelectionMembership.organic(this);
    }

    public Selection transports(boolean excludeOverlords)  {
        return SelectionMembership.transports(this, excludeOverlords);
    }

    public Selection bases()  {
        return SelectionMembership.bases(this);
    }

    public Selection workers()  {
        return SelectionMembership.workers(this);
    }

    public Selection realUnits()  {
        return SelectionMembership.realUnits(this);
    }

    public Selection realUnitsAndBuildings()  {
        return SelectionMembership.realUnitsAndBuildings(this);
    }

    public Selection realUnitsAndCombatBuildings()  {
        return SelectionMembership.realUnitsAndCombatBuildings(this);
    }

    public Selection air()  {
        return SelectionMembership.air(this);
    }

    public Selection notLifted()  {
        return SelectionMembership.notLifted(this);
    }

    public Selection notUnpowered()  {
        return SelectionMembership.notUnpowered(this);
    }

    public Selection lifted()  {
        return SelectionMembership.lifted(this);
    }

    public Selection melee()  {
        return SelectionMembership.melee(this);
    }

    public Selection nonRanged()  {
        return SelectionMembership.nonRanged(this);
    }

    public Selection ranged()  {
        return SelectionMembership.ranged(this);
    }

    public Selection wounded()  {
        return SelectionMembership.wounded(this);
    }

    public Selection crucialUnits()  {
        return SelectionMembership.crucialUnits(this);
    }

    public Selection criticallyWounded()  {
        return SelectionMembership.criticallyWounded(this);
    }

    public Selection havingSeriousShieldWound()  {
        return SelectionMembership.havingSeriousShieldWound(this);
    }

    public Selection notAttacking()  {
        return SelectionMembership.notAttacking(this);
    }

    public Selection attacking()  {
        return SelectionMembership.attacking(this);
    }

    public Selection terranInfantryWithoutMedics()  {
        return SelectionMembership.terranInfantryWithoutMedics(this);
    }

    public Selection tankSupport()  {
        return SelectionMembership.tankSupport(this);
    }

    public Selection attackStatePending()  {
        return SelectionMembership.attackStatePending(this);
    }

    public Selection havingTargeted(AUnit targetUnit)  {
        return SelectionMembership.havingTargeted(this, targetUnit);
    }

    public Selection havingTargetedBuildings()  {
        return SelectionMembership.havingTargetedBuildings(this);
    }

    public Selection havingAnyTarget()  {
        return SelectionMembership.havingAnyTarget(this);
    }

    public Selection buildings()  {
        return SelectionMembership.buildings(this);
    }

    public Selection havingPosition()  {
        return SelectionMembership.havingPosition(this);
    }

    public Selection beingVisibleUnitOrNotVisibleFoggedUnit()  {
        return SelectionMembership.beingVisibleUnitOrNotVisibleFoggedUnit(this);
    }

    public int countRunning(int maxStartedRunningAgo)  {
        return SelectionMembership.countRunning(this, maxStartedRunningAgo);
    }

    public int countRetreating()  {
        return SelectionMembership.countRetreating(this);
    }

    public Selection cloakable()  {
        return SelectionMembership.cloakable(this);
    }

    public Selection notDeadMan()  {
        return SelectionMembership.notDeadMan(this);
    }

    public Selection havingWeapon()  {
        return SelectionMembership.havingWeapon(this);
    }

    public Selection havingAntiGroundWeapon()  {
        return SelectionMembership.havingAntiGroundWeapon(this);
    }

    public Selection notHavingAntiAirWeapon()  {
        return SelectionMembership.notHavingAntiAirWeapon(this);
    }

    public Selection havingAntiAirWeapon()  {
        return SelectionMembership.havingAntiAirWeapon(this);
    }

    public Selection havingSmallerRange(AUnit than)  {
        return SelectionMembership.havingSmallerRange(this, than);
    }

    public Selection notPurelyAntiAir()  {
        return SelectionMembership.notPurelyAntiAir(this);
    }

    public int totalHp() {
        return data.stream()
            .map(AUnit::hp)
            .reduce(0, Integer::sum);
    }

    public int totalHits() {
        return data.stream()
            .map(AUnit::totalHitCount)
            .reduce(0, Integer::sum);
    }

    public Selection inSquad(Squad squad)  {
        return SelectionMembership.inSquad(this, squad);
    }

    public Selection combatUnits()  {
        return SelectionMembership.combatUnits(this);
    }

    public Selection combatBuildings(boolean includeCreepColonies)  {
        return SelectionMembership.combatBuildings(this, includeCreepColonies);
    }

    public Selection combatBuildingsAntiLand()  {
        return SelectionMembership.combatBuildingsAntiLand(this);
    }

    public Selection combatBuildingsAntiAir()  {
        return SelectionMembership.combatBuildingsAntiAir(this);
    }

    public Selection combatBuildingsAnti(AUnit unit) {
        return unit.isGroundUnit() ? combatBuildingsAntiLand() : combatBuildingsAntiAir();
    }

    public Selection onlyCompleted()  {
        return SelectionMembership.onlyCompleted(this);
    }

    public Selection onlyCompletedOrAlmost()  {
        return SelectionMembership.onlyCompletedOrAlmost(this);
    }

    public Selection notCompleted()  {
        return SelectionMembership.notCompleted(this);
    }

    public Selection repairable(boolean checkIfWounded)  {
        return SelectionMembership.repairable(this, checkIfWounded);
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

    public boolean onlyGround() {
        return units().onlyGround();
    }

    public boolean onlyOfType(AUnitType type) {
        return count() > 0 && count() == countOfType(type);
    }

    public boolean mostlyOfType(AUnitType type, int percentThreshold) {
        return count() > 0 && (count() * percentThreshold >= countOfType(type) * 100);
    }

    public Selection burrowed()  {
        return SelectionMembership.burrowed(this);
    }

    public Selection burrowing()  {
        return SelectionMembership.burrowing(this);
    }

    public Selection loaded()  {
        return SelectionMembership.loaded(this);
    }

    public Selection notLoaded()  {
        return SelectionMembership.notLoaded(this);
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


    public Selection builders()  {
        return SelectionMembership.builders(this);
    }

    public Selection nonBuilders()  {
        return SelectionMembership.nonBuilders(this);
    }

    public Selection notProtectors()  {
        return SelectionMembership.notProtectors(this);
    }

    public Selection protectors()  {
        return SelectionMembership.protectors(this);
    }

    public Selection producing(AUnitType producingThisUnit)  {
        return SelectionMembership.producing(this, producingThisUnit);
    }

    public Selection healthy()  {
        return SelectionMembership.healthy(this);
    }

    public int averageShields() {
        if (data.isEmpty()) return 0;

        int totalShields = 0;
        for (AUnit unit : data) {
            totalShields += unit.shields();
        }

        return totalShields / data.size();
    }

    public Selection havingSpaceFree(int spaceRequired)  {
        return SelectionMembership.havingSpaceFree(this, spaceRequired);
    }

    public Selection havingCooldownMin(int minCooldown)  {
        return SelectionMembership.havingCooldownMin(this, minCooldown);
    }

    public Selection havingCooldownMax(int maxCooldown)  {
        return SelectionMembership.havingCooldownMax(this, maxCooldown);
    }

    public Selection havingEnergy(int minEnergy)  {
        return SelectionMembership.havingEnergy(this, minEnergy);
    }

    public Selection notHavingHp(int maxHp)  {
        return SelectionMembership.notHavingHp(this, maxHp);
    }

    public Selection notBeingHealed()  {
        return SelectionMembership.notBeingHealed(this);
    }

    public Selection havingAtLeastHp(int minHp)  {
        return SelectionMembership.havingAtLeastHp(this, minHp);
    }

    public Selection nonStasisedOrLockedDown()  {
        return SelectionMembership.nonStasisedOrLockedDown(this);
    }

    public Selection mechanical()  {
        return SelectionMembership.mechanical(this);
    }

    public Selection medics()  {
        return SelectionMembership.medics(this);
    }

    public Selection observers()  {
        return SelectionMembership.observers(this);
    }

    public Selection observatories()  {
        return SelectionMembership.observatories(this);
    }

    public Selection bunkers()  {
        return SelectionMembership.bunkers(this);
    }

    public Selection cannons()  {
        return SelectionMembership.cannons(this);
    }

    public Selection forge()  {
        return SelectionMembership.forge(this);
    }

    public Selection dts()  {
        return SelectionMembership.dts(this);
    }

    public Selection scarabs()  {
        return SelectionMembership.scarabs(this);
    }

    public Selection lurkers()  {
        return SelectionMembership.lurkers(this);
    }

    public Selection reavers()  {
        return SelectionMembership.reavers(this);
    }

    public Selection notConstructing()  {
        return SelectionMembership.notConstructing(this);
    }

    public Selection free()  {
        return SelectionMembership.free(this);
    }

    public Selection withAddon()  {
        return SelectionMembership.withAddon(this);
    }

    public Selection withoutAddon()  {
        return SelectionMembership.withoutAddon(this);
    }

    public Selection notScout()  {
        return SelectionMembership.notScout(this);
    }

    public Selection notCarrying()  {
        return SelectionMembership.notCarrying(this);
    }

    public Selection notImmobilized()  {
        return SelectionMembership.notImmobilized(this);
    }

    public Selection excludeTypes(AUnitType... types)  {
        return SelectionMembership.excludeTypes(this, types);
    }

    public Selection excludeMedics()  {
        return SelectionMembership.excludeMedics(this);
    }

    public Selection excludeOverlords()  {
        return SelectionMembership.excludeOverlords(this);
    }

    public Selection excludeMarines()  {
        return SelectionMembership.excludeMarines(this);
    }

    public Selection excludeTanks()  {
        return SelectionMembership.excludeTanks(this);
    }

    public Selection excludeEggsAndLarvae()  {
        return SelectionMembership.excludeEggsAndLarvae(this);
    }

    public Selection notRunning()  {
        return SelectionMembership.notRunning(this);
    }

    public Selection notRetreating()  {
        return SelectionMembership.notRetreating(this);
    }

    public Selection farFromAntiAirBuildings(double minDistToBuilding) {
        return cloneByRemovingIf((unit -> {
            AUnit nearestAntiAirBuilding = EnemyUnits.discovered().combatBuildingsAntiAir().nearestTo(unit);
            return nearestAntiAirBuilding != null && nearestAntiAirBuilding.distTo(unit) < minDistToBuilding;
        }), "farFromAntiAirBuildings:" + minDistToBuilding);
    }


    public Selection hasPathFrom(HasPosition fromPosition) {
        if (fromPosition == null) return cloneByRemovingIf((unit) -> true, "hasPathFrom:" + fromPosition);

        return cloneByRemovingIf(
            (unit -> !unit.hasPathTo(fromPosition)), "hasPathFrom:" + fromPosition
        );
    }

    public Selection hasPathFromUs() {
        AUnit we = Select.mainOrAnyUnit();
        if (we == null) we = Select.our().groundUnits().first();
        if (we == null) {
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("hasPathFromUs: 'we' unit is null");
            return cloneByRemovingIf((unit) -> true, "hasPathFromUs");
        }

        AUnit finalWe = we;
        return cloneByRemovingIf(
            (unit -> !unit.hasPathTo(finalWe)), "hasPathFromUs"
        );
    }

    public Selection canBeAttackedBy(AUnit attacker, double extraMargin)  {
        return SelectionMembership.canBeAttackedBy(this, attacker, extraMargin);
    }

    public Selection canAttack(AUnit target, boolean checkShootingRange, boolean checkVisibility, double safetyMargin)  {
        return SelectionMembership.canAttack(this, target, checkShootingRange, checkVisibility, safetyMargin);
    }

    public Selection canAttack(AUnit target, double safetyMargin)  {
        return SelectionMembership.canAttack(this, target, safetyMargin);
    }

    public Selection inShootRangeOf(AUnit attacker) {
        return canBeAttackedBy(attacker, 0);
    }

    public Selection inShootRangeOf(double shootingRangeBonus, AUnit attacker) {
        return canBeAttackedBy(attacker, shootingRangeBonus);
    }

    public Selection facingThisUnit(AUnit subject)  {
        return SelectionMembership.facingThisUnit(this, subject);
    }

    public Selection notShowingBackToUs(AUnit subject)  {
        return SelectionMembership.notShowingBackToUs(this, subject);
    }

    public Selection notMoving()  {
        return SelectionMembership.notMoving(this);
    }

    public Selection moving()  {
        return SelectionMembership.moving(this);
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

    public double nearestToDist(HasPosition position) {
        AUnit nearest = nearestTo(position);
        if (nearest == null) return 9999;

        return nearest.distTo(position);
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

    public AUnit nearestToMain() {
        return nearestTo(Select.mainOrAnyUnit());
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

        if (first.isWounded()) {
//            AUnit least = data.get(1);
//            System.out.println("Most wounded: " + first.typeWithUnitId() + " (" + first.hp() + ")");
//            if (least != null) System.out.println("Least wounded: " + least.typeWithUnitId() + " (" + least.hp() + ")");
            return first;
        }

//        System.err.println("Nope, return nearest = " + nearestTo(nearestTo));

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
        return (AUnit) ARandom.getRandomElement(data);
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
        if (position == null) {
            return data;
        }

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

        if (data.size() >= 2) {
            data.sort(new Comparator<AUnit>() {
                @Override
                public int compare(AUnit p1, AUnit p2) {
                    if (unit == null) return 1;

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

    public double estimate() {
        return Estimate.forSelection(this);
    }
}
