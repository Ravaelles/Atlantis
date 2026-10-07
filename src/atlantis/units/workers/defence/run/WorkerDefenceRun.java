package atlantis.units.workers.defence.run;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.BaseUnderAttack;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;
import atlantis.util.We;

import java.util.List;

public class WorkerDefenceRun extends Manager {

    /** From this many raiders the workers flee, if the fight is also going badly. */
    private static final int RAIDERS_THAT_MEAN_FLEE = 3;

    /** Above this local combat eval we are winning; below it we are behind. */
    private static final double EVAL_WE_ARE_BEHIND = 1.5;

    /** Early game: a worker at least this healthy stands and fights. */
    private static final int EARLY_GAME_HP_ENOUGH = 38;

    /**
     * At home a worker holds ground above this health; below it, it runs even
     * during a base attack, because it cannot survive the next exchange.
     */
    private static final int HOLD_GROUND_MIN_HP = 30;

    /** How far a helper will travel to answer a wounded fellow worker. */
    private static final double HELP_RADIUS = 2.2;

    /** The helper must be this healthy, or it is just another victim. */
    private static final int HELPER_MIN_HP = 35;

    /** The victim must be at most this healthy to be worth helping. */
    private static final int VICTIM_MAX_HP = 25;

    public WorkerDefenceRun(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        // Hold ground at home while the base is attacked: fleeing then is fatal
        // (B-19) - nothing would stand between the attackers and the base once
        // the army dies. But a WOUNDED worker must still leave: a Probe at 12 hp
        // is not holding anything, it is feeding the enemy.
        if (BaseUnderAttack.workerShouldHoldGround(unit) && unit.hp() > HOLD_GROUND_MIN_HP) {
            return false;
        }

        // Early game, healthy worker: stay. Six workers ARE the economy, and a
        // Probe that runs from a single raider abandons more than the minerals -
        // it leaves the raider free to pick the next one. The old threshold was
        // hp >= 38 with the same shape but no way out for a hurt worker, which
        // is why a Probe at 39 hp stood still until it died (owner's 6:44 and
        // 6:46 death logs, inside this window).
        if (A.s <= 60 * 7 && unit.hp() >= EARLY_GAME_HP_ENOUGH) return false;

        if (unit.hp() <= 20 && unit.enemiesNear().notEmpty()) return true;

        return !ignoreWhenOnlyAirUnits();
    }

    private boolean ignoreWhenOnlyAirUnits() {
        return unit.hp() >= 34
            && unit.enemiesNear().groundUnits().havingWeapon().empty()
            && unit.enemiesNear().air().ofType(
            AUnitType.Protoss_Scout, AUnitType.Protoss_Arbiter,
            AUnitType.Terran_Wraith,
            AUnitType.Zerg_Overlord, AUnitType.Zerg_Queen
        ).atMost(2);
    }

    @Override
    public Manager handle() {
        // Help a wounded fellow first: this worker is healthy (checked inside),
        // and the victim's own run manager handles its fleeing. Owner's request,
        // 2026-10-07.
        if (helpWoundedFriend()) return usedManager(this);

        if (Enemy.protoss()) {
            if (runFromZealots()) return usedManager(this);
            if (runFromDragoons()) return usedManager(this);
            if (runFromReaver()) return usedManager(this);
            if (runFromCarrier()) return usedManager(this);
        }

        if (Enemy.zerg()) {
            if (runFromMassLings()) return usedManager(this);
            if (runFromMutas()) return usedManager(this);
        }

        return null;
    }

    /**
     * A healthy worker helps a WOUNDED fellow instead of mining next to it.
     *
     * <p>
     * Owner's request (2026-10-07): "it would be good if at least one Probe
     * nearby helped, and the wounded one fled". Before this, an attacked Probe
     * was on its own - the fight managers each decided for themselves whether to
     * engage, and a Probe gathering at full health had no reason to join - so an
     * enemy scout killed workers one at a time while the rest mined.
     * </p>
     *
     * <p>
     * It lives here rather than in a class of its own on purpose: the defence
     * chain is assembled in {@code WorkerDefenceManager.managers()}, and the
     * architecture rule freezes exactly those constructor references
     * (ArchitectureBoundaryTest). A new manager class would mean a new frozen
     * violation, which CONVENTIONS §5 forbids. The behaviour belongs to "run"
     * anyway: both answer the same question - get this worker out of trouble, or
     * make the trouble cost the enemy something.
     * </p>
     *
     * <p>
     * The rule is deliberately narrow, because workers are the economy:
     * one helper per victim (the nearest healthy worker), only when the victim is
     * genuinely hurt, only against a single attacker a Probe can fight, and only
     * while the helper is healthy itself - so we trade one Probe for one kill
     * instead of feeding a second corpse.
     * </p>
     */
    private boolean helpWoundedFriend() {
        if (unit.hp() < HELPER_MIN_HP) return false;
        if (unit.isConstructing()) return false;
        if (unit.isAttacking()) return false;

        AUnit victim = victimToHelp();
        if (victim == null) return false;

        AUnit attacker = attackerOf(victim);
        if (attacker == null) return false;

        if (unit.attackUnit(attacker)) {
            unit.setTooltip("HelpFriend");
            return true;
        }

        // Could not reach it this frame (range, pathing): step towards the fight
        // rather than ignoring it, so the helper arrives while the victim lives.
        if (unit.move(attacker, Actions.MOVE_ATTACK, "HelpFriendMove")) {
            return true;
        }

        return false;
    }

