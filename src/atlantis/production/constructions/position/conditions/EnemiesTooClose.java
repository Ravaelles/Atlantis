package atlantis.production.constructions.position.conditions;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class EnemiesTooClose {
    public static boolean check(AUnit builder, AUnitType building, APosition position) {
        if (EnemyTooCloseToUnstartedConstruction.check(builder, building, position)) {
            return failed("TooCloseEnemy:" + EnemyTooCloseToUnstartedConstruction._STATUS);
        }

        return false;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._STATUS = reason;
        return true;
    }
}
