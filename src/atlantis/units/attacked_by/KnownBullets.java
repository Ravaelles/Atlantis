package atlantis.units.attacked_by;

import atlantis.game.AGame;
import atlantis.map.base.ABaseLocation;
import bwapi.Bullet;

import java.util.*;
import java.util.stream.Collectors;

public class KnownBullets {
    private static Map<Integer, Bullet> knownBullets = new TreeMap<>();
    private static List<Bullet> visibleBullets;

    public static void updateKnown() {
        visibleBullets = AGame.get().getBullets();

        rememberNew(visibleBullets);
        detectHits();
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
            .map(Bullet::getID)
            .collect(Collectors.toSet());
    }

    private static void rememberNew(List<Bullet> visibleBullets) {
        for (Bullet bullet : visibleBullets) {
            knownBullets.put(bullet.getID(), bullet);
        }
    }
}
