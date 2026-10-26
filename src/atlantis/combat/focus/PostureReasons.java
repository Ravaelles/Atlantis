package atlantis.combat.focus;

import atlantis.combat.cluster.ClusterMission;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

/**
 * One place deciding which ground the army wants, and why (NEXT #197).
 * The old mission changer answered the same question with thirty
 * interleaved conditions and no owner; each reason below owns its
 * shape, its numbers carry their provenance, and the order is explicit:
 * the lost-attack fallback first, the standing headcount next, the
 * early-game flips next, hold last.
 *
 * <p>Thresholds: the trapped line is sim-measured
 * ({@code TrappedDragoonsAttackOutTest}: hold loses dragoons at 6 and 8
 * zealots, wins at 4; dragoon floor and the 650 s window ride the
 * live-proven old class, GoWithGoonz shape). The banked line rides the
 * old StrongAndBig/beBrave shape (four-digit bank behind a real army);
 * a below-headcount banked flip has no provenance and is deliberately
 * not implemented.</p>
 */
public final class PostureReasons {

    private PostureReasons() {
    }

    /** A bank that buys its own army while the attack runs. */
    public static final int BANKED_MINERALS = 1000;

    /**
     * Owner order 2026-10-26: a small but relatively strong team marches
     * instead of holding - 3+ units at 140%+ relative strength. Strength
     * is relative (ours/enemy, 999 vs an unscouted map), so this fires
     * blind on 3 units; that is the order, not an accident (§3 challenge
     * recorded in the landing commit: it overrides the live-proven
     * #200/#205 small-army holds).
     */
    public static final int EARLY_MARCH_ARMY_SIZE = 3;

    /** Relative-strength floor shared by both early-march flips. */
    public static final int EARLY_MARCH_STRENGTH = 140;

    /** Dragoon pair that kites instead of holding (owner order). */
    public static final int DRAGOON_PAIR_COUNT = 2;

    /**
     * Lost-attack fallback (owner order 2026-10-26): the attack found a
     * stronger army - at most one dragoon left and 120% or less relative
     * strength means coming home instead of feeding. Applies only while
     * attacking (it ends attacks, never prevents them).
     */
    public static final int FALLBACK_STRENGTH = 120;

    /** Dragoons that still count as teeth for the fallback. */
    public static final int FALLBACK_MAX_DRAGOONS = 1;

    /** One decision with its reason. Posture is a direction, never a tile. */
    public static final class Verdict {
        public final ClusterMission.Posture posture;
        public final PostureReason reason;
        /** Human sentence for the CherryVis board, with the deciding numbers. */
        public final String text;

        Verdict(PostureReason reason, ClusterMission.Posture posture) {
            this(reason, posture, reason.name());
        }

        public Verdict(PostureReason reason, ClusterMission.Posture posture, String text) {
            this.reason = reason;
            this.posture = posture;
            this.text = text;
        }
    }

    /**
     * Pure decision: posture plus the reason, from explicit inputs so it
     * pins without a game. The live inputs ride {@link #decideLive}.
     */
    public static Verdict decide(
        int ours, int ourDragoons, int enemyZealots, int minerals, int gameSeconds,
        int strength, boolean dragoonMatchup, boolean wasAttacking, String matchup
    ) {
        if (wasAttacking && ourDragoons <= FALLBACK_MAX_DRAGOONS && strength <= FALLBACK_STRENGTH) {
            return new Verdict(PostureReason.OUTGUNNED_SO_COME_HOME, ClusterMission.Posture.DEFEND,
                "Outgunned (" + ourDragoons + " goon, " + strength + "%), coming home");
        }

        if (ours >= ClusterMission.ATTACK_ARMY_SIZE) {
            if (minerals >= BANKED_MINERALS) {
                return new Verdict(PostureReason.BANKED_MINERALS, ClusterMission.Posture.ATTACK,
                    "Banked " + minerals + ", attack");
            }

            return new Verdict(PostureReason.HEADCOUNT, ClusterMission.Posture.ATTACK,
                "Headcount ok (" + ours + " units)");
        }

        if (ourDragoons >= DRAGOON_PAIR_COUNT && strength >= EARLY_MARCH_STRENGTH && dragoonMatchup) {
            return new Verdict(PostureReason.EARLY_DRAGOONS_OK_SO_PRESSURE, ClusterMission.Posture.ATTACK,
                "Early dragoons ok (" + ourDragoons + " goons, " + strength + "%), pressure");
        }

        if (ours >= EARLY_MARCH_ARMY_SIZE && strength >= EARLY_MARCH_STRENGTH) {
            return new Verdict(PostureReason.EARLY_STRENGTH, ClusterMission.Posture.ATTACK,
                "Early strength ok (" + ours + " units, " + strength + "%)");
        }

        if (atlantis.combat.focus.TrappedByMassZealots.applies(ours, ourDragoons, enemyZealots, gameSeconds)) {
            return new Verdict(PostureReason.TRAPPED_BY_MASS_ZEALOTS, ClusterMission.Posture.ATTACK,
                "Trapped by mass zealots (" + ours + " vs " + enemyZealots + ")");
        }

        return new Verdict(PostureReason.HOLD_GROUND, ClusterMission.Posture.DEFEND,
            holdText(ours, ourDragoons, strength, dragoonMatchup, matchup));
    }

    private static String holdText(
        int ours, int ourDragoons, int strength, boolean dragoonMatchup, String matchup
    ) {
        if (ourDragoons >= DRAGOON_PAIR_COUNT && strength >= EARLY_MARCH_STRENGTH && !dragoonMatchup) {
            return "No pair pressure (ranged " + matchup + ")";
        }
        if (ourDragoons < DRAGOON_PAIR_COUNT) {
            return "Not enough Goons (" + ourDragoons + "/" + DRAGOON_PAIR_COUNT + ") " + matchup;
        }
        if (strength < EARLY_MARCH_STRENGTH) {
            return "Too weak (" + strength + "%<" + EARLY_MARCH_STRENGTH + "%) " + matchup;
        }
        return "Hold ground";
    }

    /** The live reading behind {@link #decide}: counts, bank and clock. */
    public static Verdict decideLive() {
        boolean enemyKnown = Select.enemy().combatUnits().notEmpty();
        boolean vsTerran = atlantis.game.player.Enemy.terran();
        boolean vsZerg = atlantis.game.player.Enemy.zerg();
        boolean protossWithoutRanged = atlantis.game.player.Enemy.protoss() && enemyKnown
            && Select.enemy().combatUnits().ranged().empty();
        String matchup = vsZerg ? "vZ"
            : atlantis.game.player.Enemy.protoss() ? "vP" : vsTerran ? "vT" : "?";
        return decide(
            Count.ourCombatUnits(),
            Count.dragoons(),
            EnemyUnits.zealots(),
            A.minerals(),
            A.s,
            atlantis.information.generic.Army.strength(),
            vsZerg || vsTerran || protossWithoutRanged,
            atlantis.battle.cluster.Missions.globalMission().isMissionAttack(),
            matchup);
    }
}
