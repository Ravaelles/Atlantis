package atlantis.map.bullets;

import atlantis.combat.state.AttackState;
import atlantis.game.A;
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
    private boolean exists;

    // =========================================================

    public ABullet(AUnit attacker, AUnit target, boolean exists) {
        this.attacker = attacker;
        this.target = target;
        this.createdAt = A.now;
        this.exists = exists;
    }

    // =========================================================

    public static ABullet fromBullet(Bullet b) {
        AUnit attacker = AUnit.createFrom(b.getSource(), false);
        AUnit target = AUnit.createFrom(b.getTarget(), false);

        if (attacker == null) {
            System.err.println("bullet attacker is null");
//            System.err.println("bullet.attacker null, target = " + bullet.target + "/our=" + bullet.target.isOur());
            return null;
        }

        if (target == null) {
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

        // =========================================================

        ABullet bullet = new ABullet(attacker, target, true);

        bullet.attacker = attacker;
        bullet.attacker.setAttackState(AttackState.PENDING);
        bullet.attacker.setLastBullet(bullet);
        bullet.target = target;
        bullet.b = b;

//        System.err.println("      [BULLET #" + b.getID() + "] by " + bullet.attacker.id() + " @" + A.now);
//        System.err.println("       lastAge = " + bullet.attacker.lastBulletAge());

        return bullet;
    }

    public static ABullet fromPendingAttack(AUnit attacker, AUnit enemy) {
        ABullet bullet = new ABullet(attacker, enemy, false);

        return bullet;
    }

    // =========================================================

    public double distToTargetPosition() {
        if (target == null || target.position() == null) return 9972;

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

    public boolean exists() {
        return exists;
    }
}
