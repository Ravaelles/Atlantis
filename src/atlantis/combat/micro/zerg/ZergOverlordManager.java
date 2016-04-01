package atlantis.combat.micro.zerg;

import atlantis.combat.group.AtlantisGroupManager;
import atlantis.debug.tooltip.TooltipManager;
import atlantis.information.AtlantisEnemyInformationManager;
import atlantis.information.AtlantisMap;
import atlantis.scout.AtlantisScoutManager;
import atlantis.units.AUnit;
import atlantis.util.PositionUtil;
import atlantis.units.Select;
import bwapi.Position;


/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ZergOverlordManager {

    public static void update(AUnit unit) {

        // We know enemy building
        if (AtlantisEnemyInformationManager.hasDiscoveredEnemyBuilding()) {
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

        Position medianUnitPosition = AtlantisGroupManager.getAlphaGroup().getMedianUnitPosition();
        if (medianUnitPosition != null) {
            if (overlord.distanceTo(medianUnitPosition) > 2.5) {
                overlord.move(medianUnitPosition);
            }
        }
    }

    /**
     * We don't know at any enemy building location.
     */
    private static void actWhenDontKnowEnemyLocation(AUnit unit) {
        AtlantisScoutManager.tryToFindEnemy(unit);
        TooltipManager.setTooltip(unit, "Find enemy");
        //unit.setTooltip("Find enemy");
    }

}
