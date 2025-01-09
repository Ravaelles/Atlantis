package atlantis.combat.squad.positioning.formations.moon;

import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class MoonSeparation {
    /**
     * Distance between units in the formation.
     */
    protected static double defineSeparation(Selection ourUnits, AUnit leader) {
        double min = minSeparation(leader);

        return A.inRange(min, 20.0 / (10 + ourUnits.size()), 1.5);
    }

    private static double minSeparation(AUnit leader) {
        if (We.protoss()) {
//            return Count.dragoons() >= 3 ? 0.8 : 0.6;
//
            if (leader.isRanged()) return AUnitType.Protoss_Dragoon.widthInTiles() + 0.1 + Alpha.count() / 140.0;
            return AUnitType.Protoss_Zealot.widthInTiles() + 0.05;
        }

        return 0.04;
    }
}
