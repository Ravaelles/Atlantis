package atlantis.map.bullets;

import atlantis.game.A;
import atlantis.game.CameraCommander;
import atlantis.game.GameSpeed;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import bwapi.Bullet;
import bwapi.BulletType;

public class ABullet implements HasPosition {
    protected AUnit attacker;
    protected AUnit target;
    protected Bullet b;
    protected AUnit _initTarget;
    protected int _initTargetHp;
    private boolean consumed = false;

    // =========================================================

    public static ABullet fromBullet(Bullet b) {
//        System.err.println("b = " + b.getID());
        ABullet bullet = new ABullet();
        bullet.attacker = AUnit.createFrom(b.getSource(), false);

        if (bullet.attacker == null) return null;

        bullet.target = AUnit.createFrom(b.getTarget(), false);

        if (bullet.target == null) {
//            A.errPrintln(
//                "@" + A.now() + " - ABullet.fromBullet: target null \n"
//                    + "       (" + b.getTarget() + "), \n"
//                    + "       (attacker:" + bullet.attacker.typeWithUnitId() + ")"
//                    + "       (cooldown:" + bullet.attacker.cooldown() + ")"
//                    + "       (AttackFrameAgo:" + bullet.attacker.lastAttackFrameAgo() + ")"
//            );
//            CameraCommander.centerCameraOn(bullet.attacker);
//            GameSpeed.changeSpeedTo(40);
            return null;
        }

//        System.err.println("            [ BULLET with ID:" + b.getID() + " ]");

        bullet.b = b;
        return bullet;
    }

//    public static ABullet fromBullet(Bullet b, AUnit attacker, AUnit target) {
//        ABullet bullet = new ABullet();
//        bullet.attacker = attacker;
//        bullet.target = target;
//        bullet.b = b;
//        return bullet;
//    }

    // =========================================================

    public double distToTargetPosition() {
        return position().distTo(target.position());
    }

    // =========================================================

    @Override
    public String toString() {
        return "ABullet{" +
            "attacker=" + attacker +
            ", target=" + target +
            '}';
    }

    // =========================================================

    public BulletType type() {
        return b.getType();
    }

    @Override
    public APosition position() {
        return APosition.create(b.getPosition());
    }

    @Override
    public int x() {
        return position().x();
    }

    @Override
    public int y() {
        return position().y();
    }

    public AUnit attacker() {
        return attacker;
    }

    public AUnit target() {
        return target;
    }

    public int id() {
        return b.getID();
    }

    public Bullet b() {
        return b;
    }

    public void markAsConsumed() {
        consumed = true;
    }

    public boolean isConsumed() {
        return consumed;
    }
}
