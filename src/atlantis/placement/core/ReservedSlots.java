package atlantis.placement.core;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Pre-planned building slots (owner order 2026-11-04, Stardust
 * doctrine): instead of hunting a site every frame, the game picks
 * slots for pylons and 10 gateways once at game start, near the main,
 * numbered for the painter (G1..G10). Pure geometry over the
 * catalogue the production planner itself uses - power is deliberately
 * NOT consulted here (a slot is ground, power arrives with pylons;
 * the single mint-time verdict still enforces it).
 *
 * <p>Display first: nothing mints from these slots yet, so a weird
 * layout cannot break a game - it only draws. Wiring issuance to the
 * slots is the follow-up slice, with live proof.</p>
 */
public final class ReservedSlots {

    /**
     * Gateway slots, numbered G1..G10 (owner order: ten gateways).
     */
    public static final int GATEWAY_SLOTS = 10;


    /**
     * Power cover in build tiles (Chebyshev on origins): a Gateway
     * slot counts as covered when a Pylon slot sits this close. The
     * engine applies 6 tiles from the Pylon's own tiles +1
     * ({@code EnginePowerSource#PYLON_RADIUS_TILES}); one tile of
     * margin is kept for the footprints (Gateway 4x3 vs Pylon 2x2
     * shift the centers), so a covered slot is powered in the worst
     * measured geometry, not just the best.
     */
    static final int POWER_COVER_TILES = 6;

    public static final class Slot {
        public final String label;
        public final AUnitType type;
        public final APosition tile;

        Slot(String label, AUnitType type, APosition tile) {
            this.label = label;
            this.type = type;
            this.tile = tile;
        }
    }

    private static List<Slot> slots = new ArrayList<Slot>();

    private ReservedSlots() {
    }

    /**
     * Live entry: plan once at game start around our main. Never throws
     * (a failed layout must not break game start) and never touches
     * production state (the mint door still owns issuance).
     */
    public static void planAtGameStart(
        java.util.function.BiPredicate<APosition, AUnitType> occupied) {
        try {
            atlantis.units.AUnit main = atlantis.units.select.Select.main();
            if (main == null) main = atlantis.units.select.Select.ourBases().first();
            if (main == null || main.position() == null) return;

            atlantis.placement.core.TileAvailabilityGrid grid =
                new atlantis.placement.core.TileAvailabilityGrid(
                    new atlantis.placement.engine.EngineTerrainSource());
            BuildLocationCatalogue catalogue = new BuildLocationCatalogue(grid);
            slots = planOn(
                catalogue, main.position(),
                AUnitType.Protoss_Gateway, GATEWAY_SLOTS);
            slots = pruneCoveredSlots(slots, occupied);
            nameSlotsInDistanceOrder(slots, main.position());

            if (!slots.isEmpty()) {
                atlantis.util.AConsole.println(atlantis.util.log.LogLevel.LOW,
                    "Reserved slots: " + slots.size() + " (" + slots.get(0).label
                        + " at " + slots.get(0).tile + " .. "
                        + slots.get(slots.size() - 1).label
                        + " at " + slots.get(slots.size() - 1).tile + ")");
            }
        } catch (Exception | Error ignored) {
        }
    }

    /**
     * The current layout (empty until planned). Read-only view for the
     * painter; production never reads this (display-only slice).
     */
    public static List<Slot> slots() {
        return Collections.unmodifiableList(slots);
    }

    public static void clearForTests() {
        slots = new ArrayList<Slot>();
    }

    /**
     * Test seam: inject a layout computed by {@link #planOn} (pure) so
     * issuance tests pin slot order without a live game start.
     */
    public static void setSlotsForTests(List<Slot> layout) {
        slots = layout == null ? new ArrayList<Slot>() : new ArrayList<Slot>(layout);
    }

