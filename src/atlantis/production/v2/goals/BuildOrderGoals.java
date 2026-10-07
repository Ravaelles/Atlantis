package atlantis.production.v2.goals;

import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.TargetPlacement;
import atlantis.production.v2.UnitProducible;
import atlantis.units.AUnitType;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns a text build order into v2 goals (M5 of
 * _AI/redesign/01_PRODUCTION.md): the existing {@code .txt} files stay the
 * source of the opening, they simply stop being a queue.
 *
 * <p>
 * The conversion is deliberately small. Line order is priority (line 3 beats
 * line 14, within one band), non-production rows (missions, settings) are
 * skipped, and the row's supply gate is honoured by <b>not emitting the goal
 * until the projected supply covers it</b> - the row's own meaning, per
 * {@code BuildOrderRowParser}, is "the supply you are using once this is done",
 * not "before you start it".
 * </p>
 *
 * <p>
 * Pure logic: the goals are computed from rows and unit types only, and the one
 * game question - how much supply is in flight - is answered through
 * {@link SupplyProjection}, so the tests reason about an opening without a
 * game. Nothing here knows about {@code Queue}, statuses or reservations.
 * </p>
 */
public final class BuildOrderGoals {

    /**
     * Answers "supply used plus supply already under way". The engine adapter
     * projects it from units and the plan; a test supplies a number.
     */
    public interface SupplyProjection {
        int supplyWithWhatIsProduced();
    }

    private BuildOrderGoals() {
    }

    public static List<ProductionGoal> from(List<ProductionOrder> rows, SupplyProjection supply) {
        List<ProductionGoal> goals = new ArrayList<>();
        if (rows == null) return goals;

        int projectedSupply = supply != null ? supply.supplyWithWhatIsProduced() : 0;

        for (int line = 0; line < rows.size(); line++) {
            ProductionOrder row = rows.get(line);
            AUnitType unitType = row.unitType();
            if (unitType == null) continue;

            // The row means "at this supply", i.e. after the previous rows have
            // been produced. Rows we cannot afford to even start yet are not
            // dropped - they are simply not goals of this frame's pass, which is
            // exactly the "shift forward, never drop" behaviour.
            if (row.minSupply() > 0 && projectedSupply < row.minSupply()) continue;

            goals.add(new ProductionGoal(
                    UnitProducible.of(unitType),
                    priorityForLine(line),
                    1,
                    0,
                    TargetPlacement.anywhere()));

            projectedSupply += Math.max(0, unitType.supplyNeeded());
        }

        return goals;
    }

    /**
     * Earlier lines are higher priority, but only within a coarse band: a build
     * order is an opening, and a dozen identical bands would be a second,
     * brittle copy of the file rather than a priority. What the bands must get
     * right is the big picture - the first Pylon outranks a Zealot, an early
     * Gateway outranks a late tech - not the exact line number.
     */
    private static int priorityForLine(int line) {
        if (line < 4) return ProductionGoal.PRIORITY_DEPOTS;
        if (line < 12) return ProductionGoal.PRIORITY_BASEDEFENSE;
        return ProductionGoal.PRIORITY_NORMAL;
    }

    /**
     * The live projection: what we have plus what is already being produced.
     * Kept here, next to the use, because it is the only game-facing part of
     * this class.
     */
    public static SupplyProjection liveSupplyProjection() {
        return new SupplyProjection() {
            @Override
            public int supplyWithWhatIsProduced() {
                return atlantis.game.A.supplyUsed() + supplyInTrainingQueues();
            }
        };
    }

    /**
     * Supply already committed in a production facility's build queue: the
     * units whose orders are in the game but which do not count in
     * {@code supplyUsed} yet. This is the "plus what is produced" half of the
     * build-order row, and it is what stops an opening from re-issuing a unit
     * that is already on the way.
     */
    private static int supplyInTrainingQueues() {
        int total = 0;
        for (atlantis.units.AUnit unit : atlantis.units.select.Select.ourBuildings().list()) {
            for (atlantis.units.AUnitType type : unit.trainingQueue()) {
                total += Math.max(0, type.supplyNeeded());
            }
        }
        return total;
    }
}
