package atlantis.production.constructing.position.protoss;

import atlantis.game.A;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.information.generic.OurArmy;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.production.constructing.position.BuildingTileHelper;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class ProtossHasEnoughSidesFreeFromOtherBuildings {
    public static boolean isOkay(AUnit builder, AUnitType building, APosition position) {
        if (true) return true;

        if (!We.protoss()) return true;
        if (building.isPylon()) return true;
        if (building.isBase()) return true;
        if (building.isCannon()) return true;

        if (preventWhenWeak()) return true;
        if (preventEnemyInBaseSituation()) return true;

        if (
            !BuildingTileHelper.tileLeftFrom(building, position).isWalkable()
//                && !BuildingTileHelper.tileUpFrom(building, position).isWalkable()
        ) {
            return forbidden("Not enough side on left and up");
        }

        if (
            !BuildingTileHelper.tileDownFrom(building, position).isWalkable()
//                && !BuildingTileHelper.tileRightFrom(building, position).isWalkable()
        ) {
            return forbidden("Not enough side on down and right");
        }

        return true;
    }

    private static boolean preventWhenWeak() {
        return A.supplyUsed() <= 60 && OurArmy.strength() <= 120;
    }

    private static boolean preventEnemyInBaseSituation() {
        return A.supplyUsed() <= 50 && EnemyUnitBreachedBase.notNull();
    }

    private static boolean forbidden(String reason) {
        AbstractPositionFinder._STATUS = reason;
        return false;
    }
}
