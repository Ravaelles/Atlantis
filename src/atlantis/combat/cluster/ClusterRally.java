package atlantis.combat.cluster;
import atlantis.battle.cluster.Alpha;

import atlantis.combat.formation.FormationSlots;
import atlantis.battle.kite.CombatTuning;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.move.MoveAway;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

/**
 * Where the cluster and its units move when they are not fighting
 * (extracted from BattleCommander): baseless regroup points away from
 * the threat, leader bunching on the march, formation slots when the
 * cluster forms up. Positions only, no orders - the caller moves.
 */
public final class ClusterRally {

    private ClusterRally() {
    }

    /** Tiles behind the leader before a unit turns back to rejoin. */
    private static final double CLUSTER_GATHER_RADIUS_TILES = 2;

    /**
     * A small army holds the main itself, never the halfway standoff
     * (owner order, lost game vs early Zerg: with fewer than this many
     * battle units the line stays within worker-help range of the main).
     */
    private static final int EARLY_ZERG_HOLD_MAX_ARMY = 5;

    /** Worker-help range of the main base (owner order: 6 tiles). */
    private static final double EARLY_ZERG_HOLD_TILES = 6;

    /**
     * An anchor this close to the main still counts as home: the
     * no-contact hold owns the unit instead of marching it out.
     */
    public static final double NO_CONTACT_HOME_TILES = 20;

    /**
     * Where a baseless retreat rallies (owner): a point away from the
     * nearest threat to regroup at, never the enemy base the mission
     * anchor would march it to. Null when nothing presses (then the
     * caller fights in place) or when no direction steps - same
     * fall-through contract as the kite escape. Public as a seam.
     */
    public static HasPosition regroupRallyPoint(AUnit unit) {
        if (unit == null || !unit.isAlive()) return null;

        AUnit threat = atlantis.combat.targeting.ASquadTargeting
            .nearestEngageableEnemy(unit, CombatTuning.current().getRegroupThreatTiles());
        if (threat == null) return null;

        return MoveAway.bestDestination(unit, threat, CombatTuning.current().getRegroupRallyTiles());
    }

    /**
     * Where a bigger-than-squad army forms up instead of running home
     * (owner 2026-10-26, from a lost game vs Protoss): RADIUS + 6 tiles
     * from the main choke center toward the main - the same depth every
     * choke standoff answers (owner 2026-10-25: always stand that far
     * back, so the enemy commits into the funnel). Retreating a 5+ army
     * into the base piles it onto the mineral line with no room to
     * re-engage - the choke standoff keeps the line facing out with room
     * behind it. Null-safe: no main, no choke, or an unhostable point
     * falls back to the caller (which keeps its own base rally). Public
     * as a seam: the point pins in a stub state without a game.
     */
    public static HasPosition chokeStandoffPoint() {
        HomeGround home = HomeGround.resolve();
        if (!home.isComplete()) return null;

        AUnit main = home.main();

        try {
            AChoke choke = home.mainChoke();

            APosition point = choke.center().translateTilesTowards(
                atlantis.combat.strategy.ChokeStandoff.standoffTiles(choke), main.position());
            if (point == null) return null;
            if (!point.isWalkable()) point = point.makeWalkable(6, 3, null);

            return point;
        }
        catch (RuntimeException ignored) {
            return null;
        }
    }

    /**
     * Where the line holds with no enemy to engage (owner 2026-10-26,
     * live game vs Protoss: a 2-unit line marched to the far choke
     * standoff with no contact and uncovered the base while Zealots ran
     * by - the standoff is the fighting point, not the waiting point).
     * Halfway between the main base and the main choke: close enough to
     * answer a breach, far enough out not to pile onto the mineral line.
     * Only for a home anchor - an attack anchor must still march out -
     * so the caller checks {@link #isHomeAnchor}. Null when there is no
     * main, no choke, or no room to stand. Public as a seam: the point
     * pins in a stub state without running the frame.
     */
    public static APosition noContactHoldPoint(HasPosition anchor) {
        HomeGround home = HomeGround.resolve();
        if (!home.isComplete()) return null;

        AUnit main = home.main();
        if (!isHomeAnchor(anchor, main)) return null;

        try {
            AChoke choke = home.mainChoke();

            APosition half = main.position().translatePercentTowards(50, choke.center());
            if (half == null) return null;
            if (!half.isWalkable()) half = half.makeWalkable(6, 3, null);

            if (holdsAtMain(main, half)) return main.position();

            return half;
        }
        catch (RuntimeException ignored) {
            return null;
        }
    }

