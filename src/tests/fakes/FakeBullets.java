package tests.fakes;

import atlantis.map.bullets.ABullet;
import atlantis.units.attacked_by.Bullets;

import java.util.*;

/**
 * The harness's bullets, and the adapter that hands them to production.
 *
 * <p>Before the {@link Bullets.Source} port this class was imported directly by
 * {@code Bullets}, which is one of the reasons the game jar had to ship
 * {@code tests/fakes}. Now production asks a question and the answer comes from
 * whichever source is installed - the engine in a game, this one in a test.</p>
 */
public class FakeBullets {
    public static Set<ABullet> allBullets = new TreeSet<>();
//    public static Map<Integer, ABullet> allBullets = new HashMap<>();

    public static void installAsSource() {
        Bullets.useSource(new Source());
    }

    public static class Source implements Bullets.Source {
        @Override
        public Set<ABullet> currentBullets() {
            return allBullets;
        }

        @Override
        public boolean bulletStillExists(ABullet bullet) {
            // A fake bullet has no engine object behind it, and it exists for as
            // long as the test keeps it in the set - which is what the old
            // `&& !Env.isTesting()` carve-out in Bullets meant.
            return true;
        }
    }
}
