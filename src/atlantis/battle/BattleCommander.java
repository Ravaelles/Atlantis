package atlantis.battle;

import atlantis.battle.twins.ShotTwin;

import atlantis.battle.twins.HitTwin;

import atlantis.battle.twins.FightTrace;

import atlantis.battle.kite.CombatTuning;

import atlantis.battle.cluster.FightOrFlight;

import atlantis.battle.cluster.Missions;

import atlantis.battle.cluster.ClusterBreach;
import atlantis.battle.cluster.ClusterMission;
import atlantis.battle.cluster.ClusterRally;
import atlantis.battle.cluster.ClusterTactic;

import atlantis.battle.cluster.ClusterAnchor;

import atlantis.battle.cluster.Alpha;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.battle.formation.CoverWithdraw;
import atlantis.battle.formation.FocusFire;
import atlantis.battle.formation.FormationSlots;
import atlantis.battle.formation.FormationTwin;
import atlantis.battle.formation.SharedEscape;
import atlantis.battle.kite.Kite;
import atlantis.battle.targeting.StickyTarget;
import atlantis.battle.kite.KiteTwin;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.actions.move.MoveAway;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

/**
 * Owns every army unit each frame. Phase 0 of __PLAN-COMBAT step 5: one
 * cluster (Alpha), one posture (main base or enemy base), one gate
 * (evaluator fight/flee with a retreat latch). No per-unit decisions.
 */
public class BattleCommander extends Commander {