    /**
     * The owner's "stay at the main" answers, both needing the halfway
     * standoff to sit outside worker-help range in the first place (a
     * short march is not leaving the base): a small army vs Zerg holds
     * the main so the miners can join (owner order, lost game vs early
     * Zerg), and the first-Dragoon gate holds it for a dragoon build
     * until its first Dragoon is finished (owner order 2026-10-26).
     * Far stands and non-Zerg matchups keep today's point.
     */
    private static boolean holdsAtMain(AUnit main, APosition half) {
        if (main.position().distTo(half) <= EARLY_ZERG_HOLD_TILES) return false;
        if (FirstDragoonGate.holdsAtMain()) return true;

        return atlantis.game.player.Enemy.zerg()
            && atlantis.units.select.Count.ourCombatUnits() < EARLY_ZERG_HOLD_MAX_ARMY;
    }

    /**
     * True when the mission anchor is home: the main-choke focus, or
     * anything standing near the main. An attack anchor (enemy base, far
     * unexplored start) is never home - the line must march out to it.
     */
    public static boolean isHomeAnchor(HasPosition anchor, AUnit main) {
        if (anchor == null || main == null || main.position() == null) return false;

        try {
            if (anchor instanceof atlantis.battle.advance.focus.AFocusPoint
                && ((atlantis.battle.advance.focus.AFocusPoint) anchor).isMainChoke()) {
                return true;
            }

            return anchor.distTo(main) <= NO_CONTACT_HOME_TILES;
        }
        catch (RuntimeException ignored) {
            return false;
        }
    }

    /**
     * Where this unit moves when there is no enemy to engage: the leader
     * when straggling behind it, else the mission anchor. On FLEE the
     * leader is never the answer (owner 2026-10-23): rallying stragglers
     * onto a forward-kiting leader marches them back into the fight,
     * possibly through the blocking force - everyone shares the home rally.
     * Public as a seam: the bunching rule pins in a stub state without
     * running the frame.
     */
    public static HasPosition gatherPointFor(AUnit unit, AUnit leader, HasPosition anchor, FightOrFlight.Tactic tactic) {
        if (tactic == FightOrFlight.Tactic.FLEE) return anchor;
        if (leader != null && unit != leader && unit.distTo(leader) > CLUSTER_GATHER_RADIUS_TILES) {
            return leader;
        }
        return anchor;
    }

    /**
     * Formation slot for a unit with no enemy in reach (Phase A): the
     * crescent around the leader's nearest enemy. Null when the
     * formation does not apply (too few units, no pivot, pivot in
     * contact, no walkable slot, or any FLEE) - the caller bunches on
     * the leader or rallies home instead of standing in an arc.
     *
     * <p>No visible pivot does not mean no direction (owner 2026-10-26):
     * on FIGHT the blind crescent still faces where the enemy is
     * expected - the last-seen enemy center on UMS, the main choke at
     * the home base on melee, the mission anchor past that - instead
     * of collapsing into a clump facing nowhere. FLEE never forms up:
     * arcs facing the pursuer pinned retreating armies in place
     * (ZealDrag_v_Zeal: static center through 550 frames of FLEE),
     * so the retreat owns movement instead. Truly blind on FIGHT
     * still bunches, honestly standing instead of guessing.</p>
     */
    public static HasPosition formationSlotFor(
        AUnit unit, AUnit leader, HasPosition anchor, FightOrFlight.Tactic tactic
    ) {
        if (Alpha.get().size() < FormationSlots.MIN_CLUSTER_SIZE) return null;
        if (leader == null || !leader.isAlive()) return null;
        if (tactic == FightOrFlight.Tactic.FLEE) return null;

        HasPosition guard = guardSlotFor(unit, anchor);
        if (guard != null) return guard;

        HasPosition pivot = atlantis.combat.targeting.ASquadTargeting
            .nearestEngageableEnemy(leader, 30);
        if (pivot == null && tactic == FightOrFlight.Tactic.FIGHT) {
            pivot = expectedDirectionPoint(leader, anchor);
        }
        if (pivot == null) return null;

        return FormationSlots.slotFor(unit, Alpha.get().selection(), pivot);
    }

