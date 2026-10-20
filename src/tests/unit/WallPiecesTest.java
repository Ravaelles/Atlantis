package tests.unit;

import atlantis.map.analysis.WallKit;
import atlantis.map.analysis.WallPieces;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * M4 planner slice: wall pieces are fit-checked demand from an anchor row -
 * pure slots in, typed orders out. A 7-wide choke takes Pylon + Gateway +
 * door; a 9-wide needs one cannon; an 11-wide with one cannon allowed
 * refuses (cannons-as-spots instead, never a gappy wall).
 */
@Tag("placement")
public class WallPiecesTest {

    private static List<APosition> row(int x0, int y, int n) {
        List<APosition> slots = new ArrayList<>();
        for (int i = 0; i < n; i++) slots.add(APosition.create(x0 + i, y));
        return slots;
    }

    @Test
    public void narrowChokeTakesPylonPlusGateway() {
        List<WallPieces.Piece> pieces = WallPieces.plan(
            row(10, 20, 8), APosition.create(13, 20), APosition.create(10, 30), 7, 2, WallKit.PROTOSS);

        assertEquals(2, pieces.size());
        assertEquals(AUnitType.Protoss_Pylon, pieces.get(0).type());
        assertEquals(AUnitType.Protoss_Gateway, pieces.get(1).type());
        for (WallPieces.Piece piece : pieces) {
            assertTrue(!(piece.tile().tx() == 13 && piece.tile().ty() == 20), "door never takes a piece");
        }
    }

    @Test
    public void widerChokeSpendsACannon() {
        List<WallPieces.Piece> pieces = WallPieces.plan(
            row(10, 20, 10), APosition.create(14, 20), APosition.create(10, 30), 9, 2, WallKit.PROTOSS);

        assertEquals(3, pieces.size());
        assertEquals(AUnitType.Protoss_Photon_Cannon, pieces.get(2).type());
    }

    @Test
    public void tooWideChokeRefuses() {
        List<WallPieces.Piece> pieces = WallPieces.plan(
            row(10, 20, 12), APosition.create(15, 20), APosition.create(10, 30), 11, 1, WallKit.PROTOSS);

        assertEquals(0, pieces.size());
    }

    @Test
    public void pylonSitsBaseSide() {
        // Row given choke-side first: the planner orients it base-side first.
        List<APosition> slots = row(10, 20, 8);
        java.util.Collections.reverse(slots);

        List<WallPieces.Piece> pieces = WallPieces.plan(
            slots, APosition.create(13, 20), APosition.create(10, 30), 7, 2, WallKit.PROTOSS);

        assertEquals(10, pieces.get(0).tile().tx());
    }

    @Test
    public void kitDecidesWidths() {
        // Same choke, narrower plug piece: a 7-wide choke now needs a gun.
        WallKit narrow = new WallKit(
            AUnitType.Protoss_Pylon, 2,
            AUnitType.Protoss_Gateway, 2,
            AUnitType.Protoss_Photon_Cannon, 2);

        assertEquals(0, WallPieces.gunsNeeded(7, WallKit.PROTOSS));
        assertEquals(1, WallPieces.gunsNeeded(7, narrow));

        List<WallPieces.Piece> pieces = WallPieces.plan(
            row(10, 20, 8), APosition.create(13, 20), APosition.create(10, 30), 7, 2, narrow);

        assertEquals(3, pieces.size());
        assertEquals(AUnitType.Protoss_Photon_Cannon, pieces.get(2).type());
    }

    @Test
    public void cannonsNeededMath() {
        assertEquals(0, WallPieces.gunsNeeded(7, WallKit.PROTOSS));
        assertEquals(0, WallPieces.gunsNeeded(6, WallKit.PROTOSS));
        assertEquals(1, WallPieces.gunsNeeded(9, WallKit.PROTOSS));
        assertEquals(2, WallPieces.gunsNeeded(11, WallKit.PROTOSS));
    }
}
