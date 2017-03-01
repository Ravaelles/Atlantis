package atlantis.combat.micro.zerg;

import atlantis.combat.squad.ASquadManager;
import atlantis.enemy.AEnemyUnits;
import atlantis.position.APosition;
import atlantis.scout.AScoutManager;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ZergOverlordManager {

    public static void update(AUnit unit) {

        // We know enemy building
        if (AEnemyUnits.hasDiscoveredAnyEnemyBuilding()) {
            actWhenWeKnowEnemy(unit);
        } 

        // =========================================================
        // We don't know any enemy building
        else {
            actWhenDontKnowEnemyLocation(unit);
        }
    }

    // =========================================================
    /**
     * We know at least one enemy building location.
     */
    private static void actWhenWeKnowEnemy(AUnit overlord) {
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

        APosition medianUnitPosition = ASquadManager.getAlphaSquad().getMedianUnitPosition();
        if (medianUnitPosition != null) {
            if (overlord.distanceTo(medianUnitPosition) > 2.5) {
                overlord.move(medianUnitPosition, UnitActions.MOVE);
            }
        }
    }

    /**
     * We don't know at any enemy building location.
     */
    private static void actWhenDontKnowEnemyLocation(AUnit unit) {
        AScoutManager.tryFindingEnemyBase(unit);
        unit.setTooltip("Find enemy");
        //unit.setTooltip("Find enemy");
    }

}
