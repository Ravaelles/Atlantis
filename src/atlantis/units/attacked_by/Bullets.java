package atlantis.units.attacked_by;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.bullets.ABullet;
import atlantis.map.bullets.ClearsCache;
import atlantis.units.AUnit;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The bullets in flight, and whether they are still real.
 *
 * <p>Two questions, two answers, and in a game both come from the engine
 * ({@link AGame#get()}): which bullets exist right now, and whether the engine
 * object behind a known bullet is still there. A bullet whose object is gone is
 * dropped from the known set - without that check, a bullet fired at a unit that
 * died mid-flight would stay "pending" forever.</p>
 *
 * <p>The harness answers both questions its own way (see
 * {@code tests.fakes.FakeBulletsSource}), which is why this is a port rather
 * than an {@code Env.isTesting()} branch: production code decides nothing about
 * where the bullets came from, and the jar stops needing {@code tests.fakes} to
 * answer it. With no source installed the class is pure delegation to the engine,
 * so a game behaves exactly as before.</p>
 */
public class Bullets implements ClearsCache {
    /**
     * Where bullets come from. {@link #engine()} is what production uses; the
     * harness installs its own.
     */
    public interface Source {
        /** Every bullet that currently exists, known or new. */
        Set<ABullet> currentBullets();

        /**
         * Is the engine object behind this bullet still there? A source without
         * engine objects (the harness) answers true: a fake bullet exists for as
         * long as the test keeps it.
         */
        boolean bulletStillExists(ABullet bullet);
    }

    private static Source source = null;

    public static void useSource(Source newSource) {
        source = newSource;
    }

    public static void useEngine() {
        source = null;
    }

    private static Source source() {
        return source != null ? source : engine();
    }

    private static Source engine() {
        return EngineBulletsSource;
    }

    private static Map<Integer, ABullet> allRawBullets = new HashMap<>();
    private static Map<Integer, ABullet> validBullets = new HashMap<>();

    /** The engine: {@link AGame#get()} for the bullets, {@code bullet.b()} for their existence. */
    private static final class EngineBulletsSource implements Source {
        @Override
        public Set<ABullet> currentBullets() {
            return AGame.get().getBullets()
                .stream()
                .filter(b -> b.getTarget() != null && !allRawBullets.containsKey(b.getID()))
//            .peek(b -> System.out.println("b.getID()=" + b.getID() + " / allRaw=" + A.keysToString(allRawBullets.keySet())))
                .map(ABullet::fromBullet)
//            .filter(b -> b != null && !b.isConsumed())
                .filter(b -> b != null)
//                .collect(Collectors.toMap(ABullet::id, b -> b));
                .collect(Collectors.toSet());
        }

        @Override
        public boolean bulletStillExists(ABullet bullet) {
            return bullet.b() != null && bullet.b().exists();
        }
    }

    private static final Source EngineBulletsSource = new EngineBulletsSource();

    public static void updateKnown() {
        addNewRawBullets();
        defineValidBullets();
    }

    private static void addNewRawBullets() {
        for (ABullet bullet : newMissingRawBullets()) {
            allRawBullets.put(bullet.id(), bullet);
        }
    }

    private static Set<ABullet> newMissingRawBullets() {
        return source().currentBullets();
    }

    private static void defineValidBullets() {
        validBullets.clear();
        List<Integer> removeBullets = new ArrayList<>();

        for (ABullet bullet : allRawBullets.values()) {
            if (!source().bulletStillExists(bullet)) {
                removeBullets.add(bullet.id());
                continue;
            }

            if (bullet.isConsumed()) continue;

            if (bullet.distToTargetPosition() < 0.0001) {
                bullet.markAsConsumed();
                continue;
            }

            validBullets.put(bullet.id(), bullet);
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

    public static List<ABullet> existingAgainst(AUnit unit) {
        return validBullets
            .values()
            .stream()
            .filter(bullet -> bullet.target() != null && bullet.target().id() == unit.id())
            .collect(Collectors.toList());
    }

    public static List<ABullet> against(AUnit unit) {
        Collection<ABullet> pendingAttackBullets = PendingAttacksAgainstEnemyUnit.against(unit);
        List<ABullet> existingBullets = existingAgainst(unit);

        if (pendingAttackBullets.isEmpty()) return existingBullets;

        existingBullets.addAll(pendingAttackBullets);

        return existingBullets;
    }
}
