package tests.unit;

import atlantis.cherryvis.simple.ACherryVis_UnitFirstSeen;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Direct our-id to engine-id pairs (NEXT #76): when {@code unit_ids} is
 * present the viewer trusts the pairs and skips its fuzzy match (which has no
 * retry, so one missed seed frame strands dozens of units as "i???" forever).
 *
 * <p>No CherryVis frontend is needed: the section is a pure function of noted
 * pairs, so the test pins the exact JSON the viewer parses.</p>
 */
@Tag("infra")
public class CherryVisUnitIdsTest {

    @Test
    public void pairsRenderAsTwoElementArrays() {
        ACherryVis_UnitFirstSeen.clear();
        ACherryVis_UnitFirstSeen.noteEngineId(246, 37);
        ACherryVis_UnitFirstSeen.noteEngineId(249, 102);

        assertEquals(
            "[246,37],[249,102]",
            ACherryVis_UnitFirstSeen.engineIdsJson()
        );
    }

    @Test
    public void clearEmptiesThePairs() {
        ACherryVis_UnitFirstSeen.clear();
        ACherryVis_UnitFirstSeen.noteEngineId(246, 37);
        ACherryVis_UnitFirstSeen.clear();

        assertEquals("", ACherryVis_UnitFirstSeen.engineIdsJson());
    }
}
