package atlantis.combat.micro.avoid.margin.protoss;

import atlantis.combat.micro.avoid.margin.SafetyMarginAgainstMelee;
import atlantis.units.AUnit;

public class ProtossSafetyMarginAgainstMelee extends SafetyMarginAgainstMelee {
    public ProtossSafetyMarginAgainstMelee(AUnit defender) {
        super(defender);
    }

    public double handle(AUnit attacker) {
//        if (defender.isDragoon()) return forDragoon(attacker);

        if (defender.isZealot()) return asZealot(attacker);
        if (defender.isDragoon()) return (new DragoonSafetyMarginAgainstMelee(defender)).marginAgainst(attacker);

        return -1;
    }

    private double asZealot(AUnit attacker) {
        if (defender.hp() <= 20 && defender.isMissionDefend() && attacker.isZealot()) return 3.5;

        return -1;
    }
}
