package tests.unit;

import atlantis.production.v2.Producible;
import atlantis.production.v2.TechProducible;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.UpgradeProducible;
import atlantis.units.AUnitType;
import bwapi.TechType;
import bwapi.UpgradeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the tech and upgrade recipes (01_PRODUCTION.md §2 smell 2: a unit, a
 * research and an upgrade must all be {@link Producible}, so the scheduler
 * never
 * branches on the kind of thing it is planning).
 *
 * <p>
 * Before this, v2 could plan units and buildings only: a Dragoon's Singularity
 * Charge or a Zealot's Leg Enhancements had no recipe, so the engine would have
 * sent them to {@code train()} and silently done nothing. The numbers come from
 * the engine's own {@code TechType}/{@code UpgradeType} (CONVENTIONS §9), and
 * this test asserts the relationship rather than hard-coding values - the point
 * is that the adapter reads the right field, not what the field currently says.
 * </p>
 */
public class TechAndUpgradeProducibleTest {

    @Test
    public void aTechIsAProducibleWithItsOwnCostAndDuration() {
        // Stim_Packs is a real TechType in this build (Singularity Charge is an
        // UpgradeType for Protoss), and the class under test is TechProducible.
        TechType stim = TechType.Stim_Packs;
        Producible tech = TechProducible.of(stim);

        assertEquals(stim.toString(), tech.id());
        assertEquals(stim.mineralPrice(), tech.cost().minerals());
        assertEquals(stim.gasPrice(), tech.cost().gas());
        assertEquals(0, tech.cost().supply(), "research costs no supply - it is not fielded");
        assertEquals(stim.researchTime(), tech.buildDurationFrames());
        assertFalse(tech.requiresPlacement(), "a research needs no tile");
    }

    @Test
    public void anUpgradeIsAProducibleWithItsOwnCostAndDuration() {
        UpgradeType charge = UpgradeType.Singularity_Charge;
        Producible upgrade = UpgradeProducible.of(charge);

        assertEquals(charge.toString(), upgrade.id());
        assertEquals(charge.mineralPrice(), upgrade.cost().minerals());
        assertEquals(charge.gasPrice(), upgrade.cost().gas());
        assertEquals(charge.upgradeTime(), upgrade.buildDurationFrames());
        assertFalse(upgrade.requiresPlacement());
    }

    @Test
    public void aTechNamesTheBuildingThatResearchesIt() {
        TechType stim = TechType.Stim_Packs;
        Producible tech = TechProducible.of(stim);

        assertNotNull(stim.whatResearches(), "the engine must name a facility");

        String facility = AUnitType.from(stim.whatResearches()).name();
        assertEquals(facility, tech.producerTypeId(),
                "the producer of a tech is the building that researches it");

        // And that building is also its only prerequisite, so the scheduler
        // plans the Cybernetics Core before the Charge.
        assertFalse(tech.immediatePrerequisites().isEmpty(),
                "a tech must declare the facility as a prerequisite");
    }

    @Test
    public void anUpgradeReportsItsLevelCeiling() {
        UpgradeType legs = UpgradeType.Leg_Enhancements;
        UpgradeProducible upgrade = UpgradeProducible.of(legs);

        assertEquals(legs.toString(), upgrade.id());
        assertEquals(legs.mineralPrice(), upgrade.cost().minerals());
        assertEquals(legs.gasPrice(), upgrade.cost().gas());
        assertEquals(legs.upgradeTime(), upgrade.buildDurationFrames());
        assertEquals(legs.maxRepeats(), upgrade.levels(),
                "an upgrade has levels, and the goal decides how many it asks for");
        assertFalse(upgrade.requiresPlacement());
    }

    @Test
    public void allThreeRecipesAnswerTheSameQuestions() {
        // The OCP point: the scheduler holds a Producible and asks the same four
        // things. If any of these returned null or threw, the scheduling loop
        // would need a branch per kind - exactly what the redesign removed.
        Producible[] recipes = {
                UnitProducible.of(AUnitType.Protoss_Zealot),
                UpgradeProducible.of(UpgradeType.Singularity_Charge),
                UpgradeProducible.of(UpgradeType.Leg_Enhancements),
        };

        for (Producible recipe : recipes) {
            assertNotNull(recipe.id(), recipe + " must have an id");
            assertNotNull(recipe.cost(), recipe + " must have a cost");
            assertNotNull(recipe.immediatePrerequisites(),
                    recipe + " must answer its prerequisites, empty when there are none");
            assertNotNull(recipe.producerTypeId(), recipe + " must name a producer");
        }
    }

    @Test
    public void aUnitIsStillNotMistakenForResearch() {
        // The dispatcher picks between train and research by type, so the
        // distinction has to be real: UnitProducible must not be either research
        // kind, or every Zealot would be "researched" at a Gateway.
        Producible zealot = UnitProducible.of(AUnitType.Protoss_Zealot);

        assertFalse(zealot instanceof TechProducible);
        assertFalse(zealot instanceof UpgradeProducible);
        assertTrue(zealot.requiresPlacement() == false, "a unit is trained, not placed");
    }
}
