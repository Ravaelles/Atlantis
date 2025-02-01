package atlantis.combat.micro.avoid.buildings.protoss;

import atlantis.combat.micro.avoid.buildings.ShouldAvoidBunkerAsProtoss;
import atlantis.decisions.Decision;
import atlantis.units.AUnit;

public class ShouldAvoidCombatBuildingAsProtoss {
    public static Decision decision(AUnit unit, AUnit combatBuilding) {
        Decision decision;

        if (combatBuilding.isBunker()) {
            if ((decision = ShouldAvoidBunkerAsProtoss.shouldAvoid(unit, combatBuilding)).notIndifferent()) {
                return decision;
            }
        }

        if (combatBuilding.isCannon()) {
            if ((decision = ShouldAvoidCannonAsProtoss.shouldAvoid(unit, combatBuilding)).notIndifferent()) {
                return decision;
            }
        }

        if (combatBuilding.isSunken()) {
            if ((decision = ShouldAvoidSunkenAsProtoss.shouldAvoid(unit, combatBuilding)).notIndifferent()) {
                return decision;
            }
        }

        return Decision.INDIFFERENT;
    }
}
