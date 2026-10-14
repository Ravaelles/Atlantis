package tests.unit;

import atlantis.production.dynamic.protoss.tech.ResearchProtossGroundArmor;
import atlantis.production.dynamic.protoss.tech.ResearchProtossGroundWeapons;
import atlantis.units.AUnitType;
import bwapi.Race;
import bwapi.UpgradeType;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeResearch;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.fakes.FakeUnitHelper.merge;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * NEXT #85: upgrades never pass level 1. The per-type researchers DO carry
 * level logic, so the suspects are the gates around it. Replicates a
 * gas-healthy mid game (Forge, 12 Zealots, bank 800/300, Zerg enemy):
 * with weapons at level 1 the weapons researcher must re-fire for L2, and
 * the armor researcher must fire for L1 (vanilla has no weapons-armor
 * cross requirement; Terran researchers have none either).
 */
public class ProtossUpgradesLevelGatingTest extends AbstractTestWithWorld {

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

    private FakeUnit[] army() {
        FakeUnit[] zealots = new FakeUnit[12];
        for (int i = 0; i < 12; i++) zealots[i] = fake(AUnitType.Protoss_Zealot, 30 + i, 20);
        return merge(
            fakeOurs(
                fake(AUnitType.Protoss_Nexus, 20, 20),
                fake(AUnitType.Protoss_Forge, 24, 20)
            ),
            zealots
        );
    }

    @Test
    public void weaponsRefireForL2WhenL1Done() {
        currentMinerals = 800;
        currentGas = 400;
        currentSupplyUsed = 40;
        currentSupplyTotal = 60;

        world(20, army(), fakeEnemies(fake(AUnitType.Zerg_Zergling, 60, 20)), () -> {
            if (atlantis.game.A.now() != 17) return;
            FakeResearch.withUpgradeLevel(UpgradeType.Protoss_Ground_Weapons, 1);

            assertTrue(
                new ResearchProtossGroundWeapons().applies(),
                "weapons L1 done + Forge + 12 Zealots + 800/400 bank must research L2");
        });
    }

    @Test
    public void armorFiresOnceWeaponsL1Done() {
        // NOTE: the stub enemy reads Protoss (Enemy.protoss()=true), so the
        // gas bar is 270+100=370, not 270 - 400 keeps the state gas-healthy
        // under either reading.
        currentMinerals = 800;
        currentGas = 400;
        currentSupplyUsed = 40;
        currentSupplyTotal = 60;

        world(20, army(), fakeEnemies(fake(AUnitType.Zerg_Zergling, 60, 20)), () -> {
            if (atlantis.game.A.now() != 17) return;
            FakeResearch.withUpgradeLevel(UpgradeType.Protoss_Ground_Weapons, 1);

            assertTrue(
                new ResearchProtossGroundArmor().applies(),
                "weapons L1 done + Forge + 12 Zealots + 800/400 bank must research armor L1");
        });
    }
}
