package tests.unit;

import atlantis.production.demand.ProductionGoal;
import atlantis.production.policy.ProductionRacePolicy;
import atlantis.production.producibles.UnitProducible;
import atlantis.production.demand.DynamicGoals;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Upgrade ladders at the V2 goal layer (legacy ResearchProtossGroundWeapons,
 * ResearchProtossGroundArmor, verbatim gates): weapons climb past a Forge
 * with combat and bank behind them, armor waits for weapons L1, both stop
 * at level 3. The too-weak veto expires after minute 10. Queue-history and
 * already-queued guards stay behind - the scheduler dedups.
 * The PvP gateway rush (legacy PrioritizeGatewaysVsProtoss) raises the
 * demand cap to 6 against unfortified Protoss.
 *
 * <p>Pure snapshot state, no world.</p>
 */
@Tag("production")
public class TechUpgradesTest {

    private static DynamicGoals.GameSnapshot state(
        int supply, int minerals, int gas, int strength, int seconds,
        boolean forge, int combat, int bases, int weapons, int armor,
        boolean zerg, boolean protoss, int enemyBuildings,
        int gateways, int freeGateways) {
        DynamicGoals.GameSnapshot snapshot = new DynamicGoals.GameSnapshot(
            24, 2, supply, 10, supply + 10, minerals, false, combat, bases,
            0, zerg, protoss, 0, gas, true, 6, 1, false, strength);
        snapshot.hasForge = forge;
        snapshot.combatUnits = combat;
        snapshot.seconds = seconds;
        snapshot.weaponsLevel = weapons;
        snapshot.armorLevel = armor;
        snapshot.enemyCombatBuildings = enemyBuildings;
        snapshot.gatewaysExisting = gateways;
        snapshot.gatewaysFree = freeGateways;

        return snapshot.atBase(100, 60);
    }

    private static List<ProductionGoal> contribute(DynamicGoals.GameSnapshot snapshot) {
        return DynamicGoals.contribute(snapshot, ProductionRacePolicy.forRace("Protoss"));
    }

    private static boolean hasUpgrade(List<ProductionGoal> goals, bwapi.UpgradeType type, int level) {
        String id = atlantis.production.producibles.UpgradeProducible.of(type, level).id();

        return goals.stream().anyMatch(goal -> goal.item().id().equals(id));
    }

    private static boolean hasGoal(List<ProductionGoal> goals, AUnitType type) {
        String id = UnitProducible.of(type).id();

        return goals.stream().anyMatch(goal -> goal.item().id().equals(id));
    }

    @Test
    public void richMidGameResearchesWeapons() {
        assertTrue(hasUpgrade(
            contribute(state(80, 800, 250, 200, 400, true, 12, 2, 0, 0, true, false, 0, 2, 0)),
            bwapi.UpgradeType.Protoss_Ground_Weapons, 1),
            "Forge, combat and bank order weapons L1");
    }

    @Test
    public void maxedLadderHoldsFire() {
        assertFalse(hasUpgrade(
            contribute(state(120, 900, 400, 300, 700, true, 20, 2, 3, 3, true, false, 0, 2, 0)),
            bwapi.UpgradeType.Protoss_Ground_Weapons, 4),
            "level 3 needs nothing more");
        assertFalse(hasUpgrade(
            contribute(state(120, 900, 400, 300, 700, true, 20, 2, 3, 3, true, false, 0, 2, 0)),
            bwapi.UpgradeType.Protoss_Ground_Armor, 4),
            "armor at 3 holds too");
    }

    @Test
    public void noForgeNoTech() {
        assertFalse(hasUpgrade(
            contribute(state(80, 800, 250, 200, 400, false, 12, 2, 0, 0, true, false, 0, 2, 0)),
            bwapi.UpgradeType.Protoss_Ground_Weapons, 1),
            "without the Forge nothing researches");
    }

    @Test
    public void weakEarlyLineHoldsTech() {
        assertFalse(hasUpgrade(
            contribute(state(100, 700, 250, 80, 300, true, 12, 2, 0, 0, true, false, 0, 2, 0)),
            bwapi.UpgradeType.Protoss_Ground_Weapons, 1),
            "sub-90 strength in time waits, banked or not");
    }

    @Test
    public void weakLateLineStillTechs() {
        assertTrue(hasUpgrade(
            contribute(state(100, 700, 250, 80, 700, true, 12, 2, 0, 0, true, false, 0, 2, 0)),
            bwapi.UpgradeType.Protoss_Ground_Weapons, 1),
            "past minute 10 the weak veto expires");
    }

    @Test
    public void armorWaitsForWeapons() {
        assertFalse(hasUpgrade(
            contribute(state(100, 700, 400, 200, 700, true, 15, 2, 0, 0, false, true, 0, 2, 0)),
            bwapi.UpgradeType.Protoss_Ground_Armor, 1),
            "armor without weapons L1 waits");
    }

    @Test
    public void armorFollowsWeapons() {
        assertTrue(hasUpgrade(
            contribute(state(100, 700, 400, 200, 700, true, 15, 2, 1, 0, false, true, 0, 2, 0)),
            bwapi.UpgradeType.Protoss_Ground_Armor, 1),
            "weapons L1 opens the armor ladder");
    }

    @Test
    public void pvpRushesSixGateways() {
        assertTrue(hasGoal(
            contribute(state(60, 800, 200, 200, 700, true, 15, 2, 1, 1, false, true, 0, 4, 0)),
            AUnitType.Protoss_Gateway),
            "vs unfortified Protoss the demand runs to 6");
    }

    @Test
    public void pvpFortificationsCapRush() {
        assertFalse(hasGoal(
            contribute(state(60, 800, 200, 200, 700, true, 15, 2, 1, 1, false, true, 2, 4, 0)),
            AUnitType.Protoss_Gateway),
            "fortified enemies hold the cap at 4");
    }

    @Test
    public void otherMatchupsCapFour() {
        assertFalse(hasGoal(
            contribute(state(60, 800, 200, 200, 700, true, 15, 2, 1, 1, true, false, 0, 4, 0)),
            AUnitType.Protoss_Gateway),
            "vs Zerg the cap stays 4");
    }
}
