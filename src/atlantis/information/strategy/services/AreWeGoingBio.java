package atlantis.information.strategy.services;

import atlantis.game.A;
import atlantis.information.strategy.Strategy;

public class AreWeGoingBio {
    public static boolean check() {
        return Strategy.get().goingBio();
    }

    public static boolean doNotFocusOnTanksForNow() {
        return A.supplyUsed() <= 100 && !A.canAfford(500, 200);
    }
}
