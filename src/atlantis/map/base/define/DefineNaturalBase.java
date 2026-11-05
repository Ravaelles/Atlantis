package atlantis.map.base.define;

import atlantis.game.A;
import atlantis.map.MapTiles;
import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;
import atlantis.util.log.ErrorLog;

public class DefineNaturalBase {
    private static final Cache<Object> cache = new Cache<>();

    /**
     * Returns nearest base location (by the actual ground distance) to the given base location.
     */
    public static APosition natural() {
        APosition natural = (APosition) cache.get(
            "natural",
            -1,
            () -> {
                ABaseLocation naturalLocation = naturalIfMainIsAt(Select.mainOrAnyBuildingPosition());

                return naturalLocation != null ? naturalLocation.position() : null;
            }
        );

        // A null answer is never cached: early in the game (map analysis,
        // no main yet, BWEM still listing bases) the inputs are simply
        // not ready, and caching the miss poisons every natural-anchored
        // decision for the whole game (measured live: "Natural base can
        // not be determined" every minute on TauCross while expansions
        // worked - the anchor was missing, not the base). Re-evaluate
        // until it resolves; shout once per game, with context instead
        // of a per-minute stack trace.
        if (natural == null) {
            cache.forget("natural");

            // Transient inputs are silent: no main yet, no bases listed
            // (map analysis), or a UMS map with no natural by design.
            // Shouting there turns every normal game start into one HIGH
            // with a stack trace. The genuine failure - inputs ready but
            // still no natural - shouts once per game, with context.
            AUnit mainNow = null;
            int basesNow = -1;
            try {
                mainNow = Select.mainOrAnyBuilding();
                basesNow = BaseLocations.baseLocations().size();
            } catch (Exception | Error ignored) {
            }
            if (mainNow == null || basesNow <= 0 || A.isUms()) return null;

            shoutOncePerGame(mainNow, basesNow);
        }

        return natural;
    }

    private static boolean shoutedThisGame = false;

    /**
     * Per-game one-shot: a new game re-arms the shout (a new map may
     * genuinely lack a natural, and that deserves exactly one witness
     * row, not silence and not spam).
     */
    public static void resetForNewGame() {
        shoutedThisGame = false;
        cache.forget("natural");
    }

    private static void shoutOncePerGame(AUnit main, int bases) {
        if (shoutedThisGame) return;
        shoutedThisGame = true;

        ErrorLog.printMaxOncePerMinutePlusPrintStackTrace(
            "Natural base can not be determined (main="
                + (main == null ? "-" : main.position())
                + " basesKnown=" + bases + ")");
    }

    /**
     * Returns nearest base location (by the actual ground distance) to the given base location.
     */
    public static ABaseLocation naturalIfMainIsAt(HasPosition nearestTo) {
        if (nearestTo == null) return null;

        String key = "naturalIfMainIsAt:" + nearestTo;
        ABaseLocation found = (ABaseLocation) cache.get(
            key,
            -1,
            () -> {
                // Get all base locations, sort by being closest to given nearestTo position
                Positions<ABaseLocation> baseLocations = new Positions<>();
                baseLocations.addPositions(BaseLocations.baseLocations());
                baseLocations.sortByGroundDistanceTo(nearestTo, true);

                for (ABaseLocation baseLocation : baseLocations.list()) {
                    if (nearestTo.distTo(baseLocation) <= 6 || !isConnected(nearestTo, baseLocation)) {
                        continue;
                    }
//                    if (!Chokes.fullfillsConditionsForNatural(choke, Chokes.mainChoke())) continue;

                    return baseLocation;
                }

                return null;
            });

        // Same no-poison rule as natural(): a map-analysis-time miss
        // must not stick once the bases are listed.
        if (found == null) cache.forget(key);

        return found;
    }

    private static boolean isConnected(HasPosition nearestTo, ABaseLocation baseLocation) {
        return MapTiles.hasPathBetween(baseLocation.position(), nearestTo);
    }
}