    /** Per-game reset next to the other static resets in OnGameStarted. */
    public static void reset() {
        ClusterTactic.reset();
        CombatTuning.resetToDefaults();
        FormationSlots.clearForTests();
        SharedEscape.clearForTests();
        FightTrace.clearForTests();
        lastRepositionFrame.clear();
        lastStepFrame.clear();
        lastMovedFrame.clear();
        lastMovedPos.clear();
    }

    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{};
    }

    @Override
    protected boolean handle() {
        Alpha.gather();
        if (Alpha.get().isEmpty()) return false;

        ClusterMission.Posture posture = ClusterMission.postureFor(
            Count.ourCombatUnits(), ClusterBreach.breachForcesFight()
        );

        if (posture == ClusterMission.Posture.ATTACK) {
            Missions.forceGlobalMissionAttack("cluster-attack");
        }
        else {
            Missions.forceGlobalMissionDefend("cluster-defend");
        }

        FightOrFlight.Tactic tactic = ClusterTactic.decide();
        HasPosition anchor = posture == ClusterMission.Posture.ATTACK
            ? ClusterAnchor.attackAnchor() : ClusterAnchor.defendAnchor();

        // Throttled state line: the only per-game proof of what the
        // cluster does (posture, tactic, size, anchor). JSON-only, stderr.
        if (A.everyNthGameFrame(31)) {
            atlantis.util.log.EventLog.key(atlantis.util.log.LogKey.CLUSTER)
                .param("cluster", Alpha.get().size())
                .param("posture", posture.name())
                .param("tactic", tactic.name())
                .param("breached", ClusterBreach.breachForcesFight())
                .param("anchor", anchor == null ? "-" : anchor.toString())
                .log(A.now(), A.minSec());
        }

        for (AUnit unit : Alpha.get().selection().list()) {
            order(unit, tactic, anchor, Alpha.get().leader());
        }

        return false;
    }

    private void order(AUnit unit, FightOrFlight.Tactic tactic, HasPosition anchor, AUnit leader) {
        if (unit == null || !unit.isAlive()) return;
        act(unit, tactic, anchor, leader);
    }

    /**
     * One unit's act for the frame, extracted so the composition pins
     * without a game: FLEE with nowhere to go falls through to the
     * fight logic (no DoNothing deaths on baseless maps), a cooling
     * Dragoon with a close threat steps back, a mid-animation unit is
     * never interrupted, otherwise it attacks or regroups.
     */
    public static void act(AUnit unit, FightOrFlight.Tactic tactic, HasPosition anchor, AUnit leader) {
        if (tactic == FightOrFlight.Tactic.FLEE) {
            // Owner: ClusterFlee is banned on baseless UMS maps - with no
            // base anywhere a retreat (shared direction or per-unit) only
            // donates the duel, so the unit falls through and fights.
            boolean noFleeOnBaselessUms = A.isUms() && Count.bases() == 0;
            // Baseless maps (UMS micro duels) have no home to run to: the
            // mission anchor there is the ENEMY base, so following it
            // marches the retreat straight into the enemy. Regroup away
            // from the threat instead; with nowhere to step, fall through
            // and fight in place. Based maps keep the choke-side rally.
            // Mid-swing the rally waits: a move order now cancels the shot
            // (PurpleWave AttackDelay) - the fight path below holds it the
            // same way.
            if (!noFleeOnBaselessUms && !unit.isAttackFrame() && !unit.isStartingAttack()) {
                HasPosition home;
                if (Select.main() != null) {
                    home = ClusterBreach.breachRallyPoint();
                }
                else {
                    // Formation Phase A: one shared escape direction per
                    // cluster (leader, ~10f cadence) instead of per-unit
                    // compass picks that break the line. Null falls back
                    // to the per-unit sampling, unchanged.
                    home = SharedEscape.rallyFor(
                        unit, leader, Alpha.get().selection(),
                        CombatTuning.current().getRegroupRallyTiles(), CombatTuning.current().getRegroupThreatTiles());
                    if (home == null) home = ClusterRally.regroupRallyPoint(unit);
                }
                HasPosition point = ClusterRally.gatherPointFor(unit, leader, home);
                if (point != null && unit.distTo(point) >= 0.2) {
                    unit.move(point, Actions.RUN_ENEMY, "ClusterFlee");
                    return;
                }
            }
            // Nowhere to run to (baseless map, null anchor): standing
            // still with DoNothing until death is never the answer -
            // fall through and fight in place instead (kite included).
        }

        // The only retreat allowed on a breached base (owner): a unit this
        // close to death with no shot ready may leave - everything else
        // fights. Per-unit, never the cluster: tactic() above already
        // committed the cluster to FIGHT, this is the individual hatch.
        // Mid-swing it waits one frame like every other move here: cancelling
        // the shot to flee is the same bug as cancelling it to kite.
        if (ClusterBreach.breachForcesFight() && !unit.isAttackFrame() && !unit.isStartingAttack()
            && unit.hp() <= CombatTuning.current().getBreachRetreatHp() && unit.cooldown() >= CombatTuning.current().getBreachRetreatCooldown()) {
            HasPosition rally = ClusterBreach.breachRallyPoint();
            if (rally != null && unit.distTo(rally) >= 0.2) {
                unit.move(rally, Actions.RUN_ENEMY, "ClusterBreachRetreat");
                return;
            }
        }

        // Sticky first: a committed unit finishes its own target (never
        // re-pick every frame - the restart costs the shot). Uncommitted
        // units join the shared focus target (weakest in reach of the
        // most guns); the squad target and the nearest enemy stay as
        // fallbacks, in that order.
        AUnit target = StickyTarget.currentTarget(unit);
        if (target == null) {
            target = FocusFire.sharedTarget(unit, 9);
        }
        if (target == null) {
            CoverWithdraw.Cover cover = CoverWithdraw.coverTarget(unit);
            if (cover != null) {
                target = cover.pursuer;
                FormationTwin.covering(unit, cover.pursuer, cover.escaper);
            }
        }
        if (target == null) {
            target = atlantis.battle.targeting.ASquadTargeting.useSquadTargetIfPossible(unit);
        }
        if (target == null) {
            target = atlantis.battle.targeting.ASquadTargeting.nearestEngageableEnemy(unit, 9);
        }
        if (target == null) {
            target = ClusterBreach.breachedBaseTarget(unit);
        }
        if (target != null) {
            // Never interrupt our own shot: a move order during the attack
            // animation cancels the damage (PurpleWave AttackDelay) - the
            // shot that just started must be allowed to land, so an
            // animating unit gets no new order at all this frame.
            if (unit.isAttackFrame() || unit.isStartingAttack()) {
                ShotTwin.holding(unit, target);
                return;
            }

            // A Dragoon steps back only when the enemy is inside its
            // safety margin - otherwise it stands and lets the standing
            // attack order fire the instant the cooldown hits zero
            // (PurpleWave BeDragoonVsSlowlot: margin over threats inside
            // 5 tiles, never a flat distance + cooldown gate; the threat
            // is the nearest enemy inside the envelope, not necessarily
            // the focused target: focus fire must not pin a Dragoon that
            // is being shredded).
            AUnit threat = atlantis.battle.targeting.ASquadTargeting
                .nearestEngageableEnemy(unit, CombatTuning.current().getKiteEnemyTiles());
            HitTwin.sample(unit, threat != null ? threat : target);
            FightTrace.sample(unit, threat != null ? threat : target);
            int now = A.now();
            Integer lastStep = lastStepFrame.get(unit.id());
            // Corridor clearing (#162): a healthy unit standing in a
            // withdrawing ally's escape ray steps laterally and resumes
            // next frame. Urgent-only (escaper within 1.5 tiles), gated
            // to 3+ army units so duels stay frozen, cadence-kept like
            // every step (reposition rides, re-issues at most every
            // 10f). No twin this slice - twins ride the live UMS proof.
            APosition aside = null;
            CoverWithdraw.Step makeWay = CoverWithdraw.makeWayStep(unit);
            if (makeWay != null) aside = makeWay.dest;
            if (aside != null
                && (lastStep == null || now - lastStep >= CombatTuning.current().getKiteRestepsEveryFrames())
                && unit.move(aside, Actions.MOVE_AVOID, "ClusterMakeWay")) {
                FormationTwin.madeWay(unit, makeWay.escaper, aside);
                lastStepFrame.put(unit.id(), now);
                lastRepositionFrame.put(unit.id(), now);
                return;
            }
            if (threat != null && Kite.shouldKite(unit, threat)) {
                // Frozen: steps keep issuing but the unit never moves -
                // the order queue is wedged (the vanilla engine bug both
                // reference bots unstick: PurpleWave Unstick issues STOP
                // past 24 failing frames, Stardust resets past 48
                // motionless ones). STOP clears the queue; sleep this
                // frame so it lands, then the next step starts a fresh
                // queue. Trips only while steps keep issuing (lastStep
                // fresh) with no motion for 48 frames - a unit that
                // simply stood still answers false on the movement check
                // after it walks.
                Integer lastMoved = lastMovedFrame.get(unit.id());
                APosition lastMovedAt = lastMovedPos.get(unit.id());
                boolean trying = lastStep != null && now - lastStep < 48;
                boolean motionless = lastMoved != null && lastMovedAt != null
                    && now - lastMoved >= 48 && unit.distTo(lastMovedAt) < 0.5;
                if (trying && motionless) {
                    KiteTwin.frozenReset(unit, threat);
                    unit.stop("ClusterUnstick");
                    lastStepFrame.remove(unit.id());
                    lastMovedFrame.remove(unit.id());
                    lastMovedPos.remove(unit.id());
                    return;
                }
                if (lastMovedAt == null || unit.distTo(lastMovedAt) >= 0.5) {
                    lastMovedFrame.put(unit.id(), now);
                    lastMovedPos.put(unit.id(), unit.position());
                }
                // A failed step must never mean standing idle: a cornered
                // Dragoon fights instead of waiting to die (Wine
                // 1Drag_v_1Zeal follow-up: kite-move-kite, then a stop,
                // then trading at point blank).
                // Glued with a ready weapon, a step is worse than useless:
                // it cannot outrun the swing already in progress, and
                // stepping every frame means zero shots until death (Wine
                // 1Drag_v_1Zeal: 190 frames glued at dist 0, cd 0, 17 hits,
                // no shot fired). Shoot instead - the ready shot is free
                // damage and the only DPS that can end the corner. Same
                // when the threat's swing is already in flight anywhere:
                // a step cannot dodge damage that is locked in, but our
                // ready shot still lands (symmetric animation discipline).
                boolean swingIncoming = threat.isAttackFrame() || threat.isStartingAttack();
                boolean glued = unit.distTo(threat) < CombatTuning.current().getKiteGlueTiles() && unit.cooldown() == 0;
                boolean tradeShot = unit.cooldown() == 0 && (glued || swingIncoming);
                if (!tradeShot && (lastStep == null || now - lastStep >= CombatTuning.current().getKiteRestepsEveryFrames())) {
                    // Stardust desired distance: step exactly the gap to the
                    // hold point (capped at one full step), never a blind
                    // shove past the firing position - stepping 2.0 tiles
                    // from dist 3.5 lands at ~5.5, OUT of a 4.0 range, and
                    // the glide then donates the walk back. That overshoot
                    // is the measured "ucieka za daleko" shape; holding at
                    // the formula's point keeps shooting instead.
                    double stepLen = Math.min(CombatTuning.current().getKiteStepTiles(),
                        Math.max(0.0, Kite.desiredKiteDistance(unit, threat) - unit.distTo(threat)));
                    APosition dest = stepLen > 0
                        ? MoveAway.bestDestination(unit, threat, stepLen)
                        : null;
                    if (dest != null && unit.move(dest, Actions.MOVE_AVOID, "ClusterKite")) {
                        KiteTwin.steppedAside(unit, threat);
                        lastStepFrame.put(unit.id(), now);
                        lastRepositionFrame.put(unit.id(), now);
                        return;
                    }
                    KiteTwin.nowhereToStep(unit, threat);
                }
            }
            // Out of range: close to the max-range firing position with
            // moves only. A melee unit has no firing position to hold -
            // with integer tile ranges its glide destination lands exactly
            // on the deadband at every distance (measured: Zealot range 0
            // in tiles per the engine pins, overshoot always 0.5), so the
            // glide below would never issue and the unit could never close.
            // Melee skips it and falls through to the attack order, which
            // the engine chases on its own; the new/overdue discipline
            // still prevents order spam.
            // An attack order here doubles as the chase and
            // gets cancelled by the next kite step before firing (Wine
            // 8Drag_v_8Drag: cd stuck at 0 alternating chase with steps,
            // 156 attack orders, zero shots landed). The attack issues on
            // arrival, when the target is actually in range.
            double standOff = unit.groundWeaponRange() - CombatTuning.current().getKiteStandoffInsetTiles();
            if (unit.groundWeaponRange() > 1 && unit.distTo(target) > unit.groundWeaponRange()) {
                APosition fireAt = null;
                if (unit.distTo(target) > standOff + CombatTuning.current().getKiteGlideDeadbandTiles()
                    && (lastStep == null || now - lastStep >= CombatTuning.current().getKiteRestepsEveryFrames())) {
                    fireAt = target.position()
                        .translateTilesTowards(unit, unit.distTo(target) - standOff);
                    if (fireAt != null
                        && unit.distTo(fireAt) > CombatTuning.current().getKiteGlideDeadbandTiles()
                        && unit.move(fireAt, Actions.MOVE_ENGAGE, "ClusterReengage")) {
                        lastStepFrame.put(unit.id(), now);
                        lastRepositionFrame.put(unit.id(), now);
                        return;
                    }
                }
                // No glide point computable (map edge exhausted the spiral -
                // measured: error spam + idle Dragoon): fall through to the
                // attack order below, which the engine chases on its own.
                // Standing idle where standing still is dying is never the
                // answer (same class as the melee and baseless fall-throughs
                // above). A computable point that only fails the deadband
                // or the move still waits here instead of spamming attack.
                if (fireAt != null) return;
            }
            // Let the reposition ride (PurpleWave sleep after move):
            // a fresh attack order here would cancel the step or glide
            // issued a frame ago and pin the unit standing through the
            // whole cooldown while the threat closes for free - the
            // stutter that bled every cycle (all 6 hits of the glue
            // game landed while alternating move with attack-stop).
            // Only a ready weapon re-arms the attack. Deliberately NOT
            // gated on isMoving: one acceleration frame still reports
            // idle velocity, and that single gap re-armed the attack
            // every second frame (Wine 1Drag_v_1Zeal: 28 steps in 63
            // frames, dist shrinking 1.9->1.3 while "retreating").
            // The kite branch above still preempts for new threats, and
            // a finished glide re-arms via the attack check below.
            if (unit.cooldown() > 0
                && A.now() - lastRepositionFrame.getOrDefault(unit.id(), -1000) < CombatTuning.current().getKiteRideFrames()) return;
            // Order discipline, not order spam: a new attack order issues
            // only when the engine holds a different target (first
            // acquisition, lost order while moving, target switch).
            // Otherwise nothing issues this frame - the standing order
            // fires on its own the moment the weapon is ready (PurpleWave
            // Commander.attack: order on new/overdue target, else sleep).
            if (target.equals(unit.target())) return;
            ShotTwin.fired(unit, target);
            unit.attackUnit(target);
            if (unit.squad() != null) unit.squad().targeting().setLastTarget(target);
            return;
        }

        // No enemy in reach: on baseless maps, hunt the last known
        // contact instead of sitting (1Drag_v_1Zeal: brief contact, then
        // 9 min idle 60 tiles apart - neither side re-acquires). The squad
        // remembers the last target; its position is the last contact
        // point. No UMS check on purpose: the gate is contact memory
        // (lastTarget exists only after a real fight) plus baselessness,
        // so melee frame 0 and based maps can never reach here. Gated
        // beyond the 9-tile squad envelope: inside it the fight logic
        // above already decided (focus refusals included - never
        // overruled from here).
        if (Count.bases() == 0 && unit.squad() != null) {
            AUnit last = unit.squad().targeting().lastTargetIfAlive();
            if (last != null && last.position() != null && unit.distTo(last) > 9) {
                if (unit.move(last.position(), Actions.MOVE_ENGAGE, "ClusterHunt")) {
                    lastRepositionFrame.put(unit.id(), A.now());
                    return;
                }
            }
        }

        // No enemy in reach: take a formation slot when the cluster
        // forms up (3+ combat units, known pivot), else bunch on the
        // leader so the cluster advances (or retreats) as one instead
        // of a line (owner 2026-10-19: march and retreat both strung
        // out). The leader sets the pace; stragglers catch up instead
        // of everyone racing the anchor.
        HasPosition point = ClusterRally.formationSlotFor(unit, leader, anchor);
        if (point == null) point = ClusterRally.gatherPointFor(unit, leader, anchor);
        if (point != null && unit.move(point, Actions.MOVE_ENGAGE, "ClusterMove")) {
            lastRepositionFrame.put(unit.id(), A.now());
        }
    }


    private static final java.util.Map<Integer, Integer> lastRepositionFrame = new java.util.HashMap<>();
    private static final java.util.Map<Integer, Integer> lastStepFrame = new java.util.HashMap<>();
    private static final java.util.Map<Integer, Integer> lastMovedFrame = new java.util.HashMap<>();
    private static final java.util.Map<Integer, APosition> lastMovedPos = new java.util.HashMap<>();

    public static void clearForTests() {
        CombatTuning.resetToDefaults();
        FormationSlots.clearForTests();
        SharedEscape.clearForTests();
        FightTrace.clearForTests();
        lastRepositionFrame.clear();
        lastStepFrame.clear();
        lastMovedFrame.clear();
        lastMovedPos.clear();
    }

}
