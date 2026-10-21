package atlantis.battle.strategy;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

/**
 * Zealot hold-fire discipline (NEXT #179, owner 2026-10-21): zealots
 * break formation and attack first, then die one by one while the
 * dragoons watch (ZealDrag_v_Zeal: all 5 zealots dead by frame 457,
 * dragoons alive to 883, 1/5 lost). Until a ranged weapon fires
 * inside the cluster, zealots hold instead of initiating; the moment
 * a zealot is really hit, the formation breaks and it attacks.
 *
 * <p>Scope: clusters that HAVE ranged units (lone-zealot armies fight
 * as today - otherwise two facing zealots would stare forever).
 * "Really hit" is hp+shields more than {@link #WOUND_HP} below max:
 * ~10 damage is not a wound (owner), shields regen, hp does not.
 */
public final class ZealotHoldFire {

    /** Damage at or below this is not a wound: hold the line. */
    public static final double WOUND_HP = 10.0;

    private ZealotHoldFire() {
    }

    /**
     * True when this zealot must hold instead of attacking: ranged
     * allies exist, it is unwounded, and none of them fired yet.
     */
    public static boolean hold(AUnit unit) {
        if (unit == null || !unit.isZealot()) return false;
        if (!squadHasRanged(unit)) return false;
        if (isWounded(unit)) return false;
        return !rangedAllyFired(unit);
    }

    static boolean isWounded(AUnit unit) {
        return unit.hp() + unit.shields()
            < unit.maxHp() + unit.maxShields() - WOUND_HP;
    }

    static boolean squadHasRanged(AUnit unit) {
        try {
            // ourCombatUnits answers live units; no alive filter needed.
            return !Select.ourCombatUnits().ofType(AUnitType.Protoss_Dragoon)
                .exclude(unit).isEmpty();
        } catch (Exception ignored) {
            return false;
        }
    }

    static boolean rangedAllyFired(AUnit unit) {
        try {
            for (AUnit ally : Select.ourCombatUnits()
                .ofType(AUnitType.Protoss_Dragoon).list()) {
                if (ally == null || ally.id() == unit.id()) continue;
                if (!ally.isAlive()) continue;
                if (ally.cooldown() > 0) return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
