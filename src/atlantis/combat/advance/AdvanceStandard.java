package atlantis.combat.advance;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.retreating.ShouldRetreat;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.util.We;

public class AdvanceStandard extends MissionManager {
    public AdvanceStandard(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isCombatUnit()
            && A.seconds() % 6 <= 3
            && unit.lastActionMoreThanAgo(10)
//            && !unit.isLeader()
//            && unit.combatEvalRelative() > 1.5
            && !ShouldRetreat.shouldRetreat(unit)
            && unit.enemiesNear().canBeAttackedBy(unit, 3).empty()
            && (
            (unit.distToLeader() <= 7 + (A.supplyUsed() / 25.0))
//                A.seconds() % 6 <= 2 || (unit.distToLeader() <= 7 + (A.supplyUsed() / 25.0))
        );
    }

    protected Manager handle() {
        if (asTerran()) return usedManager(this);

//        if (ToLastSquadTarget.goTo(unit)) return usedManager(this, "ToSquadTarget");

        if (A.seconds() % 6 <= 3 && (!unit.isMoving() && !unit.isAttacking())) {
            return usedManager(this, "AdvanceContinue");
        }

        if (focusPoint != null) {
            unit.move(focusPoint, Actions.MOVE_FOCUS, "Advance");
            return usedManager(this);
        }

        return null;
    }

    private boolean asTerran() {
        if (!We.terran()) return false;

        if (unit.isGroundUnit() && !unit.isTank() && Count.tanks() >= 2) {
            if (tooFarFromTank()) return true;
        }

        return false;
    }

    private boolean tooFarFromTank() {
        AUnit target = unit.nearestOurTank();
        if (target != null && unit.distTo(target) >= 5) {
            unit.move(target, Actions.MOVE_FORMATION, "RollWithTank");
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "AdvanceStandard(" + A.digit(unit.distToLeader()) + ")";
    }
}
