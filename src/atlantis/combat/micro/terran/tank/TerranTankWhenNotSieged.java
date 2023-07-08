package atlantis.combat.micro.terran.tank;

import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.terran.repair.UnitBeingReparedManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.architecture.Manager;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class TerranTankWhenNotSieged extends TerranTank {

    public static final double COMBAT_BUILDING_DIST_SIEGE = 10.8;

//    private TerranTankWhenSieged terranTankWhenSieged;

    public TerranTankWhenNotSieged(AUnit unit) {
        super(unit);
//        this.terranTankWhenSieged = new TerranTankWhenSieged(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isTankUnsieged() || !siegeResearched()) {
            return false;
        }

        // Just recently sieged
        if (
            unit.lastActionLessThanAgo(30 * (5 + (unit.idIsOdd() ? 4 : 0)), Actions.UNSIEGE)
                || unit.lastActionLessThanAgo(30 * (5 + (unit.idIsOdd() ? 4 : 0)), Actions.SIEGE)
        ) {
            return true;
        }

        return false;
    }

    @Override
    public Manager handle() {
//        if (shouldSkip()) {
//            return null;
//        }

        // =========================================================

//        if (handleMissionDefend() != null) {
//            return usingManager(this);
//        }

        if ((new SiegeHereDuringMissionDefend(unit)).handle() != null) {
            return usedManager(this);
        }

        if (handleNearEnemyCombatBuilding() != null) {
            return usedManager(this);
        }

        if ((new UnitBeingReparedManager(unit)).handleDontRunWhenBeingRepared() != null) {
            return lastManager();
        }

        if (areEnemiesTooClose()) {
            return null;
        }

        if (handleSiegeBecauseSpecificEnemiesNear() != null) {
            return lastManager();
        }

        if (goodDistanceToContainFocusPoint() != null) {
            return lastManager();
        }

        return null;
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
        if (unit.enemiesNear().combatUnits().groundUnits().inRadius(5, unit).notEmpty()) {
            return true;
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
                && canSiegeHere(true)
        ) {
            return wantsToSiege("ContainSiege");
        }

        return null;
    }

    private Manager handleSiegeBecauseSpecificEnemiesNear() {
        Selection enemies = unit.enemiesNear().groundUnits().nonBuildings().nonWorkers();
        AUnit enemy = unit.nearestEnemy();

        double maxDist = enemy != null && enemy.isMoving() && unit.isOtherUnitFacingThisUnit(enemy) ? 14.5 : 11.98;
        if (
            enemies
                .clone()
                .ofType(
                    AUnitType.Protoss_Dragoon, AUnitType.Zerg_Hydralisk,
                    AUnitType.Terran_Siege_Tank_Tank_Mode,
                    AUnitType.Terran_Siege_Tank_Siege_Mode
                )
                .inRadius(maxDist, unit)
                .isNotEmpty()
        ) {
            if (enemies.groundUnits().inRadius(5 + unit.id() % 4, unit).isEmpty()) {
                return usedManager(this);
            }
        }

        if (enemies.inRadius(15, unit).atLeast(2)) {
            return usedManager(this);
        }

        return null;
    }

    protected Manager wantsToSiege(String log) {
        if ((new WouldBlockChoke(unit)).handle() != null) {
            return null;
        }

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

    private Manager handleNearEnemyCombatBuilding() {
        AUnit combatBuilding = Select.enemy().combatBuildings(false).inRadius(COMBAT_BUILDING_DIST_SIEGE, unit).nearestTo(unit);
//        unit.setTooltip("Buildz:" + Select.enemy().combatBuildings().count());

        if (combatBuilding != null) {
            if (
                (unit.distToLessThan(combatBuilding, COMBAT_BUILDING_DIST_SIEGE) && canSiegeHere(false))
                    || unit.distToLessThan(combatBuilding, 8.6)
            ) {
                return forceSiege("SiegeBuilding" + A.dist(unit, combatBuilding));
            }
        }

        return null;
    }

}
