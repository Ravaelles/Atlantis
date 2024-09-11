package atlantis.combat.retreating.terran;

import atlantis.decisions.Decision;
import atlantis.units.AUnit;
import atlantis.util.cache.Cache;

import static atlantis.combat.retreating.terran.TerranShouldNotRetreat.shouldNotConsiderRetreatingNow;

public class TerranRetreating {
    public static Decision decision(AUnit unit) {
        terranInfantryShouldRetreat = new TerranInfantryShouldRetreat(unit);
        terranShouldNotRetreat = new TerranShouldNotRetreat(unit);

        if (shouldNotConsiderRetreatingNow(unit)) return Decision.FALSE;

        if (terranInfantryShouldRetreat.shouldRetreat() != null) return Decision.TRUE;
        if (terranShouldNotRetreat.shouldNotRetreat()) return Decision.FALSE;

        if ("Retreat".equals(unit.tooltip())) {
            unit.removeTooltip();
        }

        return Decision.FALSE;
    }

    private static Cache<Boolean> cache = new Cache<>();
    private static TerranShouldNotRetreat terranShouldNotRetreat;
    private static TerranInfantryShouldRetreat terranInfantryShouldRetreat;
}
