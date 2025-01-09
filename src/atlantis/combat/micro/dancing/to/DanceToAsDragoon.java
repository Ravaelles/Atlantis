package atlantis.combat.micro.dancing.to;

import atlantis.game.A;
import atlantis.units.AUnit;

public class DanceToAsDragoon extends DanceTo {
    private AUnit target;
    private double distToTarget;

    public DanceToAsDragoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isDragoon()) return false;
        if (unit.cooldown() <= 9) return false;
//        if (unit.cooldown() >= UnitAttackWaitFrames.DRAGOON) return false;

        target = unit.target();
        if (target == null || !target.hasPosition() || target.isDead()) return false;
        distToTarget = unit.distTo(target);

        if (forceDanceToBunker()) return true;

        if (unit.isTargetedBy(target)) return false;

        return !asDragoonDontDanceTo();
    }

    private boolean forceDanceToBunker() {
        return target.isBunker()
            && (unit.eval() >= 1.2 || A.supplyUsed() >= 170 || A.minerals() >= 1500)
            && unit.distToNearestChokeCenter() <= 4
            && unit.friendsInRadiusCount(1.5) >= 2;
    }

    // =========================================================

    private boolean asDragoonDontDanceTo() {
        if (unit.hp() <= 22) return true;
        if (distToTarget <= 3.4) return true;
        if (unit.cooldown() >= 19 || unit.cooldown() <= 9) return true;

        return unit.enemiesNear().combatBuildingsAntiLand().inRadius(8, unit).notEmpty();
    }
}
