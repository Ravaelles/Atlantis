package atlantis.protoss.zealot;

import atlantis.architecture.Manager;
import atlantis.information.generic.Army;
import atlantis.map.position.HasPosition;
import atlantis.production.dynamic.protoss.tech.ResearchLegEnhancements;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;

public class ZealotStayInBackOfGoon extends Manager {
    private AUnit dragoon;
    private AUnit stayBackFrom;

    public ZealotStayInBackOfGoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (Count.dragoons() <= 1) return false;
        if (ResearchLegEnhancements.isResearched()) return false;

        dragoon = unit.friendsNear().dragoons().nearestTo(unit);
        if (dragoon == null) return false;
//        distToGoon = unit.distTo(dragoon);

        return nearEnemyCBStayInBackLine();
    }

    @Override
    public Manager handle() {
        if (unit.moveAwayFrom(stayBackFrom, 2, Actions.MOVE_AVOID)) {
            return usedManager(this);
        }

        return null;
    }

    protected boolean nearEnemyCBStayInBackLine() {
        if (unit.eval() >= 1.5 && Army.strengthWithoutCB() >= 400) return false;

        AUnit stayBackFrom = stayBackFrom();
        if (stayBackFrom == null) return false;

        Selection goons = stayBackFrom.enemiesNear().dragoons().havingCooldownMin(1).inRadius(10, stayBackFrom);
        if (goons.atMost(1) && unit.eval() <= 7) return true;

        double zealotToCB = unit.distTo(stayBackFrom);
        double goonToCB = dragoon.distTo(stayBackFrom);

        return zealotToCB >= goonToCB - 1.5;
    }

    protected AUnit stayBackFrom() {
        return stayBackFrom = unit.enemiesNear().combatBuildingsAntiLand().canAttack(
            unit, 2.8 + unit.woundPercent() / 40.0
        ).nearestTo(unit);
    }
}
