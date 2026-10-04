package atlantis.production.dynamic.expansion.decision;

import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.APosition;

/**
 * Is an expansion site under real enemy pressure - as opposed to "our army is
 * small", which is what {@code Army.strength() <= 80} answered and what kept
 * cancelling real expansions every 47 frames (GAME_366E9D6C: a natural nexus
 * warped at 6983 and died at 7007 with the nearest marines ~110 tiles away,
 * 400 minerals burned per cycle, five cycles in a row).
 *
 * <p>Pressure means enemy bodies near the site: any combat unit within
 * {@value #PRESSURE_RADIUS} tiles, or at least {@value #WORKERS_TO_COUNT_AS_PRESSURE}
 * workers (a lone scout is not pressure). No position, no measurable pressure.</p>
 */
public class ExpansionUnderPressure {
    /** Matches the "near the expansion" radius the DT exception in
     * ProtossCancelExpansionCommander already uses. */
    private static final double PRESSURE_RADIUS = 12;

    /** Fewer enemy workers than this is a scout, not pressure. */
    private static final int WORKERS_TO_COUNT_AS_PRESSURE = 3;

    public static boolean check(APosition at) {
        if (at == null) return false;

        if (EnemyUnits.discovered().combatUnits().inRadius(PRESSURE_RADIUS, at).notEmpty()) {
            return true;
        }

        return EnemyUnits.discovered().workers().inRadius(PRESSURE_RADIUS, at).count()
            >= WORKERS_TO_COUNT_AS_PRESSURE;
    }
}
