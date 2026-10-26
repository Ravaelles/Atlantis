package atlantis.combat.cluster;

import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

/**
 * The home ground every anchor and rally resolves from (NEXT #194):
 * the main base plus the main choke, read ONCE per resolution instead
 * of once per seam. {@code ClusterRally}, {@code ProtossDefendFocusPoint}
 * and the retreat each used to read {@code Select.main()} and
 * {@code Chokes.mainChoke()} on their own with their own null-handling -
 * same cached answers, five copies of the "is there a home?" question,
 * and no single place to ask which home-pointer latched the army (NEXT
 * #206: the army comes home, then stays tucked into the main).
 *
 * <p>Positions only, no orders, no state: {@link #resolve()} answers the
 * live pair null-safely (an unreadable game resolves incomplete, never
 * throws), {@link #resolve(AUnit, AChoke)} answers the same question for
 * given values so the contract pins without a world.</p>
 */
public final class HomeGround {

    private final AUnit main;
    private final AChoke mainChoke;

    private HomeGround(AUnit main, AChoke mainChoke) {
        this.main = main;
        this.mainChoke = mainChoke;
    }

    /** The live pair, null-safe: an unreadable game is incomplete ground. */
    public static HomeGround resolve() {
        try {
            return resolve(Select.main(), Chokes.mainChoke());
        }
        catch (RuntimeException ignored) {
            return new HomeGround(null, null);
        }
    }

    /** The same question for given values - the pinnable seam. */
    public static HomeGround resolve(AUnit main, AChoke mainChoke) {
        return new HomeGround(main, mainChoke);
    }

    /**
     * True when there is a home to stand on: a main with a position and
     * a choke with a center. Every consumer used to ask exactly this
     * before standing anywhere - now they ask it here.
     */
    public boolean isComplete() {
        return main != null
            && main.position() != null
            && mainChoke != null
            && mainChoke.center() != null;
    }

    public AUnit main() {
        return main;
    }

    public AChoke mainChoke() {
        return mainChoke;
    }
}