    /**
     * PurpleWave guard (SquadDefendBase): at our main choke against
     * melee-only enemies the line plugs the mouth - melee in, ranged
     * behind - instead of taking the free crescent. Null when the setup
     * is not a guarded choke (duels, open field, ranged enemies, no
     * anchor): the crescent below owns those. Also null while the ghost
     * march owns the unit (known enemy elsewhere to march at): the plug
     * is the formation answer when there is nowhere to go, never a
     * leash against the pursuit.
     */
    private static HasPosition guardSlotFor(AUnit unit, HasPosition anchor) {
        if (!(anchor instanceof atlantis.battle.advance.focus.AFocusPoint)) return null;
        if (!((atlantis.battle.advance.focus.AFocusPoint) anchor).isMainChoke()) return null;

        try {
            Selection foes = Select.enemy().combatUnits();
            if (foes == null || foes.empty() || foes.ranged().notEmpty()) return null;

            APosition march = atlantis.combat.strategy.GhostAdvance.effectiveGhostPointFor(unit);
            if (march != null) {
                try {
                    if (unit.distTo(march) >= 2) return null;
                } catch (Exception ignored) {
                    return null;
                }
            }

            HomeGround home = HomeGround.resolve();
            if (!home.isComplete()) return null;

            AUnit main = home.main();
            AChoke choke = home.mainChoke();

            APosition plug = atlantis.combat.formation.ChokePlug.plugPointFor(choke, main);
            if (plug == null) return null;

            return atlantis.combat.formation.ChokePlug.slotFor(
                unit, Alpha.get().selection(), plug, choke.center(), choke.width() / 2.0);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    /**
     * Where the enemy is expected when nobody sees it: the whole ghost
     * chain (fresh contact, then fogged memory, then unexplored ground)
     * on UMS; the main choke when the cluster holds the home base on
     * melee; the mission anchor past that (the march direction itself).
     * Null-safe by construction - the choke library and the ghost chain
     * both answer null outside their world.
     */
    private static HasPosition expectedDirectionPoint(AUnit leader, HasPosition anchor) {
        try {
            if (atlantis.game.A.isUms()) {
                // The effective ghost, not the newest memory: a reached
                // kill site faces the line at its own feet instead of
                // at where the fight lives on.
                APosition ghost = atlantis.combat.strategy.GhostAdvance.effectiveGhostPointFor(leader);
                if (ghost != null) return ghost;
            }
            else if (atlantis.combat.cluster.ClusterTactic.holdsHomeGround()) {
                atlantis.map.choke.AChoke choke =
                    atlantis.combat.cluster.HomeGround.resolve().mainChoke();
                if (choke != null && choke.center() != null) return choke.center();
            }
        } catch (Exception ignored) {
        }

        // NEXT #208: an anchor behind the army faces the formation at our
        // own base. The anchor is where to stand, not where to look: when
        // the army sits past it (away from home) the expected contact is
        // outward, not back at the anchor. At the anchor the old answer
        // stands (a blind army bunches honestly instead of guessing).
        if (anchorBehindArmy(leader, anchor)) return outwardPoint(leader);

        return anchor;
    }

    /**
     * True when the army stands meaningfully past its anchor, away from
     * home: the anchor is behind, so facing it cups our own base. The
     * 2-tile deadband is the holdsHomeGround granularity - standing on
     * the anchor is not "past it".
     */
    private static final int ANCHOR_BEHIND_DEADBAND_TILES = 2;

    /**
     * How far past the leader the outward pivot sits: beyond contact
     * (the 3.3-tile formation floor) and inside the 11-tile arc cap, so
     * the crescent that forms there is a real shape.
     */
    private static final int OUTWARD_PIVOT_TILES = 8;

    private static boolean anchorBehindArmy(AUnit leader, HasPosition anchor) {
        if (leader == null || anchor == null) return false;

        try {
            AUnit main = Select.main();
            if (main == null || main.position() == null) return false;

            return leader.distTo(main) > anchor.distTo(main) + ANCHOR_BEHIND_DEADBAND_TILES;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static HasPosition outwardPoint(AUnit leader) {
        try {
            AUnit main = Select.main();
            if (leader == null || main == null || main.position() == null) return null;

            APosition l = leader.position();
            APosition m = main.position();
            double dx = l.x() - m.x();
            double dy = l.y() - m.y();
            double len = Math.hypot(dx, dy);
            if (len < 1) return null;

            double k = OUTWARD_PIVOT_TILES * 32.0 / len;
            return APosition.createFromPixels(
                (int) (l.x() + dx * k),
                (int) (l.y() + dy * k));
        } catch (Exception ignored) {
            return null;
        }
    }
}
