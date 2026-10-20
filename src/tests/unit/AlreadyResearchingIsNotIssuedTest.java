package tests.unit;

import atlantis.production.v2.ProductionItem;
import atlantis.production.v2.ProductionPlan;
import atlantis.production.v2.TechProducible;
import atlantis.production.v2.UpgradeProducible;
import atlantis.production.v2.execution.DispatchResult;
import atlantis.production.v2.execution.GameOrderDirector;
import atlantis.production.v2.execution.ProductionDispatcher;
import atlantis.units.AUnitType;
import bwapi.Race;
import bwapi.TechType;
import bwapi.UpgradeType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * A V2 research that hits the already-running dedup issues no command, so
 * it is not "issued" - same contract as the train twin
 * ({@code AlreadyTrainingIsNotIssuedTest}): the row must measure engine
 * accepts, not dispatches.
 *
 * <p>
 * Measured need: the Singularity want re-plans every frame while the Core
 * researches, so each "researched" dispatcher row without this fix is a
 * dispatch, not an accept - and the pending Singularity live-fire proof
 * would read inflated rows.
 * </p>
 */
@Tag("production")
public class AlreadyResearchingIsNotIssuedTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void runningUpgradeReportsNotIssued() {
        FakeUnit core = fake(AUnitType.Protoss_Cybernetics_Core, 20, 20);
        core.upgrading = UpgradeType.Singularity_Charge;

        world(5, fakeOurs(
            core,
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            ProductionItem item = new ProductionItem(
                UpgradeProducible.of(UpgradeType.Singularity_Charge), 0, false, null, core.id());
            ProductionPlan plan = new ProductionPlan();
            plan.add(item);

            List<DispatchResult> results = new ProductionDispatcher(new GameOrderDirector())
                .dispatch(plan, 3, 10);

            assertEquals(1, results.size());
            assertFalse(results.get(0).issued(),
                "the Core researched nothing this frame - Singularity Charge was already running");
            assertEquals("facility busy or missing", results.get(0).detail());
        });
    }

    @Test
    public void runningTechReportsNotIssued() {
        FakeUnit archives = fake(AUnitType.Protoss_Templar_Archives, 20, 20);
        archives.researching = TechType.Psionic_Storm;

        world(5, fakeOurs(
            archives,
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            ProductionItem item = new ProductionItem(
                TechProducible.of(TechType.Psionic_Storm), 0, false, null, archives.id());
            ProductionPlan plan = new ProductionPlan();
            plan.add(item);

            List<DispatchResult> results = new ProductionDispatcher(new GameOrderDirector())
                .dispatch(plan, 3, 10);

            assertEquals(1, results.size());
            assertFalse(results.get(0).issued(),
                "the Archives researched nothing this frame - Psionic Storm was already running");
            assertEquals("facility busy or missing", results.get(0).detail());
        });
    }
}
