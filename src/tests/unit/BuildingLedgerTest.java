package tests.unit;

import atlantis.game.util.BuildingLedger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The buildings ledger pins its JSON exactly: one row per warped building
 * with actual vs build-order supply. Engine-free by construction.
 */
public class BuildingLedgerTest {

    @AfterEach
    public void clearLedger() {
        BuildingLedger.clearForTests();
    }

    @Test
    public void twoRowsRenderAsJsonArray() {
        BuildingLedger.record(11, "Pylon", 640, "0:26", 8, 9, 100, 8, "At 8 Pylon READY_TO_PRODUCE(#1)");
        BuildingLedger.record(23, "Gateway", 6750, "4:40", 35, 41, 155, null, null);

        assertEquals(
            "[\n"
                + "  {\"type\":\"Pylon\",\"frame\":640,\"time\":\"0:26\","
                + "\"supplyUsed\":8,\"supplyTotal\":9,\"minerals\":100,"
                + "\"expectedSupply\":8,\"order\":\"At 8 Pylon READY_TO_PRODUCE(#1)\"},\n"
                + "  {\"type\":\"Gateway\",\"frame\":6750,\"time\":\"4:40\","
                + "\"supplyUsed\":35,\"supplyTotal\":41,\"minerals\":155,"
                + "\"expectedSupply\":null,\"order\":null}\n"
                + "]\n",
            BuildingLedger.toJson()
        );
    }

    @Test
    public void sameUnitRecordedOnceFirstWins() {
        BuildingLedger.record(11, "Pylon", 640, "0:26", 8, 9, 100, 8, "At 8 Pylon (#1)");
        BuildingLedger.record(11, "Pylon", 660, "0:27", 8, 9, 104, 8, "At 8 Pylon (#1)");

        assertEquals(1, countRows());
    }

    @Test
    public void frameGoingBackwardsStartsNewGame() {
        BuildingLedger.record(11, "Pylon", 640, "0:26", 8, 9, 100, 8, "At 8 Pylon (#1)");
        BuildingLedger.record(12, "Gateway", 5, "0:00", 5, 9, 50, 9, "At 9 Gateway (#2)");

        assertEquals(1, countRows());
        assertEquals(
            "[\n"
                + "  {\"type\":\"Gateway\",\"frame\":5,\"time\":\"0:00\","
                + "\"supplyUsed\":5,\"supplyTotal\":9,\"minerals\":50,"
                + "\"expectedSupply\":9,\"order\":\"At 9 Gateway (#2)\"}\n"
                + "]\n",
            BuildingLedger.toJson()
        );
    }

    @Test
    public void emptyLedgerIsEmptyArray() {
        assertEquals("[\n]\n", BuildingLedger.toJson());
    }

    private int countRows() {
        String json = BuildingLedger.toJson();
        int count = 0;
        int at = 0;
        while ((at = json.indexOf("\"type\":", at)) >= 0) {
            count++;
            at++;
        }
        return count;
    }
}
