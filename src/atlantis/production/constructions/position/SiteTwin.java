package atlantis.production.constructions.position;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.production.v2.PlacementReservation;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

import java.util.function.Supplier;

/**
 * The {@code CATALOGUE_SITE} twin: every catalogue answer, decline and
 * perf sample for both funnel entries - plus the counters behind the perf
 * rows. One class so the row shape stays identical wherever it is written:
 * grepping thousands of near-identical lines stays filtering one key
 * (the {@code LogKey} rule).
 */
final class SiteTwin {

    private static long reserveNanosTotal = 0;
    private static int reserveCalls = 0;
    private static int rebuilds = 0;
    private static int lastPerfSample = -1;
    private SiteTwin() {
    }

    /**
     * Times one catalogue proposal for the perf rows.
     */
    static PlacementReservation timed(Supplier<PlacementReservation> proposal) {
        long start = System.nanoTime();
        try {
            return proposal.get();
        } finally {
            reserveNanosTotal += System.nanoTime() - start;
            reserveCalls++;
            samplePerf(AGame.now());
        }
    }

    static void addRebuild() {
        rebuilds++;
    }

    private static void samplePerf(int frame) {
        int bucket = frame / 720;
        if (bucket == lastPerfSample) return;

        lastPerfSample = bucket;
        if (reserveCalls == 0) return;

        EventLog.key(LogKey.CATALOGUE_SITE)
            .param("type", "-")
            .param("result", "perf")
            .param("calls", reserveCalls)
            .param("avgMicros", (int) (reserveNanosTotal / 1000 / reserveCalls))
            .param("rebuilds", rebuilds)
            .log(frame, A.minSec());
    }

    static void answered(AUnitType building, APosition site, String via,
                         PlacementReservation reservation,
                         atlantis.production.v2.TargetPlacement constraint) {
        if (site == null) return;

        EventLog.Builder row = EventLog.key(LogKey.CATALOGUE_SITE)
            .param("type", building.name())
            .param("result", "answered")
            .param("tx", site.tx())
            .param("ty", site.ty())
            .param("nearTx", constraint == null ? -1 : constraint.nearTileX())
            .param("nearTy", constraint == null ? -1 : constraint.nearTileY())
            .param("via", via);
        if (reservation != null && reservation.coversChoke() != null) {
            row.param("coversChoke", reservation.coversChoke());
            row.param("coversAnchor", reservation.coversAnchor());
        }
        row.log(AGame.now(), A.minSec());
    }

    static void declined(AUnitType building, String reason, String via) {
        EventLog.key(LogKey.CATALOGUE_SITE)
            .param("type", building == null ? "-" : building.name())
            .param("result", "declined")
            .param("reason", reason)
            .param("via", via)
            .log(AGame.now(), A.minSec());
    }
}
