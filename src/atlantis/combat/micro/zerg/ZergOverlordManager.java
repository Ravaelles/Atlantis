package atlantis.combat.micro.zerg;

import atlantis.combat.micro.stack.StackedUnitsManager;
import atlantis.combat.squad.Squad;
import atlantis.enemy.AEnemyUnits;
import atlantis.position.APosition;
import atlantis.scout.AScoutManager;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;

public class ZergOverlordManager {

    public static boolean update(AUnit unit) {

        // Dont cluster Overlords too much
        if (StackedUnitsManager.dontStackTooMuch(unit, 2.5, true)) {
            return true;
        }

        // We know enemy building
        if (AEnemyUnits.hasDiscoveredAnyEnemyBuilding()) {
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

        APosition medianUnitPosition = Squad.alpha().center();
        if (medianUnitPosition != null) {
            if (overlord.distTo(medianUnitPosition) > 2.5) {
                overlord.move(medianUnitPosition, UnitActions.MOVE, "Follow army");
                return true;
            }
        }

        return false;
    }

    /**
     * We don't know at any enemy building location.
     */
    private static boolean actWhenDontKnowEnemyLocation(AUnit unit) {
        unit.setTooltip("Find enemy");
        return AScoutManager.tryFindingEnemyBuilding(unit);
    }

}
