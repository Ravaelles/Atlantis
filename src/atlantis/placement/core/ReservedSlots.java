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
     * Pylon slots, numbered P1... Opening capacity plus buffer;
     * tune with live proof.
     */
    public static final int PYLON_SLOTS = 6;

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
    public static void planAtGameStart() {
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
                AUnitType.Protoss_Pylon, PYLON_SLOTS,
                AUnitType.Protoss_Gateway, GATEWAY_SLOTS);

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
     * Ranked, non-overlapping slots of two footprint families around a
     * point, nearest first (stable order: distance, then tile). Pure
     * over the catalogue. Public so the layout stays pinnable from
     * outside the package.
     */
    public static List<Slot> planOn(BuildLocationCatalogue catalogue, APosition main,
                             AUnitType first, int firstCount,
                             AUnitType second, int secondCount) {
        List<Slot> out = new ArrayList<Slot>();
        List<Rect> taken = new ArrayList<Rect>();

        takeSlots(catalogue, main, first, firstCount, "P", out, taken);
        takeSlots(catalogue, main, second, secondCount, "G", out, taken);

        return out;
    }

    private static void takeSlots(BuildLocationCatalogue catalogue,
                                  APosition main, AUnitType type, int count, String prefix,
                                  List<Slot> out, List<Rect> taken) {
        List<BuildLocation> candidates =
            catalogue.candidates(type.getTilesWidth(), type.getTilesHeights(), null);
        sortByDistanceToMain(candidates, main);

        int n = 1;
        for (BuildLocation candidate : candidates) {
            if (n > count) break;

            Rect rect = new Rect(
                candidate.tileX(), candidate.tileY(),
                candidate.tileWidth(), candidate.tileHeight());
            if (overlapsTaken(rect, taken)) continue;

            out.add(new Slot(prefix + n, type, APosition.create(candidate.tileX(), candidate.tileY())));
            taken.add(rect);
            n++;
        }
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
