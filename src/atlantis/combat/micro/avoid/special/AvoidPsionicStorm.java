package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.map.bullets.BulletsOnMap;
import atlantis.map.position.APosition;
import atlantis.map.position.Positions;
import atlantis.units.AUnit;
import bwapi.Bullet;
import bwapi.BulletType;

import java.util.ArrayList;

public class AvoidPsionicStorm extends Manager {
    public AvoidPsionicStorm(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return !unit.isABuilding();
    }

    @Override
    protected Manager handle() {
        ArrayList<Bullet> bullets = BulletsOnMap.ofType(BulletType.Psionic_Storm, 5, unit);

        if (bullets.isEmpty()) return null;

        if (handleMoveAwayIfPsionicCloserThan(bullets, 3.1)) {
            return usedManager(this);
        }

        return null;
    }

    protected boolean handleMoveAwayIfPsionicCloserThan(ArrayList<Bullet> bullets, double minDist) {
        APosition avoidCenter = psionicCenter(bullets);

        if (unit.distTo(avoidCenter) < minDist) {
            unit.runningManager().runFromAndNotifyOthersToMove(avoidCenter, "PSIONIC-STORM");
            return true;
        }
        else return false;
    }

    private APosition psionicCenter(ArrayList<Bullet> bullets) {
        Positions<APosition> positions = new Positions<>();
        for (Bullet bullet : bullets) {
            positions.addPosition(APosition.create(bullet.getPosition()));
        }

        return positions.average();
    }
}
