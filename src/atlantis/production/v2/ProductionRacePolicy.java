package atlantis.production.v2;

import atlantis.units.AUnitType;

/**
 * Race identities named by Production V2 goal generation (ADR 0007, NEXT #8):
 * worker, baseline army unit, base, supply provider, and gas building.
 *
 * <p>A context-local port, not a global race strategy: it answers <i>which
 * producibles</i> the goal layer names, never priorities, arbitration, or
 * strategic targets. Implementations are immutable and stateless.</p>
 *
 * <p>Selection reads {@code Main.ourRace()} - the same source the migrated
 * {@code We.*()} branches read (pinned by {@code RaceWiringTest}), not
 * {@code AtlantisRaceConfig.MY_RACE}. This deviates from ADR 0007's letter
 * deliberately: resolving from MY_RACE would change behavior in exactly the
 * mismatch case the race-wiring fix eliminated.</p>
 */
public interface ProductionRacePolicy {

    AUnitType workerType();

    AUnitType armyUnitType();

    AUnitType baseType();

    AUnitType supplyProviderType();

    AUnitType gasBuildingType();

    /**
     * The single selection site: composition root and tests resolve here,
     * never by re-branching on race downstream. Unknown race fails fast -
     * producing the wrong race's units silently is what the old fallbacks
     * did.
     */
    static ProductionRacePolicy forRace(String race) {
        if ("Protoss".equalsIgnoreCase(race)) return ProtossProductionRacePolicy.INSTANCE;
        if ("Terran".equalsIgnoreCase(race)) return TerranProductionRacePolicy.INSTANCE;
        if ("Zerg".equalsIgnoreCase(race)) return ZergProductionRacePolicy.INSTANCE;
        throw new IllegalArgumentException("Unknown race for production policy: " + race);
    }

    /**
     * Ambient race for no-arg constructors and legacy callers: the same
     * {@code We.*()} truth the migrated branches read (not
     * {@code AtlantisRaceConfig.MY_RACE}, which may be null before launch).
     */
    static ProductionRacePolicy ambient() {
        if (atlantis.util.We.protoss()) return ProtossProductionRacePolicy.INSTANCE;
        if (atlantis.util.We.terran()) return TerranProductionRacePolicy.INSTANCE;
        if (atlantis.util.We.zerg()) return ZergProductionRacePolicy.INSTANCE;
        throw new IllegalArgumentException("No production race policy for ambient race");
    }
}
