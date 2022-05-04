package atlantis.combat.squad;

import atlantis.game.A;
import atlantis.information.strategy.GamePhase;
import atlantis.map.position.APosition;
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

//        unit.setTooltipTactical(unit.squad().cohesionPercent() + "%/" + A.trueFalse(unit.outsideSquadRadius()) + "/" + A.trueFalse(isSquadCohesionOkay(unit)));
        if (isSquadCohesionOkay(unit)) {
            return false;
        }

        // Too stacked for cohesion
        if (
            unit.friendsInRadius(4).count() >= 6
            || unit.friendsInRadius(7).count() >= 20
        ) {
            return false;
        }

        if (!We.terran() && unit.enemiesNear().units().onlyMelee()) {
            return false;
        }

//        double maxDist = preferredDistToSquadCenter(unit.squad());
//        unit.setTooltipTactical(A.digit(unit.distToSquadCenter()) + " / " + A.digit(unit.squadRadius()));
        if (unit.outsideSquadRadius() && unit.meleeEnemiesNearCount(4) == 0) {
            String t = "Cohesion";

            if (unit.lastActionMoreThanAgo(40, Actions.MOVE_FORMATION)) {
                APosition goTo = unit.squadCenter()
                    .translateTilesTowards(2, unit.position())
                    .makeFreeOfOurUnits(5, 0.3, unit);

                if (goTo == null) {
                    return false;
                }

                unit.addLog(t);
                unit.move(
                    goTo,
                    Actions.MOVE_FORMATION,
                    t,
                    false
                );
            }
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
        double base;

        if (We.terran()) {
            base = 0;
        }
        else if (We.protoss()) {
            base = (squad.size() >= 8 ? 3 : 0);
        }
        else {
            base = Math.min(8, 2 + (squad.size() / 3));
        }

        double tanksBonus = Math.min(6, (Count.tanks() >= 2 ? (1 + Count.tanks() / 1.6) : 0));

        return Math.max(2.7, base + Math.sqrt(squad.size()) + tanksBonus);
    }
}
