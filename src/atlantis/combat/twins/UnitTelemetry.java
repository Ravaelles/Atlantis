package atlantis.combat.twins;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-second unit telemetry (NEXT #180, owner 2026-10-26): the death
 * log alone cannot explain a fight, so every combat unit reports once
 * per game-second: hp, shields, distance to the nearest enemy, to the
 * nearest dragoon and zealot of its own army, and how many enemies
 * stand within 2 tiles. The last 5 snapshots stay in memory per unit
 * so the death log prints the approach, not just the corpse.
 * Sampled, not pushed: one row per unit per 24 frames, deaths read
 * the ring without touching the file.
 */
public final class UnitTelemetry {

    /** One row per game-second keeps even long games readable. */
    public static final int TRACE_EVERY_FRAMES = 24;

    /** Snapshots kept per unit for the death log. */
    public static final int TRAIL_LENGTH = 5;

    private static final Map<Integer, Integer> lastSampleFrame = new HashMap<>();
    private static final Map<Integer, Deque<String>> trails = new HashMap<>();

    private UnitTelemetry() {
    }

    public static void sample(AUnit unit) {
        if (unit == null || !unit.isAlive() || !unit.isCombatUnit()) return;

        int now = A.now();
        Integer last = lastSampleFrame.get(unit.id());
        if (last != null && now - last < TRACE_EVERY_FRAMES) return;

        lastSampleFrame.put(unit.id(), now);

        int hp = unit.hp();
        int shields = unit.shields();
        int foe = distX10(unit, nearestFoe(unit));
        int dragoon = distX10(unit, nearestAllyOfType(unit, AUnitType.Protoss_Dragoon));
        int zealot = distX10(unit, nearestAllyOfType(unit, AUnitType.Protoss_Zealot));
        int foes2 = foesNear(unit, 2.0);
        int target = currentTargetId(unit);
        int destX = -1;
        int destY = -1;
        try {
            APosition dest = unit.targetPosition();
            if (dest != null) {
                destX = dest.position().x() / 32;
                destY = dest.position().y() / 32;
            }
        } catch (Exception ignored) {
        }

        EventLog.key(LogKey.UNIT_TELEMETRY)
            .param("unit", unit.id())
            .param("type", unit.type().name())
            .param("hp", hp)
            .param("shields", shields)
            .param("foe", foe)
            .param("dragoon", dragoon)
            .param("zealot", zealot)
            .param("foes2", foes2)
            .param("tgt", target)
            .param("dx", destX)
            .param("dy", destY)
            .param("ux", unit.x() / 32)
            .param("uy", unit.y() / 32)
            .log(now, A.minSec());

        Deque<String> trail = trails.get(unit.id());
        if (trail == null) {
            trail = new ArrayDeque<>();
            trails.put(unit.id(), trail);
        }
        trail.addLast("f" + now + " hp" + hp + " sh" + shields
            + " foe" + foe + " dg" + dragoon + " zl" + zealot + " n2:" + foes2
            + " tgt" + (target < 0 ? "-" : target)
            + " dst" + (destX < 0 ? "-" : destX + ":" + destY));
        while (trail.size() > TRAIL_LENGTH) trail.removeFirst();
    }

    /** The last snapshots of a dead unit, oldest first, for the death log. */
    public static List<String> trailFor(int unitId) {
        Deque<String> trail = trails.get(unitId);
        if (trail == null) return new ArrayList<>();

        return new ArrayList<>(trail);
    }

    public static void clearForTests() {
        lastSampleFrame.clear();
        trails.clear();
    }

    private static int distX10(AUnit unit, AUnit other) {
        if (other == null) return -1;

        try {
            return (int) (unit.distTo(other) * 10);
        } catch (Exception ignored) {
            return -1;
        }
    }

    private static AUnit nearestFoe(AUnit unit) {
        try {
            Selection foes = Select.enemy().combatUnits();

            return foes == null ? null : foes.nearestTo(unit);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static AUnit nearestAllyOfType(AUnit unit, AUnitType type) {
        try {
            Selection allies = Select.ourCombatUnits().ofType(type).exclude(unit);

            return allies == null ? null : allies.nearestTo(unit);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static int currentTargetId(AUnit unit) {
        try {
            AUnit target = unit.target();

            return target == null ? -1 : target.id();
        } catch (Exception ignored) {
            return -1;
        }
    }

    private static int foesNear(AUnit unit, double radiusTiles) {
        try {
            Selection foes = Select.enemy().combatUnits();

            return foes == null ? 0 : foes.inRadius(radiusTiles, unit).count();
        } catch (Exception ignored) {
            return 0;
        }
    }
}
