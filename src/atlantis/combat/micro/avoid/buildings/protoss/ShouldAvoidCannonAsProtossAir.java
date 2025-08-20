package atlantis.combat.micro.avoid.buildings.protoss;

import atlantis.combat.eval.protoss.ProtossEvaluateAgainstCombatBuildings;
import atlantis.decisions.Decision;
import atlantis.units.AUnit;

public class ShouldAvoidCannonAsProtossAir {
    public static Decision shouldAvoid(AUnit unit, AUnit combatBuilding) {
        if (!combatBuilding.canAttackAirUnits()) return Decision.FALSE;

        return Decision.fromBoolean(unit.distTo(combatBuilding) <= minDist(unit));
    }

    private static double minDist(AUnit unit) {
        return baseDist(unit)
            + unit.woundPercent() / 30.0;
    }

    private static double baseDist(AUnit unit) {
        if (unit.isObserver()) {
            return 7.4;
        }

        return 7.8;
    }
}
