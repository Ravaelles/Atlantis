package atlantis.production.dynamic.terran.units;

import atlantis.game.A;

public class ShouldProduceInfantry {
    static boolean shouldProduceInfantry(int units) {
        return units <= 3 || A.hasMinerals(A.supplyUsed() <= 60 ? 450 : 700);
    }
}
