package atlantis.combat.micro.avoid.margin.special;

import atlantis.combat.micro.avoid.margin.SafetyMargin;
import atlantis.units.AUnit;

public class SafetyMarginAgainstSpecial extends SafetyMargin {

    public SafetyMarginAgainstSpecial(AUnit defender) {
        super(defender);
    }

    public double handle(AUnit attacker) {
        if (attacker.isLurker()) {
            return VsLurker.vsLurker(defender, attacker);
        }

        return -1;
    }
}
