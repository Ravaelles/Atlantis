package atlantis.combat.squad.positioning.formations.moon;

import atlantis.combat.squad.squads.alpha.Alpha;
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
            if (leader.isRanged()) {
                double separation = AUnitType.Protoss_Dragoon.widthInTiles()
                    + A.whenEnemyProtoss(0.3, 0.35)
                    + (Alpha.count() / 100.0);

//                System.err.println("separation = " + separation);
                
                return separation;
            }

            return AUnitType.Protoss_Zealot.widthInTiles() - 0.03;
        }

        return 0.04;
    }
}
