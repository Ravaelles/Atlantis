package tests.unit;

import atlantis.production.constructions.PrepareConstructionSite;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Owner 2026-10-18: two sites sharing a tile are the same claim.
 * Measured: Gateway [95,111] picked on top of warping [92,111] (shared
 * column x=95 on 4x3 footprints). Touching edges stay legal - packed
 * bases rely on adjacency.
 */
@Tag("infra")
public class FootprintsOverlapTest {

    @Test
    public void sharedColumnOverlaps() {
        assertTrue(PrepareConstructionSite.footprintsOverlap(
            95, 111, 4, 3,
            92, 111, 4, 3));
    }

    @Test
    public void identicalSitesOverlap() {
        assertTrue(PrepareConstructionSite.footprintsOverlap(
            16, 46, 2, 2,
            16, 46, 2, 2));
    }

    @Test
    public void touchingEdgesStayLegal() {
        assertFalse(PrepareConstructionSite.footprintsOverlap(
            16, 46, 2, 2,
            18, 46, 2, 2),
            "adjacent Pylons share an edge, no tile - packed bases need this");
    }

    @Test
    public void distantSitesDoNotOverlap() {
        assertFalse(PrepareConstructionSite.footprintsOverlap(
            16, 46, 2, 2,
            90, 112, 2, 2));
    }
}
