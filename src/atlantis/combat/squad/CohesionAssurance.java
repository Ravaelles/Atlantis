package atlantis.combat.squad;

import atlantis.information.strategy.GamePhase;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;

public class CohesionAssurance {

    /**
     * We want to make sure that at least N percent of units are inside X radius of squad center.
     */
    public static boolean handleTooLowCohesion(AUnit unit) {
        if (!isSquadCohesionTooLow(unit)) {
            return false;
        }

//        double maxDist = preferredDistToSquadCenter(unit.squad());
//        unit.setTooltipTactical(A.digit(unit.distToSquadCenter()) + " / " + A.digit(unit.squadRadius()));
        if (unit.outsideSquadRadius()) {
            unit.move(
                unit.position().translateTilesTowards(1.5, unit.squadCenter()),
                Actions.MOVE_FORMATION,
                "Cohesion",
                false
            );
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

    public static double squadMaxRadius(Squad squad) {
        double tanksBonus = (Count.tanks() >= 2 ? (2 + Count.tanks() / 3.0) : 0);
        return Math.max(2.5, Math.sqrt(squad.size()) + tanksBonus);
    }
}
