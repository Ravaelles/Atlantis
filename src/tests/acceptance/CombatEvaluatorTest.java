package tests.acceptance;

import atlantis.combat.eval.AtlantisJfap;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static atlantis.units.AUnitType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What the combat evaluator actually computes, pinned as relations instead of
 * numbers.
 *
 * <h2>The contract</h2>
 * {@link atlantis.units.AUnit#eval()} returns
 * {@code enemyScore / (ourScore + 0.001)} from
 * {@link AtlantisJfap#evaluateCombatSituation()} - an <b>unbounded ratio</b> over
 * cost-like negative side scores, where a side's score is what it lost in the
 * simulated window. So the ratio reads "how much did the enemy lose, divided by how
 * much did we lose":
 * <ul>
 *   <li>1.0 means the two sides score the same,</li>
 *   <li>for <b>our</b> unit a <b>higher</b> value means we are stronger,</li>
 *   <li>for an <b>enemy</b> unit the same formula is evaluated from its side,
 *       so a <b>lower</b> value means we are stronger.</li>
 * </ul>
 * Therefore two units in the same situation satisfy
 * {@code ourUnit.eval() * enemyUnit.eval() ~= 1}. That relation is what this
 * class asserts, plus the ordering each scenario is named after.
 *
 * <p>The direction is not a matter of taste and this class used to have it backwards
 * in its own prose - see {@link #higherEvalMeansWeAreBetter} for the measurement
 * that settles it (three Marines next to one Zergling, a fight we win, score
 * 1.667; one Marine next to one Zealot, a fight we lose, score 0.130). Three test
 * names said "lose" about scenarios the evaluator scores as wins; they are named
 * after what the number says now, and where the number disagrees with the damage
 * arithmetic the comment says so.</p>
 *
 * <h2>Why not absolute numbers</h2>
 * The previous version asserted values such as "ourEval ~= 0.73" or
 * "ourEval * 800 > enemyEval". Those numbers predate the Jfap-based evaluator,
 * and absolute expectations on an unbounded score are not maintainable anyway
 * (a data change moves every one of them; the fiction-table episode proved
 * it), so the absolute values live in per-test comments where they are
 * measured and the *relations* are asserted instead. See `_AI/BUGS.md` (the
 * threshold semantics that 228 production call sites rely on are still open).
 *
 * <p>Every test builds its own world explicitly: {@code world(1, ...)}
 * with no units silently creates the 22-unit sample world instead of calling
 * the generators, which is why this class used to throw NPEs on its own fields.
 */
public class CombatEvaluatorTest extends AbstractTestWithWorld {

    /**
     * Which way is up, measured rather than assumed.
     *
     * <p>Two fights 40 tiles apart in one world, because {@code eval()} hands a
     * unit in a squad the leader's number when they are within 7 tiles - with both
     * fights close together every reading is the same number and the test proves
     * nothing.</p>
     */
    @Test
    public void higherEvalMeansWeAreBetter() {
        FakeUnit winningMarine = fake(AUnitType.Terran_Marine, 10);
        FakeUnit winningMarine2 = fake(AUnitType.Terran_Marine, 10.5);
        FakeUnit winningMarine3 = fake(AUnitType.Terran_Marine, 11);
        FakeUnit zergling = fakeEnemy(AUnitType.Zerg_Zergling, 12);

        FakeUnit losingMarine = fake(AUnitType.Terran_Marine, 50);
        FakeUnit zealot = fakeEnemy(AUnitType.Protoss_Zealot, 51);

        world(1, fakeOurs(winningMarine, winningMarine2, winningMarine3, losingMarine),
            fakeEnemies(zergling, zealot), () -> {
                // Measured raw: 1.6667 (absolute -30) for three Marines against one
                // Zergling - a fight we win - and 0.1300 (absolute -100) for one
                // Marine against one Zealot, a fight we lose. The abs side scores say
                // the same thing the ratios do: the side that lost badly has the big
                // magnitude, so a big number means "the enemy lost more than we did".
                assertEquals(1.6667, winningMarine.ownCombatEvalRelative(), 0.01,
                    "three Marines next to one Zergling: raw above 1");
                assertEquals(0.1300, losingMarine.ownCombatEvalRelative(), 0.01,
                    "one Marine next to one Zealot: raw below 1");
                assertTrue(winningMarine.eval() > losingMarine.eval(),
                    "which is the only ordering all 236 production call sites rely on");

                // The other side of the same formula: an enemy unit's number runs the
                // other way, and the our-side hedge does not touch it.
                assertEquals(0.6000, zergling.eval(), 0.01, "from the Zergling's side it is 0.60");
                assertEquals(7.6929, zealot.eval(), 0.01, "and from the Zealot's side it is 7.69");
            });
    }

    @Test
    public void relativeScoreIsAReciprocalPair() {
        FakeUnit marine = fake(AUnitType.Terran_Marine, 10);
        FakeUnit hydra = fakeEnemy(AUnitType.Zerg_Hydralisk, 16);

        world(1, fakeOurs(marine), fakeEnemies(hydra), () -> {
            // Measured: ourEval = 0.0182, enemyEval = 55.03 -> product 1.0
            assertReciprocal(marine, hydra);
        });
    }

    @Test
    public void bothSidesScoreTheSameWithoutEnemies() {
        FakeUnit marine = fake(AUnitType.Terran_Marine, 10);

        world(1, fakeOurs(marine), fakeEnemies(), () -> {
            assertTrue(marine.eval() > 0, "with no enemy around the ratio is positive");
            assertTrue(marine.combatEvalAbsolute() < 0,
                "the absolute score is a cost-like number and stays negative");
        });
    }

    @Test
    public void fourMarinesAgainstOneSunkenColonyScoresAboutEven() {
        FakeUnit marine = fake(AUnitType.Terran_Marine, 11.5);
        FakeUnit sunken = fake(Zerg_Sunken_Colony, 13);

        world(1, fakeOurs(fake(AUnitType.Terran_Marine, 10), fake(AUnitType.Terran_Marine, 11),
                marine, fake(AUnitType.Terran_Marine, 12)), fakeEnemies(sunken), () -> {
            // Measured: the raw ratio is 0.9796 - about even, a hair our way -
            // and eval() reads 0.6796 after the our-side hedge. The
            // marines start inside their own 4-tile reach, so the 60-frame
            // window the simulation scores only sees the opening exchange.
            // Over a full fight the colony wins that damage race (300 hit
            // points against four 40-point Marines, one-shotting them at 40
            // damage a shot), which the window cannot see - see _AI/BUGS.md
            // B-18. The 2.17 this test used to pin came from a table that had
            // the colony at 150 hit points with a 6-damage, 2.5-tile tentacle;
            // a weaker colony scoring *worse* for us should have smelled.
            assertEquals(0.98, marine.ownCombatEvalRelative(), 0.01, "about even inside marine range");
            assertEquals(0.68, marine.eval(), 0.01, "and our own reading is hedged by 0.3");
        });
    }

    @Test
    public void oneMarineFarFromASunkenColonyScoresAlmostEvenly() {
        FakeUnit marine = fake(AUnitType.Terran_Marine, 10);
        FakeUnit sunken = fake(Zerg_Sunken_Colony, 23.5);

        world(1, fakeOurs(marine), fakeEnemies(sunken), () -> {
            // Measured: 1.001 both ways. A sunken colony shoots 7 tiles, so
            // from 13.5 tiles there is nothing to dodge and both sides score
            // "no threat" (1.0 = even; the extra 0.001 is eval()'s guard
            // against dividing by zero). The numbers this test used to carry
            // in its comment - ourEval = 0.7210 - came from a run in which the
            // whole suite pretended to be Protoss and used the Protoss
            // defensive-building config.
            assertEquals(1.001, marine.ownCombatEvalRelative(), 0.01,
                "13.5 tiles is well outside a sunken colony's 7 tile range");
            assertEquals(0.701, marine.eval(), 0.01, "and our own reading is hedged");
            assertEquals(1.001, sunken.eval(), 0.01,
                "an enemy's reading is not hedged - that would understate the enemy");
        });
    }

    @Test
    public void fourMarinesBeatOneHydralisk() {
        FakeUnit marine = fake(AUnitType.Terran_Marine, 11.5);

        world(1, fakeOurs(marine, fake(AUnitType.Terran_Marine, 11.6),
                fake(AUnitType.Terran_Marine, 11.8), fake(AUnitType.Terran_Marine, 12)), fakeEnemies(fake(Zerg_Hydralisk, 13.3)), () -> {
            // Measured: ourEval = 7.5557, enemyEval = 0.1324. This test used to be
            // called "...Lose...", which was this file reading the scale backwards: a
            // big number means the enemy lost more than we did, and by the damage
            // arithmetic four Marines (24 a volley) do beat one Hydralisk (160 hit
            // points, 8 a shot).
            assertTrue(marine.eval() > 1, "four marines beat a single hydralisk here");
            assertReciprocal(marine, (FakeUnit) marine.nearestEnemy());
        });
    }

    @Test
    public void threeMarinesAgainstTwoHydralisksScoreOurWay() {
        FakeUnit marine = fake(AUnitType.Terran_Marine, 11.5);

        world(1, fakeOurs(marine, fake(AUnitType.Terran_Marine, 11.6), fake(AUnitType.Terran_Marine, 12)), fakeEnemies(fake(Zerg_Hydralisk, 13.2), fake(Zerg_Hydralisk, 13.3)), () -> {
            // Measured: ourEval = 3.3694, enemyEval = 0.2968. The old expectation
            // ("ourEval * 300 < enemyEval") asserted the exact opposite of what the
            // previous name ("...Beat...") claimed, and the name after that ("...Lose
            // ...") was this file reading the scale backwards.
            //
            // Worth stating plainly: 3.3694 is the evaluator saying the fight is
            // clearly ours, while the damage arithmetic is a coin flip - three
            // Marines (18 a volley) against two Hydralisks (16 a volley, 160 hit
            // points) kill each other in about nine and about eight volleys. A gap
            // like that is what B-18 is about; what this test pins is the number and
            // the reciprocity, not the bot's chances.
            assertTrue(marine.eval() > 1, "the evaluator reads three marines as the better side");
            assertReciprocal(marine, (FakeUnit) marine.nearestEnemy());
        });
    }

    @Test
    public void marinesAndMedicBeatOneHydralisk() {
        FakeUnit marine = fake(AUnitType.Terran_Marine, 11.5);

        world(1, fakeOurs(marine, fake(AUnitType.Terran_Marine, 11.6),
                fake(AUnitType.Terran_Medic, 11.7), fake(AUnitType.Terran_Marine, 12)), fakeEnemies(fake(Zerg_Hydralisk, 13.3)), () -> {
            // Measured: ourEval = 21.2513, enemyEval = 0.0500, product 1.0626 within
            // the reciprocal tolerance - the mixed group (three Marines and a Medic)
            // scores like the plain groups, from opposite sides. The name used to say
            // "Lose", which was the backwards reading again; a Medic is worth almost
            // nothing to the ratio, which is why this row has the only product off 1.
            assertTrue(marine.eval() > 1, "a medic in the group does not turn the fight around");
            assertReciprocal(marine, (FakeUnit) marine.nearestEnemy());
        });
    }

    @Test
    public void oneMarineAgainstOneEnemyMarine() {
        FakeUnit ourMarine = fake(AUnitType.Terran_Marine, 10);
        FakeUnit enemyMarine = fakeEnemy(AUnitType.Terran_Marine, 11);

        world(1, fakeOurs(ourMarine), fakeEnemies(enemyMarine), () -> {
            // Measured: raw eval = 1.00001 for both, absolute = -88.0 for both, and
            // our own reading 0.7000 after the hedge - which is the whole doctrine in
            // one number: a mirror fight is not "even enough", it reads 0.7, so the
            // guards that want 1.0 or more do not treat it as safe.
            assertEquals(ourMarine.combatEvalAbsolute(), enemyMarine.combatEvalAbsolute(),
                "mirror units have the same absolute score");
            assertEquals(1.0, ourMarine.ownCombatEvalRelative(), 0.01, "a mirror fight is exactly even");
            assertEquals(0.7, ourMarine.eval(), 0.01, "but our own reading is hedged");

            // The assertion above cannot see a side asymmetry: combatEvalAbsolute()
            // returns one side's score, and both units' evaluations return the same
            // [-100, -88] pair, so it compares our side's score with our side's
            // score. The reciprocal check is the one that would catch it - measured
            // while settling B-18: at the shipped 60-frame horizon the pair is
            // [-88, -88] and this passes, while a 120-frame horizon produces
            // [-100, -88] and it fails. See NEXT.md #35.
            assertReciprocal(ourMarine, enemyMarine);
            assertEquals(1.0, enemyMarine.eval(), 0.01, "the enemy's own reading is untouched");
            assertReciprocal(ourMarine, enemyMarine);
        });
    }

    @Test
    public void foggedEnemiesAreCountedInTheEvaluation() {
        FakeUnit wraith = fake(AUnitType.Terran_Wraith, 90);
        FakeUnit cannon1 = fakeEnemy(Protoss_Photon_Cannon, 92);
        FakeUnit cannon2 = fakeEnemy(Protoss_Photon_Cannon, 93);

        world(1, fakeOurs(wraith), fakeEnemies(), () -> {
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(cannon1);
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(cannon2);

            assertEquals(2, wraith.enemiesNear().size(), "both cannons are near the wraith");
            assertEquals(1, cannon1.enemiesNear().size(), "but the cannons only see the wraith");

            // Measured with engine data: as Terran the wraith scores 0.0211
            // (absolute -760, the cannons -16) - the number says we are losing 47 to
            // 1, and that is right. As Protoss the same fight scored -0.3789 before
            // the floor landed: the raw ratio minus the additive Protoss tweaks (-0.1
            // enemy buildings near, -0.3 two anti-air combat buildings). A negative
            // eval cannot be compared with any threshold at all, which is what B-2
            // was; the floor restores "as bad as it gets". Tracked in
            // _AI/BUGS.md B-2, floored at CombatEvalScale.FLOOR.
            assertTrue(cannon1.eval() > 0, "from the cannons' side we are the weaker number");
            assertTrue(cannon1.eval() > wraith.eval(), "the wraith is outnumbered 1 vs 2");
        });
    }

    /**
     * Measured, one world each: two free dragoons 2 tiles away give
     * enemiesNear=2, eval=0.0197, absolute=-507. Locking one down and stasising
     * the other leaves enemiesNear=2 as well - the *selection* does not filter -
     * but the evaluator returns its "nothing to fight" values (9874.0 and -1.0).
     * That difference is the behaviour worth pinning.
     */
    @Test
    public void freeDragoonsAreEvaluatedAsEnemies() {
        FakeUnit wraith = fake(AUnitType.Terran_Wraith, 90);

        world(1, fakeOurs(wraith), fakeEnemies(fakeEnemy(Protoss_Dragoon, 92), fakeEnemy(Protoss_Dragoon, 93)), () -> {
            assertEquals(2, wraith.enemiesNear().size());
            assertTrue(wraith.eval() < 1, "two free dragoons next to a wraith is a bad fight");
            assertTrue(wraith.combatEvalAbsolute() < -100, "and it costs real strength");
        });
    }

    @Test
    public void lockedDownAndStasisedEnemiesAreIgnoredByTheEvaluator() {
        FakeUnit wraith = fake(AUnitType.Terran_Wraith, 90);
        FakeUnit dragoon1 = fakeEnemy(Protoss_Dragoon, 92).setLockedDown(true);
        FakeUnit dragoon2 = fakeEnemy(Protoss_Dragoon, 93).setStasised(true);

        world(1, fakeOurs(wraith), fakeEnemies(dragoon1, dragoon2), () -> {
            assertEquals(2, wraith.enemiesNear().size(), "the selection still lists them");
            assertEquals(0, wraith.enemiesNear().notImmobilized().size(),
                "but neither of them can shoot back");

            assertEquals(-1.0, wraith.combatEvalAbsolute(), 0.001,
                "-1 is the 'no enemy nearby' absolute value");
            assertTrue(wraith.eval() > 1000,
                "with no threat the relative score is overwhelming (9874.0), which is what "
                    + "every 'eval >= 2' check in production reads as safe");
            // The dragoons are enemy units, so their score is computed from
            // their side and stays small (their own "enemy" is the wraith).
            assertTrue(dragoon1.eval() < 1,
                "from an enemy unit's point of view the ratio is inverted");
        });
    }

    /**
     * Two units looking at the same fight must score it reciprocally, because
     * both compute enemyScore/ourScore from opposite sides. This is the
     * invariant that survives a retune of the evaluator; absolute numbers do
     * not.
     *
     * <p>Asserted on the raw reading ({@code ownCombatEvalRelative}) rather than on
     * {@code eval()}, and that is the point: {@code eval()} takes 0.3 off our own
     * number ({@code CombatEvalScale.OUR_SIDE_HEDGE}) and leaves the enemy's alone,
     * so the hedge makes the two readings deliberately non-reciprocal. Reciprocity is
     * a property of the evaluator; the hedge is our doctrine about what to do with
     * the answer it gives.</p>
     */
    private void assertReciprocal(FakeUnit ours, FakeUnit theirs) {
        double ourEval = ours.ownCombatEvalRelative();
        double theirEval = theirs.ownCombatEvalRelative();

        assertEquals(1.0, ourEval * theirEval, 0.1,
            "our eval (" + ourEval + ") and their eval (" + theirEval + ") must be reciprocal");
    }

    @Override
    protected FakeUnit[] generateOur() {
        return fakeOurs(fake(AUnitType.Terran_Marine, 10), fake(AUnitType.Terran_Wraith, 90));
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return fakeEnemies(
            fakeEnemy(AUnitType.Zerg_Hydralisk, 16),
            fakeEnemy(AUnitType.Zerg_Hydralisk, 17),
            fakeEnemy(AUnitType.Protoss_Zealot, 11)
        );
    }
}
