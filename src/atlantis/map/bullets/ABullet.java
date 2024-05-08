package atlantis.map.bullets;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import bwapi.Bullet;
import bwapi.BulletType;

public class ABullet implements HasPosition {
    protected AUnit attacker;
    protected AUnit target;
    protected Bullet b;

    // =========================================================

    public static ABullet fromBullet(Bullet b) {
        ABullet bullet = new ABullet();
        bullet.attacker = AUnit.createFrom(b.getSource());
        bullet.target = AUnit.createFrom(b.getTarget());
        bullet.b = b;
        return bullet;
    }

    public static ABullet fromBullet(Bullet b, AUnit attacker, AUnit target) {
        ABullet bullet = new ABullet();
        bullet.attacker = attacker;
        bullet.target = target;
        bullet.b = b;
        return bullet;
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
}
