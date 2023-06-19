package atlantis.combat.micro.avoid.margin.special;

import atlantis.units.AUnit;

public class AgainstLurker {
    protected static double vsLurker(AUnit defender, AUnit lurker) {
        if (defender.isWorker()) {
            return 9.5;
        }

        if (defender.isMelee() || defender.isAir() || defender.effUndetected()) {
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