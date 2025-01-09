package atlantis.combat.micro.dancing.to;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.interrupt.UnitAttackWaitFrames;

public class DanceTo extends Manager {
    private AUnit target;
    private double distToTarget;

    public DanceTo(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (unit.isMarine()) return false;
        if (unit.isDragoon()) return false;

        if (unit.isAttacking()) return false;
        if (unit.cooldown() <= 9) return false;
        if (unit.cooldown() >= UnitAttackWaitFrames.DRAGOON) return false;

        target = unit.target();
        if (target == null || !target.hasPosition() || target.isDead()) return false;
        distToTarget = unit.distTo(target);

        if (unit.isTargetedBy(target)) return false;

        return true;

//        return isATargetThatWeCanDanceTo()
//            && unit.lastAttackFrameLessThanAgo(30)
//            && (
//            shouldDanceToDuringMissionAttack()
//                || (distanceConditionIsOk() && !target.hasBiggerWeaponRangeThan(unit))
//        );
    }


    private boolean shouldDanceToDuringMissionAttack() {
        return unit.isMissionAttack()
            && A.supplyUsed() >= 165
            && unit.friendsNear().inRadius(3, unit).atLeast(4)
            && unit.distToNearestChoke() <= 4;
    }

    private boolean distanceConditionIsOk() {
        double distTo = unit.distTo(target);
        int weaponRange = unit.weaponRangeAgainst(target);

        if (
            distTo <= (weaponRange - 1)
                && unit.isDragoon()
                && unit.friendsNear().dragoons().inRadius(1, unit).atMost(1)
        ) return false;

        return distTo <= weaponRange + 0.2;
    }

    private boolean isATargetThatWeCanDanceTo() {
        if (target.isABuilding() && unit.squadSize() >= 12) return true;

        if (target.isRanged() && unit.isRanged()) return false;

        return !target.isCombatBuilding() || unit.friendsInRadiusCount(1.5) >= 4;
    }

    @Override
    public Manager handle() {
        if (target == null) return null;

        String logString = "DanceTo-" + unit.cooldownRemaining();
        unit.addLog(logString);

        if (danceToTarget(logString)) {
//            System.err.println("@ " + A.now() + " - " + unit.id() + " - __dance_@@@@_to___ " + target);
            return usedManager(this);
        }

        return null;
    }

    private boolean danceToTarget(String logString) {
        APosition goTo = unit.translateTilesTowards(0.25, target);
        if (goTo == null || !goTo.isWalkable()) return false;

        return unit.move(
            goTo, Actions.MOVE_DANCE_TO, logString, false
        );
    }
}
