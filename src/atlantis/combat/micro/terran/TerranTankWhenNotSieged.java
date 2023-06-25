package atlantis.combat.micro.terran;

import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class TerranTankWhenNotSieged extends TerranTank {

    public static final double COMBAT_BUILDING_DIST_SIEGE = 10.8;

    protected static boolean updateWhenNotSieged(AUnit unit) {
        if (shouldSkip(unit)) {
            return false;
        }

        // =========================================================

        if (handleMissionDefend(unit)) {
            return true;
        }

        if (handleNearEnemyCombatBuilding(unit)) {
            return true;
        }

        if (handleDontRunWhenBeingRepared(unit)) {
            return false;
        }

        if (enemiesTooClose(unit)) {
            return false;
        }

        if (shouldSiegeBecauseSpecificEnemiesNear(unit)) {
            return wantsToSiege(unit, "SpecificEnemies");
        }

        if (goodDistanceToContainFocusPoint(unit)) {
            return wantsToSiege(unit, "ContainPoint");
        }

        return false;
    }

    // =========================================================

    private static boolean shouldSkip(AUnit unit) {
        if (!siegeResearched()) {
            return true;
        }

        // Just recently sieged
        if (
            unit.lastActionLessThanAgo(30 * (11 + (unit.idIsOdd() ? 4 : 0)), Actions.UNSIEGE)
                || unit.lastActionLessThanAgo(30 * (15 + (unit.idIsOdd() ? 4 : 0)), Actions.SIEGE)
        ) {
            return true;
        }

        return false;
    }

    private static boolean handleDontRunWhenBeingRepared(AUnit unit) {
        if (!unit.woundPercentMin(50)) {
            return false;
        }

        if (unit.enemiesNear().melee().inRadius(1.1, unit).notEmpty()) {
            return false;
        }

        AUnit repairer = unit.repairer();
        if (repairer != null && repairer.distToLessThan(unit, 1.1) && repairer.isRepairing()) {
            unit.setTooltipTactical("BeFixed");
            return true;
        }

        return false;
    }

    private static boolean enemiesTooClose(AUnit unit) {
        if (unit.enemiesNear().combatUnits().groundUnits().inRadius(5, unit).notEmpty()) {
            return true;
        }

        return false;
    }

    private static boolean handleMissionDefend(AUnit unit) {
        if (unit.isMissionDefendOrSparta()) {
            if (TerranTankWhenSieged.shouldSiegeHereDuringMissionDefend(unit)) {
                return wantsToSiege(unit, "SiegeDefend");
            }
        }

        return false;
    }

    private static boolean goodDistanceToContainFocusPoint(AUnit unit) {
        if (!unit.isMissionContain()) {
            return false;
        }

        APosition focusPoint = Missions.globalMission().focusPoint();
        if (
            focusPoint != null
                && unit.distTo(focusPoint) <= (Enemy.terran() ? 10 : (6 + unit.id() % 3))
                && canSiegeHere(unit, true)
        ) {
            return wantsToSiege(unit, "ContainSiege");
        }

        return false;
    }

    private static boolean shouldSiegeBecauseSpecificEnemiesNear(AUnit unit) {
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
            return enemies.groundUnits().inRadius(5 + unit.id() % 4, unit).isEmpty();
        }

        return enemies.inRadius(15, unit).atLeast(2);
    }

    protected static boolean wantsToSiege(AUnit unit, String log) {
        if (TerranTankWhenSieged.wouldBlockChoke(unit)) {
            return false;
        }

        if (!Enemy.terran()) {
            if (unit.friendsNear().tanksSieged().inRadius(1.2, unit).isNotEmpty()) {
                return false;
            }

            // Prevent tanks from blocking chokes
            if (
                unit.enemiesNear().combatBuildingsAntiLand().inRadius(COMBAT_BUILDING_DIST_SIEGE, unit).empty()
                    && unit.distToNearestChokeLessThan(1.7)
            ) {
                return false;
            }
        }

        unit.siege();
        unit.setTooltipTactical(log);
        unit.addLog(log);
        return true;
    }

    private static boolean forceSiege(AUnit unit, String log) {
        unit.siege();
        unit.setTooltipTactical(log);
        unit.addLog(log);
        return true;
    }

    private static boolean handleNearEnemyCombatBuilding(AUnit unit) {
        AUnit combatBuilding = Select.enemy().combatBuildings(false).inRadius(COMBAT_BUILDING_DIST_SIEGE, unit).nearestTo(unit);
//        unit.setTooltip("Buildz:" + Select.enemy().combatBuildings().count());

        if (combatBuilding != null) {
            if (
                (unit.distToLessThan(combatBuilding, COMBAT_BUILDING_DIST_SIEGE) && canSiegeHere(unit, false))
                    || unit.distToLessThan(combatBuilding, 8.6)
            ) {
                return forceSiege(unit, "SiegeBuilding" + A.dist(unit, combatBuilding));
            }
        }

        return false;
    }

}
