package atlantis.battle;

import atlantis.battle.cluster.ClusterBreach;
import atlantis.battle.formation.CoverWithdraw;
import atlantis.battle.formation.FormationTwin;
import atlantis.battle.kite.CombatTuning;
import atlantis.battle.kite.Kite;
import atlantis.battle.kite.KiteTwin;
import atlantis.battle.strategy.BattleStrategy;
import atlantis.battle.strategy.DuelKite;
import atlantis.battle.targeting.ASquadTargeting;
import atlantis.battle.twins.FightTrace;
import atlantis.battle.twins.HitTwin;
import atlantis.battle.twins.ShotTwin;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.actions.move.MoveAway;

import java.util.HashMap;
import java.util.Map;

/**
 * One engaged frame: swing discipline, corridor, kite, re-engage, re-arm.
 * Extracted from BattleCommander with its per-unit frame maps, so the
 * commander only composes (retreat, breach, acquire, fight, hunt, hold).
 * Pure move - order and conditions unchanged.
 */
public final class FightTarget {

    private FightTarget() {
    }

    private static final Map<Integer, Integer> lastStepFrame = new HashMap<>();
    private static final Map<Integer, Integer> lastMovedFrame = new HashMap<>();
    private static final Map<Integer, APosition> lastMovedPos = new HashMap<>();
    private static final Map<Integer, Integer> lastRepositionFrame = new HashMap<>();
    private static final Map<Integer, APosition> lastContactPos = new HashMap<>();

    /** Another step rode on this unit this frame - the re-arm must not cancel it. */
    public static void markRepositioned(AUnit unit) {
        lastRepositionFrame.put(unit.id(), A.now());
    }

    /** Last engaged point while visible, or null when this unit never fought. */
    static APosition lastContact(AUnit unit) {
        return lastContactPos.get(unit.id());
    }

    public static void clearForTests() {
        lastStepFrame.clear();
        lastMovedFrame.clear();
        lastMovedPos.clear();
        lastRepositionFrame.clear();
        lastContactPos.clear();
    }

    public static void fight(AUnit unit, AUnit target, BattleStrategy strategy) {
        // Snapshot the contact point while it is visible: once sight is
        // lost the enemy position reads null, so the hunt has nothing to
        // march to (1Drag_v_1Zeal sat 9 min for exactly this - the squad
        // remembered the target, its position did not).
        if (target.position() != null) lastContactPos.put(unit.id(), target.position());
        if (holdSwing(unit, target)) return;
        // A Dragoon steps back only when the enemy is inside its
        // safety margin - otherwise it stands and lets the standing
        // attack order fire the instant the cooldown hits zero
        // (PurpleWave BeDragoonVsSlowlot: margin over threats inside
        // 5 tiles, never a flat distance + cooldown gate; the threat
        // is the nearest enemy inside the envelope, not necessarily
        // the focused target: focus fire must not pin a Dragoon that
        // is being shredded).
        AUnit threat = ASquadTargeting
            .nearestEngageableEnemy(unit, CombatTuning.current().getKiteEnemyTiles());
        HitTwin.sample(unit, threat != null ? threat : target);
        FightTrace.sample(unit, threat != null ? threat : target);
        int now = A.now();
        Integer lastStep = lastStepFrame.get(unit.id());
        if (makeWay(unit, now, lastStep)) return;
        if (kiteThreat(unit, threat, now, lastStep, strategy)) return;
        if (reengage(unit, target, now, lastStep)) return;
        rearmAttack(unit, target);
    }

    /** Mid-swing the unit holds: a move now would cancel the damage. */
    private static boolean holdSwing(AUnit unit, AUnit target) {
        // Never interrupt our own shot: a move order during the attack
        // animation cancels the damage (PurpleWave AttackDelay) - the
        // shot that just started must be allowed to land, so an
        // animating unit gets no new order at all this frame.
        if (unit.isAttackFrame() || unit.isStartingAttack()) {
            ShotTwin.holding(unit, target);
            return true;
        }
        return false;
    }

