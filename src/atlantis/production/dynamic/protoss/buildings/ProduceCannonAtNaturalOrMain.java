package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.map.base.Bases;

public class ProduceCannonAtNaturalOrMain {
    public static boolean request() {
        return false;
    }

    public static boolean produce() {
        boolean hasNatural = Bases.natural() != null;

        if (hasNatural || A.s >= 60 * 6.5) {
            return ProduceCannonAtNatural.produce();
        }

        return ProduceCannonAtMain.produce();
    }
}
