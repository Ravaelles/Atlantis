package tests.unit.sim;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;

/**
 * Per-tick feature snapshot: the flat numeric view of one unit the
 * learner will gate on (NEXT #185 Stage C). Fractions, not absolutes,
 * so one policy reads Dragoons and Zealots alike. Sim-side first:
 * the snapshot reads live selections, which the sim harness backs -
 * no engine needed, no game either.
 */
public final class BattleFeatures {

    public final double hpFrac;
    public final double shieldFrac;
    public final double cooldownFrac;
    public final double nearestFoeDist;
    public final int foesMelee2;
    public final int foesNear9;
    public final int friendsNear;

    private BattleFeatures(
        double hpFrac, double shieldFrac, double cooldownFrac,
        double nearestFoeDist, int foesMelee2, int foesNear9, int friendsNear
    ) {
        this.hpFrac = hpFrac;
        this.shieldFrac = shieldFrac;
        this.cooldownFrac = cooldownFrac;
        this.nearestFoeDist = nearestFoeDist;
        this.foesMelee2 = foesMelee2;
        this.foesNear9 = foesNear9;
        this.friendsNear = friendsNear;
    }

    /** Empty reading: null or dead unit answers zeros, never throws. */
    public static BattleFeatures snapshot(AUnit unit) {
        double hpFrac = 0;
        double shieldFrac = 0;
        double cooldownFrac = 0;
        double nearest = -1;
        int foes2 = 0;
        int foes9 = 0;
        int friends = 0;

        if (unit != null && unit.isAlive()) {
            try {
                double maxHp = Math.max(1, unit.maxHp());
                double maxSh = Math.max(1, unit.maxShields());
                double maxCd = Math.max(1, unit.cooldownAbsolute());
                hpFrac = unit.hp() / maxHp;
                shieldFrac = unit.shields() / maxSh;
                cooldownFrac = unit.cooldown() / maxCd;
                try {
                    foes2 = unit.enemiesNear().melee().inRadius(2, unit).count();
                    foes9 = unit.enemiesNear().combatUnits().inRadius(9, unit).count();
                    AUnit foe = unit.enemiesNear().combatUnits().nearestTo(unit);
                    if (foe != null) nearest = unit.distTo(foe);
                } catch (Exception | Error ignored) {
                }
                try {
                    friends = unit.friendsNear().combatUnits().count();
                } catch (Exception | Error ignored) {
                }
            } catch (Exception | Error ignored) {
                return new BattleFeatures(0, 0, 0, -1, 0, 0, 0);
            }
        }

        return new BattleFeatures(hpFrac, shieldFrac, cooldownFrac, nearest, foes2, foes9, friends);
    }

    /** Compact CSV row for episode archives (manifests, not console). */
    public String toCsv() {
        return String.format(java.util.Locale.US, "%.3f,%.3f,%.3f,%.2f,%d,%d,%d",
            hpFrac, shieldFrac, cooldownFrac, nearestFoeDist, foesMelee2, foesNear9, friendsNear);
    }
}
