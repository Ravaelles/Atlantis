package atlantis.combat.micro.avoid.margin;

import atlantis.units.AUnit;

public class BaseSafetyMargin {
    public static double baseSafetyDistance(AUnit defender, AUnit attacker) {
        return (defender.isSquadScout() ? -2.7 : 0)
            + (defender.lastRetreatedAgo() <= 40 ? -3.3 : 0);
    }
}
