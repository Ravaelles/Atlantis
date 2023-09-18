package atlantis.combat.micro.avoid.special;

import atlantis.Atlantis;
import atlantis.architecture.Manager;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import bwapi.Bullet;
import bwapi.BulletType;

public class AvoidPsionicStorm extends Manager {
    public AvoidPsionicStorm(AUnit unit) {
        super(unit);
    }

    @Override
    protected Manager handle() {
        if (unit.isUnderStorm()) {
            for (Bullet bullet : Atlantis.game().getBullets()) {
                if (bullet.getType().equals(BulletType.Psionic_Storm)) {
                    if (handleMoveAwayIfCloserThan(APosition.create(bullet.getPosition()), 3.1)) {
                        return usedManager(this);
                    }
                }
            }
        }

        return null;
    }

    protected boolean handleMoveAwayIfCloserThan(APosition avoidCenter, double minDist) {
        if (unit.distTo(avoidCenter) < minDist) {
            unit.runningManager().runFromAndNotifyOthersToMove(avoidCenter, "PSIONIC-STORM");
            return true;
        }
        else return false;
    }
}
