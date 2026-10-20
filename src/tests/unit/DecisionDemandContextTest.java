package tests.unit;

import atlantis.decisions.board.ADecision;
import atlantis.decisions.board.DecisionLog;
import atlantis.production.requests.produce.ProduceBuilding;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BUILD rows must answer "why this building now" on their own: the order
 * line plus the demand-side state behind it (free supply, unfinished of
 * type, previous link, fresh-vs-adopt path) and the order id as a join
 * key into the EVENTS chain. Wine oid-72: 39 identical rows with no way
 * to tell a re-request after an unlink from a first request - the 40th
 * row must name its cause.
 */
@Tag("production")
public class DecisionDemandContextTest {

    private byte[] backup;
    private boolean hadFile;

    @BeforeEach
    public void backupAndClean() throws Exception {
        File out = new File(DecisionLog.OUTPUT_PATH);
        hadFile = out.exists();
        backup = hadFile ? Files.readAllBytes(out.toPath()) : null;
        DecisionLog.clearForTests();
    }

    @AfterEach
    public void restore() throws Exception {
        DecisionLog.clearForTests();
        File out = new File(DecisionLog.OUTPUT_PATH);
        if (hadFile) {
            Files.write(out.toPath(), backup);
        }
        else if (out.exists()) {
            out.delete();
        }
    }

    @Test
    public void contextSuffixNamesDemandState() {
        assertEquals(
            "At 30 Gateway* (READY_TO_PRODUCE)(#72) [free=16 unfinished=0 prev=link-null path=new]",
            ProduceBuilding.demandContext(
                "At 30 Gateway* (READY_TO_PRODUCE)(#72)", 16, 0, "link-null", "new"));
    }

    @Test
    public void contextSuffixNamesPreviousLinkAndAdopt() {
        assertEquals(
            "At 8 Pylon (READY_TO_PRODUCE)(#1) [free=1 unfinished=1 prev=cid68:PREPARING path=adopt]",
            ProduceBuilding.demandContext(
                "At 8 Pylon (READY_TO_PRODUCE)(#1)", 1, 1, "cid68:PREPARING", "adopt"));
    }

    @Test
    public void orderIdReachesTheFile() throws Exception {
        File out = new File(DecisionLog.OUTPUT_PATH);
        if (out.getParentFile() != null) out.getParentFile().mkdirs();

        DecisionLog.record(ADecision.decide(
                ADecision.Kind.BUILD, "Gateway", "At 30 Gateway* (READY_TO_PRODUCE)(#72)",
                ADecision.Source.DYNAMIC)
            .frame(6300).supply(33, 49).bank(104, 176).orderId(72)
            .build());

        String content = new String(Files.readAllBytes(out.toPath()), "UTF-8");
        assertTrue(content.contains("\"oid\":72"),
            "every BUILD row joins its EVENTS chain, no join by frame guessing");
    }

    @Test
    public void orderIdSurvivesRefineAndStamp() {
        ADecision refined = ADecision.decide(
                ADecision.Kind.BUILD, "building", "build-want", ADecision.Source.DYNAMIC)
            .frame(0).supply(12, 17).bank(200, 0)
            .build()
            .refine("Gateway", "At 12 Gateway (#3)", ADecision.Source.BUILD_ORDER, null)
            .atFrame(2500);

        // refine without an order carries none; the door stamps the real one.
        assertEquals(null, refined.orderId());

        ADecision stamped = ADecision.decide(
                ADecision.Kind.BUILD, "building", "build-want", ADecision.Source.DYNAMIC)
            .frame(0).supply(12, 17).bank(200, 0).orderId(72)
            .build()
            .refine("Gateway", "At 12 Gateway (#3)", ADecision.Source.BUILD_ORDER, null)
            .atFrame(2500);

        assertEquals(72, stamped.orderId());
    }
}
