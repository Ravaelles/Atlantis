package tests.unit;

import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.ProductionRacePolicy;
import atlantis.production.v2.goals.DynamicGoals;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fortification is a post-Forge concern (owner 2026-10-27, three LIVE
 * games built a Forge before the Gateway in a build order with no
 * Forge): legacy secured bases with cannons only once a Forge stood
 * ({@code ProtossSecureBasesCommander.applies} gates on
 * {@code Have.forge()}), and the V2 port dropped that gate - so a
 * Cannon goal from frame 0 planned an orphan Forge prerequisite that
 * the dispatcher built while the Cannon itself never scheduled.
 */
@Tag("production")
public class FortificationForgeGateTest {

    private static DynamicGoals.GameSnapshot snapshot() {
        return new DynamicGoals.GameSnapshot(
            20, 1, 20, 4, 20, 300, false,
            0, 1, 0, false, false, 0).atBase(30, 30);
    }

    private static boolean wantsCannons(DynamicGoals.GameSnapshot state) {
        List<ProductionGoal> goals =
            DynamicGoals.contribute(state, ProductionRacePolicy.forRace("Protoss"));
        for (ProductionGoal goal : goals) {
            if (goal.item().id().equals(AUnitType.Protoss_Photon_Cannon.name())) return true;
        }
        return false;
    }

    private static boolean wantsForge(DynamicGoals.GameSnapshot state) {
        List<ProductionGoal> goals =
            DynamicGoals.contribute(state, ProductionRacePolicy.forRace("Protoss"));
        for (ProductionGoal goal : goals) {
            if (goal.item().id().equals(AUnitType.Protoss_Forge.name())) return true;
        }
        return false;
    }

    @Test
    public void noForgeEmitsNoCannonGoals() {
        DynamicGoals.GameSnapshot opening = snapshot();
        opening.hasForge = false;

        assertFalse(wantsCannons(opening),
            "a Cannon goal with no Forge standing is what planned the orphan Forge "
                + "ahead of the Gateway - the opening owns its minerals, not fortification");
    }

    @Test
    public void noForgeEmitsNoForgeGoalsEither() {
        DynamicGoals.GameSnapshot opening = snapshot();
        opening.hasForge = false;

        assertFalse(wantsForge(opening),
            "the dynamic layer names no Forge at all: with no Cannon goal nothing "
                + "can pull a Forge prerequisite into the plan");
    }

    @Test
    public void aStandingForgeKeepsTheBaselineCannon() {
        DynamicGoals.GameSnapshot secured = snapshot();
        secured.hasForge = true;

        assertTrue(wantsCannons(secured),
            "with a Forge standing the baseline cannon still flows - the gate "
                + "owns the opening, never the mid-game");
    }
}
