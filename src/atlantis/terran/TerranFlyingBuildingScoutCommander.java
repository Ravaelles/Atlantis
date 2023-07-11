package atlantis.terran;

import atlantis.architecture.Commander;
import atlantis.architecture.Manager;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.debug.painter.APainter;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.Color;

import java.util.ArrayList;
import java.util.Iterator;

public class TerranFlyingBuildingScoutCommander extends Commander {

    private static final ArrayList<AUnit> flyingBuildings = new ArrayList<>();

    public TerranFlyingBuildingScoutCommander() {}

    @Override
    public void handle() {
        if (AGame.isUms()) {
            return;
        }

        if (needNewFlyingBuilding()) {
            liftABuildingAndFlyAmongStars();
        }

        for (Iterator<AUnit> it = flyingBuildings.iterator(); it.hasNext(); ) {
            AUnit unit = it.next();

            if (!unit.isAlive()) {
                flyingBuildings.remove(unit);
                continue;
            }

            (new TerranFlyingBuildingScoutManager(unit)).handle();
        }
    }

    // =========================================================

    private boolean needNewFlyingBuilding() {
        if (!flyingBuildings.isEmpty()) {
            return false;
        }

        return Select.ourWithUnfinished(AUnitType.Terran_Machine_Shop).atLeast(1)
                || Select.ourTanks().atLeast(3);
    }

    private void liftABuildingAndFlyAmongStars() {
        AUnit flying = Select.ourOfType(AUnitType.Terran_Barracks).free().first();
        if (flying != null) {
            flying.lift();
            flyingBuildings.add(flying);
        }
    }

    public static boolean isFlyingBuilding(AUnit unit) {
        return unit.type().isBuilding() && flyingBuildings.contains(unit);
    }

}
