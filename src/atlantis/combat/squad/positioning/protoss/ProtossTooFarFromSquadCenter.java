package atlantis.combat.squad.positioning.protoss;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.generic.Army;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
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

//        if (unit.enemiesNear().empty()) return false;

//        if (Count.ourCombatUnits() >= 7) {
//            boolean anyEnemiesClose = unit.enemiesNear().combatUnits().countInRadius(8.2, unit) >= 1;
//            if (anyEnemiesClose) return false;
//        }

        if (unit.isLeader()) return false;
        if (ignoreWhenBigSupply()) return false;
        if (unit.lastUnderAttackLessThanAgo(50)) return false;
        if (unit.hp() <= 63 && unit.meleeEnemiesNearCount(2.3) > Enemy.zergElse(2, 1)) return false;

//        if (unit.isMoving() && unit.lastPositioningActionLessThanAgo(40)) return false; // Continue
//        if (unit.isMissionDefendOrSparta()) return false;

        AUnit leader = unit.squadLeader();
        if (leader != null && (leader.isRunning() || leader.isRetreating())) return false;

        if (unit.squad() == null) squadCenter = squad.units().exclude(unit).nearestTo(unit);
        else squadCenter = leader;
        if (squadCenter == null) return false;
        double distToCenter = unit.distTo(squadCenter.position());

//        if (unit.lastActionMoreThanAgo(15, Actions.MOVE_FORMATION)) return false;

//        if (unit.lastUnderAttackLessThanAgo(50)) return false;
        if (distToCenter >= 7 && unit.friendsNear().combatUnits().countInRadius(4, unit) >= 4) return true;

        if (enemiesTooClose()) return false;

//        if (unit.enemiesNear().inRadius(7, unit).notEmpty()) return false;

        int maxDistToLeader = unit.squadSize() <= 15 ? 3 : 6;
        if (distToCenter >= maxDistToLeader) return true;

//        System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - ");

        if (nearestFriendTooFar()) return true;

        if (A.supplyUsed() >= 100 && squadCenter.isOvercrowded()) return false;

        if (unit.enemiesNear().inRadius(AUnit.NEAR_DIST, unit).havingWeapon().notEmpty()) return false;

        if (distToCenter <= (unit.squadSize() <= 4 ? 3 : 10)) return false;
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

    private boolean ignoreWhenBigSupply() {
        return A.supplyUsed() >= 110
            && unit.woundPercent() <= 6
            && Army.strength() >= 450
            && (unit.isMoving() || unit.isAttacking() || unit.hasCooldown());
    }

    private boolean enemiesTooClose() {
        return unit.enemiesNear().inRadius(8, unit).canAttack(unit, 2.2).atLeast(2);
    }

    private boolean nearestFriendTooFar() {
        if (unit.squadSize() <= 2) return false;

        return unit.friendsNear().countInRadius(1.5, unit) <= 1;

//        AUnit friend = unit.friendsNear().countInRadius(1.5, unit)
//
//        return friend != null && friend.distTo(unit) >= 1;
    }

    private double preferedDist() {
        double bonus = unit.squadSize() / 4.5;

        return 2.5 + bonus;
    }

    @Override
    public Manager handle() {
//        unit.paintCircle(8, Color.Red);
//        unit.paintCircle(7, Color.Red);

//        if (!unit.isAttacking() && (!unit.isMoving() || A.everyNthGameFrame(9))) {
//        if (!unit.isAttacking() && (!unit.isMoving() || A.everyNthGameFrame(9))) {
        HasPosition moveTo = moveTo();

        if (unit == null) {
            System.err.println("ProtossTooFarFromSquadCenter.handle() - unit is null!");
            return null;
        }

        if (moveTo == null || !moveTo.hasPosition()) return null;

        if (unit.distTo(moveTo) >= 2.2 && unit.move(moveTo, Actions.MOVE_FORMATION, "ToCenter")) {
//                unit.paintCircleFilled(8, Color.Red);
            return usedManager(this);
        }
//        }

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
