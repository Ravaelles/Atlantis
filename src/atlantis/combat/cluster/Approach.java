package atlantis.combat.cluster;

import atlantis.combat.strategy.BattleStrategies;
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
        /** How many (breakout doctrine counts them). */
        public int basesCount;
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
     * Slice 3 (2026-11-09): duel kiting + the faithfulness guard.
     * A dragoon in a tiny baseless fight kites the duel whatever the
     * mission names (mirrored from the live pick, which never knew
     * the mission); the collapse needs bases like the live rule
     * (baseless fights kite, they never collapse onto a bait).
     */
    public static BattleStrategy forTask(ClusterTask task, Geometry geometry) {
        if (geometry != null) {
            if (geometry.isDuel() && geometry.isDragoon) return BattleStrategy.DUEL_KITE;

            if (breakout(geometry)) {
                if (task != null) {
                    switch (task.mission()) {
                        case HOLD_MAIN:
                        case HOLD_MAIN_CHOKE:
                        case HOLD_NATURAL_CHOKE:
                        case HOLD_MIDDLE:
                            return BattleStrategy.OPEN_FIELD;
                        default:
                            break;
                    }
                }
                else {
                    return BattleStrategy.OPEN_FIELD;
                }
            }

            if (geometry.hasBases
                && geometry.chokeNear
                && geometry.enemiesNearChoke > 0
                && geometry.crushing()) {
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
        }

        return forTask(task);
    }

    /**
     * Home-choke breakout (owner 2026-10-29, mirrored from the live
     * pick): at 150%+ strength with fewer than 2 bases the line never
     * holds its own main or natural choke - it marches out instead of
     * parking a finished army at home. The choke near the unit
     * decides, like the live rule.
     */
    private static boolean breakout(Geometry g) {
        if (!g.chokeNear || (!g.chokeIsMain && !g.chokeIsNatural)) return false;
        if (g.basesCount >= 2) return false;

        return g.armyStrength >= BattleStrategies.BREAKOUT_STRENGTH;
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
            g.basesCount = atlantis.units.select.Count.bases();
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