    /**
     * The first slot of this type no live building covers, oldest
     * planned first. Occupancy answers the caller (footprint overlap,
     * standing and unfinished alike), so this stays free of engine
     * imports and the single door can share it with the funnel.
     * Never throws: a failed occupancy read skips the slot, a failed
     * pass reads as no slot - the hunt below owns the frame either way.
     */
    public static APosition firstFreeSlotOfType(
        AUnitType type, java.util.function.BiPredicate<APosition, AUnitType> occupied
    ) {
        if (type == null || occupied == null) return null;
        try {
            for (Slot slot : slots) {
                if (slot == null || slot.tile == null || slot.type == null) continue;
                if (!slot.type.equals(type)) continue;

                boolean busy;
                try {
                    busy = occupied.test(slot.tile, type);
                } catch (Exception | Error ignored) {
                    continue;
                }
                if (!busy) return slot.tile;
            }
        } catch (Exception | Error ignored) {
        }

        return null;
    }

    /**
     * Ranked, non-overlapping slots of two footprint families around a
     * point, nearest first (stable order: distance, then tile). Pure
     * over the catalogue. Public so the layout stays pinnable from
     * outside the package.
     *
     * <p>Stardust order: power is part of the layout, not a follow-up.
     * Pylons are planned TO the Gateways (each Gateway gets a covering
     * Pylon), not beside them: planning the families apart drifts them
     * out of power reach (measured TauCross: 2 of 10 Gateways covered
     * by 6 near-main Pylons). Labels stay distance-ordered (G1 nearest
     * the main), so the painter contract does not move.
     */
    public static List<Slot> planOn(BuildLocationCatalogue catalogue, APosition main,
                             AUnitType gatewayType, int gatewayCount) {
        List<Slot> out = new ArrayList<Slot>();
        List<Rect> taken = new ArrayList<Rect>();

        takeGatewaySlotsWithPower(catalogue, main, gatewayType, gatewayCount, out, taken);
        nameSlotsInDistanceOrder(out, main);
        return out;
    }

    private static void takeGatewaySlotsWithPower(BuildLocationCatalogue catalogue,
        APosition main, AUnitType gatewayType, int gatewayCount,
        List<Slot> out, List<Rect> taken) {
        List<BuildLocation> candidates =
            catalogue.candidates(gatewayType.getTilesWidth(), gatewayType.getTilesHeights(), null);
        sortByDistanceToMain(candidates, main);

        List<BuildLocation> pylonCandidates =
            catalogue.candidates(
                AUnitType.Protoss_Pylon.getTilesWidth(), AUnitType.Protoss_Pylon.getTilesHeights(), null);
        int gateways = 0;
        for (BuildLocation candidate : candidates) {
            if (gateways >= gatewayCount) break;

            // No edge slots, ever (owner order: Pylon warped ON the map
            // edge on TauCross while the funnel declines edge tiles -
            // the layout is the first issuer, so it owns the same rule.
            // Same 4-tile footprint-aware numbers as the verdict; layout
            // is planned once at game start, no emergency exemption).
            if (isEdgeTile(candidate.tileX(), candidate.tileY(),
                candidate.tileWidth(), candidate.tileHeight())) continue;

            Rect rect = new Rect(
                candidate.tileX(), candidate.tileY(),
                candidate.tileWidth(), candidate.tileHeight());
            if (overlapsTaken(rect, taken)) continue;

            APosition cover = coveringPylonSlot(
                candidate.tileX(), candidate.tileY(), pylonCandidates, taken, rect);
            if (cover == null) continue;

            taken.add(new Rect(cover.tx(), cover.ty(),
                AUnitType.Protoss_Pylon.getTilesWidth(),
                AUnitType.Protoss_Pylon.getTilesHeights()));
            out.add(new Slot("P", AUnitType.Protoss_Pylon, cover));
            out.add(new Slot("G", gatewayType, APosition.create(candidate.tileX(), candidate.tileY())));
            taken.add(rect);
            gateways++;
        }
    }

