package atlantis.map.bullets;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.attacked_by.KnownBullets;
import atlantis.util.cache.Cache;
import atlantis.util.cache.CacheKey;

import java.util.List;

public class DeadMan {
    private static Cache<Boolean> cache = new Cache<>();
    private static List<ABullet> bulletsAgainst;

    public static boolean isDeadMan(AUnit unit) {
        return cache.get(
            CacheKey.toKey(unit),
            0,
            () -> willBeDeadMan(unit)
        );
    }

    private static boolean willBeDeadMan(AUnit unit) {
//        if (KnownBullets.knownBullets().size() <= 1) return false;

        bulletsAgainst = KnownBullets.against(unit);
        if (bulletsAgainst.isEmpty()) return false;

        return damageWithAllPendingBullets(unit) > unit.hp();
    }

    private static int damageWithAllPendingBullets(AUnit unit) {
        int damage = 0;
        for (ABullet bullet : bulletsAgainst) {
            damage += BulletDamageAgainst.forBullet(bullet);
        }
//        System.err.println("bulletsAgainst = " + bulletsAgainst.size() + " / damage = " + damage);
        return damage;
    }
}
