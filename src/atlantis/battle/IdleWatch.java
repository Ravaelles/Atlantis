package atlantis.battle;

import java.util.HashMap;
import java.util.Map;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

/**
 * Post-fight freeze detector (owner, Drag_v_LingsUpg2): a combat unit
 * that does not displace while no enemy is near and its weapon is
 * ready is the standstill signature - healthy play always has an
 * address (formation slot, ghost march, unexplored ground). Passive
 * diagnostics: {@code note} rides the per-frame battle hook next to
 * FrozenWatch (which owns the stuck-with-orders shape instead);
 * nothing here issues orders.
 *
 * <p>Thresholds name the owner's bar: still for {@link #IDLE_FRAMES}
 * with no enemy combat unit inside {@link #FOE_FAR_TILES} and cooldown
 * ready. Throttled rows (one per unit per {@link #LOG_EVERY_FRAMES}),
 * MEDIUM chatter for the analyst, not an alarm - the army-wide stall
 * twin (ten seconds, nobody moving) is the HIGH one.
 */
public final class IdleWatch {

    /** Still this long with no foe near and weapon ready: idle, not holding. */
    public static final int IDLE_FRAMES = 120;

    /** No enemy combat unit within: far enough that standing is a choice. */
    public static final double FOE_FAR_TILES = 12.0;

    /** Displacement below this still counts as standing still. */
    public static final double STILL_TILES = 0.5;

    /** How often one unit may log its idle state while it persists. */
    private static final int LOG_EVERY_FRAMES = 120;

    private static final Map<Integer, APosition> lastPos = new HashMap<>();
    private static final Map<Integer, Integer> stillFrames = new HashMap<>();
    private static final Map<Integer, Integer> lastLogFrame = new HashMap<>();

    private IdleWatch() {
    }

    public static void clearForTests() {
        lastPos.clear();
        stillFrames.clear();
        lastLogFrame.clear();
    }

    /** Still frames banked by this unit since it last displaced, or 0. */
    public static int stillFramesOf(int unitId) {
        Integer still = stillFrames.get(unitId);

        return still == null ? 0 : still;
    }

    public static void note(AUnit unit) {
        if (unit == null || !unit.isAlive() || !unit.isCombatUnit()) return;
        if (unit.isGatheringMinerals() || unit.isGatheringGas()) return;

        int now = A.now();
        APosition pos;
        try {
            pos = unit.position();
        } catch (Exception | Error ignored) {
            return;
        }
        if (pos == null) return;

        APosition last = lastPos.get(unit.id());
        try {
            if (last == null || unit.distTo(last) >= STILL_TILES) {
                lastPos.put(unit.id(), pos);
                stillFrames.put(unit.id(), 0);

                return;
            }
        } catch (Exception | Error ignored) {
            return;
        }
        int still = stillFramesOf(unit.id()) + 1;
        stillFrames.put(unit.id(), still);

        if (still < IDLE_FRAMES) return;
        if (!foeFar(unit) || !weaponReady(unit)) return;

        Integer lastLog = lastLogFrame.get(unit.id());
        if (lastLog != null && now - lastLog < LOG_EVERY_FRAMES) return;
        lastLogFrame.put(unit.id(), now);
        log(unit, pos, still, now);
    }

    private static boolean foeFar(AUnit unit) {
        try {
            Selection foes = Select.enemy().combatUnits();
            if (foes == null) return true;

            return foes.inRadius(FOE_FAR_TILES, unit).empty();
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    private static boolean weaponReady(AUnit unit) {
        try {
            return unit.cooldown() <= 0;
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    private static void log(AUnit unit, APosition pos, int still, int now) {
        int foe = -1;
        int cd = -1;
        int destX = -1;
        int destY = -1;
        String tag = "-";
        try {
            atlantis.units.AUnit nearest = Select.enemy().combatUnits().nearestTo(unit);
            if (nearest != null) foe = (int) (unit.distTo(nearest) * 10);
        } catch (Exception | Error ignored) {
        }
        try {
            cd = unit.cooldown();
        } catch (Exception | Error ignored) {
        }
        try {
            APosition dest = unit.targetPosition();
            if (dest != null) {
                destX = dest.position().x() / 32;
                destY = dest.position().y() / 32;
            }
        } catch (Exception | Error ignored) {
        }
        try {
            if (unit.tooltip() != null) tag = unit.tooltip();
        } catch (Exception | Error ignored) {
        }
        EventLog.key(LogKey.IDLE)
            .param("unit", unit.id())
            .param("ux", pos.position().x() / 32)
            .param("uy", pos.position().y() / 32)
            .param("still", still)
            .param("foe", foe)
            .param("cd", cd)
            .param("dx", destX)
            .param("dy", destY)
            .param("tag", tag)
            .log(now, A.minSec());
    }
}
