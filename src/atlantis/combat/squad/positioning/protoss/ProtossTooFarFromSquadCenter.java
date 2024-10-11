package atlantis.combat.squad.positioning.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.information.generic.OurArmy;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class ProtossTooFarFromSquadCenter extends Manager {

    //    private HasPosition squadCenter;
    private AUnit squadCenter;

    public ProtossTooFarFromSquadCenter(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (!We.protoss()) return false;
        if (!unit.isCombatUnit()) return false;
        if (A.minerals() >= 1500) return false;
        if (unit.isLeader()) return false;
        if (Count.ourCombatUnits() >= 8 && OurArmy.strength() >= 300) return false;
//        if (unit.isMoving() && unit.lastPositioningActionLessThanAgo(40)) return false; // Continue
//        if (unit.isMissionDefendOrSparta()) return false;

        if (unit.enemiesNear().inRadius(15, unit).havingWeapon().notEmpty()) return false;

        AUnit leader = unit.squadLeader();

        if (unit.squad() == null) squadCenter = Select.ourCombatUnits().exclude(unit).nearestTo(unit);
        else squadCenter = leader;

        if (squadCenter == null) return false;
        double distToCenter = unit.distTo(squadCenter.position());

        if (distToCenter >= 12) return true;

        if (A.supplyUsed() >= 100 && squadCenter.isOvercrowded()) return false;

        if (distToCenter <= (unit.squadSize() <= 4 ? 3 : 10) && unit.enemiesNear().empty()) return false;
        if (distToCenter >= preferedDist()) return true;

        if (
            unit.isRunning()
                && (
                unit.lastStartedRunningLessThanAgo(30 * 3)
                    || unit.enemiesThatCanAttackMe(3.2).atLeast(2)
//                        || unit.meleeEnemiesNearCount(3.2) >= 2
            )
        ) return false;
        else if (unit.lastStoppedRunningLessThanAgo(40)) return false;

        if (unit.lastUnderAttackLessThanAgo(35)) return false;

        if (unit.isOvercrowded()) return false;

        if (unit.isLeader() && squad.cohesionPercent() <= 68 && !leader.isOvercrowded()) return true;

        Selection friends = unit.friendsNear().combatUnits();
        if (
            friends.inRadius(2, unit).atLeast(5)
                && friends.inRadius(5, unit).atLeast(12)
        ) return false;

        if (squadCenter.friendsNear().inRadius(1, squadCenter).atLeast(4)) return false;

        return false;

//        if (unit.meleeEnemiesNearCount(4) >= 3) return false;
//
////        if (unit.enemiesThatCanAttackMe(4).notEmpty()) return false;
//
//        return distToCenter > 5 && !isOvercrowded();
    }

    private double preferedDist() {
        double bonus = unit.squadSize() / 4.5;

        return 2.5 + bonus;
    }

    @Override
    public Manager handle() {
        if (!unit.isAttacking() && (!unit.isMoving() || A.everyNthGameFrame(9))) {
            HasPosition moveTo = moveTo();
            if (moveTo != null && unit.distTo(moveTo) >= 2.2 && unit.move(moveTo, Actions.MOVE_FORMATION, "ToCenter")) {
                return usedManager(this);
            }
        }

        return null;
    }

    private boolean isOvercrowded() {
        return unit.friendsNear().groundUnits().inRadius(1, unit).count() >= 3;
    }

    private HasPosition moveTo() {
        if (unit.isLeader()) return unit.friendsNear().combatUnits().nearestTo(unit);

//        return squadCenter.translateTilesTowards(unit, 2);
        return squadCenter;
    }
}
