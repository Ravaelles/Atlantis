package tests.fakes;

import atlantis.map.bullets.ABullet;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;

public class FakeBullet extends ABullet {
    private static int firstFreeId = 1;

    protected int id;
    protected HasPosition position;

    public static FakeBullet fromPosition(HasPosition position, AUnit attacker, AUnit target) {
        FakeBullet bullet = new FakeBullet();
        bullet.attacker = attacker;
        bullet.target = target;
        bullet.id = firstFreeId++;
        bullet.position = position;
        return bullet;
    }

    @Override
    public APosition position() {
        return position.position();
    }

    @Override
    public int id() {
        return id;
    }
}