    /**
     * The one worker this unit should help, or null. Only the nearest healthy
     * worker answers, so a whole mineral line does not abandon mining at once.
     */
    private AUnit victimToHelp() {
        for (AUnit friend : unit.friendsNear().workers().inRadius(HELP_RADIUS, unit).list()) {
            if (friend.equals(unit)) continue;
            if (!isInTrouble(friend)) continue;
            if (attackerOf(friend) == null) continue;
            if (!isTheChosenHelper(friend)) continue;

            return friend;
        }

        return null;
    }

    private boolean isInTrouble(AUnit friend) {
        if (friend.hp() <= VICTIM_MAX_HP) return true;

        return friend.lastUnderAttackLessThanAgo(30 * 2);
    }

    /**
     * True when this unit is the closest eligible helper for {@code victim}.
     * Ties are broken by id so the decision is stable frame to frame - a helper
     * that flickers between victims helps neither.
     */
    private boolean isTheChosenHelper(AUnit victim) {
        AUnit chosen = null;
        double bestDistance = Double.MAX_VALUE;

        for (AUnit candidate : Select.ourWorkers().inRadius(HELP_RADIUS, victim).list()) {
            if (candidate.equals(victim)) continue;
            if (candidate.hp() < HELPER_MIN_HP) continue;
            if (candidate.isConstructing()) continue;

            double distance = candidate.distTo(victim);
            if (distance < bestDistance) {
                bestDistance = distance;
                chosen = candidate;
            }
        }

        return chosen != null && chosen.equals(unit);
    }

    /**
     * The enemy the victim is fighting, if it is something a Probe can answer.
     * Air units and buildings are excluded on purpose: a Probe cannot hurt them,
     * and "helping" there just walks workers into their death.
     */
    private AUnit attackerOf(AUnit victim) {
        Selection attackers = victim.enemiesNear().combatUnits().canAttack(victim, 4.5);

        if (attackers.empty()) return null;

        // One attacker only: a Probe joining a 3-on-1 is a donation.
        if (attackers.count() > 1) return null;

        return attackers.nearestTo(victim);
    }

    private boolean runFromMutas() {
        if (!Enemy.zerg()) return false;

        Selection mutas = unit.enemiesNear().mutalisks();
        if (mutas.countInRadius(7, unit) >= 2) {
            return runFromEnemyToAnotherRegion(unit, mutas.first());
        }

        return false;
    }

    private boolean runFromMassLings() {
        if (!Enemy.zerg()) return false;
        if (unit.hp() >= 39) return false;

        Selection lings = unit.enemiesNear().zerglings();
        if (lings.countInRadius(3, unit) >= (unit.isWounded() ? 1 : 3)) {
            return runFromEnemyToAnotherRegion(unit, lings.first());
        }

        if (unit.isHealthy() && lings.nearestToDist(unit) >= 2.5) {
            return false;
        }

        if (unit.runOrMoveAway(lings.nearestTo(unit), 5)) {
            return true;
        }

        return false;
    }


    private boolean runFromDragoons() {
        if (!Enemy.protoss()) return false;

        // Was `.zealots()` - a copy-paste from runFromZealots() below, so this
        // method never saw a Dragoon and workers stood still in front of them.
        // Measured 2026-10-07 (owner report: workers neither fight nor flee
        // nearby Zealots/Dragoons).
        Selection dragoons = unit.enemiesNear().dragoons();
        int dragoonsNear = dragoons.countInRadius(4.5, unit);

        if (dragoonsNear == 0) return false;

        // A Dragoon outranges and outdamages a Probe badly: fleeing is the only
        // sane answer, and it must happen BEFORE the shot lands (range ~4, so
        // 4.5 tiles is already inside the danger zone).
        if (dragoonsNear >= 1 && unit.hp() <= 40) {
            AUnit dragoon = dragoons.nearestTo(unit);
            if (dragoon != null && unit.runOrMoveAway(dragoon, 5)) return true;
        }

        return false;
    }

