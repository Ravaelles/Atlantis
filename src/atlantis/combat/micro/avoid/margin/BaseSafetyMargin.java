package atlantis.combat.micro.avoid.margin;

import atlantis.units.AUnit;

public class BaseSafetyMargin {
    public static double baseSafetyDistance(AUnit defender, AUnit attacker) {
        if (defender.isWorker() && defender.hp() <= 40) return 2.9 + defender.woundPercent() / 44.0;

        return (defender.isSquadScout() ? -2.7 : 0)
            + (defender.lastRetreatedAgo() <= 40 ? -3.3 : 0);
    }
}
