package atlantis.combat.behaviors.protoss.corsair;

import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

/**
 * Corsairs hunt known Overlords (owner 2026-11-09, port of the old
 * {@code CorsairHuntKnownOverlords}): the nearest Overlord to our
 * main that no anti-air weapon covers. Unlike the duel hunt this
 * answers the whole known board, not just the neighbourhood - an
 * overlord creeping the natural at 30 tiles still gets visited.
 */
public final class CorsairHuntKnownOverlords {

    /**
     * Anti-air cover radius around the Overlord that vetoes the hunt.
     */
    static final double AA_COVER_TILES = 7.1;

    private CorsairHuntKnownOverlords() {
    }

    public static boolean applies(AUnit unit) {
        try {
            if (unit == null || !unit.isAlive() || !unit.isCorsair()) return false;
            if (!Enemy.zerg()) return false;

            return overlord() != null;
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    public static boolean act(AUnit unit) {
        AUnit overlord = applies(unit) ? overlord() : null;
        if (overlord == null) return false;

        unit.setTooltipTactical("HuntKnownOverlords");

        return unit.attackUnit(overlord);
    }

    static AUnit overlord() {
        try {
            Selection overlords = Select.enemy().ofType(AUnitType.Zerg_Overlord)
                .havingPosition()
                .sortByNearestTo(Select.mainOrAnyBuilding());
            if (overlords.isEmpty()) return null;

            for (AUnit overlord : overlords.list()) {
                if (overlord == null || !overlord.isAlive() || !overlord.hasPosition()) continue;
                if (overlord.hp() <= 0 && overlord.position().isPositionVisible()) continue;
                if (overlord.friendsNear().havingAntiAirWeapon().countInRadius(AA_COVER_TILES, overlord) > 0) {
                    continue;
                }

                return overlord;
            }

            return null;
        } catch (Exception | Error ignored) {
            return null;
        }
    }
}
