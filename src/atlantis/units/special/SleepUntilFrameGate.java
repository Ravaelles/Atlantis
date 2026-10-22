package atlantis.units.special;

import atlantis.game.A;
import atlantis.units.AUnit;

/**
 * Order-latency gate: a unit that was just given an order must not be re-issued
 * anything until the engine has had its full command latency to REGISTER that
 * order - and for a few frames beyond, because the engine-side state
 * (`isAttacking`, `isGathering`, `order`) updates only after the order lands.
 *
 * <p>
 * <b>The recommendation this implements (owner, 2026-10-12):</b> after
 * {@code unit.attack(target)} set {@code sleepUntilFrame = now +
 * getRemainingLatencyFrames() + 1} and skip the unit while
 * {@code now < sleepUntilFrame}. Never trust {@code unit.isAttacking()} in the
 * first frames after the command - read {@code getOrder()} or the weapon
 * cooldown instead, or simply wait the latency out. Applied to BOTH workers and
 * combat units.
 * </p>
 *
 * <p>
 * <b>Why the top of the manager chain.</b> Every manager below this one makes
 * decisions from engine state that is stale in exactly this window (the
 * GatherResources refusals "worker is already gathering" against an order the
 * engine never registered, the build refusals "lastCommand=Move" against a move
 * the engine already replaced). One gate above everything expresses the rule
 * once: an order in flight is an order that owns the unit.
 * </p>
 *
 * <p>
 * <b>The +2 margin.</b> The recommendation says latency + 1. Two more frames
 * cover the frame the command is issued on (the remaining-latency counter still
 * holds its full value then) and the frame the engine publishes the state
 * update. Both measured on OpenBW: a gather re-issued at
 * {@code lastCommandIssuedAgo=1} was refused by the engine while a re-issued
 * at 3 was accepted.
 * </p>
 */
public class SleepUntilFrameGate {

    /** The frame until which the unit must not be touched; -1 = no hold. */
    private static final java.util.Map<Integer, Integer> sleepUntil = new java.util.HashMap<>();

    /**
     * Hold the unit from now until the current command can have registered, plus
     * the
     * safety margin. Call right after issuing ANY order (attack, gather, build,
     * move).
     */
    public static void holdAfterOrder(AUnit unit) {
        sleepUntil.put(unit.id(), A.now() + latencyFrames() + 2);
    }

    /**
     * True when the unit must be left alone - an order is still in flight.
     */
    public static boolean mustSleep(AUnit unit) {
        Integer until = sleepUntil.get(unit.id());
        if (until == null)
            return false;

        if (A.now() >= until) {
            sleepUntil.remove(unit.id());

            return false;
        }

        return true;
    }

    /**
     * The engine's remaining command latency. Falls back to a safe constant when
     * the
     * engine does not answer (the stub world and some tests have no Game).
     * Public: cooldown-relative decisions (Stardust attack readiness:
     * fire iff cooldown <= latency + 2) read the live value per unit,
     * never a flat constant (NEXT #154).
     */
    public static int latencyFrames() {
        try {
            int latency = atlantis.Atlantis.game().getRemainingLatencyFrames();
            // The stub world and some engine states answer 0; a 0-frame hold would let the
            // very next frame re-issue the order against the stale state the gate exists
            // to avoid (measured: the refusal happens at lastCommandIssuedAgo=1-2). The
            // fallback is also the real minimum: BWAPI's fast command latency is never 0.

            return latency > 0 ? latency : 3;
        } catch (Throwable e) {
            return 3;
        }
    }

    /** Test seam. */
    public static void clear() {
        sleepUntil.clear();
    }
}
