package atlantis.combat.micro.avoid.always;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.game.player.Enemy;

public class ZealotAlwaysAvoidEnemy extends Manager {
    private double eval;
    private String _lastReason = "";

    public ZealotAlwaysAvoidEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isZealot()) return false;
        eval = unit.eval();

        if (Enemy.zerg()) return vsZerg();
        if (Enemy.protoss()) return vsProtoss();

        return false;
    }

    private boolean vsProtoss() {
        if (woundedEarly()) return true;
        if (badEvalEarly()) return true;

        return false;
    }

    private boolean t(String reason) {
        if (!reason.equals(_lastReason)) {
            System.err.println("ZealotAvoid: " + reason + " e:" + unit.evalDigit() + ", hp:" + unit.hp());
            _lastReason = reason;
        }

        return true;
    }

    private boolean badEvalEarly() {
        if (A.s >= 60 * 8) return false;
        if (unit.isMissionDefend() && (eval >= 0.8 || unit.distToMain() <= 6) && unit.hp() >= 34) return false;

        if (eval <= 0.9 && !unit.isMissionSparta()) return t("BadEvalEarlyA");

        double evalThreshold = unit.isMissionAttack() ? 1.2 : 1;
        if (eval < evalThreshold) return t("BadEvalEarlyB");

        return false;
    }

    private boolean woundedEarly() {
        if (A.supplyUsed() >= 80) return false;
        if (unit.shieldHealthy()) return false;

        if (unit.hp() <= veryWoundedHp()) return t("WoundedEarly36");

        if (unit.friendsNear().workers().inRadius(1.5, unit).atLeast(2)) return false;

        if (unit.hp() >= 35 && unit.friendsNear().workers().inRadius(3, unit).atLeast(1)) return false;
        if (unit.hp() <= 35 && unit.eval() <= 3) t("WoundedEarlyB");

        if (unit.hp() <= 32 || unit.eval() <= 0.95) {
            int meleeEnemiesNear = unit.meleeEnemiesNearCount(3.6);

            if (meleeEnemiesNear >= 1 && meleeEnemiesNear <= 4) return t("WoundedEarlyA");
        }

        return false;
    }

    private int veryWoundedHp() {
        return 20
            + (unit.meleeEnemiesNearCount(4) >= 2 ? 20 : 0);
    }

    private boolean vsZerg() {
        if (neverAvoidIfCannonInRange()) return false;

        return false;
    }

    private boolean neverAvoidIfCannonInRange() {
        return unit.isMissionDefend()
            && Count.cannons() >= 1
            && unit.shieldWound() >= 3
            && A.s <= 60 * 6
//            && Army.strength() <= 135
            && unit.friendsNear().cannons().inRadius(AUnit.NEAR_DIST, unit).notEmpty()
            && unit.friendsNear().cannons().inRadius(2.3, unit).empty();
    }
}
