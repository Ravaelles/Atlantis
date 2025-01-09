package atlantis.map.bullets;

import atlantis.combat.state.AttackState;
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
    //    protected AUnit _initTarget;
//    protected int _initTargetHp;
    private boolean consumed = false;
    private int createdAt;

    // =========================================================

    public static ABullet fromBullet(Bullet b) {
        ABullet bullet = new ABullet();
        bullet.createdAt = A.now;
        bullet.attacker = AUnit.createFrom(b.getSource(), false);
        if (bullet.attacker != null) {
            bullet.attacker.setAttackState(AttackState.PENDING);
            bullet.attacker.setLastBullet(bullet);
//            System.err.println("      [ BULLET with ID:" + b.getID() + " ] from " + bullet.attacker);
        }

        bullet.target = AUnit.createFrom(b.getTarget(), false);

        if (bullet.attacker == null) {
//            System.err.println("bullet.attacker null, target = " + bullet.target + "/our=" + bullet.target.isOur());
            return null;
        }

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

    public int createdAt() {
        return createdAt;
    }
}
