package atlantis.combat.micro.avoid;

import atlantis.units.AUnit;

public class SafetyMarginSpecial {
    
    public static double handle(AUnit defender, AUnit attacker) {
        if (defender.isGhost()) {
            return forGhost(defender, attacker);
        }

        return -1;
    }

    // =========================================================

    private static double forGhost(AUnit defender, AUnit attacker) {
        if (attacker.isCombatBuilding()) {
            return 8;
        }

        return 6.5;
    }

}
