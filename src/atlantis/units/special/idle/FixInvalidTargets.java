package atlantis.units.special.idle;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class FixInvalidTargets extends Manager {
    public FixInvalidTargets(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;
        if (!unit.isCombatUnit()) return false;
//        if (unit.lastPositionChangedAgo() <= 3) return false;
        if (!unit.isStopped()) return false;

        if (
            (unit.isAttacking() || unit.action().isAttacking())
//                && unit.action().isAttacking()
                && !unit.hasValidTarget()
//                && unit.lastPositionChangedAgo() >= 5
//                && unit.cooldown() <= 7
        ) {
//            System.err.println(unit.target());
//            if (unit.target() != null) System.err.println("   " + unit.target().hp());
//            if (A.isUms() && AliveEnemies.get().notEmpty()) {
//                A.errPrintln(A.now() + ": FixInvalidTargets: " + unit + " is attacking null target");
//                PauseAndCenter.on(unit, true);
//            }
            return true;
        }

        return false;
    }

    @Override
    public Manager handle() {
//        unit.paintCircle(10, Color.Teal);
//        unit.paintCircle(11, Color.Teal);
//        unit.paintCircle(12, Color.Teal);
//        unit.paintCircle(13, Color.Teal);

        if (FixActions.attackEnemies(unit, this, 0.7)) return usedManager(this, "InvTarget-Attack");
        if (FixActions.moveToLeader(unit)) return usedManager(this, "InvTarget-2Leader");
        if (FixActions.movedSlightly(unit)) return usedManager(this, "InvTarget-2Focus");

//        AUnit enemy = unit.nearestEnemy();
//        if (enemy == null) {
//            if (unit.moveToLeader(Actions.MOVE_FOLLOW, "FixInvalidTargetLeader")) return usedManager(this);
//
//            return null;
//        }
//
//        double margin = unit.distTo(enemy) - unit.weaponRangeAgainst(enemy);
//
//        if (margin > 0) {
//            if ((new AttackNearbyEnemies(unit)).forceHandle() != null) return usedManager(this);
//
//            if (unit.moveAwayFrom(enemy, -0.1, Actions.MOVE_DANCE_TO, "FixInvalidMoveTo")) {
//                return usedManager(this);
//            }
//        }

        return null;
    }
}
