package atlantis.map.bullets;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.attacked_by.Bullets;
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
//        if (Bullets.knownBullets().size() <= 1) return false;
        if (unit.isFoggedUnitWithKnownPosition()) return false;

        bulletsAgainst = Bullets.against(unit);
        if (bulletsAgainst.isEmpty()) return false;

        int willGetDamage = damageWithAllPendingBullets(unit);
        int hasHp = unit.hp() + healthBonus(unit);
        boolean isDeadManWalking = willGetDamage >= hasHp;

//        if (isDeadManWalking) {
//            System.err.println(
//                "@ " + A.now() + " - " + unit.typeWithUnitId()
//                    + " - HP: " + hasHp
//                    + " / Damage:" + willGetDamage
//            );
//        }

        return isDeadManWalking;
    }

    private static int healthBonus(AUnit unit) {
        if (unit.isTerran()) return 0;
        return 2;
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
