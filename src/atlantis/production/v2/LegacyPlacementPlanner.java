package atlantis.production.v2;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.position.APositionFinder;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The production-v2 placement adapter over the legacy position finder.
 *
 * <p>
 * M3 of _AI/redesign/01_PRODUCTION.md: the scheduler asks this seam where a
 * planned building will stand; the implementation delegates to
 * {@link APositionFinder#findStandardPosition} (the same code the legacy
 * construction pipeline uses, including its BWEM/map logic). Nothing in the
 * legacy finder changes - v2 consumes it, exactly the strangler shape the
 * redesign prescribes (phase-out of {@code Construction/**} comes later, and
 * this seam is what decouples the scheduler from it until then).
 * </p>
 *
 * <p>
 * {@code reservePlacement} is where a builder is chosen and the tile is
 * validated as buildable now; a failed reservation skips the item for this
 * pass and the next frame's pass retries. Tiles reserved earlier in the same
 * pass are excluded from the finder (otherwise two Pylons planned in one frame
 * would get the same spot - the position finder caches per builder/type/near
 * and knows nothing about this pass), and one builder is chosen per pass, not
 * per building.
 * </p>
 *
 * <p>
 * Block templates and pylon-power scoring (Stardust's richer model) are later
 * refinements behind this same interface - they change the body of this class
 * and nothing else, which is the whole point of the seam.
 * </p>
 * <p>
 * Pass-scoped state is reset by {@link ResourceTimeline#originFrame()}-independent
 * {@link #startPass()}: two identical buildings in one frame would otherwise
 * receive the same tile.
 * </p>
 */
public class LegacyPlacementPlanner implements PlacementPlanner {

    /**
     * Ranking seam for candidates: the legacy finder returns one validated tile,
     * so today there is nothing to choose between (see the NOTE in
     * {@link #reservePlacement}). When a finder exposes several validated
     * candidates, only this resolver is replaced - the scheduler does not
     * change.
     */
    public interface CandidateResolver {
        int choose(List<APosition> candidates, Producible building);
    }

    private static final CandidateResolver FIRST_CANDIDATE = new CandidateResolver() {
        @Override
        public int choose(List<APosition> candidates, Producible building) {
            return candidates.isEmpty() ? -1 : 0;
        }
    };

    private CandidateResolver candidateResolver = FIRST_CANDIDATE;

    public void useCandidateResolver(CandidateResolver resolver) {
        this.candidateResolver = resolver != null ? resolver : FIRST_CANDIDATE;
    }

    /** Build tiles already claimed in the current pass: "x:y". */
    private final Set<String> reservedTilesThisPass = new HashSet<>();

    /** Chosen once per pass: every building in a frame is served by one builder. */
    private AUnit passBuilder;

    /**
     * Reservations of the last pass, replayed to the dispatcher: a building
     * whose builder is already walking keeps reporting its committed frame, so
     * it is re-committed idempotently instead of being orphaned when the builder
     * dies (01_PRODUCTION.md §5A).
     */
    private final List<PlacementReservation> lastPassReservations = new ArrayList<>();

    @Override
    public void startPass() {
        reservedTilesThisPass.clear();
        lastPassReservations.clear();
        passBuilder = null;
    }

    /** Refusal reasons already twinned this game: a planner that fails every
     * frame must say so once, not 22,000 times (measured: Core refused from
     * frame 4587 to game end with zero rows anywhere). */
    private final Set<String> reportedRefusals = new HashSet<>();

    private void twinRefusal(AUnitType unitType, String reason) {
        String key = unitType.name() + ":" + reason;
        if (!reportedRefusals.add(key)) return;
        // CATALOGUE_SITE, not PLACEMENT_FAILED: a speculative probe with no
        // pylons yet (Forge at frame 2) is an expected refusal, not a defect,
        // and PLACEMENT_FAILED trips scenario gates. Same row shape as the
        // adapter so one key filters the whole site search.
        EventLog.key(LogKey.CATALOGUE_SITE)
            .param("type", unitType.name())
            .param("result", "declined")
            .param("reason", reason)
            .param("via", "v2-legacy")
            .log(AGame.now(), A.minSec());
    }

    /** Most refused term of the last search, or the last status when the sweep never ran (cache hit). */
    private static String topRefusal() {
        // The race finder counts every refused tile per term (power included);
        // the shared chain only sees tiles that pass power. Read the race map
        // first. Either map can hold another building's sweep on a cache hit -
        // the twin fires once per type, almost always on a real sweep.
        java.util.Map<String, Integer> breakdown =
            atlantis.production.constructions.position.protoss.ProtossPositionFinder.lastRefusalBreakdown();
        if (breakdown.isEmpty()) {
            breakdown = atlantis.production.constructions.position.PositionFulfillsAllConditions.lastRefusalBreakdown();
        }
        String top = null;
        int topCount = 0;
        int total = 0;
        for (java.util.Map.Entry<String, Integer> entry : breakdown.entrySet()) {
            total += entry.getValue();
        }
        for (java.util.Map.Entry<String, Integer> entry : breakdown.entrySet()) {
            if (entry.getValue() > topCount) {
                topCount = entry.getValue();
                top = entry.getKey() + " x" + entry.getValue() + "/" + total;
            }
        }
        if (top == null) return AbstractPositionFinder._STATUS;
        return top;
    }

    @Override
    public PlacementReservation reservePlacement(Producible building, TargetPlacement constraint, int targetFrame) {
        AUnitType unitType = resolveUnitType(building);
        if (unitType == null)
            return PlacementReservation.failure();

        AUnit builder = builderForThisPass();
        if (builder == null) {
            twinRefusal(unitType, "no-builder");
            return PlacementReservation.failure();
        }

        // Gas buildings take no search: a refinery stands on its geyser's
        // exact tile (the oldest base without one), so the ring sweep below
        // would only invent broken tiles. Same finder the legacy pipeline
        // uses, behind this seam.
        if (unitType.isGasBuilding()) {
            APosition gas = atlantis.production.constructions.position.GasBuildingPositionFinder
                .findPositionForGasBuilding(unitType, resolveNear(constraint));
            if (gas == null)
                return PlacementReservation.failure();
            return PlacementReservation.success(gas.tx(), gas.ty(), targetFrame);
        }

        // One candidate from the legacy finder, which already excludes buildings
        // on the map and honours the constraint.
        //
        // NOTE for the next step (01_PRODUCTION.md's BuildPositionResolver):
        // choosing among SEVERAL candidate tiles by Pylon power value needs a
        // finder that can return more than one, which the legacy
        // findStandardPosition does not expose. `PylonPlacementScore` is written
        // and tested for that step; wiring it here would mean inventing
        // candidates the finder never validated, which is worse than the single
        // validated tile we get today.
        HasPosition near = resolveNear(constraint);
        // Authoritative refusal attribution: the shared chain counts every
        // refused tile per term; cleared here so the breakdown belongs to
        // this search (a cached null skips the sweep and falls back to the
        // last status below).
        atlantis.production.constructions.position.PositionFulfillsAllConditions.clearRefusalBreakdown();
        APosition position = APositionFinder.findStandardPosition(builder, unitType, near, 12);
        if (position == null) {
            twinRefusal(unitType, topRefusal());
            return PlacementReservation.failure();
        }

        // Power-smart Pylon siting (Phase 5(a)): the legacy finder answers
        // one validated tile, but a Pylon belongs where it unlocks the most
        // unpowered room. Validated ring alternatives around it are scored
        // and the best wins - every candidate went through the same full
        // find chain, so nothing here is invented, only ranked. Other
        // buildings keep the first tile; with fewer than two distinct
        // candidates there is nothing to choose between.
        if (unitType.isPylon()) {
            APosition powered = bestPoweredSite(builder, unitType, position);
            if (powered != null) position = powered;
        }

        List<APosition> candidates = new ArrayList<>();
        candidates.add(position);
        int chosen = candidateResolver.choose(candidates, building);
        if (chosen < 0)
            return PlacementReservation.failure();
        position = candidates.get(chosen);

        String tile = position.tx() + ":" + position.ty();
        if (!reservedTilesThisPass.add(tile))
            return PlacementReservation.failure();

        PlacementReservation reservation = PlacementReservation.success(position.tx(), position.ty(), targetFrame);
        lastPassReservations.add(reservation);
        return reservation;
    }

    /**
     * Ring alternatives around the finder's first answer, each validated by
     * the same full chain, ranked by unlocked unpowered room. Frame cost is
     * one sweep per offset plus tile reads in a 7-tile box - no pathing;
     * V2 runs this only while its Pylon row is unbuilt, same cadence as
     * the single sweep it replaces. Watch the frame budget on the next
     * V2 LIVE run; the fallback is the first tile (delete the call).
     */
    private static final int[][] POWER_RING = {{4, 0}, {-4, 0}, {0, 4}, {0, -4}};

    private APosition bestPoweredSite(AUnit builder, AUnitType unitType, APosition first) {
        List<APosition> validated = new ArrayList<>();
        validated.add(first);
        for (int[] step : POWER_RING) {
            APosition alt = APositionFinder.findStandardPosition(
                builder, unitType, first.translateByTiles(step[0], step[1]), 12);
            if (alt == null || sameTile(alt, first) || containsTile(validated, alt)) continue;
            String tile = alt.tx() + ":" + alt.ty();
            if (reservedTilesThisPass.contains(tile)) continue;
            validated.add(alt);
        }
        if (validated.size() < 2) return first;

        List<PylonPlacementScore.Tile> buildable = new ArrayList<>();
        List<PylonPlacementScore.Tile> powered = new ArrayList<>();
        List<AUnit> pylons = Select.ourOfType(AUnitType.Protoss_Pylon).list();
        for (int dx = -PylonPlacementScore.POWER_RADIUS_TILES;
             dx <= PylonPlacementScore.POWER_RADIUS_TILES; dx++) {
            for (int dy = -PylonPlacementScore.POWER_RADIUS_TILES;
                 dy <= PylonPlacementScore.POWER_RADIUS_TILES; dy++) {
                APosition tile = first.translateByTiles(dx, dy);
                if (!tile.isBuildableIncludeBuildings()) continue;
                PylonPlacementScore.Tile scored =
                    new PylonPlacementScore.Tile(tile.tx(), tile.ty());
                buildable.add(scored);
                if (isPoweredBy(tile, pylons)) powered.add(scored);
            }
        }

        APosition best = PylonPowerSiting.best(validated, buildable, powered);
        return best != null ? best : first;
    }

    private static boolean sameTile(APosition a, APosition b) {
        return a.tx() == b.tx() && a.ty() == b.ty();
    }

    private static boolean containsTile(List<APosition> tiles, APosition tile) {
        for (APosition existing : tiles) {
            if (existing != null && sameTile(existing, tile)) return true;
        }
        return false;
    }

    private static boolean isPoweredBy(APosition tile, List<AUnit> pylons) {
        for (AUnit pylon : pylons) {
            if (pylon == null || !pylon.isAlive()) continue;
            if (Math.abs(pylon.tx() - tile.tx()) <= PylonPlacementScore.POWER_RADIUS_TILES
                && Math.abs(pylon.ty() - tile.ty()) <= PylonPlacementScore.POWER_RADIUS_TILES) return true;
        }
        return false;
    }
    /**
     * Reservation carried over from the previous pass for this item, already
     * marked as committed, or null when the item is new or was never started.
     */
    public PlacementReservation carriedOver(Producible building, int now) {
        AUnitType unitType = resolveUnitType(building);
        if (unitType == null) return null;

        for (Construction construction : ConstructionRequests.constructions) {
            if (construction.buildingType() != unitType || construction.buildPosition() == null) continue;
            return PlacementReservation
                    .success(construction.buildPosition().tx(), construction.buildPosition().ty()
                            + 0, now)
                    .committedAt(now);
        }
        return null;
    }

    private AUnit builderForThisPass() {
        if (passBuilder == null || !passBuilder.isAlive()) {
            // Owner: always the free pool, never a hand-rolled chain.
            passBuilder = FreeWorkers.get().first();
        }
        return passBuilder;
    }

    private AUnitType resolveUnitType(Producible producible) {
        if (producible instanceof UnitProducible) {
            return ((UnitProducible) producible).type();
        }
        return null;
    }

    private HasPosition resolveNear(TargetPlacement constraint) {
        switch (constraint.mode()) {
            case EXACT_TILE:
                return APosition.create(constraint.exactTileX() * 32, constraint.exactTileY() * 32);
            case NEIGHBOURHOOD:
                return APosition.create(constraint.tileX() * 32, constraint.tileY() * 32);
            case NAMED_AREA:
                return areaCentre(constraint.areaName());
            case ANYWHERE:
            default:
                // The legacy finder requires a centre to search around (it
                // asserts non-null), so "anywhere" means "around our base" -
                // which is what the legacy pipeline does too.
                AUnit base = Select.ourBases().first();
                return base != null ? base : Select.ourBuildings().first();
        }
    }

    /**
     * Build-order position modifiers, deliberately a small subset: MAIN and
     * NATURAL are what the shipped files use. Unknown names fall back to the
     * main base rather than failing the pass.
     */
    private HasPosition areaCentre(String areaName) {
        if ("NATURAL".equals(areaName)) {
            APosition natural = BaseLocations.natural();
            if (natural != null) return natural;
        }
        AUnit main = Select.main();
        if (main != null) return main;
        AUnit base = Select.ourBases().first();
        return base != null ? base : Select.ourBuildings().first();
    }
}
