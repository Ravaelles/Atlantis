package atlantis.battle;

import atlantis.battle.cluster.FightOrFlight;
import atlantis.units.AUnit;

/**
 * Formation hold (owner 2026-10-26, Stardust doctrine): engaging must not
 * dissolve the line. Stardust opens even its attack in an arc and breaks
 * it only for danger or weapon range ({@code attack()} forms the arc
 * while nobody is threatened or in range; {@code shouldStandGround}
 * holds while nobody has a target and nobody is threatened).
 *
 * <p>Our version, without a combat sim: while the enemy fields no ranged
 * units, the formation holds until contact - any our unit within
 * {@link #CONTACT_TILES} of an enemy, any our unit taking damage, or any
 * our ranged unit able to fire. Then the fight owns the unit as before.
 * With a ranged enemy the arc would march into fire, so the hold never
 * engages and micro runs as today. FLEE never holds: retreats own the
 * unit above this.</p>
 *
 * <p>Small groups are unaffected by construction: the hold marches at a
 * formation slot, and slots need three units - duels fall through to
 * the fight exactly as before.</p>
 */
public final class FormationHold {

    /** Contact breaks the hold: this close the phalanx becomes the fight. */
    public static final double CONTACT_TILES = 2.5;

    private FormationHold() {
    }

    /**
     * Hold the line instead of engaging this target: true while the
     * enemy is melee-only, the tactic is FIGHT, and none of the contact
     * breaks fired (2.5-tile proximity, damage taken, own ranged able
     * to fire). The caller marches at the formation slot; a missing
     * slot falls through to the fight. Kiting outranks holding (owner
     * 2026-10-26): a chewed unit breaks to the fight instead of
     * holding its slot - safety of the single unit first.
     */
    public static boolean shouldHold(AUnit unit, AUnit target, FightOrFlight.Tactic tactic) {
        if (unit == null || target == null || !unit.isAlive()) return false;
        if (tactic != FightOrFlight.Tactic.FIGHT) return false;
        try {
            if (!unit.isCombatUnit()) return false;
            if (atlantis.units.select.Select.enemy().ranged().notEmpty()) return false;
            if (unit.isUnderAttack()) return false;
            if (unit.hasWeaponRangeToAttack(target, 0)) return false;
            AUnit nearest = nearestEnemyOf(unit);
            if (nearest != null && unit.distTo(nearest) <= CONTACT_TILES) return false;
            if (needsKite(unit)) return false;
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    /**
     * Kite preemption (owner 2026-10-26): a close enemy that can hit
     * this unit outranks the formation - with a cooling weapon
     * (cooldown >= 8) or a real wound it breaks to the fight (and
     * its kite) instead of holding the slot while being chewed.
     * Public as a seam: the preemption pins without running the frame.
     */
    public static boolean needsKite(AUnit unit) {
        try {
            if (unit.enemiesThatCanAttackMe(3).count() < 1) return false;
            return unit.cooldown() >= 8 || unit.hp() + unit.shields() < 80;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static AUnit nearestEnemyOf(AUnit unit) {
        try {
            atlantis.units.select.Selection enemies = unit.enemiesNear();
            if (enemies == null || enemies.isEmpty()) return null;
            return enemies.nearestTo(unit);
        } catch (Exception ignored) {
            return null;
        }
    }
}