    /**
     * A free Pylon origin within power reach of a Gateway origin:
     * nearest to the Gateway (stable tie-break), never overlapping
     * an already taken rect. Null when no free Pylon tile reaches.
     */
    private static APosition coveringPylonSlot(int gx, int gy,
        List<BuildLocation> pylonCandidates, List<Rect> taken, Rect gatewayRect) {
        APosition best = null;
        int bestDist = Integer.MAX_VALUE;
        for (BuildLocation candidate : pylonCandidates) {
            if (Math.max(Math.abs(candidate.tileX() - gx), Math.abs(candidate.tileY() - gy))
                > POWER_COVER_TILES) continue;

            if (isEdgeTile(candidate.tileX(), candidate.tileY(),
                candidate.tileWidth(), candidate.tileHeight())) continue;

            Rect rect = new Rect(
                candidate.tileX(), candidate.tileY(),
                candidate.tileWidth(), candidate.tileHeight());
            if (overlapsTaken(rect, taken)) continue;
            if (gatewayRect != null && rect.overlaps(gatewayRect)) continue;

            int dist = Math.abs(candidate.tileX() - gx) + Math.abs(candidate.tileY() - gy);
            if (dist < bestDist
                || (dist == bestDist && (best == null
                || candidate.tileX() != best.tx()
                ? candidate.tileX() < (best == null ? Integer.MAX_VALUE : best.tx())
                : candidate.tileY() < best.ty()))) {
                bestDist = dist;
                best = APosition.create(candidate.tileX(), candidate.tileY());
            }
        }
        return best;
    }

    /**
     * Labels in distance order: Pylons then Gateways, nearest the main
     * first (P1..Pn, G1..Gn). The plan order above interleaves pairs;
     * the painter contract (G1 nearest) is restored here.
     */
    private static void nameSlotsInDistanceOrder(List<Slot> out, final APosition main) {
        List<Slot> pylons = new ArrayList<Slot>();
        List<Slot> gateways = new ArrayList<Slot>();
        for (Slot slot : out) {
            if (slot.type.isPylon()) pylons.add(slot);
            else gateways.add(slot);
        }
        sortSlotsByDistanceToMain(pylons, main);
        sortSlotsByDistanceToMain(gateways, main);

        out.clear();
        int n = 1;
        for (Slot slot : pylons) out.add(new Slot("P" + n++, slot.type, slot.tile));
        n = 1;
        for (Slot slot : gateways) out.add(new Slot("G" + n++, slot.type, slot.tile));
    }

    /**
     * Whether a Gateway origin sits within power reach of given Pylon
     * origins (Chebyshev, {@link #POWER_COVER_TILES}). Test seam: pins
     * the layout doctrine without a game.
     */
    public static boolean coveredByPylonSlots(int tx, int ty, java.util.List<APosition> pylons) {
        if (pylons == null || pylons.isEmpty()) return false;
        for (APosition pylon : pylons) {
            if (pylon == null) continue;
            if (Math.max(Math.abs(tx - pylon.tx()), Math.abs(ty - pylon.ty()))
                <= POWER_COVER_TILES) {
                return true;
            }
        }
        return false;
    }

    /**
     * The free Pylon slot covering a Gateway slot: what the support
     * flow orders when the Gateway slot declines unpowered (a tile a
     * siting Pylon can actually land on, unlike a bare Gateway tile
     * far from any power). Standing Pylons hold their slot; rows in
     * flight hold theirs too (their power is coming, ordering past
     * them doubles Pylons). Null when no free Pylon slot reaches the
     * Gateway slot.
     */
    public static APosition supportPylonSlotFor(APosition gatewaySlot, AUnitType pylonType,
        java.util.function.BiPredicate<APosition, AUnitType> occupied) {
        if (gatewaySlot == null || pylonType == null || occupied == null) return null;
        try {
            APosition best = null;
            int bestDist = Integer.MAX_VALUE;
            for (Slot slot : slots) {
                if (slot == null || slot.tile == null || slot.type == null) continue;
                if (!slot.type.equals(pylonType)) continue;
                if (Math.max(Math.abs(slot.tile.tx() - gatewaySlot.tx()),
                    Math.abs(slot.tile.ty() - gatewaySlot.ty())) > POWER_COVER_TILES) continue;

                boolean busy;
                try {
                    busy = occupied.test(slot.tile, pylonType);
                } catch (Exception | Error ignored) {
                    continue;
                }
                if (busy) continue;

                int dist = Math.abs(slot.tile.tx() - gatewaySlot.tx())
                    + Math.abs(slot.tile.ty() - gatewaySlot.ty());
                if (dist < bestDist) {
                    bestDist = dist;
                    best = slot.tile;
                }
            }

            return best;
        } catch (Exception | Error ignored) {
            return null;
        }
    }