    private boolean runFromZealots() {
        if (!Enemy.protoss()) return false;

        Selection zealots = unit.enemiesNear().zealots();
        int zealotsNear = zealots.countInRadius(7.0, unit);

        if (zealotsNear == 0) return false;

        // Three or more raiders is the point where the workers stop swarming -
        // but only when we are actually losing the trade (eval <= 1.5, the
        // project's "we are behind" threshold). Above that the mineral line can
        // win, and fleeing would just hand over the base.
        if (zealotsNear >= RAIDERS_THAT_MEAN_FLEE && unit.eval() <= EVAL_WE_ARE_BEHIND) {
            return runFromEnemyToAnotherRegion(unit, zealots.first());
        }

        // Few raiders, or a fight we are winning: stay and swarm them
        // (WorkerDefenceFightCombatUnits). A lone Zealot killing a whole mineral
        // line is the owner's report (2026-10-07).
        if (zealotsNear < RAIDERS_THAT_MEAN_FLEE) return false;

        if (unit.eval() <= EVAL_WE_ARE_BEHIND) {
            AUnit zealot = zealots.nearestTo(unit);
            if (zealot != null && unit.moveAwayFrom(zealot, 2, Actions.MOVE_AVOID, "RunFromZealot")) {
                return true;
            }
        }

        return false;
    }

    private boolean runFromReaver() {
        AUnit reaver = unit.enemiesNear().ofType(AUnitType.Protoss_Reaver).nearestTo(unit);
        if (reaver != null) {
            double distTo = reaver.distTo(unit);

            if (distTo <= 11.4) {
                runFromEnemyToAnotherRegion(unit, reaver);
                unit.setTooltip("OhFuckReaver!", true);
                unit.addLog("OhFuckReaver!");
                return true;
            }

            if (distTo <= 12) {
                AUnit goTo = Select.minerals().inRadius(30, unit).mostDistantTo(unit);
                if (goTo != null && goTo.distTo(unit) >= 10) {
//                    unit.gather(goTo, Actions.MOVE_AVOID, "RunToAnotherBase");
                    unit.gather(goTo);
                    unit.setTooltip("OhShitReaver");
                    return true;
                }

                HasPosition runTo = Select.all().inRadius(60, unit).mostDistantTo(unit);
                if (runTo != null && runTo.isWalkable() && runTo.distTo(unit) >= 2) {
                    unit.move(runTo, Actions.MOVE_AVOID, "RunToHell");
                    return true;
                }
            }
        }

        return false;
    }

    private boolean runFromCarrier() {
        if (!Enemy.protoss()) return false;

        // Early exit first: no carriers or interceptors around, nothing to run from.
        Selection carriers = unit.enemiesNear().ofType(
            AUnitType.Protoss_Carrier, AUnitType.Protoss_Interceptor
        );
        if (carriers.countInRadius(10, unit) == 0) return false;

        // Terran exception: a Missile Turret or Goliath within 10 tiles handles air,
        // so workers hold ground instead of fleeing the mineral line.
        if (We.terran() && Select.our().ofType(
            AUnitType.Terran_Missile_Turret, AUnitType.Terran_Goliath
        ).inRadius(10, unit).notEmpty()) return false;

        // Our Protoss exception: more than 2 nearby units with an anti-air weapon
        // (Dragoons, Archons, Cannons) means the air threat is being answered.
        if (We.protoss() && unit.friendsNear().havingAntiAirWeapon()
            .countInRadius(10, unit) > 2) return false;

        return runFromEnemyToAnotherRegion(unit, carriers.first());
    }

    private boolean runFromEnemyToAnotherRegion(AUnit worker, AUnit enemy) {
        AUnit main = Select.mainOrAnyBuilding();

        if (main != null && main.distTo(worker) >= 16) {
            return worker.move(main, Actions.MOVE_AVOID, "RunFar");
        }

        int maxDist = 25;
        List<AUnit> anywhere = Select.all().inRadius(maxDist, worker).sortDataByGroundDistanceTo(enemy, false);
        for (HasPosition goTo : anywhere) {
            if (goTo.distTo(worker) <= (maxDist + 6) && goTo.position().hasPathTo(worker.position())) {
                return worker.move(goTo, Actions.MOVE_AVOID, "RunHellFar");
            }
        }

        return unit.runningManager().runFrom(enemy, 4, Actions.RUN_ENEMY, true);
    }
}
