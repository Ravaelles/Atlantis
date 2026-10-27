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
 * the zealot-vs-Zerg hold first, the lost-attack fallback next,
 * the standing headcount next, the early-game flips next, hold last.
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

    /**
     * A bank that buys its own army while the attack runs.
     */
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
    /**
     * Relative-strength floor shared by both early-march flips.
     */
    public static final int EARLY_MARCH_STRENGTH = 140;
    /**
     * Dragoon pair that kites instead of holding (owner order).
     */
    public static final int DRAGOON_PAIR_COUNT = 2;
    /**
     * Owner order 2026-10-27: vs Terran we pressure early - 2 combat
     * units or a single finished Dragoon march out under the same
     * strength floor as every other early attack.
     */
    public static final int TERRAN_EARLY_ARMY = 2;
    public static final int TERRAN_EARLY_GOONS = 1;
    /**
     * Owner order 2026-10-27: a discovered enemy Dragoon mass outguns a
     * small line - below this many own Dragoons and below this eval the
     * composition cannot decide yet, so the army comes home first.
     */
    public static final int DRAGOON_MASS_COUNT = 4;
    public static final double HOLD_VS_DRAGOON_EVAL = 1.3;
    /**
     * Lost-attack fallback (owner order 2026-10-26): the attack found a
     * stronger army - at most one dragoon left and 120% or less relative
     * strength means coming home instead of feeding. Applies only while
     * attacking (it ends attacks, never prevents them).
     */
    public static final int FALLBACK_STRENGTH = 120;
    /**
     * Dragoons that still count as teeth for the fallback.
     */
    public static final int FALLBACK_MAX_DRAGOONS = 1;

    private PostureReasons() {
    }

    /**
     * Pure decision: posture plus the reason, from explicit inputs so it
     * pins without a game. The live inputs ride {@link #decideLive}.
     */
    public static Verdict decide(Input in) {
        if (in.enemyDragoons > 0 && in.ourDragoons < DRAGOON_MASS_COUNT && in.eval < HOLD_VS_DRAGOON_EVAL) {
            return new Verdict(PostureReason.ENEMY_DRAGOONS_SO_COME_HOME, ClusterMission.Posture.DEFEND,
                "Enemy dragoons (" + in.ourDragoons + " goons, eval " + in.eval + "), coming home");
        }

        if (in.wasAttacking && in.ourDragoons <= FALLBACK_MAX_DRAGOONS && in.strength <= FALLBACK_STRENGTH) {
            return new Verdict(PostureReason.OUTGUNNED_SO_COME_HOME, ClusterMission.Posture.DEFEND,
                "Outgunned (" + in.ourDragoons + " goon, " + in.strength + "%), coming home");
        }

        if (in.vsZerg && in.ourDragoons < 1 && in.strength < 150) {
            return new Verdict(PostureReason.ZEALOTS_HOLD_VS_ZERG, ClusterMission.Posture.DEFEND,
                "Zealots hold vs Zerg (" + in.strength + "%, no dragoon)");
        }

        if (in.ours >= ClusterMission.ATTACK_ARMY_SIZE) {
            if (in.minerals >= BANKED_MINERALS) {
                return new Verdict(PostureReason.BANKED_MINERALS, ClusterMission.Posture.ATTACK,
                    "Banked " + in.minerals + ", attack");
            }

            return new Verdict(PostureReason.HEADCOUNT, ClusterMission.Posture.ATTACK,
                "Headcount ok (" + in.ours + " units)");
        }

        if (in.vsTerran && (in.ours >= TERRAN_EARLY_ARMY || in.ourDragoons >= TERRAN_EARLY_GOONS)
            && in.strength >= EARLY_MARCH_STRENGTH) {
            return new Verdict(PostureReason.EARLY_TERRAN_PRESSURE, ClusterMission.Posture.ATTACK,
                "Early Terran pressure (" + in.ours + " units, " + in.strength + "%)");
        }

        if (in.ourDragoons >= DRAGOON_PAIR_COUNT && in.strength >= EARLY_MARCH_STRENGTH && in.dragoonMatchup) {
            return new Verdict(PostureReason.EARLY_DRAGOONS_OK_SO_PRESSURE, ClusterMission.Posture.ATTACK,
                "Early dragoons ok (" + in.ourDragoons + " goons, " + in.strength + "%), pressure");
        }

        if (in.ours >= EARLY_MARCH_ARMY_SIZE && in.strength >= EARLY_MARCH_STRENGTH) {
            return new Verdict(PostureReason.EARLY_STRENGTH, ClusterMission.Posture.ATTACK,
                "Early strength ok (" + in.ours + " units, " + in.strength + "%)");
        }

        if (atlantis.combat.focus.TrappedByMassZealots.applies(in.ours, in.ourDragoons, in.enemyZealots, in.gameSeconds)) {
            return new Verdict(PostureReason.TRAPPED_BY_MASS_ZEALOTS, ClusterMission.Posture.ATTACK,
                "Trapped by mass zealots (" + in.ours + " vs " + in.enemyZealots + ")");
        }

        return new Verdict(PostureReason.HOLD_GROUND, ClusterMission.Posture.DEFEND,
            holdText(in.ours, in.ourDragoons, in.strength, in.dragoonMatchup, in.matchup));
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

    /**
     * The live reading behind {@link #decide}: counts, bank and clock.
     */
    public static Verdict decideLive() {
        boolean enemyKnown = Select.enemy().combatUnits().notEmpty();
        boolean vsTerran = atlantis.game.player.Enemy.terran();
        boolean vsZerg = atlantis.game.player.Enemy.zerg();
        boolean protossWithoutRanged = atlantis.game.player.Enemy.protoss() && enemyKnown
            && Select.enemy().combatUnits().ranged().empty();
        String matchup = vsZerg ? "vZ"
            : atlantis.game.player.Enemy.protoss() ? "vP" : vsTerran ? "vT" : "?";
        atlantis.units.AUnit leader = atlantis.battle.cluster.Alpha.get().leader();
        atlantis.units.AUnit assessor = leader == null
            ? null : atlantis.combat.cluster.ClusterTactic.nearestToFight(leader);
        Input in = new Input();
        in.ours = Count.ourCombatUnits();
        in.ourDragoons = Count.dragoons();
        in.enemyZealots = EnemyUnits.zealots();
        in.enemyDragoons = EnemyUnits.dragoons();
        in.minerals = A.minerals();
        in.gameSeconds = A.s;
        in.strength = atlantis.information.generic.Army.strength();
        in.eval = assessor == null ? 1.0 : assessor.eval();
        in.dragoonMatchup = vsZerg || vsTerran || protossWithoutRanged;
        in.wasAttacking = atlantis.battle.cluster.Missions.globalMission().isMissionAttack();
        in.vsZerg = vsZerg;
        in.vsTerran = vsTerran;
        in.matchup = matchup;

        return decide(in);
    }

    /**
     * One decision with its reason. Posture is a direction, never a tile.
     */
    public static final class Verdict {
        public final ClusterMission.Posture posture;
        public final PostureReason reason;
        /**
         * Human sentence for the CherryVis board, with the deciding numbers.
         */
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
     * Explicit inputs, so the decision pins without a game.
     */
    public static final class Input {
        public int ours;
        public int ourDragoons;
        public int enemyZealots;
        public int enemyDragoons;
        public int minerals;
        public int gameSeconds;
        public int strength;
        public double eval = 1.0;
        public boolean vsZerg;
        public boolean dragoonMatchup;
        public boolean wasAttacking;
        public boolean vsTerran;
        public String matchup = "?";
    }
}
