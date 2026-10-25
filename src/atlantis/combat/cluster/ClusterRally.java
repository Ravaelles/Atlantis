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
     * (owner 2026-10-26, from a lost game vs Protoss): 8 tiles from the
     * main choke center toward the main. Retreating a 5+ army into the
     * base piles it onto the mineral line with no room to re-engage -
     * the choke standoff keeps the line facing out with room behind it.
     * Null-safe: no main, no choke, or an unhostable point falls back
     * to the caller (which keeps its own base rally). Public as a seam:
     * the point pins in a stub state without a game.
     */
    public static HasPosition chokeStandoffPoint() {
        AUnit main = Select.main();
        if (main == null || main.position() == null) return null;

        try {
            AChoke choke = Chokes.mainChoke();
            if (choke == null || choke.center() == null) return null;

            APosition point = choke.center().translateTilesTowards(
                CombatTuning.current().getChokeStandoffTiles(), main.position());
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
        AUnit main = Select.main();
        if (main == null || main.position() == null) return null;
        if (!isHomeAnchor(anchor, main)) return null;

        try {
            AChoke choke = Chokes.mainChoke();
            if (choke == null || choke.center() == null) return null;

            APosition half = main.position().translatePercentTowards(50, choke.center());
            if (half == null) return null;
            if (!half.isWalkable()) half = half.makeWalkable(6, 3, null);

            return half;
        }
        catch (RuntimeException ignored) {
            return null;
        }
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

            AUnit main = Select.main();
            if (main == null || main.position() == null) return null;

            AChoke choke = Chokes.mainChoke();
            if (choke == null || choke.center() == null) return null;

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
                atlantis.map.choke.AChoke choke = atlantis.map.choke.Chokes.mainChoke();
                if (choke != null && choke.center() != null) return choke.center();
            }
        } catch (Exception ignored) {
        }
        return anchor;
    }
}
