package atlantis.terran;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

import java.util.ArrayList;
import java.util.Collection;

public class FlyingBuildingScoutCommander extends Commander {
    private static final ArrayList<AUnit> flyingBuildings = new ArrayList<>();

    @Override
    protected void handle() {
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

            (new FlyingBuildingScoutManager(unit)).invokeFrom(this);
        }
    }

    // =========================================================

    private boolean needNewFlyingBuilding() {
        if (A.seconds() <= 460) return false;
        if (!flyingBuildings.isEmpty()) return false;

        return Select.ourWithUnfinished(AUnitType.Terran_Siege_Tank_Tank_Mode).atLeast(1);
    }

    private void liftABuildingAndFlyAmongStars() {
        AUnit flying = Select.ourOfType(AUnitType.Terran_Barracks).free().first();
        if (flying != null) {
            flying.lift();
            flyingBuildings.add(flying);
        }
    }

    public static boolean isFlyingBuilding(AUnit unit) {
        return flyingBuildings.contains(unit);
    }

}
