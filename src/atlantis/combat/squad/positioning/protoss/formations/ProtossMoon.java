package atlantis.combat.squad.positioning.protoss.formations;

import atlantis.architecture.Manager;
import atlantis.combat.squad.positioning.formations.moon.ProtossMoonFormationApplies;
import atlantis.combat.squad.positioning.formations.moon.MoonUnitPositions;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ProtossMoon extends Manager {
    private APosition goTo;

    public ProtossMoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isAir()) return false;
        if (unit.isReaver()) return false;

        AUnit leader = unit.leader();
        if (leader == null) return false;

        if (!(new ProtossMoonFormationApplies()).applies(unit, leader)) {
//            PauseAndCenter.on(unit, true);
            return false;
        }

        return true;
    }

    @Override
    public Manager handle() {
//        if (unit.lastPositionChangedAgo() >= 50) {
//            unit.paintCircleFilled(16, Color.Green);
//            System.out.println("@ " + unit + " - why waiting? " + unit.squad().lastShotLessThanAgo(3));
//        }

        AUnit leader = unit.leader();
        if (leader == null) return null;

        goTo = MoonUnitPositions.positionToGoForUnit(unit, leader);
        if (goTo == null) return null;

//        unit.paintLineDouble(goTo, Color.Green);
//        unit.paintCircleFilled(6, Color.Green);
//        goTo.paintCircleFilled(6, Color.Green);
//        System.out.println(unit + " MOON to " + goTo + " / " + unit.distToDigit(goTo));

        unit.move(goTo, Actions.MOVE_FORMATION);

        return usedManager(this);
    }
}
