package atlantis.combat.micro.terran.infantry.medic;

import atlantis.architecture.Manager;
import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.terran.chokeblockers.ChokeToBlock;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;

public class MedicChokeBlockMoveAway extends Manager {
    private AChoke choke;

    public MedicChokeBlockMoveAway(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return Missions.isGlobalMissionDefend()
            && Count.ourCombatUnits() <= 30
            && (choke = ChokeToBlock.get()) != null
            && needToMoveSpaceForWorkers();
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

    private boolean needToMoveSpaceForWorkers() {
        return unit.friendsNear()
            .workers()
            .notRepairing()
            .notProtectors()
            .notSpecialAction()
            .notConstructing()
            .notScout()
            .inRadius(6, unit)
            .atLeast(1);
    }
}
