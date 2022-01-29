package atlantis.combat.micro.zerg;

import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.combat.micro.stack.StackedUnitsManager;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.information.enemy.EnemyInformation;
import atlantis.map.position.APosition;
import atlantis.map.scout.AScoutManager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class ZergOverlordManager {

    public static boolean update(AUnit unit) {

        if (AAvoidUnits.avoidEnemiesIfNeeded(unit)) {
            unit.setTooltipTactical("Uaaa!");
            return true;
        }

        // Dont cluster Overlords too much
        if (StackedUnitsManager.dontStackTooMuch(unit, 1.5, true)) {
            return true;
        }

        // We know enemy building
        if (EnemyInformation.hasDiscoveredAnyBuilding()) {
            return actWhenWeKnowEnemy(unit);
        } 

        // =========================================================
        // We don't know any enemy building

        return actWhenDontKnowEnemyLocation(unit);
    }

    // =========================================================
    /**
     * We know at least one enemy building location.
     * @return
     */
    private static boolean actWhenWeKnowEnemy(AUnit overlord) {
//        Position goTo = AtlantisMap.getMainBaseChokepoint();
//        if (goTo == null) {
//            goTo = Select.mainBase();
//        }
//
//        unit.setTooltip("Retreat");
//        if (goTo != null && goTo.distanceTo(unit) > 3) {
//            unit.setTooltip("--> Retreat");
//            unit.move(goTo, false);
//        }

        if (overlord.id() % 5 == 0) {
            return followArmy(overlord);
        } else {
            return stayInHome(overlord);
        }
    }

    private static boolean stayInHome(AUnit overlord) {
        AUnit main = Select.main();
        if (main != null && overlord.distToMoreThan(main, 8)) {
            return overlord.move(main, Actions.MOVE_FOCUS, "Home", true);
        }

        return false;
    }

    private static boolean followArmy(AUnit overlord) {
        APosition medianUnitPosition = Alpha.get().center();
        if (medianUnitPosition != null) {
            if (overlord.distTo(medianUnitPosition) > 2.5) {
                overlord.move(medianUnitPosition, Actions.MOVE_FOLLOW, "Follow army", true);
                return true;
            }
        }

        return false;
    }

    /**
     * We don't know at any enemy building location.
     */
    private static boolean actWhenDontKnowEnemyLocation(AUnit unit) {
        unit.setTooltipTactical("Find enemy");
        return AScoutManager.tryFindingEnemy(unit);
    }

}
