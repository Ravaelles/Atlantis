package atlantis.game.util;

import atlantis.units.AUnitType;
import atlantis.util.We;

/**
 * How many of each unit the GAME hands the player at the start - the units the
 * bot did not
 * build.
 *
 * <p>
 * <b>Measured, not remembered.</b> These numbers come from the engine's own
 * starting-unit
 * code, OpenBW's {@code create_starting_units} ({@code bwgame.h:21138-21176}):
 * it creates one
 * resource depot - Nexus, Command Center or Hatchery by race - and then
 *
 * <pre>
 * for (size_t i = 0; i != 4; ++i) {
 *     create_starting_unit(worker_type, position, owner);
 * }
 * </pre>
 *
 * so four workers for every race, plus an Overlord for Zerg. CONVENTIONS §9
 * puts engine
 * behaviour above anyone's memory, including the owner's - and it agrees with
 * the owner
 * here, which is why the number is written down once with its source rather
 * than
 * re-derived at each use.
 * </p>
 *
 * <p>
 * <b>Why the report needs it.</b> The end-of-game line counts what the bot
 * COMPLETED, via
 * {@code OnUnitCompleted} - which never fires for these, because they were not
 * produced.
 * So a Protoss game that built 17 Probes and lost three ends with 18 alive and
 * 17 built,
 * and without this class that reads as an off-by-one bug in the counter. It is
 * not: the
 * four starting workers are part of the 18 and none of the 17.
 * </p>
 */
public final class StartingUnits {

    private StartingUnits() {
    }

    /** The four workers every race begins with. */
    public static final int STARTING_WORKERS = 4;

    /**
     * How many of this type the game gave us at the start; 0 for anything the bot
     * produced.
     *
     * <p>
     * Race-aware because the depot and the worker differ per race, and Zerg
     * additionally
     * starts with an Overlord - which is the one unit whose absence from a naive "4
     * workers
     * and a depot" model would be a silent mistake.
     * </p>
     */
    public static int of(AUnitType type) {
        if (type == null)
            return 0;

        if (type.equals(We.protoss() ? AUnitType.Protoss_Nexus
                : We.terran() ? AUnitType.Terran_Command_Center
                        : AUnitType.Zerg_Hatchery)) {
            return 1;
        }

        if (type.equals(workerType()))
            return STARTING_WORKERS;

        if (We.zerg() && type.equals(AUnitType.Zerg_Overlord))
            return 1;

        return 0;
    }

    /**
     * Everything the game gave us, for a one-number summary of \"not built by us\".
     */
    public static int totalGiven() {
        return 1 + STARTING_WORKERS + (We.zerg() ? 1 : 0);
    }

    private static AUnitType workerType() {
        return We.protoss() ? AUnitType.Protoss_Probe
                : We.terran() ? AUnitType.Terran_SCV
                        : AUnitType.Zerg_Drone;
    }
}
