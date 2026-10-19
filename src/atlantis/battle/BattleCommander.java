package atlantis.battle;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
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

    private static int fleeStartedAt = -1;

    /** Per-game reset next to the other static resets in OnGameStarted. */
    public static void reset() {
        fleeStartedAt = -1;
    }

    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{};
    }

    @Override
    protected boolean handle() {
        gatherCluster();
        if (Alpha.get().isEmpty()) return false;

        ClusterMission.Posture posture = ClusterMission.decide(Count.ourCombatUnits());
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
        if (breachForcesFight()) {
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
            HasPosition home = breachRallyPoint();
            HasPosition point = gatherPointFor(unit, leader, home);
            if (point != null && unit.distTo(point) >= 0.2) {
                unit.move(point, Actions.RUN_ENEMY, "ClusterFlee");
                return;
            }
            // Nowhere to run to (baseless map, null anchor): standing
            // still with DoNothing until death is never the answer -
            // fall through and fight in place instead (kite included).
        }

        // The only retreat allowed on a breached base (owner): a unit this
        // close to death with no shot ready may leave - everything else
        // fights. Per-unit, never the cluster: tactic() above already
        // committed the cluster to FIGHT, this is the individual hatch.
        if (breachForcesFight()
            && unit.hp() <= BREACH_RETREAT_HP && unit.cooldown() >= BREACH_RETREAT_COOLDOWN) {
            HasPosition rally = breachRallyPoint();
            if (rally != null && unit.distTo(rally) >= 0.2) {
                unit.move(rally, Actions.RUN_ENEMY, "ClusterBreachRetreat");
                return;
            }
        }

        AUnit target = atlantis.battle.targeting.ASquadTargeting.useSquadTargetIfPossible(unit);
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
                .nearestEngageableEnemy(unit, KITE_ENEMY_TILES);
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
                boolean glued = unit.distTo(threat) < KITE_GLUE_TILES && unit.cooldown() == 0;
                boolean tradeShot = unit.cooldown() == 0 && (glued || swingIncoming);
                if (!tradeShot && (lastStep == null || now - lastStep >= KITE_RESTEPS_EVERY_FRAMES)) {
                    APosition dest = MoveAway.bestDestination(unit, threat, KITE_STEP_TILES);
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
            double standOff = unit.groundWeaponRange() - KITE_STANDOFF_INSET_TILES;
            if (unit.groundWeaponRange() > 1 && unit.distTo(target) > unit.groundWeaponRange()) {
                APosition fireAt = null;
                if (unit.distTo(target) > standOff + KITE_GLIDE_DEADBAND_TILES
                    && (lastStep == null || now - lastStep >= KITE_RESTEPS_EVERY_FRAMES)) {
                    fireAt = target.position()
                        .translateTilesTowards(unit, unit.distTo(target) - standOff);
                    if (fireAt != null
                        && unit.distTo(fireAt) > KITE_GLIDE_DEADBAND_TILES
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
                && A.now() - lastRepositionFrame.getOrDefault(unit.id(), -1000) < KITE_RIDE_FRAMES) return;
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

        // No enemy in reach: bunch on the leader so the cluster advances
        // (or retreats) as one instead of a line (owner 2026-10-19: march
        // and retreat both strung out). The leader sets the pace; stragglers
        // catch up instead of everyone racing the anchor.
        HasPosition point = gatherPointFor(unit, leader, anchor);
        if (point != null && unit.move(point, Actions.MOVE_ENGAGE, "ClusterMove")) {
            lastRepositionFrame.put(unit.id(), A.now());
        }
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
                .translateTilesTowards(BREACH_RALLY_TILES, choke.center());
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
        if (unit.distTo(threat) >= KITE_ENEMY_TILES) return false;
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
        if (isHuntedBy(unit, threat)) margin -= KITE_HUNTED_TILES;
        if (unit.isOtherFacingThisUnit(threat)) margin -= KITE_FACED_TILES;
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
    private static double kiteMarginRequired(AUnit unit) {
        double hitsLeft = (unit.hp() + unit.shields()) / KITE_ZEALOT_PASS_DAMAGE;
        return Math.max(KITE_MARGIN_BASE_TILES,
            KITE_MARGIN_DYING_TILES - KITE_MARGIN_PER_HIT_TILES * hitsLeft);
    }

    /**
     * Kiting coefficients, public and mutable on purpose (owner: the
     * ParamTweaker randomizes them in-JVM and scores games to CSV, so
     * tuning needs no rebuild and no external file). Defaults are the
     * engine-faithful values (PurpleWave margins, jar ranges); the
     * tuner only explores around them. Long term these become a typed
     * profile struct (see `_AI/CHALLENGES/MicroKiting.md`); scattering
     * mutable statics is the deliberate short-term shape, not the
     * architecture.
     */
    /** Enemies inside this range count as kiting threats (PurpleWave threatsInPixels(160)). */
    public static double KITE_ENEMY_TILES = 5.0;

    /** At or under this threat distance the duel is glue: bodies overlap, no step opens. */
    public static double KITE_GLUE_TILES = 0.5;

    /** How far the kiting step carries (tiles). */
    public static double KITE_STEP_TILES = 2.0;

    /** A step candidate has to open at least this much beyond the current distance (tiles). Anti-jitter only: above a body radius, below any real sidestep. */
    public static double KITE_MIN_STEP_GAIN_TILES = 0.3;

    /** Threat committed to exactly this unit shrinks the margin by this much (tiles). */
    public static double KITE_HUNTED_TILES = 0.5;

    /** Threat facing this unit shrinks the margin by this much (tiles). */
    public static double KITE_FACED_TILES = 0.5;

    /** Margin a full-health Dragoon demands before yielding (tiles). */
    public static double KITE_MARGIN_BASE_TILES = 1.0;

    /** Margin a dying Dragoon demands before yielding (tiles). */
    public static double KITE_MARGIN_DYING_TILES = 4.0;

    /** Each survived Zealot pass lowers the demanded margin by this much (tiles). */
    public static double KITE_MARGIN_PER_HIT_TILES = 0.25;

    /** One Zealot pass deals this much damage (2x8 swings; jar reports 8x1). */
    public static double KITE_ZEALOT_PASS_DAMAGE = 16.0;

    /** Firing point sits this far inside our max range (tiles). */
    public static double KITE_STANDOFF_INSET_TILES = 0.5;

    /** Glide deadband around the firing point (tiles). */
    public static double KITE_GLIDE_DEADBAND_TILES = 0.5;

    /** A reposition keeps riding this long before the attack re-arms (frames). */
    public static int KITE_RIDE_FRAMES = 30;

    /** A step or glide re-issues at most this often (frames, Stardust latency+6 cadence). */
    public static int KITE_RESTEPS_EVERY_FRAMES = 10;

    /** Breach retreat hatch (owner): at or under this hp a unit may leave a breached base. */
    public static int BREACH_RETREAT_HP = 40;

    /** ... and only with at least this much cooldown left (no shot to give). */
    public static int BREACH_RETREAT_COOLDOWN = 5;

    /** Rally stands this far from the main toward the main choke (tiles). */
    public static double BREACH_RALLY_TILES = 3;

    private static final java.util.Map<Integer, Integer> lastRepositionFrame = new java.util.HashMap<>();
    private static final java.util.Map<Integer, Integer> lastStepFrame = new java.util.HashMap<>();
    private static final java.util.Map<Integer, Integer> lastMovedFrame = new java.util.HashMap<>();
    private static final java.util.Map<Integer, APosition> lastMovedPos = new java.util.HashMap<>();

    public static void clearForTests() {
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
