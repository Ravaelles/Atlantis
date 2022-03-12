package atlantis.combat.micro.terran;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.ABaseLocation;
import atlantis.map.Bases;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import java.util.List;

public class TerranCommandCenter {

    public static boolean update(AUnit building) {
        if (A.seconds() <= 600 || AGame.notNthGameFrame(39)) {
            return false;
        }

        if (baseMinedOut(building)) {
            return flyToNewMineralPatches(building);
        }

        return false;
    }

    // =========================================================

    private static boolean baseMinedOut(AUnit building) {
        return Select.minerals().inRadius(12, building).isEmpty();
    }

    private static boolean flyToNewMineralPatches(AUnit building) {
        List<? extends AUnit> minerals = Select.minerals().sortDataByDistanceTo(building, true);
        Selection bases = Select.ourBuildingsWithUnfinished().ofType(AUnitType.Terran_Command_Center);
        for (AUnit mineral : minerals) {
            if (bases.clone().inRadius(10, mineral).isEmpty()) {
                ABaseLocation baseLocation = Bases.expansionFreeBaseLocationNearestTo(mineral);
                if (baseLocation != null) {
                    if (!building.isLifted()) {
                        building.lift();
                    } else {
                        if (A.everyNthGameFrame(31)) {
                            APosition landable = building.makeLandable();
                            if (landable != null) {
                                building.land(landable.toTilePosition());
                                building.setTooltip("Rebase", true);
                            }
                        }
//                        if (building.distToLessThan(baseLocation, 3)) {
//                        } else {
//                            building.move(baseLocation.position(), Actions.MOVE_SPECIAL, "Rebase", true);
//                        }
                    }
                    return true;
                }
            }
        }

        return false;
    }

}
