package atlantis.units.attacked_by;

import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.bullets.ABullet;
import atlantis.units.AUnit;
import tests.fakes.FakeBullets;

import java.util.*;
import java.util.stream.Collectors;

public class Bullets {
    private static Map<Integer, ABullet> allRawBullets = new HashMap<>();
    private static Map<Integer, ABullet> validBullets = new HashMap<>();

    public static void updateKnown() {
//        System.out.println("UPDATE KNOWN BULLETS @" + A.now());
        addNewRawBullets();
        defineValidBullets();

//        System.err.println(
//            AGame.get().getBullets().size()
//                + " / allRawBullets= " + A.keysToString(allRawBullets.keySet())
//                + " / valid= " + A.keysToString(validBullets.keySet())
//        );

//        System.err.println("B visibleBullets = " + bulletsAtUnits.size() + " / " + AGame.get().getBullets().size());

//        if (validBullets.values().size() > 0) {
//            ABullet b = validBullets.values().stream().findFirst().get();
//            System.err.println(
//                b.distToTargetPosition()
////                    + " / ID " + b.id()
//                    + " / VIS " + b.b().isVisible()
//                    + " / hp:" + b.target().hp()
//            );
//        }
    }

    private static void addNewRawBullets() {
        for (ABullet bullet : newMissingRawBullets()) {
            allRawBullets.put(bullet.id(), bullet);
        }
    }

    private static Set<ABullet> newMissingRawBullets() {
        if (Env.isTesting()) return FakeBullets.allBullets;

        return AGame.get().getBullets()
            .stream()
            .filter(b -> b.getTarget() != null && !allRawBullets.containsKey(b.getID()))
//            .peek(b -> System.out.println("b.getID()=" + b.getID() + " / allRaw=" + A.keysToString(allRawBullets.keySet())))
            .map(b -> ABullet.fromBullet(b))
//            .filter(b -> b != null && !b.isConsumed())
            .filter(b -> b != null)
//            .collect(Collectors.toMap(ABullet::id, b -> b));
            .collect(Collectors.toSet());
    }

    private static void defineValidBullets() {
        validBullets.clear();
        List<Integer> removeBullets = new ArrayList<>();

        for (ABullet bullet : allRawBullets.values()) {
            if (bullet.b() == null || !bullet.b().exists()) {
                removeBullets.add(bullet.id());
                continue;
            }

            if (bullet.isConsumed()) continue;

            if (bullet.distToTargetPosition() < 0.0001) {
                bullet.markAsConsumed();
                continue;
            }

            validBullets.put(bullet.id(), bullet);
//            knownBullets.put(bullet.id(), bullet);
        }

        if (!removeBullets.isEmpty()) {
            for (int bulletID : removeBullets) {
                allRawBullets.remove(bulletID);
            }
        }
    }

    public static Collection<ABullet> knownBullets() {
        return validBullets.values();
    }

    public static List<ABullet> against(AUnit unit) {
        if (validBullets.isEmpty()) return Collections.emptyList();

        return validBullets
            .values()
            .stream()
            .filter(bullet -> bullet.target() != null && bullet.target().id() == unit.id())
            .collect(Collectors.toList());
    }
}
