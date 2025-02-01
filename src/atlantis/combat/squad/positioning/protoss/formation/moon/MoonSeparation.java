package atlantis.combat.squad.positioning.protoss.formation.moon;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
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
            if (leader.isRanged()) return AUnitType.Protoss_Dragoon.widthInTiles() + 0.1;
            return AUnitType.Protoss_Zealot.widthInTiles() + 0.1;
        }

        return 0.5;
    }
}
