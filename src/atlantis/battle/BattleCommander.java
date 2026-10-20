package atlantis.battle;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.battle.formation.FocusFire;
import atlantis.battle.formation.FormationSlots;
import atlantis.battle.formation.SharedEscape;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.actions.move.MoveAway;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.units.special.SleepUntilFrameGate;

/**
 * Owns every army unit each frame. Phase 0 of __PLAN-COMBAT step 5: one
 * cluster (Alpha), one posture (main base or enemy base), one gate
 * (evaluator fight/flee with a retreat latch). No per-unit decisions.
 */
public class BattleCommander extends Commander {

    private static int fleeStartedAt = -1;

    /**
     * The live combat coefficients (NEXT #158): one immutable snapshot,
     * replaced per game by the tuner, reset to defaults with the game.
     * Domain code reads but never owns it; the values and their
     * rationale live in {@link CombatTuning}.
     */
    private static volatile CombatTuning tuning = CombatTuning.defaults();

    /** The current combat coefficients; the tuner replaces these per game. */
    public static CombatTuning tuning() {
        return tuning;
    }

    /** Installs a derived snapshot - the tuner's only write path. */
    public static void installTuning(CombatTuning next) {
        if (next != null) tuning = next;
    }

    /** Per-game reset next to the other static resets in OnGameStarted. */
    public static void reset() {
        fleeStartedAt = -1;
        tuning = CombatTuning.defaults();
        FormationSlots.clearForTests();
        SharedEscape.clearForTests();
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
        gatherCluster();
        if (Alpha.get().isEmpty()) return false;

        ClusterMission.Posture posture = ClusterMission.postureFor(
            Count.ourCombatUnits(), breachForcesFight());
        if (posture == ClusterMission.Posture.ATTACK) {
            Missions.forceGlobalMissionAttack("cluster-attack");
        }
        else {
            Missions.forceGlobalMissionDefend("cluster-defend");
        }

        FightOrFlight.Tactic tactic = tactic();
        HasPosition anchor = posture == ClusterMission.Posture.ATTACK
            ? ClusterAnchor.attackAnchor() : ClusterAnchor.defendAnchor();

        // Throttled state line: the only per-game proof of what the
        // cluster does (posture, tactic, size, anchor). JSON-only, stderr.
        if (A.everyNthGameFrame(31)) {
            atlantis.util.log.EventLog.key(atlantis.util.log.LogKey.CLUSTER)
                .param("cluster", Alpha.get().size())
                .param("posture", posture.name())
                .param("tactic", tactic.name())
                .param("breached", breachForcesFight())
                .param("anchor", anchor == null ? "-" : anchor.toString())
                .log(A.now(), A.minSec());
        }

        for (AUnit unit : Alpha.get().selection().list()) {
            order(unit, tactic, anchor, Alpha.get().leader());
        }

        return false;
    }

    private FightOrFlight.Tactic tactic() {
        AUnit leader = Alpha.get().leader();
        AUnit assessor = vanguardAssessor(leader);
        double eval = assessor != null ? assessor.eval() : 0;

        // Breach rule (owner): raiders in the base mean fight - always. Two
        // workers chewing probes is not an evaluation the cluster can walk
        // away from, however lopsided the numbers read (the evaluator is
        // not even asked). The latch resets with it: a retreat ordered
        // before the breach does not drag the cluster home. The only
        // retreat on a breach is per-unit, below in act().
        // Same for a UMS opening: with no base anywhere, an early retreat
        // just feeds - stand the first 15 seconds and let the fight happen.
        if (breachForcesFight() || earlyUmsStandGround()) {
            fleeStartedAt = -1;
            return FightOrFlight.Tactic.FIGHT;
        }

        // Home ground holds: under two tiles from the main base the
        // cluster stands instead of fleeing (owner: retreating into the
        // mineral line donates the base). Individual kiting inside act()
        // still applies - this gates only the cluster tactic.
        if (clusterHoldsHomeGround()) {
            fleeStartedAt = -1;
            return FightOrFlight.Tactic.FIGHT;
        }

        FightOrFlight.Tactic tactic = FightOrFlight.decide(eval, A.now(), fleeStartedAt);
        if (tactic == FightOrFlight.Tactic.FLEE && fleeStartedAt < 0) {
            fleeStartedAt = A.now();
        }
        if (tactic == FightOrFlight.Tactic.FIGHT) {
            fleeStartedAt = -1;
        }
        return tactic;
    }

