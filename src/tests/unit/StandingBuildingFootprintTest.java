package tests.unit;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.BaseSelect;
import bwapi.TilePosition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Standing-building coverage reads top-left tiles on both sides.
 *
 * <p>A live building reports its CENTER tile ({@code tx()/ty()} read engine
 * pixels), while a construction site is top-left. Comparing the center as
 * if it were top-left shifts even footprints by a tile and an adjacent
 * site reads as covered - measured live: Pylon [95,121] cancelled 256x
 * in a row against standing [93,121] (center tile [94,122]). The mock
 * below is a live-shaped unit (center tx/ty, top-left tilePosition);
 * plain fakes cannot express the split.</p>
 */
@Tag("production")
public class StandingBuildingFootprintTest extends AbstractTestWithUnits {

    private MockedStatic<BaseSelect> baseSelect;

    @BeforeEach
    public void mockSelection() {
        baseSelect = Mockito.mockStatic(BaseSelect.class);
    }

    @AfterEach
    public void unmockSelection() {
        baseSelect.close();
    }

    /** A live-shaped standing building: center tile in tx()/ty(), top-left in tilePosition(). */
    private static AUnit liveBuilding(AUnitType type, int centerTx, int centerTy, int left, int top) {
        AUnit unit = Mockito.mock(AUnit.class);
        when(unit.isAlive()).thenReturn(true);
        when(unit.isABuilding()).thenReturn(true);
        when(unit.isCompleted()).thenReturn(true);
        when(unit.type()).thenReturn(type);
        when(unit.tx()).thenReturn(centerTx);
        when(unit.ty()).thenReturn(centerTy);
        when(unit.tilePosition()).thenReturn(new TilePosition(left, top));

        return unit;
    }

    private void seeAsStanding(AUnit unit) {
        baseSelect.when(BaseSelect::allUnits).thenAnswer(
            invocation -> Collections.singletonList(unit));
    }

    @Test
    public void adjacentSiteOfLivePylonIsNotCovered() {
        // Live Pylon standing at top-left [93,121]: center tile (94,122).
        // The adjacent [95,121] shares no tile - packed base, not coverage.
        seeAsStanding(liveBuilding(AUnitType.Protoss_Pylon, 94, 122, 93, 121));

        assertFalse(
            atlantis.units.BuildingTilesAreOccupied.checkBuildingsOnly(
                atlantis.map.position.APosition.create(95, 121), AUnitType.Protoss_Pylon),
            "adjacent site is legal - the engine would warp there");
    }

    @Test
    public void sameTileStillCovered() {
        // Same live Pylon, ordered tile identical: genuinely covered.
        seeAsStanding(liveBuilding(AUnitType.Protoss_Pylon, 94, 122, 93, 121));

        assertTrue(
            atlantis.units.BuildingTilesAreOccupied.checkBuildingsOnly(
                atlantis.map.position.APosition.create(93, 121), AUnitType.Protoss_Pylon),
            "same tile is covered - the guard still fires where it must");
    }
}
