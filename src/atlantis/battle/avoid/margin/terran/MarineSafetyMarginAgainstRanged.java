package atlantis.battle.avoid.margin.terran;

import atlantis.battle.avoid.margin.SafetyMarginAgainstRanged;
import atlantis.battle.avoid.margin.protoss.KeepDragoonBattleLineVsRanged;
import atlantis.units.AUnit;
import atlantis.units.range.OurDragoonRange;

public class MarineSafetyMarginAgainstRanged extends SafetyMarginAgainstRanged {
    public MarineSafetyMarginAgainstRanged(AUnit defender) {
        super(defender);
    }

    @Override
    public double marginAgainst(AUnit attacker) {
        if (!attacker.isMutalisk()) return -1;

        return -0.1;
    }
}
