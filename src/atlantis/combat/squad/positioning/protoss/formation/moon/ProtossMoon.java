package atlantis.combat.squad.positioning.protoss.formation.moon;

import atlantis.architecture.Manager;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.PauseAndCenter;
import bwapi.Color;

public class ProtossMoon extends Manager {
    private APosition goTo;

    public ProtossMoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        AUnit leader = unit.squadLeader();
        if (leader == null) return false;

        if (!(new MoonFormationApplies()).applies(unit, leader)) {
//            PauseAndCenter.on(unit, true);
            return false;
        }

        goTo = MoonUnitPositions.positionToGoForUnit(unit, leader);
        if (goTo == null) return false;

        return true;
    }

    @Override
    public Manager handle() {
        unit.paintLineDouble(goTo, Color.Green);
        unit.paintCircleFilled(6, Color.Green);
        goTo.paintCircleFilled(6, Color.Green);
//        System.out.println(unit + " MOON to " + goTo + " / " + unit.distToDigit(goTo));

        unit.move(goTo, Actions.MOVE_FORMATION);

        return usedManager(this);
    }
}
