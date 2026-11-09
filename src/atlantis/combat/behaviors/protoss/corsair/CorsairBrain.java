package atlantis.combat.behaviors.protoss.corsair;

import atlantis.units.AUnit;

/**
 * The restored Corsair brain (owner 2026-11-09, port of the old
 * {@code Corsair} manager chain): one ordered chain decides what a
 * Corsair does instead of idling on #NO_COMMAND. Muta packs first,
 * then the overlord duel (dance while cooling, kill on ready), then
 * the known-board overlord sweep, then scouting, then anti-idle
 * relocation. The old AsAir avoid legs have no new-code counterpart
 * and stay out - the shared avoidance owns that half.
 */
public final class CorsairBrain {

    private CorsairBrain() {
    }

    public static boolean applies(AUnit unit) {
        return unit != null && unit.isCorsair() && unit.isAlive();
    }

    /**
     * @return true when an order went out.
     */
    public static boolean act(AUnit unit) {
        if (!applies(unit)) return false;

        if (CorsairRelocateWhenIdle.act(unit)) return true;
        if (CorsairHuntMutas.act(unit)) return true;
        if (CorsairDanceToOverlord.act(unit)) return true;
        if (CorsairHunt.act(unit)) return true;
        if (CorsairHuntKnownOverlords.act(unit)) return true;
        if (CorsairExploreEnemyMain.act(unit)) return true;

        return false;
    }
}
