package tests.unit;

import atlantis.game.util.BuildingLedger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The buildings ledger pins its JSON exactly: one row per warped building
 * with actual vs build-order supply. Engine-free by construction.
 */
@Tag("production")
public class BuildingLedgerTest {

    @AfterEach
    public void clearLedger() {
        BuildingLedger.clearForTests();
    }

    @Test
    public void twoRowsRenderAsJsonArray() {
        BuildingLedger.recordBuilding(11, "Pylon", 16, 46, 640, "0:26", 8, 9, 100, 0, 0, 0,
            8, "At 8 Pylon READY_TO_PRODUCE(#1)", 7, 1);
        BuildingLedger.recordUnit(23, "Zealot", 20, 20, 6750, "4:40", 35, 41, 155, 80, 300, 0,
            12, "At 12 Zealot (#4)", null, 4);

        assertEquals(
            "[\n"
                + "  {\"kind\":\"building\",\"type\":\"Pylon\",\"tile\":[16,46],\"frame\":640,\"time\":\"0:26\","
                + "\"supplyUsed\":8,\"supplyTotal\":9,\"minerals\":100,\"gas\":0,"
                + "\"reservedMinerals\":0,\"reservedGas\":0,"
                + "\"expectedSupply\":8,\"order\":\"At 8 Pylon READY_TO_PRODUCE(#1)\","
                + "\"construction\":7,\"orderId\":1},\n"
                + "  {\"kind\":\"unit\",\"type\":\"Zealot\",\"tile\":[20,20],\"frame\":6750,\"time\":\"4:40\","
                + "\"supplyUsed\":35,\"supplyTotal\":41,\"minerals\":155,\"gas\":80,"
                + "\"reservedMinerals\":300,\"reservedGas\":0,"
                + "\"expectedSupply\":12,\"order\":\"At 12 Zealot (#4)\","
                + "\"construction\":null,\"orderId\":4}\n"
                + "]\n",
            BuildingLedger.toJson()
        );
    }

    @Test
    public void sameUnitRecordedOnceFirstWins() {
        BuildingLedger.recordBuilding(11, "Pylon", 16, 46, 640, "0:26", 8, 9, 100, 0, 0, 0,
            8, "At 8 Pylon (#1)", 7, 1);
        BuildingLedger.recordBuilding(11, "Pylon", 16, 46, 660, "0:27", 8, 9, 104, 0, 0, 0,
            8, "At 8 Pylon (#1)", 7, 1);

        assertEquals(1, countRows());
    }

    @Test
    public void frameGoingBackwardsStartsNewGame() {
        BuildingLedger.recordBuilding(11, "Pylon", 16, 46, 640, "0:26", 8, 9, 100, 0, 0, 0,
            8, "At 8 Pylon (#1)", 7, 1);
        BuildingLedger.recordBuilding(12, "Gateway", 17, 41, 5, "0:00", 5, 9, 50, 0, 0, 0,
            9, "At 9 Gateway (#2)", 8, 2);

        assertEquals(1, countRows());
        assertEquals(
            "[\n"
                + "  {\"kind\":\"building\",\"type\":\"Gateway\",\"tile\":[17,41],\"frame\":5,\"time\":\"0:00\","
                + "\"supplyUsed\":5,\"supplyTotal\":9,\"minerals\":50,\"gas\":0,"
                + "\"reservedMinerals\":0,\"reservedGas\":0,"
                + "\"expectedSupply\":9,\"order\":\"At 9 Gateway (#2)\","
                + "\"construction\":8,\"orderId\":2}\n"
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
