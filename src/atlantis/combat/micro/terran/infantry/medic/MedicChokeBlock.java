package atlantis.combat.micro.terran.infantry.medic;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.terran.chokeblockers.ChokeBlockers;
import atlantis.terran.chokeblockers.NeedChokeBlockers;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class MedicChokeBlock extends Manager {
    private AChoke choke;

    public MedicChokeBlock(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        AFocusPoint focus = Alpha.get().mission().focusPoint();
        return focus != null
            && "MainChoke".equals(focus.name())
            && NeedChokeBlockers.check();

//        return Missions.isGlobalMissionDefend()
//            && Count.ourCombatUnits() <= 30
//            && (choke = ChokeToBlock.get()) != null;
    }

    @Override
    public Manager handle() {
        APosition goTo = pointToBlock();

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

    private APosition pointToBlock() {
//        int totalMedics = Math.max(1, Count.medics());
//        int points = choke.perpendicularLine().size();
//        int index = unit.getUnitIndexInBwapi() * (points - 1) / totalMedics;
//        return choke.perpendicularLine().get(index % points);
//        int index = unit.getUnitIndexInBwapi();

        return unit.getUnitIndexInBwapi() % 2 == 0
            ? ChokeBlockers.chokeBlockPoint1() : ChokeBlockers.chokeBlockPoint2();
    }
}