    /**
     * The UMS opening stand-ground (owner): on custom maps there is no
     * base to run to, so a retreat in the first 15 seconds is never the
     * answer - the unit would just feed. Public as a seam: the rule pins
     * in a stub state without running the frame.
     */
    public static boolean earlyUmsStandGround() {
        return atlantis.game.A.isUms() && atlantis.game.A.now() < tuning().getUmsNoRetreatFrames();
    }

    /**
     * Where a baseless retreat rallies (owner): a point away from the
     * nearest threat to regroup at, never the enemy base the mission
     * anchor would march it to. Null when nothing presses (then the
     * caller fights in place) or when no direction steps - same
     * fall-through contract as the kite escape. Public as a seam.
     */
    public static HasPosition regroupRallyPoint(AUnit unit) {
        if (unit == null || !unit.isAlive()) return null;
        AUnit threat = atlantis.battle.targeting.ASquadTargeting
            .nearestEngageableEnemy(unit, tuning().getRegroupThreatTiles());
        if (threat == null) return null;
        return MoveAway.bestDestination(unit, threat, tuning().getRegroupRallyTiles());
    }

    /**
     * The breach half of the tactic gate: raiders in the base force FIGHT
     * before any evaluation runs. Workers included, deliberately wider
     * than {@code EnemyUnitBreachedBase} (measured: both its paths filter
     * on combat units, so two worker-raiders chewing the base read as no
     * breach - exactly the flee this rule exists to forbid). Radius 8 is
     * the detector's own close-to-base radius; the shared detector stays
     * untouched (its other readers must not start firing on scouts).
     * Public as a seam: the rule pins in a stub state without running
     * the frame.
     */
    public static boolean breachForcesFight() {
        if (atlantis.information.enemy.EnemyUnitBreachedBase.someone()) return true;
        AUnit main = Select.main();
        if (main == null) return false;
        AUnit foe = main.enemiesNear().nearestTo(main);
        return foe != null && foe.distTo(main) <= 8;
    }

    /**
     * Stardust's desired-distance rule (MyDragoon::attackUnit), in tiles:
     * max range, reduced by half the gap between "how far the target
     * still is outside range" and "how far we walk before the weapon is
     * ready". A healthy Dragoon holds a tile inside max range against
     * melee-ranged threats (Stardust: -32px vs much-shorter range -
     * kiting to max edge donates the fight); a WOUNDED one skips the
     * closer and holds the max-range edge instead (PurpleWave hitsLeft
     * shape: the fewer passes it survives, the farther it stays).
     * Public as a seam: the formula pins without running the frame (see
     * `_AI/CHALLENGES/MicroKiting.md`, Stardust reference).
     */
    public static double desiredKiteDistance(AUnit unit, AUnit threat) {
        double myRange = unit.groundWeaponRange();
        double targetRange = unit.enemyWeaponRangeAgainstThisUnit(threat);
        double dist = unit.distTo(threat);
        double tilesPerFrame = unit.maxSpeed() / 32.0;
        double cooldownDistance = Math.max(0.0, unit.cooldown() - attackLatencyFrames() - 2) * tilesPerFrame;
        double desired = Math.min(myRange, myRange + (cooldownDistance - (dist - myRange)) / 2.0);
        if (targetRange <= myRange - tuning().getKiteMeleeEdgeTiles()) {
            double hitsLeft = (unit.hp() + unit.shields()) / tuning().getKiteZealotPassDamage();
            double closer = hitsLeft <= tuning().getKiteWoundedHitsLeft()
                ? tuning().getKiteWoundedEdgeInsetTiles() : tuning().getKiteMeleeCloserTiles();
            desired -= closer;
        }
        return Math.max(0.0, desired);
    }

