package tests.unit;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.goals.protoss.ExpansionGoal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The second-base WHEN lives on the board: the top-level matrix of
 * ProtossShouldExpand, row for row. Stub states, milliseconds.
 * Third and later bases stay silent (their thicket migrates only
 * with a game proof each).
 */
public class ExpansionGoalTest {

    private BoardState state(
        int minerals, int workers, int bases, int unfinished, int planned,
        String race, int mutas, int cannons, int army, int seconds, boolean attacked
    ) {
        return BoardState.builder()
            .minerals(minerals).workers(workers).bases(bases)
            .basesUnfinished(unfinished).basesPlanned(planned)
            .enemyRace(race).enemyUnits(0, 0, mutas, false).cannons(cannons)
            .armyStrength(army).seconds(seconds).underAttack(attacked)
            .build();
    }

    private BoardState healthy() {
        return state(400, 20, 1, 0, 1, "Protoss", 0, 0, 200, 400, false);
    }

    @Test
    public void poorBaseHolds() {
        assertFalse(ExpansionGoal.wanted(state(299, 20, 1, 0, 1, "Protoss", 0, 0, 200, 400, false)));
        assertTrue(ExpansionGoal.wanted(healthy()));
    }

    @Test
    public void windfallOverrulesEverything() {
        // 777 bank with at most three bases planned: the Nexus goes
        // even under mutas or rush pressure (legacy LimitedBases row).
        assertTrue(ExpansionGoal.wanted(state(800, 20, 1, 0, 3, "Zerg", 5, 0, 50, 300, false)));
        assertFalse(ExpansionGoal.wanted(state(800, 20, 1, 0, 4, "Zerg", 5, 0, 50, 300, false)));
    }

    @Test
    public void mutasVetoUnlessCovered() {
        // Covered (3x unfinished >= cannons): the veto still fires when
        // rich - mutas punish a banking base (legacy DontExpandAgainstMutas).
        // (planned=4 dodges the 777 windfall so the veto decides.)
        assertFalse(ExpansionGoal.wanted(state(900, 20, 1, 0, 4, "Zerg", 3, 0, 200, 400, false)));
        // Poor and uncovered: expand anyway, nothing to defend with.
        assertTrue(ExpansionGoal.wanted(state(400, 20, 1, 0, 1, "Zerg", 3, 9, 200, 400, false)));
        // Non-Zerg ignores the muta count entirely.
        assertTrue(ExpansionGoal.wanted(state(400, 20, 1, 0, 1, "Protoss", 3, 0, 200, 400, false)));
    }

    @Test
    public void rushDefenseVetoesWhenPoorAndWeak() {
        assertFalse(ExpansionGoal.wanted(state(300, 20, 1, 0, 1, "Protoss", 0, 0, 100, 300, false)));
        assertTrue(ExpansionGoal.wanted(state(300, 20, 1, 0, 1, "Protoss", 0, 0, 200, 300, false)));
        assertTrue(ExpansionGoal.wanted(state(500, 20, 1, 0, 1, "Protoss", 0, 0, 100, 300, false)));
        assertTrue(ExpansionGoal.wanted(state(300, 20, 1, 0, 1, "Protoss", 0, 0, 100, 600, false)));
    }

    @Test
    public void unfinishedSaturationVetoes() {
        // 400 bank against one unfinished base: 400/400 = 1 <= 1.15 holds.
        assertEquals("HasInProgress",
            ExpansionGoal.veto(state(400, 20, 1, 1, 2, "Protoss", 0, 0, 200, 400, false)));
        // 800 bank flows: 800/400 = 2 > 1.15, no veto named.
        assertEquals(null,
            ExpansionGoal.veto(state(800, 20, 1, 1, 4, "Protoss", 0, 0, 200, 400, false)));
        // The veto names itself through the same cases.
        assertEquals("DontExpandAgainstMutas",
            ExpansionGoal.veto(state(900, 20, 1, 0, 1, "Zerg", 3, 0, 200, 400, false)));
        assertEquals("DefendRush",
            ExpansionGoal.veto(state(300, 20, 1, 0, 1, "Protoss", 0, 0, 100, 300, false)));
    }

    @Test
    public void sevenBasesCap() {
        assertFalse(ExpansionGoal.wanted(state(900, 60, 7, 0, 7, "Protoss", 0, 0, 500, 1200, false)));
    }

    @Test
    public void thirdBaseStaysSilent() {
        // The thicket (worker/gateway ratios, pressure reads) is not
        // mirrored: the board says nothing rather than overrule legacy
        // (600 bank dodges the 777 windfall so the silence decides).
        assertFalse(ExpansionGoal.wanted(state(600, 50, 2, 0, 2, "Protoss", 0, 0, 500, 1200, false)));
    }

    @Test
    public void workersAndAttacksGate() {
        assertFalse(ExpansionGoal.wanted(state(400, 11, 1, 0, 1, "Protoss", 0, 0, 200, 400, false)));
        assertFalse(ExpansionGoal.wanted(state(400, 20, 1, 0, 1, "Protoss", 0, 0, 200, 400, true)));
    }
}
