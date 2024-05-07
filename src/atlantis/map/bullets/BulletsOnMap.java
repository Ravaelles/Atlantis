package atlantis.map.bullets;

import atlantis.Atlantis;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.util.cache.Cache;
import bwapi.Bullet;
import bwapi.BulletType;

import java.util.ArrayList;

public class BulletsOnMap {
    private static Cache<ArrayList<Bullet>> cache = new Cache<>();

    public static ArrayList<Bullet> ofType(BulletType type, int maxDist, HasPosition from) {
        ArrayList<Bullet> bullets = ofType(type);
        if (bullets.isEmpty()) return bullets;

        bullets.removeIf(bullet -> APosition.create(bullet.getPosition()).distToMoreThan(from, maxDist));
        return bullets;
    }

    public static ArrayList<Bullet> ofType(BulletType type) {
        return cache.get(
            "ofType:" + type.name(),
            0,
            () -> {
                ArrayList<Bullet> bullets = new ArrayList<>();

                for (Bullet bullet : Atlantis.game().getBullets()) {
                    if (bullet.getType().equals(type)) {
                        bullets.add(bullet);
                    }
                }

                return bullets;
            }
        );
    }
}
