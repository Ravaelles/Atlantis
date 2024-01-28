package atlantis.combat.micro.avoid.margin.special;

import atlantis.combat.micro.avoid.margin.SafetyMargin;
import atlantis.units.AUnit;

public class SafetyMarginAgainstMelee_Special extends SafetyMargin {

    public SafetyMarginAgainstMelee_Special(AUnit defender) {
        super(defender);
    }

    public double handle(AUnit attacker) {
        if (attacker.isLurker()) {
            return VsLurker.vsLurker(defender, attacker);
        }

        return -1;
    }

    public double handleSpecially(AUnit attacker) {
        if (!defender.isScout()) return -1;

        return 2 + defender.woundPercent() / 90.0 + attacker.groundWeaponRange();
    }
}
