package atlantis.combat.micro.avoid.margin.special;

import atlantis.units.AUnit;

public class SafetyMarginAgainstSpecial {

    public static double handle(AUnit defender, AUnit attacker) {
        if (attacker.isLurker()) {
            return AgainstLurker.vsLurker(defender, attacker);
        }

        return -1;
    }

}
