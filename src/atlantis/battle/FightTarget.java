package atlantis.battle;

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

/**
 * One engaged frame: swing discipline, corridor, kite, re-engage, re-arm.
 * Extracted from BattleCommander with its per-unit frame maps, so the
 * commander only composes (retreat, breach, acquire, fight, hunt, hold).
 * Pure move - order and conditions unchanged.
 */
public final class FightTarget {

    private FightTarget() {
    }

    /** Memory lives in {@link DuelMemory}; the four methods below only forward it. */
    public static void markRepositioned(AUnit unit) {
        DuelMemory.markRepositioned(unit);
    }

    /** Last engaged point while visible, or null when this unit never fought. */
    public static APosition lastContact(AUnit unit) {
        return DuelMemory.lastContact(unit);
    }

    /** Frame of the last visible contact snapshot, or null when never fought. */
    public static Integer lastContactFrame(AUnit unit) {
        return DuelMemory.lastContactFrame(unit);
    }

    public static void clearForTests() {
        DuelMemory.clear();
    }

    public static void fight(AUnit unit, AUnit target, BattleStrategy strategy) {
        // Snapshot the contact point while it is visible: once sight is
        // lost the enemy position reads null (or goes stale), so the hunt
        // has nothing to march to (1Drag_v_1Zeal sat 9 min for exactly
        // this - the squad remembered the target, its position did not).
        // Only visible contacts refresh it: an invisible "fight" must not
        // drag the last-known point onto a ghost.
        if (target.isVisibleUnitOnMap() && target.position() != null) {
            DuelMemory.snapshotContact(unit, target.position());
        }
        // (moved to BattleCommander.act: kiting units never reach fight(),
        // so the army contact must be recorded at acquisition, not here)
        if (!target.isVisibleUnitOnMap()) {
            // Fighting an invisible target: the order cannot land (measured
            // 1Drag_v_1Zeal: 9 min of shot-fired with zero damage while
            // both duelists lived). Name it instead of silently spinning.
            Integer lastGhost = DuelMemory.lastGhostFrame(unit);
            if (lastGhost == null || A.now() - lastGhost >= 150) {
                DuelMemory.noteGhost(unit);
                atlantis.util.log.EventLog.key(atlantis.util.log.LogKey.GHOST_TARGET)
                    .param("unit", unit.id())
                    .param("target", target.id())
                    .param("dist", (int) (unit.distTo(target) * 10))
                    .log(A.now(), A.minSec());
            }
        }
        if (holdSwing(unit, target)) return;
        // Post-swing settle (PurpleWave AttackDelay, owner 2026-10-26):
        // a move or target switch landing right after the attack
        // animation sticks the unit - the order must arrive no earlier
        // than animation start + observed duration + 2 (latency-aware).
        // holdSwing owns the animation itself; this owns the frames
        // after it. Calibrates per unit from observed swings, so no
        // hand-written stop frames anywhere.
        if (attackSettleHold(unit)) return;
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
        Integer lastStep = DuelMemory.lastStepFrame(unit);
        if (makeWay(unit, now, lastStep)) return;
        // Zealot hold-fire discipline (NEXT #179): until a ranged weapon
        // fires inside the cluster, zealots hold instead of initiating -
        // the moment one is really hit, the formation breaks and it
        // attacks. Lone-zealot armies never hold (they would stare).
        if (unit.isZealot()
            && atlantis.battle.strategy.ZealotHoldFire.hold(unit)) return;
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

    /**
     * Post-swing settle, the observable half of {@link DuelMemory}'s
     * calibration (PurpleWave AttackDelay): while an attack animation
     * runs the start is recorded; after it closes, follow-up orders
     * wait until start + observed duration + 2, latency-aware. Before
     * any swing was ever seen there is nothing to settle. Public as a
     * seam: the window pins without running frames.
     */
    public static boolean attackSettleHold(AUnit unit) {
        if (unit == null || !unit.isAlive()) return false;
        try {
            if (unit.isAttackFrame() || unit.isStartingAttack()) {
                if (!DuelMemory.attackAnimOpen(unit)) {
                    DuelMemory.openAttackAnim(unit, atlantis.game.A.now());
                }
                return true;
            }
            if (DuelMemory.attackAnimOpen(unit)) {
                DuelMemory.closeAttackAnim(unit, atlantis.game.A.now());
            }
            Integer start = DuelMemory.lastAttackStart(unit);
            if (start == null) return false;
            int latency;
            try {
                latency = atlantis.units.special.SleepUntilFrameGate.latencyFrames();
            } catch (Exception ignored) {
                latency = 0;
            }
            return atlantis.game.A.now() + latency < start + DuelMemory.attackSettle(unit);
        } catch (Exception ignored) {
            return false;
        }
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
            && unit.move(aside, Actions.MOVE_AVOID, Behavior.MAKE_WAY.tag())) {
            FormationTwin.madeWay(unit, makeWay.escaper, aside);
            DuelMemory.stepped(unit, now);
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
            if (yield != null && unit.move(yield, Actions.MOVE_AVOID, Behavior.DUEL_YIELD.tag())) {
                KiteTwin.steppedAside(unit, threat);
                DuelMemory.stepped(unit, now);
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
            //
            // Zealot withdraw (NEXT #178): a withdrawing zealot has no
            // hold point - the formula above is built for ranged hold
            // and yields no step for melee-vs-melee, so the spent-weapon
            // intent died here (sim: 130/130 stuck-no-step, zero moves).
            // Take a full footwork step back instead of standing through
            // the cooldown; the cadence gate above keeps it to one step
            // per window, the direction still comes from MoveAway.
            double stepLen;
            if (unit.isZealot()
                && atlantis.battle.kite.ZealotWithdraw.should(unit, threat)) {
                stepLen = CombatTuning.current().getKiteStepTiles();
            }
            else {
                stepLen = Math.min(CombatTuning.current().getKiteStepTiles(),
                    Math.max(0.0, Kite.desiredKiteDistance(unit, threat) - unit.distTo(threat)));
            }
            APosition dest = stepLen > 0
                ? MoveAway.bestDestination(unit, threat, stepLen)
                : null;
            if (dest != null) dest = bendTowardSquad(unit, dest);
            if (dest != null && unit.move(dest, Actions.MOVE_AVOID, Behavior.KITE.tag())) {
                KiteTwin.steppedAside(unit, threat);
                DuelMemory.stepped(unit, now);
                return true;
            }
            KiteTwin.nowhereToStep(unit, threat);
        }
        return false;
    }

    /**
     * Arc retreat (NEXT #175): bend the straight kite step back toward the
     * squad instead of running straight away. Default blend is zero, so this
     * returns the step unchanged unless the sim or the tuner asks for the
     * arc - bit-identical straight kiting until then. Far units (beyond the
     * squad radius) and squadless units keep the straight step.
     */
    private static APosition bendTowardSquad(AUnit unit, APosition dest) {
        double blend;
        try {
            blend = atlantis.battle.escape.EscapeTuning.current().getArcBlend();
        } catch (Exception ignored) {
            return dest;
        }
        if (!(blend > 0)) return dest;
        try {
            // Own squad first; Alpha roster as the fallback (units never
            // get a back-ref assigned - Alpha.gather fills the roster only,
            // so unit.squad() reads null in games and in the sim).
            APosition center = null;
            atlantis.battle.cluster.Squad squad = unit.squad();
            if (squad != null && squad.isNotEmpty()) center = squad.median();
            if (center == null) {
                atlantis.battle.cluster.Squad alpha = atlantis.battle.cluster.Alpha.get();
                if (alpha != null && alpha.isNotEmpty()) center = alpha.median();
            }
            if (center == null) return dest;
            if (unit.distTo(center) > atlantis.battle.escape.EscapeArc.SQUAD_RADIUS_TILES) return dest;
            return atlantis.battle.escape.EscapeArc.bend(unit.position(), dest, center, blend);
        } catch (Exception ignored) {
            return dest;
        }
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
        Integer lastMoved = DuelMemory.lastMovedFrame(unit);
        APosition lastMovedAt = DuelMemory.lastMovedPos(unit);
        boolean trying = lastStep != null && now - lastStep < 48;
        boolean motionless = lastMoved != null && lastMovedAt != null
            && now - lastMoved >= 48 && unit.distTo(lastMovedAt) < 0.5;
        if (trying && motionless) {
            KiteTwin.frozenReset(unit, threat);
            unit.stop(Behavior.UNSTICK.tag());
            DuelMemory.forgetMotion(unit);
            return true;
        }
        if (lastMovedAt == null || unit.distTo(lastMovedAt) >= 0.5) {
            DuelMemory.noteMoved(unit, now, unit.position());
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
        // Kiting outranks trading (owner 2026-10-26): a glued unit
        // with a ready weapon used to stand and trade while being
        // chewed apart. Safety of the single unit comes first - the
        // margin rule above (which already scales with wounds) decides.
        // Only the in-flight swing still trades: damage locked in
        // cannot be dodged, so the free shot lands instead of a
        // donated step.
        boolean swingIncoming = threat.isAttackFrame() || threat.isStartingAttack();
        return unit.cooldown() == 0 && swingIncoming;
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
                    && unit.move(fireAt, Actions.MOVE_ENGAGE, Behavior.REENGAGE.tag())) {
                    DuelMemory.stepped(unit, now);
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
            && DuelMemory.repositionedAgo(unit) < CombatTuning.current().getKiteRideFrames()) return;
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
