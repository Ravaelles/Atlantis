package atlantis.combat.cluster;

import atlantis.combat.strategy.BattleStrategy;
import atlantis.combat.strategy.BattleSetup;
import atlantis.combat.strategy.ChokeStandoff;
import atlantis.combat.strategy.DuelKite;
import atlantis.map.choke.AChoke;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;

/**
 * Which approach the cluster fights under (owner 2026-11-09, approach
 * narrowing slice 1): the task instantiated for the frame, narrowed by
 * an explicit geometry snapshot - snapshot in, approach out, no live
 * reads inside the decision.
 *
 * <p>Slice 1 is vocabulary + the snapshot boundary only: {@link #forTask}
 * answers the per-mission default from the MDC §2 table (holds stand,
 * stages and attacks march), {@link Geometry#capture} isolates every
 * live read {@code BattleStrategies.pick} does today into one plain
 * record. Nothing reads the answer yet - the narrowing slices widen
 * {@code forTask(task, geometry)} one geometry shape at a time (contact
 * shapes first), with the commander port last. Until then posture,
 * tactic and strategy keep deciding on their own.</p>
 */
public final class Approach {

    private Approach() {
    }

    /**
     * The detected setup every approach decides on, as plain numbers
     * and flags. Captured once per frame at the call site; the decision
     * below never touches live state.
     */
    public static final class Geometry {

        /** Any bases finished or under construction. */
        public boolean hasBases;
        /** Ours + theirs combat units on the map. */
        public int combatants;
        /** A choke within standoff detection range of the unit. */
        public boolean chokeNear;
        public boolean chokeIsMain;
        public boolean chokeIsNatural;
        /** Headcounts around that choke (contest radius), 0 when none. */
        public int enemiesNearChoke;
        public int oursNearChoke;
        /** Relative army strength (breakout doctrine input). */
        public int armyStrength;
        /** Any enemy near the unit right now. */
        public boolean inContact;
        /** Duel scope is dragoon-only (NEXT #169). */
        public boolean isDragoon;

        public boolean crushing() {
            return ChokeStandoff.mayAdvance(oursNearChoke, enemiesNearChoke);
        }

        public boolean isDuel() {
            return !hasBases && combatants <= DuelKite.MAX_DUEL_COMBATANTS;
        }
    }

    /**
     * The per-mission default approach (MDC §2 hypothesis): holds stand
     * the choke, stages and attacks march the open field. Geometry
     * narrowing lands slice-by-slice on top of these defaults.
     */
    public static BattleStrategy forTask(ClusterTask task) {
        if (task == null) return BattleStrategy.CHOKE_HOLD;

        switch (task.mission()) {
            case HOLD_MAIN:
            case HOLD_MAIN_CHOKE:
            case HOLD_NATURAL_CHOKE:
            case HOLD_MIDDLE:
                return BattleStrategy.CHOKE_HOLD;
            case STAGE_MIDDLE:
            case ATTACK_ENEMY_BASE:
                return BattleStrategy.OPEN_FIELD;
            default:
                return BattleStrategy.CHOKE_HOLD;
        }
    }

    /**
     * Slice 2 (2026-11-09): the first geometry narrowing. A hold
     * at a contested choke the line crushes joins the bait at the
     * funnel mouth instead of standing the standoff (the collapse
     * trigger, mirrored from the live pick); anything else keeps the
     * mission default above. ATTACK keeps OPEN_FIELD even crushing -
     * attack shapes get their own slice once observed. Bait
     * detachment itself stays with the live pick (its pins stand).
     */
    public static BattleStrategy forTask(ClusterTask task, Geometry geometry) {
        if (geometry != null && geometry.chokeNear && geometry.enemiesNearChoke > 0 && geometry.crushing()) {
            if (task != null) {
                switch (task.mission()) {
                    case HOLD_MAIN:
                    case HOLD_MAIN_CHOKE:
                    case HOLD_NATURAL_CHOKE:
                    case HOLD_MIDDLE:
                        return BattleStrategy.COLLAPSE;
                    default:
                        break;
                }
            }
            else {
                return BattleStrategy.COLLAPSE;
            }
        }

        return forTask(task);
    }

    /**
     * The snapshot boundary: every live read the current pick does,
     * in one place, defensive (no map data in tests - missing reads
     * are defaults, never exceptions). Pure code takes the record,
     * never the game.
     */
    public static Geometry capture(AUnit unit, HasPosition anchor) {
        Geometry g = new Geometry();
        if (unit == null) return g;

        try {
            g.hasBases = atlantis.units.select.Count.bases() > 0;
        } catch (Exception ignored) {
        }
        try {
            g.combatants = BattleSetup.combatants();
        } catch (Exception ignored) {
        }
        try {
            g.armyStrength = atlantis.information.generic.Army.strength();
        } catch (Exception ignored) {
        }
        try {
            g.isDragoon = unit.isDragoon();
        } catch (Exception ignored) {
        }
        try {
            g.inContact = !unit.enemiesNear().isEmpty();
        } catch (Exception ignored) {
        }

        AChoke choke = null;
        try {
            choke = BattleSetup.chokeNear(unit);
        } catch (Exception ignored) {
        }
        if (choke == null || choke.center() == null) return g;
        g.chokeNear = true;

        try {
            g.chokeIsMain = choke.isMainChoke();
        } catch (Exception ignored) {
        }
        try {
            g.chokeIsNatural = choke.isNaturalChoke();
        } catch (Exception ignored) {
        }
        try {
            g.enemiesNearChoke = BattleSetup.enemiesNear(choke.center(), ChokeStandoff.CONTEST_TILES);
        } catch (Exception ignored) {
        }
        try {
            g.oursNearChoke = BattleSetup.oursNear(choke.center(), ChokeStandoff.CONTEST_TILES);
        } catch (Exception ignored) {
        }

        return g;
    }
}
