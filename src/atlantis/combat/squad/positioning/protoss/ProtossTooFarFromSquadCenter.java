package atlantis.combat.squad.positioning.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class ProtossTooFarFromSquadCenter extends Manager {

    //    private HasPosition squadCenter;
    private AUnit squadCenter;

    public ProtossTooFarFromSquadCenter(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (true) return false;

        if (!We.protoss()) return false;
        if (!unit.isCombatUnit()) return false;
//        if (unit.isMoving() && unit.lastPositioningActionLessThanAgo(40)) return false; // Continue
        if (
            unit.isRunning()
                && (unit.lastStartedRunningLessThanAgo(30 * 3) || unit.meleeEnemiesNearCount(3) >= 2)
        ) return false;

        if (unit.squad() == null) return false;
        squadCenter = unit.squadCenterUnit();
        if (squadCenter == null) return false;
        if (unit.meleeEnemiesNearCount(3) >= 2) return false;

        if (squad.cohesionPercent() <= 70) return true;

        double distToCenter = unit.distTo(squadCenter);
        if (distToCenter >= 6) return true;
//        if (unit.enemiesThatCanAttackMe(4).notEmpty()) return false;

        return distToCenter > 5 && !isOvercrowded();
    }

    @Override
    public Manager handle() {
        if (!unit.isMoving() && !unit.isAttacking()) {
            HasPosition moveTo = moveTo();
            if (unit.distTo(moveTo) >= 2.2 && unit.move(moveTo, Actions.MOVE_FORMATION, "ToCenter")) {
                return usedManager(this);
            }
        }

        return null;
    }

    private boolean isOvercrowded() {
        return unit.friendsNear().groundUnits().inRadius(1, unit).count() >= 3;
    }

    private HasPosition moveTo() {
        return squadCenter.translateTilesTowards(unit, 2);
    }
}
