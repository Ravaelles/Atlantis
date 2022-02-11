package atlantis.combat.squad;

import atlantis.information.strategy.GamePhase;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class SquadCohesionAssurance {

    /**
     * We want to make sure that at least N percent of units are inside X radius of squad center.
     */
    public static boolean handleTooLowCohesion(AUnit unit) {
        if (!isSquadCohesionTooLow(unit)) {
            return false;
        }

        double maxDist = preferredDistToSquadCenter(unit.squad());
        if (unit.distToMoreThan(unit.squadCenter(), maxDist)) {
            unit.move(unit.squadCenter(), Actions.MOVE_FORMATION, "Cohesion", false);
            unit.addLog("Cohesion");
            return true;
        }

        return false;
    }

    private static boolean isSquadCohesionTooLow(AUnit unit) {
        Squad squad = unit.squad();
        HasPosition squadCenter = unit.squadCenter();
        if (squad == null || squadCenter == null) {
            return false;
        }

        int cohesionPercent = squad.cohesionPercent();
        return cohesionPercent >= minCohesion();
    }

    private static int minCohesion() {
        if (GamePhase.isEarlyGame()) {
            return 90;
        }

        return 75;
    }

    public static double preferredDistToSquadCenter(Squad squad) {
        return Math.max(2.5, 1.3 * Math.sqrt(squad.size()));
    }
}
