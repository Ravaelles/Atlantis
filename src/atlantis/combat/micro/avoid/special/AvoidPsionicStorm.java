package atlantis.combat.micro.avoid.special;

import atlantis.Atlantis;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import bwapi.Bullet;
import bwapi.BulletType;

public class AvoidPsionicStorm {
    static boolean avoidPsionicStorm(AUnit unit) {
        if (unit.isUnderStorm()) {
            for (Bullet bullet : Atlantis.game().getBullets()) {
                if (bullet.getType().equals(BulletType.Psionic_Storm)) {
//                    System.err.println("------------- " + A.now() + " PSIONIC! ----------------");

                    if (handleMoveAwayIfCloserThan(
                        unit, APosition.create(bullet.getPosition()), 2.1
                    )) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // =========================================================

    protected static boolean handleMoveAwayIfCloserThan(AUnit unit, APosition avoidCenter, double minDist) {
        if (unit.distTo(avoidCenter) < minDist) {
            unit.runningManager().runFromAndNotifyOthersToMove(avoidCenter, "PSIONIC-STORM");
            return true;
        }
        else {
            return false;
        }
    }
}