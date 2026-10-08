package atlantis.placement.policy;

import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.TargetPlacement;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns a base-fortification intent into ordinary production goals
 * (`_AI/redesign/03_PLACEMENT.md` §5.3, §5.4 S5).
 *
 * <p>
 * This is the class that makes the boundary real. The legacy
 * {@code ProtossSecureBasesCommander} computed tiles itself and then asked for
 * a
 * cannon at them; here the policy says <b>"this base wants N cannons, near this
 * point"</b> and hands that to the goal layer as a count plus a
 * {@link TargetPlacement} constraint. The placement planner resolves the
 * constraint to a real tile - so the cannon lands somewhere choke-aware (S4's
 * {@code ChokeAffinityRanker}) without the policy knowing anything about tiles.
 * </p>
 *
 * <p>
 * Pure: no engine access. The caller passes the context and the cannon
 * producible;
 * a test can call it with hand-built values.
 * </p>
 */
public final class FortificationGoals {

    /** Priority band for defensive structures - between workers and the army. */
    public static final int PRIORITY_BASEDEFENSE = 40;

    private FortificationGoals() {
    }

    /**
     * Goals that bring a base up to its expected cannon count, or an empty list
     * when it already has enough.
     *
     * @param cannon     the Photon Cannon recipe
     * @param nearBaseX/ nearBaseY the base the cannons defend, as a
     *                   neighbourhood centre - a constraint, not a tile
     */
    public static List<ProductionGoal> forBase(
            CannonFortificationPolicy.Context context,
            atlantis.production.v2.Producible cannon,
            int nearBaseX, int nearBaseY) {
        List<ProductionGoal> goals = new ArrayList<>();
        int missing = CannonFortificationPolicy.missingCannons(context);
        if (missing <= 0)
            return goals;

        TargetPlacement placement = TargetPlacement.inNeighbourhood(nearBaseX, nearBaseY);

        // One goal per cannon rather than one goal with count=N: it lets the
        // scheduler time-shift them individually against the mineral timeline
        // instead of dropping the whole set when the first is unaffordable.
        for (int i = 0; i < missing; i++) {
            goals.add(new ProductionGoal(cannon, PRIORITY_BASEDEFENSE, 1, 0, placement));
        }

        return goals;
    }
}
