package atlantis.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Random-number helpers sharing the single game-wide {@link A#random} stream,
 * extracted from the {@code A} god utility (REVIEW §4, Stage H).
 *
 * <p>Staying in {@code atlantis.game} on purpose: the stream is global mutable
 * state that {@code randWithSeed} re-seeds, so moving the helpers elsewhere
 * would mean duplicating or injecting that state - a behaviour change, not a
 * refactoring.</p>
 */
public class ARandom {

    /**
     * @param percentChance is chance percentage of some action, e.g. 87.2 means some event occurs with 87.2%
     *                      probability
     * @return true if given random event occured
     */
    public static boolean chance(double percentChance) {
        return A.random.nextDouble() <= (percentChance / 100.0);
    }

    /**
     * @return random integer number from range [min, max]
     */
    public static int rand(int min, int max) {
        return min + A.random.nextInt(max - min + 1);
    }

    public static int randWithSeed(int min, int max, long seed) {
        A.random = new Random(seed);
        return min + A.random.nextInt(max - min + 1);
    }

    /**
     * Returns random element of given list.
     */
    public static Object getRandomListElement(List<?> list) {
        return list.get(A.random.nextInt(list.size()));
    }

    /**
     * Returns random element of given list.
     */
    public static Object getRandomElement(Collection<?> collection) {
        if (collection.isEmpty()) {
            return null;
        }

        int indexToPick = A.random.nextInt(collection.size());
        int counter = 0;
        for (Object object : collection) {
            if (indexToPick == counter++) {
                return object;
            }
        }
        return null;
    }

    /**
     *
     */
    public static String randomElement(String[] array) {
        return array[A.random.nextInt(array.length)];
    }

    /**
     *
     */
    public static Object randomElement(ArrayList<?> list) {
        return list.get(rand(0, list.size() - 1));
    }
}
