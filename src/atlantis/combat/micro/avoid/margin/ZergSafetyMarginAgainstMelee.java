package atlantis.combat.micro.avoid.margin;

import atlantis.units.AUnit;

public class ZergSafetyMarginAgainstMelee extends SafetyMarginAgainstMelee {
    public ZergSafetyMarginAgainstMelee(AUnit defender) {
        super(defender);
    }

    public double handle(AUnit attacker) {
        if (defender.isHydralisk()) {
            return forHydralisk(attacker);
        }

        if (attacker.isZealot()) return 0.8;

        return -1;
    }

    private double forHydralisk(AUnit attacker) {
        if (!defender.isHydralisk()) {
            return -1;
        }

        if (defender.isHealthy()) {
            return 0;
        }

        return defender.woundPercent() / 33;
    }
}