    /**
     * Keep shooting the committed target while it is alive, hittable and
     * inside weapon range plus a short chase allowance - never re-pick
     * every frame just because another enemy stepped marginally nearer.
     * Per-frame re-picking restarts the attack animation faster than it
     * completes (Wine ZealDrag: 519 acquisitions for 19 kills), the same
     * churn both reference bots refuse: Stardust only allows the switch
     * when the old target is gone/invalid/too far at the predicted frame
     * (MyDragoon::isReady), PurpleWave issues only on a new or overdue
     * target (Commander.attack). Sits between the squad pick (shared
     * focus in small fights stays untouched) and the nearest re-pick
     * (still the answer when commitment ends). Public as a seam.
     */
    public static AUnit stickyCurrentTarget(AUnit unit) {
        if (unit == null) return null;
        AUnit current = unit.target();
        if (current == null || !current.isAlive()) return null;
        if (!unit.hasWeaponToAttackThisUnit(current)) return null;
        if (unit.distTo(current) > unit.groundWeaponRange() + tuning().getKiteStickyChaseTiles()) return null;
        return current;
    }

    /**
     * True when the cluster sits on the main base (under 2 tiles): FLEE
     * is not an option there. Public as a seam, pinned without a game.
     */
    public static boolean clusterHoldsHomeGround() {
        AUnit main = Select.main();
        if (main == null) return false;
        HasPosition center = Alpha.get().center();
        if (center == null) return false;
        return center.distTo(main) < 2;
    }

