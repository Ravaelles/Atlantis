package atlantis.combat.kite;

import atlantis.battle.kite.CombatTuning;

import atlantis.units.AUnit;
import atlantis.units.special.SleepUntilFrameGate;

/**
 * Kiting decisions, extracted from {@code BattleCommander} (owner:
 * the 750-line commander mixed tactic arbitration with per-unit kite
 * math - half the game in one class). Pure functions of unit, threat
 * and the shared tuning snapshot: should the unit yield ground, and
 * where is its hold point. Order emission stays in the commander;
 * target selection stays in the targeting package - this class only
 * answers the kite question.
 */
public final class Kite {

    /**
     * Meat screen (owner 2026-10-26): friendly zealots inside 8 tiles and
     * 40+ hp on the dragoon. The screen tanks, so the dragoon stands and
     * shoots on ready instead of yielding ground for the margin. Public
     * as a seam: the discipline pins without running the frame.
     * <p>
     * A melee enemy this close is already hitting the guns: no stand
     * discipline applies. Same edge the sim measures contact with.
     */
    static final double CHEWING_TILES = 0.5;

    private Kite() {
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

        // Cooling dragoon holds firing distance (owner 2026-10-26): with
        // 15+ cooldown frames left there is time to brake and be weapon-
        // ready, so hold range - 0.4 - still in range, still safe - and
        // spend the tiles adjusting position instead of closing. Below
        // 15 the closing formula owns the last frames before the shot.
        if (unit.isDragoon() && unit.cooldown() >= 15) {
            return Math.max(0.0, myRange - 0.4);
        }

        double targetRange = unit.enemyWeaponRangeAgainstThisUnit(threat);
        double dist = unit.distTo(threat);
        double tilesPerFrame = unit.maxSpeed() / 32.0;
        double cooldownDistance = Math.max(0.0, unit.cooldown() - attackLatencyFrames() - 2) * tilesPerFrame;
        double desired = Math.min(myRange, myRange + (cooldownDistance - (dist - myRange)) / 2.0);

        if (targetRange <= myRange - CombatTuning.current().getKiteMeleeEdgeTiles()) {
            double hitsLeft = (unit.hp() + unit.shields()) / CombatTuning.current().getKiteZealotPassDamage();
            double closer = hitsLeft <= CombatTuning.current().getKiteWoundedHitsLeft()
                ? CombatTuning.current().getKiteWoundedEdgeInsetTiles() : CombatTuning.current().getKiteMeleeCloserTiles();
            desired -= closer;
        }

        return Math.max(0.0, desired);
    }

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

        // Zealots trade contact, except wounded on a spent weapon: with
        // 7+ frames of cooldown left a step back costs no DPS, and a
        // wounded zealot saves HP against faster attackers instead of
        // standing through the cooldown (NEXT #178, owner direction).
        // Unwounded zealots keep trading - withdrawing whole would
        // string the line out for nothing.
        if (!unit.isDragoon() && !ZealotWithdraw.should(unit, threat)) return false;

        if (unit.isDragoon()) {
            // Per-unit policy first; the margin envelope below is the
            // default for every unit type (dragoon, withdrawing zealot,
            // future Terran guns plug in the same way). Reasons live in
            // DragoonKitePolicy, split by cooldown vs health - the ladder
            // reads here, the how lives there (CONVENTIONS §23).
            KiteVerdict verdict = DragoonKitePolicy.decide(unit, threat);
            if (verdict == KiteVerdict.KITE) return true;
            if (verdict == KiteVerdict.STAND) return false;
        }

        if (unit.distTo(threat) >= CombatTuning.current().getKiteEnemyTiles()) return false;

        return kiteMargin(unit, threat) < kiteMarginRequired(unit);
    }

    /**
     * True when a listed melee enemy is already in hitting range. Public as a seam.
     */
    public static boolean isBeingChewed(AUnit unit) {
        try {
            if (unit == null || !unit.isAlive()) return false;

            for (AUnit foe : unit.enemiesNear().melee().list()) {
                if (foe == null || !foe.isAlive()) continue;
                if (unit.distTo(foe) <= CHEWING_TILES) return true;
            }
        } catch (Exception ignored) {
        }
        return false;
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
        if (isHuntedBy(unit, threat)) margin -= CombatTuning.current().getKiteHuntedTiles();
        if (unit.isOtherFacingThisUnit(threat)) margin -= CombatTuning.current().getKiteFacedTiles();

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
     * Per-unit attack latency (NEXT #154): the engine's remaining command
     * latency, read live per decision - never a flat constant. The +2 at
     * the call site is the Stardust margin (issue frame + publish frame,
     * measured in {@code SleepUntilFrameGate}).
     */
    private static int attackLatencyFrames() {
        return SleepUntilFrameGate.latencyFrames();
    }

    private static double kiteMarginRequired(AUnit unit) {
        double hitsLeft = (unit.hp() + unit.shields()) / CombatTuning.current().getKiteZealotPassDamage();
        return Math.max(CombatTuning.current().getKiteMarginBaseTiles(),
            CombatTuning.current().getKiteMarginDyingTiles() - CombatTuning.current().getKiteMarginPerHitTiles() * hitsLeft);
    }
}