    /** Corridor clearing (#162): step out of a withdrawing ally's escape ray. */
    private static boolean makeWay(AUnit unit, int now, Integer lastStep) {
        // Urgent-only (escaper within 1.5 tiles), gated to 3+ army units
        // so duels stay frozen, cadence-kept like every step (reposition
        // rides, re-issues at most every 10f). No twin this slice -
        // twins ride the live UMS proof.
        APosition aside = null;
        CoverWithdraw.Step makeWay = CoverWithdraw.makeWayStep(unit);
        if (makeWay != null) aside = makeWay.dest;
        if (aside != null
            && (lastStep == null || now - lastStep >= CombatTuning.current().getKiteRestepsEveryFrames())
            && unit.move(aside, Actions.MOVE_AVOID, "ClusterMakeWay")) {
            FormationTwin.madeWay(unit, makeWay.escaper, aside);
            lastStepFrame.put(unit.id(), now);
            lastRepositionFrame.put(unit.id(), now);
            return true;
        }
        return false;
    }

    /** Kite the threat when the margin rule says so, or false to close in. */
    private static boolean kiteThreat(AUnit unit, AUnit threat, int now, Integer lastStep, BattleStrategy strategy) {
        if (threat == null) return false;
        if (unstickFrozenQueue(unit, threat, now, lastStep)) return true;
        boolean marginKite = Kite.shouldKite(unit, threat);
        if (!marginKite && strategy == BattleStrategy.DUEL_KITE && DuelKite.shouldYield(unit, threat)
            && DuelKite.yieldNow(now, lastStep)) {
            // Duel standoff (NEXT #169): the margin above holds fire while
            // cooling, but the duel is lost by standing - yield to hold
            // distance first, argue about the margin after.
            APosition yield = DuelKite.yieldStep(unit, threat);
            if (yield != null && unit.move(yield, Actions.MOVE_AVOID, "ClusterDuelYield")) {
                KiteTwin.steppedAside(unit, threat);
                lastStepFrame.put(unit.id(), now);
                lastRepositionFrame.put(unit.id(), now);
                return true;
            }
        }
        if (!marginKite) return false;
        if (unstickFrozenQueue(unit, threat, now, lastStep)) return true;
        if (tradeShot(unit, threat)) return false;
        if (lastStep == null || now - lastStep >= CombatTuning.current().getKiteRestepsEveryFrames()) {
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
                return true;
            }
            KiteTwin.nowhereToStep(unit, threat);
        }
        return false;
    }

    /**
     * A wedged order queue looks like kiting but never moves: STOP it
     * once, sleep a frame, and the next step starts a fresh queue.
     */
    private static boolean unstickFrozenQueue(AUnit unit, AUnit threat, int now, Integer lastStep) {
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
            return true;
        }
        if (lastMovedAt == null || unit.distTo(lastMovedAt) >= 0.5) {
            lastMovedFrame.put(unit.id(), now);
            lastMovedPos.put(unit.id(), unit.position());
        }
        return false;
    }

    /**
     * Glued with a ready weapon, or the threat's swing already in
     * flight: the ready shot is free damage, stepping can only donate
     * it. Answers whether the caller should skip the step, not issue.
     */
    private static boolean tradeShot(AUnit unit, AUnit threat) {
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
        return unit.cooldown() == 0 && (glued || swingIncoming);
    }

    /** Out of range: glide to the max-range firing position with moves only. */
    private static boolean reengage(AUnit unit, AUnit target, int now, Integer lastStep) {
        // A melee unit has no firing position to hold -
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
                    return true;
                }
            }
            // No glide point computable (map edge exhausted the spiral -
            // measured: error spam + idle Dragoon): fall through to the
            // attack order below, which the engine chases on its own.
            // Standing idle where standing still is dying is never the
            // answer (same class as the melee and baseless fall-throughs
            // above). A computable point that only fails the deadband
            // or the move still waits here instead of spamming attack.
            if (fireAt != null) return true;
        }
        return false;
    }

    /** Re-arm the attack on a ready weapon only; a riding step is never cancelled. */
    private static void rearmAttack(AUnit unit, AUnit target) {
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
    }
}
