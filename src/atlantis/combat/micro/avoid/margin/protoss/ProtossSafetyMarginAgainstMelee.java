package atlantis.combat.micro.avoid.margin.protoss;

import atlantis.combat.micro.avoid.margin.SafetyMarginAgainstMelee;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class ProtossSafetyMarginAgainstMelee extends SafetyMarginAgainstMelee {
    public ProtossSafetyMarginAgainstMelee(AUnit defender) {
        super(defender);
    }

    public double handle(AUnit attacker) {
//        if (defender.isDragoon()) return forDragoon(attacker);

        if (defender.isZealot()) return forZealot(attacker);

        return -1;
    }

    private double forZealot(AUnit attacker) {
        if (defender.hp() <= 20 && defender.isMissionDefend() && attacker.isZealot()) return 3.5;

        return -1;
    }

//    private double forDragoon(AUnit attacker) {
//        if (defender.isHealthy()) return 0;
//
//        return defender.woundPercent() / 33.0;
//    }
}
