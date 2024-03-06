package atlantis.combat.micro.dancing;

import atlantis.architecture.Manager;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.generic.OurArmyStrength;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.Enemy;

public class DanceTo extends Manager {
    private AUnit target;

    public DanceTo(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        target = unit.target();

        if (target == null || !target.hasPosition()) return false;

        int cooldown = unit.cooldownRemaining();

        return (cooldown >= 6 && cooldown <= 20)
            && isATargetThatWeCanDanceTo()
            && unit.lastAttackFrameLessThanAgo(30)
            && (
            (unit.isMissionAttack() && Alpha.count() >= 30 && unit.friendsNear().inRadius(3, unit).atLeast(4))
                || (distanceConditionIsOk() && !target.hasBiggerWeaponRangeThan(unit))
        );
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
        String logString = "DanceTo-" + unit.cooldownRemaining();
        unit.addLog(logString);

        if (danceToTarget(logString)) {
//            System.err.println("@ " + A.now() + " - " + unit.id() + " - __dance_@@@@_to___ " + target);
            return usedManager(this);
        }

        return null;
    }

    private boolean danceToTarget(String logString) {
        return unit.move(
            unit.translateTilesTowards(0.25, target), Actions.MOVE_DANCE_TO, logString, false
        );
    }
}
