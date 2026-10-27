package atlantis.combat.kite;

import atlantis.units.AUnit;

/**
 * Zealot withdrawal (NEXT #178, owner 2026-10-21): zerglings attack
 * faster than zealots, so a zealot that just attacked stands through
 * its whole cooldown taking free hits. After attacking with cooldown
 * left, a WOUNDED zealot steps back to save HP; an unwounded one
 * (~10 damage is no wound) keeps trading contact. Outnumbered and
 * faster-foe fights are where this pays most, and they are already
 * covered: a wounded zealot withdrawing on cooldown saves HP against
 * exactly those. If live games show over-retreat (the line stringing
 * out), gate additionally on outnumbered/faster - recorded, not built.
 */
public final class ZealotWithdraw {

    /**
     * Missing hp+shields at or below this is not a wound: hold contact.
     */
    public static final double UNWOUND_HP = 10.0;

    /**
     * Spent weapon: stepping back costs no DPS (same bar as the kite gate).
     */
    public static final int SPENT_COOLDOWN_FRAMES = 7;

    private ZealotWithdraw() {
    }

    public static boolean should(AUnit unit, AUnit threat) {
        if (unit == null || threat == null || !unit.isZealot()) return false;
        if (unit.cooldown() < SPENT_COOLDOWN_FRAMES) return false;

        return unit.maxHp() + unit.maxShields() - unit.hp() - unit.shields() > UNWOUND_HP;
    }
}
