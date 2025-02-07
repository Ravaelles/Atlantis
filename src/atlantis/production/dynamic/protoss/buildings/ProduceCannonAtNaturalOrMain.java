package atlantis.production.dynamic.protoss.buildings;

import atlantis.map.base.Bases;

public class ProduceCannonAtNaturalOrMain {
    public static boolean request() {
        return false;
    }

    public static boolean produce() {
        boolean hasNatural = Bases.natural() != null;

        if (hasNatural) {
            return ProduceCannonAtNatural.produce();
        }

        return ProduceCannonAtMain.produce();
    }
}
