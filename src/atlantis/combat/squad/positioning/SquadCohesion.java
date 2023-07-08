package atlantis.combat.squad.positioning;

import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.information.strategy.GamePhase;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.managers.Manager;
import atlantis.units.select.Count;
import atlantis.util.We;

public class SquadCohesion extends Manager {

    public SquadCohesion(AUnit unit) {
        super(unit);
    }

    /**
     * We want to make sure that at least N percent of units are inside X radius of squad center.
     */
    public Manager handleTooLowCohesion() {
//        System.out.println(
//            A.now() + " " + unit
//                + " // okay="
//                + isSquadCohesionOkay()
//                + " // perc="
//                + unit.squad().cohesionPercent()
//                + " // outs="
//                + unit.outsideSquadRadius()
//        );
        if (unit.isVulture() || unit.isAir()) {
            return null;
        }

//        unit.setTooltipTactical(unit.squad().cohesionPercent() + "%/" + A.trueFalse(unit.outsideSquadRadius()) + "/" + A.trueFalse(isSquadCohesionOkay()));
        if (isSquadCohesionOkay()) {
            return null;
        }

        // Too stacked for cohesion
        if (
            unit.friendsInRadius(2).count() >= 3
            || unit.friendsInRadius(4).count() >= 6
            || unit.friendsInRadius(7).count() >= 20
        ) {
            return null;
        }

        if (!We.terran() && unit.enemiesNear().units().onlyMelee()) {
            return null;
        }

//        double maxDist = preferredDistToSquadCenter(unit.squad());
//        unit.setTooltipTactical(A.digit(unit.distToSquadCenter()) + " / " + A.digit(unit.squadRadius()));
        if (unit.outsideSquadRadius() && unit.meleeEnemiesNearCount(4) == 0) {
            String t = "Cohesion";
            APosition goTo = unit.squadLeader()
                .translateTilesTowards(2, unit.position());
//                .makeFreeOfAnyGroundUnits(5, unit.type().dimensionLeft() * 2, unit);

            if (goTo == null) {
                return null;
            }

            unit.addLog(t);

            if (unit.move(
                goTo,
                Actions.MOVE_FORMATION,
                t,
                false
            )) {
                return usingManager(this);
            }
        }

        return null;
    }

    private boolean isSquadCohesionOkay() {
        Squad squad = unit.squad();
        HasPosition squadCenter = unit.squadCenter();
        if (squad == null || squadCenter == null) {
            return true;
        }

        int cohesionPercent = squad.cohesionPercent();
        return cohesionPercent >= minCohesion();
    }

    private int minCohesion() {
        if (GamePhase.isEarlyGame()) {
            if (A.supplyUsed() <= 25) {
                return 85;
            }

            return 76;
        }

        return 70;
    }

    public double squadMaxRadius() {
        double base = 0;

//        if (We.terran()) {
//            base = 0;
//        }
        if (We.protoss()) {
            base = (squad.size() >= 8 ? 3 : 0);
        }
        else if (We.zerg()) {
            base = Math.min(8, 2 + (squad.size() / 3));
        }

        double tanksBonus = Math.min(6, (Count.tanks() >= 2 ? (1 + Count.tanks() / 1.6) : 0));

        return Math.max(2.7, base + Math.sqrt(squad.size()) + tanksBonus);
    }
}
