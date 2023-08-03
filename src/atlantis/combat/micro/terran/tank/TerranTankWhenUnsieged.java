package atlantis.combat.micro.terran.tank;

import atlantis.architecture.Manager;
import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.terran.repair.UnitBeingReparedManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class TerranTankWhenUnsieged extends Manager {
    public static final double COMBAT_BUILDING_DIST_SIEGE = 11.9;

    public TerranTankWhenUnsieged(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isTankUnsieged() || !TankDecisions.siegeResearched()) {
            return false;
        }

        // Just recently sieged
//        if (
//            unit.lastActionLessThanAgo(30 * (5 + (unit.idIsOdd() ? 4 : 0)), Actions.UNSIEGE)
//                || unit.lastActionLessThanAgo(30 * (5 + (unit.idIsOdd() ? 4 : 0)), Actions.SIEGE)
//        ) {
//            return true;
//        }

        return true;
    }

    @Override
    public Manager handle() {
        if ((new SiegeHereDuringMissionDefend(unit)).handle() != null) {
            return usedManager(this);
        }

        if (handleNearEnemyBuilding() != null) {
            return usedManager(this);
        }

        if (handleSiegeBecauseSpecificEnemiesNear() != null) {
            return usedManager(this);
        }

        if (EnemiesToCloseToUnsiegedTank(unit)) {
            return usedManager(this);
        }

        UnitBeingReparedManager unitBeingReparedManager = new UnitBeingReparedManager(unit);
        if (unitBeingReparedManager.handleDontRunWhenBeingRepared() != null) {
            return usedManager(unitBeingReparedManager);
        }

        if (areEnemiesTooClose()) {
            return null;
        }

        if (goodDistanceToContainFocusPoint() != null) {
            return usedManager(this);
        }

        return null;
    }

    private boolean EnemiesToCloseToUnsiegedTank(AUnit unit) {
        if (unit.noCooldown() && unit.hp() >= 70) return false;

        Selection enemies = unit.enemiesNear().groundUnits().combatUnits().inRadius(6.5, unit);
        if (enemies.atLeast(2)) {
            unit.runningManager().runFrom(enemies.nearestTo(unit), 2, Actions.MOVE_AVOID, false);
            unit.setTooltip("Careful");
            return true;
        }

        return false;
    }

    // =========================================================

//    private boolean shouldSkip() {
//        if (!siegeResearched()) {
//            return true;
//        }
//
//        // Just recently sieged
//        if (
//            unit.lastActionLessThanAgo(30 * (11 + (unit.idIsOdd() ? 4 : 0)), Actions.UNSIEGE)
//                || unit.lastActionLessThanAgo(30 * (15 + (unit.idIsOdd() ? 4 : 0)), Actions.SIEGE)
//        ) {
//            return true;
//        }
//
//        return false;
//    }

    private boolean areEnemiesTooClose() {
        Selection enemies = unit.enemiesNear().combatUnits().groundUnits();

        if (enemies.inRadius(5, unit).notEmpty()) {
            return true;
        }

        if (enemies.melee().inRadius(12, unit).atLeast(6)) {
            return unit.everyOneInNUnits(4);
        }

        return false;
    }

//    private Manager handleMissionDefend() {
//        if (unit.isMissionDefendOrSparta()) {
//            if (terranTankWhenSieged.shouldSiegeHereDuringMissionDefend()) {
//                return wantsToSiege("SiegeDefend");
//            }
//        }
//
//        return null;
//    }

    private Manager goodDistanceToContainFocusPoint() {
        if (!unit.isMissionContain()) {
            return null;
        }

        APosition focusPoint = Missions.globalMission().focusPoint();
        if (
            focusPoint != null
                && unit.distTo(focusPoint) <= (Enemy.terran() ? 10 : (6 + unit.id() % 3))
                && TankDecisions.canSiegeHere(unit, true)
        ) {
            return wantsToSiege("ContainSiege");
        }

        return null;
    }

    private Manager handleSiegeBecauseSpecificEnemiesNear() {
        Selection enemies = unit.enemiesNear().groundUnits().nonBuildings().nonWorkers().effVisible();

        if (!Enemy.terran()) {
            enemies = enemies.visibleOnMap();
        }

        AUnit enemy = unit.nearestEnemy();

        double maxDist = enemy != null && enemy.isMoving() && unit.isOtherUnitFacingThisUnit(enemy) ? 15.5 : 11.98;
        if (
            enemies
                .ofType(
                    AUnitType.Protoss_Dragoon,
                    AUnitType.Protoss_Reaver,
                    AUnitType.Protoss_High_Templar,
                    AUnitType.Terran_Siege_Tank_Tank_Mode,
                    AUnitType.Terran_Siege_Tank_Siege_Mode,
                    AUnitType.Zerg_Hydralisk,
                    AUnitType.Zerg_Defiler,
                    AUnitType.Zerg_Lurker
                )
                .inRadius(maxDist, unit)
                .isNotEmpty()
        ) {
            if (enemies.inRadius(5 + unit.id() % 4, unit).notEmpty()) {
                wantsToSiege("KeyEnemy");
                return usedManager(this);
            }
        }

        if (enemies.inRadius(15, unit).atLeast(2)) {
            wantsToSiege("Enemies!");
            return usedManager(this);
        }

        return null;
    }

    protected Manager wantsToSiege(String log) {
        if ((new WouldBlockChokeHere(unit)).handle() != null) {
            return null;
        }

        if (unit.lastStartedRunningLessThanAgo(30 * 5)) return null;

        if (!Enemy.terran()) {
            if (unit.friendsNear().tanksSieged().inRadius(1.2, unit).isNotEmpty()) {
                return null;
            }

            // Prevent tanks from blocking chokes
            if (
                unit.enemiesNear().combatBuildingsAntiLand().inRadius(COMBAT_BUILDING_DIST_SIEGE, unit).empty()
                    && unit.distToNearestChokeLessThan(1.7)
            ) {
                return null;
            }
        }

        unit.siege();
        unit.setTooltipTactical(log);
        unit.addLog(log);
        return usedManager(this);
    }

    private Manager forceSiege(String log) {
        unit.siege();
        unit.setTooltipTactical(log);
        unit.addLog(log);
        return usedManager(this, "ForceSiege");
    }

    private Manager handleNearEnemyBuilding() {
        AUnit combatBuilding = Select.enemy().combatBuildings(false).inRadius(COMBAT_BUILDING_DIST_SIEGE, unit).nearestTo(unit);
//        unit.setTooltip("Buildz:" + Select.enemy().combatBuildings().count());

        if (combatBuilding != null) {
            if (
                (
                    unit.distToLessThan(combatBuilding, COMBAT_BUILDING_DIST_SIEGE)
                        && TankDecisions.canSiegeHere(unit, false)
                )
                    || unit.distToLessThan(combatBuilding, 10.6)
            ) {
                return forceSiege("SiegeBuilding" + A.dist(unit, combatBuilding));
            }
        }

        // Siege regular buildings
        if (combatBuilding == null && unit.idIsEven()) {
            AUnit enemyBuilding = Select.enemy().buildings().inRadius(10.9, unit).nearestTo(unit);

            if (enemyBuilding != null) {
                return forceSiege("RegularBuilding");
            }
        }

        return null;
    }

}
