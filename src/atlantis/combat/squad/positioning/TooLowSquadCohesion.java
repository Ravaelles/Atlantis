package atlantis.combat.squad.positioning;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.architecture.Manager;
import atlantis.util.We;

public class TooLowSquadCohesion extends Manager {

    public TooLowSquadCohesion(AUnit unit) {
        super(unit);
    }

    @Override
    public Manager handle() {
        if (isCohesionTooLow()) {
            improveCohesion();
            return usedManager(this);
        }

        return null;
    }


    /**
     * We want to make sure that at least N percent of units are inside X radius of squad center.
     */
    public boolean isCohesionTooLow() {
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
            return false;
        }

//        unit.setTooltipTactical(unit.squad().cohesionPercent() + "%/" + A.trueFalse(unit.outsideSquadRadius()) + "/" + A.trueFalse(isSquadCohesionOkay()));
        if (squad.isCohesionPercentOkay()) {
            return false;
        }

        // Too stacked for cohesion
        if (
            unit.friendsInRadius(2).count() >= 3
                || unit.friendsInRadius(4).count() >= 6
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
            return true;
        }

        return false;
    }

    private boolean improveCohesion() {
        String t = "Cohesion";
        APosition goTo = unit.squadLeader()
            .translateTilesTowards(2, unit.position());
//                .makeFreeOfAnyGroundUnits(5, unit.type().dimensionLeft() * 2, unit);

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
        return true;
    }
}

