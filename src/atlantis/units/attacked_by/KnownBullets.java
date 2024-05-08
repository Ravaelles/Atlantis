package atlantis.units.attacked_by;

import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.bullets.ABullet;
import atlantis.units.AUnit;
import tests.fakes.FakeBullets;

import java.util.*;
import java.util.stream.Collectors;

public class KnownBullets {
    private static Map<Integer, ABullet> knownBullets = new TreeMap<>();
    private static List<ABullet> visibleBullets = new ArrayList<>();

    public static void updateKnown() {
//        System.out.println("UPDATE KNOWN BULLETS @" + A.now());
        visibleBullets = bulletsToABullets();

        rememberNew(visibleBullets);
        detectHits();
//        System.err.println("visibleBullets = " + visibleBullets.size() + " / " + AGame.get().getBullets().size());
    }

    private static List<ABullet> bulletsToABullets() {
        if (Env.isTesting()) return FakeBullets.allBullets;

        return AGame.get().getBullets()
            .stream()
            .map(b -> ABullet.fromBullet(b))
            .collect(Collectors.toList());
    }

    private static void detectHits() {
        Set<Integer> oldIds = knownBullets.keySet();
        Set<Integer> newIds = visibleBulletsIds();

        // Get dif of oldIds and newIds


        oldIds.removeAll(newIds);
    }

    private static Set<Integer> visibleBulletsIds() {
        return visibleBullets
            .stream()
            .map(ABullet::id)
            .collect(Collectors.toSet());
    }

    private static void rememberNew(List<ABullet> visibleBullets) {
        for (ABullet bullet : visibleBullets) {
            knownBullets.put(bullet.id(), bullet);
        }
    }

    public static List<ABullet> knownBullets() {
        return visibleBullets;
    }

    public static List<ABullet> against(AUnit unit) {
        if (visibleBullets.isEmpty()) return Collections.emptyList();

        return visibleBullets
            .stream()
            .filter(bullet -> bullet.target() != null && bullet.target().id() == unit.id())
            .collect(Collectors.toList());
    }
}
