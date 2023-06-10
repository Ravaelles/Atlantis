package atlantis.combat.micro.avoid.margin;

import atlantis.game.A;
import atlantis.units.AUnit;

public class SafetyMarginSpecial {

    public static double handle(AUnit defender, AUnit attacker) {
        if (attacker.isLurker()) {
            return vsLurker(defender, attacker);
        }

        return -1;
    }

    private static double vsLurker(AUnit defender, AUnit lurker) {
        if (defender.isWorker()) {
            return 9.5;
        }

        if (defender.isMelee() || defender.isAir() || defender.effCloaked()) {
            return 0;
        }

        if (lurker.isBurrowed() || !lurker.isInterruptible()) {
            return 6.5 + (defender.isMoving() ? 1.0 : 0);
        }
        else {
            double cooldownBonus = defender.hasCooldown() ? 1.5 : 0;
            return cooldownBonus + (defender.isHealthy() ? 0 : 3.0);
        }
    }

}
