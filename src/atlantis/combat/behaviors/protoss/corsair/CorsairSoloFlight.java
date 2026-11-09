package atlantis.combat.behaviors.protoss.corsair;

import atlantis.combat.BattleCommander;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

/**
 * Solo-Corsair driving (owner #201, root cause traced 2026-11-01):
 * solo Corsairs never join Alpha, so {@code BattleCommander.act} never
 * ran for them - no orders, no {@code observe()}, invisible in every
 * twin (3 warps in 115405 with zero telemetry/death/attack rows).
 * This drives the hunt, and only the hunt: observe for the twins, then
 * {@code CorsairHunt}. Never the regroup march - solo means solo, not
 * trailing the army. A Corsair that merges (threatening air) rejoins
 * Alpha and the shared pipeline instead.
 */
public final class CorsairSoloFlight {

    private CorsairSoloFlight() {
    }

    public static void drive() {
        if (Select.ourOfType(AUnitType.Protoss_Corsair).isEmpty()) return;

        for (AUnit corsair : Select.ourOfType(AUnitType.Protoss_Corsair).list()) {
            if (corsair == null || !corsair.isAlive()) continue;
            if (corsair.squad() != null) continue;

            BattleCommander.observeUnit(corsair);
            CorsairBrain.act(corsair);
        }
    }
}
