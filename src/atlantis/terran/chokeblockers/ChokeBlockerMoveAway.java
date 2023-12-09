package atlantis.terran.chokeblockers;

import atlantis.architecture.Manager;
import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class ChokeBlockerMoveAway extends Manager {
    private final APosition chokeBlockPoint;
    private AChoke choke;

    public ChokeBlockerMoveAway(AUnit unit) {
        super(unit);
        this.chokeBlockPoint = unit.specialPosition();
    }

    @Override
    public boolean applies() {
        return ChokeBlockersCommander.checkIfApplies()
            && unit.enemiesNear().inRadius(11, unit).empty()
            && (choke = ChokeToBlock.get()) != null
            && needToMoveSpaceForWorkers();
    }

    private boolean needToMoveSpaceForWorkers() {
        return unit.friendsNear()
            .workers()
            .exclude(unit)
            .notRepairing()
            .notProtectors()
            .notSpecialAction()
//            .notConstructing()
//            .notScout()
            .inRadius(7, choke.center())
            .atLeast(1);
    }

    @Override
    public Manager handle() {
        if (unit.distTo(this.chokeBlockPoint) >= 5) return null;

        HasPosition goTo = Select.mainOrAnyBuilding();

        if (goTo != null && goTo.distTo(unit) > 0.03) {
            if (A.now() % 5 == 0) {
                unit.move(goTo, Actions.SPECIAL, "ChokeBlock");
            }
        }
        else {
            unit.holdPosition("ChokeBlock");
        }
        unit.setAction(Actions.SPECIAL);

        return usedManager(this);
    }
}
