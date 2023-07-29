package atlantis.terran;

import atlantis.architecture.Commander;
import atlantis.game.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

import java.util.ArrayList;
import java.util.Collection;
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

        Collection<AUnit> allFlyingBuildings = (Collection<AUnit>) flyingBuildings.clone();
        for (AUnit unit : allFlyingBuildings) {
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
