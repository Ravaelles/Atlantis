package atlantis.terran.chokeblockers;

import atlantis.architecture.Manager;
import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.production.orders.production.queue.CountInQueue;
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
        return Missions.isGlobalMissionDefend()
            && Count.ourCombatUnits() <= 20
            && (choke = ChokeToBlock.get()) != null;
    }

    @Override
    public Manager handle() {
        APosition goTo = choke.center();

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
