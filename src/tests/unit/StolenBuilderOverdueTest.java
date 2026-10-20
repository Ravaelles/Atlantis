package tests.unit;

import atlantis.production.constructions.Construction;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Game 175707: Gateway-2's builder (#117) was stolen back to minerals while
 * its construction sat NOT_STARTED - no cancel, no finish, silence. A living
 * builder shields the construction from the overdue clock (the #58 fix), so
 * a stolen one stalls it forever. A builder back on minerals/gas while the
 * building is affordable is not working on it; a walking one still shields.
 */
@Tag("production")
public class StolenBuilderOverdueTest extends AbstractTestWithWorld {

    private Construction stolen;
    private Construction walked;

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
    public void miningBuilderDoesNotShieldStalledConstruction() {
        currentMinerals = 600;
        currentSupplyUsed = 20;
        currentSupplyTotal = 40;

        FakeUnit miner = new FakeUnit(AUnitType.Protoss_Probe, 21, 21) {
            @Override
            public boolean isGatheringMinerals() {
                return true;
            }
        };
        FakeUnit walker = fake(AUnitType.Protoss_Probe, 22, 21);

        world(700, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            miner,
            walker
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() == 5) {
                stolen = new Construction(AUnitType.Protoss_Gateway);
                stolen.setBuilder(miner);
                walked = new Construction(AUnitType.Protoss_Gateway);
                walked.setBuilder(walker);
            }
            if (atlantis.game.A.now() != 690) return;

            assertTrue(
                stolen.isOverdue(),
                "a builder back on minerals while affordable must not shield a 685-frame stall");
            assertFalse(
                walked.isOverdue(),
                "a builder walking to its site still shields the construction (#58)");
        });
    }
}
