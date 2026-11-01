package tests.unit;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * NEXT #235 (owner live game 2026-11-01 vs Steamhammer, TauCross):
 * a Gateway held builder #27 from f1917 and never warped while a
 * Pylon took the same builder at f3279 - neither construction ever
 * built, the #Move order never became #Build, both rows kept the
 * worker in their builder field. Assigning a builder to a new row
 * must unlink the old row: exclusive ownership, enforced here, not
 * just intended (the free-pool comment already forbids double-linking).
 */
@Tag("production")
public class BuilderHandoverUnlinksOldConstructionTest {

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
    }

    @Test
    public void takingABusyBuilderReleasesItsOldConstruction() {
        FakeUnit mason = fake(AUnitType.Protoss_Probe, 10, 10);
        Construction first = new Construction(AUnitType.Protoss_Gateway);
        Construction second = new Construction(AUnitType.Protoss_Pylon);
        ConstructionRequests.addConstruction(first);
        ConstructionRequests.addConstruction(second);

        try {
            first.setBuilder(mason);
            assertEquals(first, ConstructionRequests.constructionFor(mason),
                "sanity: the first row owns the builder");

            second.setBuilder(mason);

            assertNull(first.builder(),
                "the old row must release the taken builder instead of "
                    + "keeping a stale link two rows drive forever");
            assertEquals(second, ConstructionRequests.constructionFor(mason),
                "every reader must agree on the new owner");
        }
        finally {
            ConstructionRequests.constructions.clear();
        }
    }
}
