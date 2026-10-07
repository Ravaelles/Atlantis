package tests.e2e;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.workers.defence.fight.WorkerDefenceFightCombatUnits;
import atlantis.units.workers.defence.run.WorkerDefenceRun;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Workers swarm a lone raider, and flee from a crowd (owner report 2026-10-07:
 * "a single Zealot mercilessly kills many mining units").
 *
 * <p>
 * The numbers are the owner's: ONE Zealot among several workers must be
 * attacked; from THREE raiders up the workers only flee, and only when the
 * local fight is also going badly (eval below 1.5).
 * </p>
 *
 * <p>
 * These are source-level assertions for the thresholds, plus behavioural ones
 * for eligibility. The stub world has no swing simulation, so "the Zealot dies"
 * is not testable here - but "the worker attacks it" and "the worker is allowed
 * to attack it" are exactly the behaviour that was missing.
 * </p>
 */
public class WorkerSwarmTest extends AbstractTestWithWorld {

        private static final String FIGHT_SOURCE = "src/atlantis/units/workers/defence/fight/WorkerDefenceFightCombatUnits.java";
        private static final String RUN_SOURCE = "src/atlantis/units/workers/defence/run/WorkerDefenceRun.java";

        @Override
        public Race initRace() {
                return Race.Protoss;
        }

        @Override
        public Race initEnemyRace() {
                return Race.Protoss;
        }

        @Test
        public void aLoneZealotAmongWorkersIsAttacked() {
                FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
                FakeUnit probe1 = fake(AUnitType.Protoss_Probe, 11);
                FakeUnit probe2 = fake(AUnitType.Protoss_Probe, 11.5);
                FakeUnit probe3 = fake(AUnitType.Protoss_Probe, 12);
                FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 11.8);

                world(20, units(nexus, probe1, probe2, probe3), units(zealot), () -> {
                        if (A.now() != 12)
                                return;

                        AUnit worker = Select.ourWorkers().first();
                        if (worker == null)
                                return;

                        int attackers = 0;
                        for (AUnit probe : Select.ourWorkers().list()) {
                            WorkerDefenceFightCombatUnits fight = new WorkerDefenceFightCombatUnits(probe);
                            // forceHandled() runs applies() then handle() and reports whether
                            // the manager did anything - the path the game takes, without a
                            // parent manager.
                            if (fight.forceHandled()) attackers++;
                        }

                        // At least one worker must engage: the swarm is the answer to a lone
                        // raider, and "nobody reacts" is the reported bug.
                        assertTrue(attackers >= 1,
                                        "at least one of the three probes must attack a lone Zealot among them;"
                                                        + " attackers=" + attackers
                                                        + " workers=" + Select.ourWorkers().count());
                });
        }

        @Test
        public void swarmThresholdIsThree() throws Exception {
                // One and two raiders: the workers fight. Three or more: the run manager
                // takes over. Pinned at the source because the stub cannot produce the
                // damage that makes the difference observable.
                String fight = read(FIGHT_SOURCE);

                assertTrue(fight.contains("WORKERS_RUN_THRESHOLD = 3"),
                                "swarming must stop at three raiders - the owner's line");
                assertTrue(fight.contains("shouldSwarm(enemy)"),
                                "processFightEnemyCombatUnits must consult the swarm rule");
                assertTrue(fight.contains("attackUnit(enemy)"),
                                "a swarm attack must actually be issued");
        }

        @Test
        public void fleeingNeedsBothACrowdAndABadFight() throws Exception {
                String run = read(RUN_SOURCE);

                assertTrue(run.contains("RAIDERS_THAT_MEAN_FLEE = 3"),
                                "from three raiders the workers may flee");
                assertTrue(run.contains("EVAL_WE_ARE_BEHIND = 1.5"),
                                "fleeing must also require a losing fight (eval <= 1.5), or a winning"
                                                + " mineral line would abandon the base for nothing");
                assertTrue(run.contains("unit.eval() <= EVAL_WE_ARE_BEHIND"),
                                "the two conditions must be combined, not either of them alone");
        }

        @Test
        public void workersAroundTheRaiderAreCountedFromOurSide() throws Exception {
                // friendsNear() answers from the ASKING unit's side, so on an enemy it
                // returns the enemy's own friends - counting the wrong side would make
                // the swarm rule depend on enemy reinforcements.
                String fight = read(FIGHT_SOURCE);

                assertTrue(fight.contains("Select.ourWorkers().inRadius(9, enemy)"),
                                "the workers around a raider must be counted with our selection,"
                                                + " not with the enemy's friendsNear()");
                assertTrue(!fight.contains("enemy.friendsNear().workers()"),
                                "enemy.friendsNear().workers() counts the ENEMY's workers");
        }

        @Test
        public void theOldBaseRadiusGateIsGone() throws Exception {
                // Fighting used to require the raider to be within 12 tiles of one of
                // OUR bases via the enemy's own base list, which excluded forward
                // expansions. The swarm rule plus the buildings check replace it.
                String fight = read(FIGHT_SOURCE);

                assertTrue(fight.contains("friendsNear().buildings()"),
                                "eligibility must follow our buildings, which covers the mineral line");
                assertEquals(false, fight.contains("distToBase() <= 12"),
                                "the old tight base radius must be relaxed");
        }

        @Test
        public void aWoundedWorkerRunsEvenAtHomeAndInTheEarlyGame() throws Exception {
                // The owner's 6:44 and 6:46 death logs sit INSIDE the first 7 minutes,
                // and WorkerDefenceRun is absent from both logs. Two gates did that,
                // and neither had a way out for a hurt worker:
                //
                //   if (BaseUnderAttack.workerShouldHoldGround(unit)) return false;
                //   if (A.s <= 60 * 7 && unit.hp() >= 38) return false;
                //
                // A Zealot kills a 40 hp Probe in three swings, so 'hold ground at
                // 39 hp' is 'stand still and die'.
                String run = read(RUN_SOURCE);

                assertTrue(run.contains("HOLD_GROUND_MIN_HP"),
                                "holding ground at home must have a health floor, or a nearly"
                                                + " dead worker keeps feeding the enemy");
                assertTrue(run.contains("EARLY_GAME_HP_ENOUGH"),
                                "the early-game 'stay and fight' rule must have a health"
                                                + " threshold a Zealot cannot delete in three swings");
                assertTrue(!run.contains("unit.hp() >= 38) return false;"),
                                "the old flat 'hp >= 38 -> do not run' rule must be gone: it"
                                                + " covered the whole early game");
        }

        @Test
        public void theHoldGroundFloorIsAboveTheDeathThreshold() throws Exception {
                // `hp <= 20` alone is far too late: a Zealot does 16 per swing, so a
                // worker at 20 hp is dead next swing. The floor must be higher than
                // the only other way into run.
                String run = read(RUN_SOURCE);

                assertTrue(run.contains("HOLD_GROUND_MIN_HP = 30"),
                                "the hold-ground floor must sit above the 20 hp death threshold");
                assertTrue(run.contains("EARLY_GAME_HP_ENOUGH = 45"),
                                "the early-game threshold must be high enough that a hurt"
                                                + " worker is not told to stay");
        }

        private static String read(String path) throws Exception {
                return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path)),
                                java.nio.charset.StandardCharsets.UTF_8);
        }

        // =========================================================
        @Override
        protected FakeUnit[] generateOur() {
                return null;
        }

        @Override
        protected FakeUnit[] generateEnemies() {
                return null;
        }
}