    /**
     * Who the gate listens to: the unit closest to an enemy within 15
     * tiles, else the leader. A leader at the back reads a different fight
     * than the vanguard taking contact (owner 2026-10-19: one cluster, one
     * area assessment - the gate answers for where the fight is, not for
     * where the leader stands). Public as a seam, pinned without a game.
     */
    public static AUnit vanguardAssessor(AUnit leader) {
        AUnit best = leader;
        double bestDist = Double.MAX_VALUE;
        for (AUnit unit : Alpha.get().selection().list()) {
            if (unit == null || !unit.isAlive()) continue;
            AUnit foe = atlantis.battle.targeting.ASquadTargeting.nearestEngageableEnemy(unit, 15);
            if (foe == null) continue;
            double dist = unit.distTo(foe);
            if (dist < bestDist) {
                best = unit;
                bestDist = dist;
            }
        }
        return best;
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
                    home = breachRallyPoint();
                }
                else {
                    // Formation Phase A: one shared escape direction per
                    // cluster (leader, ~10f cadence) instead of per-unit
                    // compass picks that break the line. Null falls back
                    // to the per-unit sampling, unchanged.
                    home = SharedEscape.rallyFor(
                        unit, leader, Alpha.get().selection(),
                        tuning().getRegroupRallyTiles(), tuning().getRegroupThreatTiles());
                    if (home == null) home = regroupRallyPoint(unit);
                }
                HasPosition point = gatherPointFor(unit, leader, home);
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
        if (breachForcesFight() && !unit.isAttackFrame() && !unit.isStartingAttack()
            && unit.hp() <= tuning().getBreachRetreatHp() && unit.cooldown() >= tuning().getBreachRetreatCooldown()) {
            HasPosition rally = breachRallyPoint();
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
        AUnit target = stickyCurrentTarget(unit);
        if (target == null) {
            target = FocusFire.sharedTarget(unit, 9);
        }
        if (target == null) {
            target = atlantis.battle.targeting.ASquadTargeting.useSquadTargetIfPossible(unit);
        }
        if (target == null) {
            target = atlantis.battle.targeting.ASquadTargeting.nearestEngageableEnemy(unit, 9);
        }
        if (target == null) {
            target = breachedBaseTarget(unit);
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
                .nearestEngageableEnemy(unit, tuning().getKiteEnemyTiles());
            HitTwin.sample(unit, threat != null ? threat : target);
            int now = A.now();
            Integer lastStep = lastStepFrame.get(unit.id());
            if (threat != null && shouldKite(unit, threat)) {
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
                boolean glued = unit.distTo(threat) < tuning().getKiteGlueTiles() && unit.cooldown() == 0;
                boolean tradeShot = unit.cooldown() == 0 && (glued || swingIncoming);
                if (!tradeShot && (lastStep == null || now - lastStep >= tuning().getKiteRestepsEveryFrames())) {
                    // Stardust desired distance: step exactly the gap to the
                    // hold point (capped at one full step), never a blind
                    // shove past the firing position - stepping 2.0 tiles
                    // from dist 3.5 lands at ~5.5, OUT of a 4.0 range, and
                    // the glide then donates the walk back. That overshoot
                    // is the measured "ucieka za daleko" shape; holding at
                    // the formula's point keeps shooting instead.
                    double stepLen = Math.min(tuning().getKiteStepTiles(),
                        Math.max(0.0, desiredKiteDistance(unit, threat) - unit.distTo(threat)));
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
            double standOff = unit.groundWeaponRange() - tuning().getKiteStandoffInsetTiles();
            if (unit.groundWeaponRange() > 1 && unit.distTo(target) > unit.groundWeaponRange()) {
                APosition fireAt = null;
                if (unit.distTo(target) > standOff + tuning().getKiteGlideDeadbandTiles()
                    && (lastStep == null || now - lastStep >= tuning().getKiteRestepsEveryFrames())) {
                    fireAt = target.position()
                        .translateTilesTowards(unit, unit.distTo(target) - standOff);
                    if (fireAt != null
                        && unit.distTo(fireAt) > tuning().getKiteGlideDeadbandTiles()
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
                && A.now() - lastRepositionFrame.getOrDefault(unit.id(), -1000) < tuning().getKiteRideFrames()) return;
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

        // No enemy in reach: take a formation slot when the cluster
        // forms up (3+ combat units, known pivot), else bunch on the
        // leader so the cluster advances (or retreats) as one instead
        // of a line (owner 2026-10-19: march and retreat both strung
        // out). The leader sets the pace; stragglers catch up instead
        // of everyone racing the anchor.
        HasPosition point = formationSlotFor(unit, leader, anchor);
        if (point == null) point = gatherPointFor(unit, leader, anchor);
        if (point != null && unit.move(point, Actions.MOVE_ENGAGE, "ClusterMove")) {
            lastRepositionFrame.put(unit.id(), A.now());
        }
    }

    /**
     * Formation slot for a unit with no enemy in reach (Phase A): the
     * crescent around the leader's nearest enemy. Null when the
     * formation does not apply (too few units, no pivot, pivot in
     * contact, no walkable slot) - the caller bunches on the leader.
     */
    private static HasPosition formationSlotFor(AUnit unit, AUnit leader, HasPosition anchor) {
        if (Alpha.get().size() < FormationSlots.MIN_CLUSTER_SIZE) return null;
        if (leader == null || !leader.isAlive()) return null;
        AUnit pivot = atlantis.battle.targeting.ASquadTargeting
            .nearestEngageableEnemy(leader, 30);
        if (pivot == null) return null;
        return FormationSlots.slotFor(unit, Alpha.get().selection(), pivot);
    }

    /**
     * Who a unit with no enemy in reach attacks when the base is breached:
     * without this a DEFEND cluster whose 9-tile reach misses the raiders
     * chewing our workers just bunches on the anchor while the base dies.
     * Public as a seam: the rule pins in a stub state without a game.
     *
     * <p>
     * // @ToDo: replace with a proper approach - threat-aware target
     * assignment owned by the cluster (which enemy, which responders,
     * what the rest holds), instead of this per-unit fallback. Ad-hoc
     * so DEFEND units answer base attacks at all. Race-condition note:
     * cluster/base/enemy positions are read across frames, so the anchor
     * can move under us between the 30-tile check and the attack order;
     * acceptable for a fallback, the proper approach must own the
     * assignment instead of racing it. Candidate bounding (today: any
     * armed enemy, which can still be a far scout when the breach is
     * elsewhere) belongs to the same rewrite.
     * </p>
     */
    public static AUnit breachedBaseTarget(AUnit unit) {
        if (unit == null || !unit.isAlive()) return null;
        AUnit breached = atlantis.information.enemy.EnemyUnitBreachedBase.get();
        if (breached == null || !breached.isAlive()) return null;

        atlantis.units.select.Selection enemies =
            atlantis.units.select.Select.enemy().combatUnits().havingWeapon();
        if (enemies.isEmpty()) return breached;

        AUnit base = atlantis.units.select.Select.ourBases().nearestTo(unit);
        AUnit foe;
        if (base != null && unit.distTo(base) <= 30) {
            foe = enemies.nearestTo(unit);
        }
        else {
            AUnit main = atlantis.units.select.Select.ourBases()
                .nearestTo(atlantis.information.generic.InitialMainPosition.initialMainPosition());
            foe = main == null ? breached : enemies.nearestTo(main);
        }
        if (foe == null) return breached;
        return unit.hasWeaponToAttackThisUnit(foe) ? foe : null;
    }

    /**
     * Where this unit moves when there is no enemy to engage: the leader
     * when straggling behind it, else the mission anchor. Public as a seam:
     * the bunching rule pins in a stub state without running the frame.
     */
    /**
     * Where a withdrawing unit rallies (owner): three tiles from the main
     * toward the main choke - a forward defensive spot, not the mineral
     * line (retreating into the miners donates the base, and the old
     * MissionDefendFocusPoint played exactly this role). Null-safe: no
     * main, no choke, or an unhostable point falls back to the defend
     * anchor, and a null anchor falls through to fighting in place.
     * Public as a seam: the rally pins in a stub state without a game.
     */
    public static HasPosition breachRallyPoint() {
        AUnit main = Select.main();
        if (main == null || main.position() == null) return ClusterAnchor.defendAnchor();
        try {
            atlantis.map.choke.AChoke choke =
                atlantis.map.choke.Chokes.mainChoke();
            if (choke == null || choke.center() == null) return ClusterAnchor.defendAnchor();
            APosition point = main.position()
                .translateTilesTowards(tuning().getBreachRallyTiles(), choke.center());
            if (point == null) return ClusterAnchor.defendAnchor();
            return point;
        }
        catch (RuntimeException ignored) {
            return ClusterAnchor.defendAnchor();
        }
    }

    public static HasPosition gatherPointFor(AUnit unit, AUnit leader, HasPosition anchor) {
        if (leader != null && unit != leader && unit.distTo(leader) > CLUSTER_GATHER_RADIUS_TILES) {
            return leader;
        }
        return anchor;
    }

    /** Tiles behind the leader before a unit turns back to rejoin. */
    private static final double CLUSTER_GATHER_RADIUS_TILES = 2;

    /**
     * A Dragoon steps back only while the closest threat sits inside its
     * safety margin (PurpleWave BeDragoonVsSlowlot, engine numbers from
     * the vendored jar: Dragoon range 4 tiles, cooldown 30, Zealot
     * range ~0 tiles, 16 damage per pass). Margin is the tiles the
     * threat still must close before it can hit us, minus half a tile
     * when the threat is coming for exactly this unit and minus half
     * a tile when it faces us. A healthy Dragoon holds to about one
     * tile and keeps shooting through the cooldown; a dying one
     * demands up to four. Dragoon-only: Zealots want contact, other
     * races are out of scope. Public as a seam: the rule pins in a
     * stub state.
     */
    public static boolean shouldKite(AUnit unit, AUnit threat) {
        if (unit == null || threat == null) return false;
        // Zealots trade contact, except on a spent weapon: with 7+ frames
        // of cooldown left a step back costs no DPS (owner direction).
        if (!unit.isDragoon() && !(unit.isZealot() && unit.cooldown() >= 7)) return false;
        if (unit.isDragoon()) {
            boolean lowHp = unit.hp() + unit.shields() < tuning().getDragoonLowHp();
            // Pinch retreat (owner): two or more melee enemies inside 2
            // tiles on a hurt Dragoon (hp + shields < 80) means the pocket
            // is closing - yield ground instead of standing into it.
            // Overrides the surrounded-stand below: standing trades only
            // when escape already failed, never while steps still open.
            // A step that finds nothing still falls through to the
            // glue/corner trading below, so a fully pinched Dragoon
            // shoots instead of idling.
            if (unit.hp() + unit.shields() < tuning().getDragoonPinchHp()
                && unit.enemiesNear().melee().inRadius(tuning().getDragoonPinchMeleeTiles(), unit).size()
                    >= tuning().getDragoonPinchMeleeCount()) return true;
            // Surrounded: three or more melee enemies on top of the Dragoon
            // means escape is impossible - stand and shoot instead of
            // donating the retreat (owner). Either the shot is almost
            // ready or the nearest enemy is far enough to trade with.
            if (unit.enemiesNear().melee().inRadius(3, unit).size() >= tuning().getDragoonSurroundedMeleeCount()
                && (unit.cooldown() <= tuning().getDragoonSurroundedStandCooldown()
                    || unit.distTo(threat) >= tuning().getDragoonSurroundedStandDistTiles())) return false;
            // Low-HP Dragoon holds fire unless the shot is safe: at least
            // 3.2 tiles of room or an imminent weapon (owner, converging
            // with Stardust: cooldown <= latency + 2 attacks instead of
            // stepping). Below half health the threatened step would only
            // delay the one shot that still matters.
            if (lowHp && unit.cooldown() <= tuning().getDragoonLowHpFireCooldown()) return false;
            // ...otherwise it yields ground early: holding fire means
            // stepping, never standing through the cooldown. Robust to
            // margin tuning - the margin gate alone already kites here at
            // default constants, this keeps it true whatever they become.
            if (lowHp
                && unit.distTo(threat) < tuning().getDragoonLowHpHoldTiles()
                && unit.cooldown() > tuning().getDragoonLowHpFireCooldown()) return true;
        }
        if (unit.distTo(threat) >= tuning().getKiteEnemyTiles()) return false;
        return kiteMargin(unit, threat) < kiteMarginRequired(unit);
    }

    /**
     * Tiles the threat still must close before it can hit us, minus
     * half a tile when its order target is exactly this unit and minus
     * half a tile when it faces us (the cheap binary form of
     * PurpleWave's commitment read: targeted plus facing-dot; ours
     * fires within a 1.1 rad window instead of scaling continuously).
     * Distances are center-based tiles, not edge pixels - about half
     * a tile more permissive than the original, Wine decides.
     */
    private static double kiteMargin(AUnit unit, AUnit threat) {
        double margin = unit.distTo(threat) - unit.enemyWeaponRangeAgainstThisUnit(threat);
        if (isHuntedBy(unit, threat)) margin -= tuning().getKiteHuntedTiles();
        if (unit.isOtherFacingThisUnit(threat)) margin -= tuning().getKiteFacedTiles();
        return margin;
    }

    /**
     * The threat's order target is exactly this unit (PurpleWave's
     * presumptive-target read: the threat is coming for us, not
     * passing by).
     */
    private static boolean isHuntedBy(AUnit unit, AUnit threat) {
        AUnit hunted = threat.orderTarget();
        return hunted != null && hunted.id() == unit.id();
    }

    /**
     * Tiles of margin a Dragoon demands before it yields ground:
     * about one while healthy, up to four while dying (PurpleWave:
     * max(32, 128 - 8 * hitsLeft) pixels, Zealot pass = 16 damage).
     */
    /**
     * Per-unit attack latency (NEXT #154): the engine's remaining command
     * latency, read live per decision - never a flat constant. The +2 at
     * the call site is the Stardust margin (issue frame + publish frame,
     * measured in {@code SleepUntilFrameGate}).
     */
    private static int attackLatencyFrames() {
        return SleepUntilFrameGate.latencyFrames();
    }

    private static double kiteMarginRequired(AUnit unit) {
        double hitsLeft = (unit.hp() + unit.shields()) / tuning().getKiteZealotPassDamage();
        return Math.max(tuning().getKiteMarginBaseTiles(),
            tuning().getKiteMarginDyingTiles() - tuning().getKiteMarginPerHitTiles() * hitsLeft);
    }

    /**
     * Kiting rules. The coefficients used to live here as public mutable
     * statics; they moved to {@link CombatTuning} (NEXT #158) and are read
     * through {@link #tuning()} below.
     */

    private static final java.util.Map<Integer, Integer> lastRepositionFrame = new java.util.HashMap<>();
    private static final java.util.Map<Integer, Integer> lastStepFrame = new java.util.HashMap<>();
    private static final java.util.Map<Integer, Integer> lastMovedFrame = new java.util.HashMap<>();
    private static final java.util.Map<Integer, APosition> lastMovedPos = new java.util.HashMap<>();

    public static void clearForTests() {
        tuning = CombatTuning.defaults();
        FormationSlots.clearForTests();
        SharedEscape.clearForTests();
        lastRepositionFrame.clear();
        lastStepFrame.clear();
        lastMovedFrame.clear();
        lastMovedPos.clear();
    }

    private void gatherCluster() {
        Selection army = Select.ourCombatUnits();
        if (army == null) return;
        for (AUnit unit : army.list()) {
            if (unit != null && unit.isAlive() && unit.squad() == null) {
                Alpha.get().addUnit(unit);
            }
        }
    }
}
