package atlantis.production.v2.execution;

import atlantis.production.v2.PlacementReservation;
import atlantis.production.v2.Producible;

/**
 * The single door in production-v2 through which engine commands leave the
 * scheduling core (M4 of _AI/redesign/01_PRODUCTION.md).
 *
 * <p>
 * DIP: {@code ProductionDispatcher} decides <em>what</em> to issue and
 * <em>when</em> - pure logic, provable in JUnit - and this interface decides how
 * an order reaches the game. The live implementation wraps the legacy builder
 * pipeline; a test installs a recording fake and asserts on the commands without
 * a running StarCraft.
 * </p>
 */
public interface OrderDirector {

    /**
     * Issues a train command on an existing production facility.
     *
     * @return true when a command was actually issued (false when the unit
     *         could not take it now, e.g. it is already training the same thing)
     */
    boolean trainFacility(String typeId, Producible item);

    /**
     * Commits a builder to a planned building at its reserved tile. The same
     * tile is re-offered every frame while the construction is pending, so an
     * implementation must be idempotent (doing nothing when that tile already
     * has a pending construction).
     *
     * @return true when a builder was committed or was already committed
     */
    boolean buildAt(Producible building, PlacementReservation placement);

    /**
     * Starts a research or an upgrade on a facility. Only attempted when the
     * item has a producer; techs and upgrades without one are skipped.
     *
     * @return true when the command was issued
     */
    boolean researchOrUpgrade(String typeId, Producible item);
}
