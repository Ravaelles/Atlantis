package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.game.A;
import atlantis.game.CameraCommander;
import atlantis.game.GameSpeed;
import atlantis.map.bullets.BulletsOnMap;
import atlantis.map.position.APosition;
import atlantis.map.position.Positions;
import atlantis.units.AUnit;
import bwapi.Bullet;
import bwapi.BulletType;
import bwapi.Color;

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
        ArrayList<Bullet> bullets = BulletsOnMap.ofType(BulletType.Psionic_Storm, 999999, unit);

        if (bullets.isEmpty()) return null;

        AAdvancedPainter.forcePainting();
        paintBullets(bullets, Color.Cyan);
        AAdvancedPainter.liftForcedPainting();

        if (handleMoveAwayIfPsionicCloserThan(bullets, 4)) return usedManager(this);

        return null;
    }

    private void paintBullets(ArrayList<Bullet> bullets, Color color) {
        for (Bullet bullet : bullets) {
            AAdvancedPainter.paintCircleFilled(APosition.create(bullet.getPosition()), 25, color);
        }
    }

    protected boolean handleMoveAwayIfPsionicCloserThan(ArrayList<Bullet> bullets, double minDist) {
        APosition avoidCenter = psionicCenter(bullets);
//        System.out.println("PSIONIC avoidCenter = " +  avoidCenter + " / " + unit.distTo(avoidCenter));

        if (unit.distTo(avoidCenter) < minDist) {
            unit.runningManager().runFromAndNotifyOthersToMove(avoidCenter, "PSIONIC-STORM");

//            AAdvancedPainter.forcePainting();
//            AAdvancedPainter.paintCircle(unit, 13, Color.Cyan);
//            AAdvancedPainter.paintLine(unit, avoidCenter, Color.Cyan);
//            AAdvancedPainter.paintLine(unit, unit.targetPosition(), Color.Cyan);
//            AAdvancedPainter.liftForcedPainting();
//            System.err.println("RUNNING FROM PSIONIC / unit:" + unit.position() + " to " + unit.targetPosition() + " " +
//                "/ " +
//                "psionic: " + avoidCenter + " / dist:" + unit.distTo(avoidCenter) + " / runTo:" + unit.distTo(unit.targetPosition()));
//            CameraCommander.centerCameraOn(avoidCenter);
//            if (GameSpeed.speed() <= 1) GameSpeed.changeSpeedTo(100);
            return true;
        }

        return false;
    }

    private APosition psionicCenter(ArrayList<Bullet> bullets) {
        Positions<APosition> positions = new Positions<>();
        for (Bullet bullet : bullets) {
            if (bullet.isVisible()) positions.addPosition(APosition.create(bullet.getPosition()));
        }

        return positions.isEmpty() ? null : positions.average();
    }
}
