package atlantis.information.decisions.protoss.dragoon;

import atlantis.game.A;
import atlantis.information.strategy.OurStrategy;
import atlantis.production.dynamic.protoss.units.ProduceZealot;

public class ZealotFocus {
    public static boolean check() {
        if (!OurStrategy.get().nameContains("speedzealot")) return false;

        return A.gas() <= 250 && ProduceZealot.producedCount <= 15;
    }
}