    /**
     * Drops slots a standing building already covers (the live main
     * always sits on its base tile, so the nearest planned slot can
     * be born dead). Pure over the given list plus the caller-owned
     * occupancy predicate, so placement stays free of engine imports.
     * Test seam: pinned with a scripted predicate, no game.
     */
    public static java.util.List<Slot> pruneCoveredSlots(java.util.List<Slot> layout,
        java.util.function.BiPredicate<APosition, AUnitType> occupied) {
        java.util.List<Slot> out = new java.util.ArrayList<Slot>();
        if (layout == null) return out;
        for (Slot slot : layout) {
            if (slot == null || slot.tile == null || slot.type == null) continue;
            boolean busy = false;
            if (occupied != null) {
                try {
                    busy = occupied.test(slot.tile, slot.type);
                } catch (Exception | Error ignored) {
                    continue;
                }
            }
            if (!busy) out.add(slot);
        }
        return out;
    }

    private static void sortSlotsByDistanceToMain(List<Slot> slots, final APosition main) {
        Collections.sort(slots, new Comparator<Slot>() {
            @Override
            public int compare(Slot a, Slot b) {
                double da = distSq(a.tile.tx(), a.tile.ty(), main);
                double db = distSq(b.tile.tx(), b.tile.ty(), main);
                if (da != db) return da < db ? -1 : 1;
                if (a.tile.tx() != b.tile.tx()) return a.tile.tx() - b.tile.tx();

                return a.tile.ty() - b.tile.ty();
            }
        });
    }

    private static void sortByDistanceToMain(List<BuildLocation> candidates, final APosition main) {
        Collections.sort(candidates, new Comparator<BuildLocation>() {
            @Override
            public int compare(BuildLocation a, BuildLocation b) {
                double da = distSq(a.tileX(), a.tileY(), main);
                double db = distSq(b.tileX(), b.tileY(), main);
                if (da != db) return da < db ? -1 : 1;
                if (a.tileX() != b.tileX()) return a.tileX() - b.tileX();

                return a.tileY() - b.tileY();
            }
        });
    }

    private static double distSq(int tx, int ty, HasPosition main) {
        double dx = tx - main.tx();
        double dy = ty - main.ty();

        return dx * dx + dy * dy;
    }

    private static boolean overlapsTaken(Rect rect, List<Rect> taken) {
        for (Rect other : taken) {
            if (rect.overlaps(other)) return true;
        }

        return false;
    }

    /**
     * The layout's copy of the map-edge rule (same 4-tile
     * footprint-aware numbers as the single verdict): blocks never
     * sit on an edge or corner, so no slot is born illegal. Local
     * on purpose - placement core must not reach into the production
     * verdict package (frozen architecture boundary). No emergency
     * exemption here: the layout is planned once at game start, the
     * exemption is a per-pass runtime call.
     */
    private static final int EDGE_MARGIN_TILES = 4;

    private static boolean isEdgeTile(int tx, int ty, int w, int h) {
        try {
            int mapW = atlantis.map.AMap.getMapWidthInTiles();
            int mapH = atlantis.map.AMap.getMapHeightInTiles();
            if (mapW <= 0 || mapH <= 0) return false;

            if (tx < EDGE_MARGIN_TILES || ty < EDGE_MARGIN_TILES) return true;

            return tx + w > mapW - EDGE_MARGIN_TILES || ty + h > mapH - EDGE_MARGIN_TILES;
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    private static final class Rect {
        final int x;
        final int y;
        final int w;
        final int h;

        Rect(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        boolean overlaps(Rect other) {
            return x < other.x + other.w && other.x < x + w
                && y < other.y + other.h && other.y < y + h;
        }
    }
}
