package atlantis.terran;

import atlantis.combat.missions.Missions;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

import java.util.ArrayList;
import java.util.Iterator;

public class TerranLiftedBuildingManager {

    public static void update() {
        for (Iterator<AUnit> it = Select.ourBuildings().list().iterator(); it.hasNext(); ) {
            AUnit building = it.next();

            if (!building.isLifted()) {
                continue;
            }

            updateLiftedBuilding(building);
        }
    }

    // =========================================================

    /**
     * Buildings will be lifted:
     * - when under attack,
     * - when base runs out of minerals and we fly to a new location
     */
    private static void updateLiftedBuilding(AUnit building) {

    }

}
