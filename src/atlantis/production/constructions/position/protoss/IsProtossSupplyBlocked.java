package atlantis.production.constructions.position.protoss;

import atlantis.game.A;

public class IsProtossSupplyBlocked {
    public static boolean duringNonEarlyGame() {
        return A.supplyFree() <= 0 && A.supplyTotal() >= 40;
    }
}
