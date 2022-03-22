package atlantis.combat.squad;

import atlantis.game.A;
import atlantis.information.strategy.GamePhase;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.util.We;

public class CohesionAssurance {

    /**
     * We want to make sure that at least N percent of units are inside X radius of squad center.
     */
    public static boolean handleTooLowCohesion(AUnit unit) {
//        System.out.println(
//            A.now() + " " + unit
//                + " // okay="
//                + isSquadCohesionOkay(unit)
//                + " // perc="
//                + unit.squad().cohesionPercent()
//                + " // outs="
//                + unit.outsideSquadRadius()
//        );
        if (unit.isVulture() || unit.isAir()) {
            return false;
        }

        unit.setTooltipTactical(unit.squad().cohesionPercent() + "%/" + A.trueFalse(unit.outsideSquadRadius()) + "/" + A.trueFalse(isSquadCohesionOkay(unit)));
        if (isSquadCohesionOkay(unit)) {
            return false;
        }

        if (!We.terran() && unit.enemiesNear().units().onlyMelee()) {
            return false;
        }

//        double maxDist = preferredDistToSquadCenter(unit.squad());
//        unit.setTooltipTactical(A.digit(unit.distToSquadCenter()) + " / " + A.digit(unit.squadRadius()));
        if (unit.outsideSquadRadius()) {
            String t = "Cohesion";

            if (unit.lastActionMoreThanAgo(20, Actions.MOVE_FORMATION)) {
                unit.move(
                    unit.position()
                        .translateTilesTowards(1.5, unit.squadCenter())
                        .makeFreeOfOurUnits(3, 0.3, unit),
                    Actions.MOVE_FORMATION,
                    t,
                    false
                );
            }
            unit.addLog(t);
            return true;
        }

        return false;
    }

    private static boolean isSquadCohesionOkay(AUnit unit) {
        Squad squad = unit.squad();
        HasPosition squadCenter = unit.squadCenter();
        if (squad == null || squadCenter == null) {
            return true;
        }

        int cohesionPercent = squad.cohesionPercent();
        return cohesionPercent >= minCohesion();
    }

    private static int minCohesion() {
        if (GamePhase.isEarlyGame()) {
            if (A.supplyUsed() <= 25) {
                return 85;
            }

            return 76;
        }

        return 70;
    }

    public static double squadMaxRadius(Squad squad) {
        double base = We.terran() ? 0 : (squad.size() >= 8 ? 3 : 0);
        double tanksBonus = (Count.tanks() >= 2 ? (2 + Count.tanks() / 3.0) : 0);

        return Math.max(2.7, base + Math.sqrt(squad.size()) + tanksBonus);
    }
}
