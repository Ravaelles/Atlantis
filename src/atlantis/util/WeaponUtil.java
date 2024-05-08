package atlantis.util;

import atlantis.units.AUnitType;
import atlantis.util.cache.Cache;
import bwapi.DamageType;
import bwapi.WeaponType;

public class WeaponUtil {

    private static Cache<Double> cacheDouble = new Cache<>();
    private static Cache<Integer> cacheInt = new Cache<>();

    public static int damageNormalized(WeaponType weapon) {
        return cacheInt.get(
            "damageNormalized:" + weapon.name(),
            1,
            () -> {
                if (weapon.equals(WeaponType.Psi_Blades)) {
                    return 16;
                }
                else {
//                    System.err.println("weapon = " + weapon);
//                    System.err.println("weapon.damageAmount() = " + weapon.damageAmount());
//                    System.err.println("weapon.damageFactor() = " + weapon.damageFactor());
                    return weapon.damageAmount() * weapon.damageFactor();
                }
            }
        );
    }

    public static double damageModifier(AUnitType attacker, AUnitType target) {
        DamageType damageType = attacker.damageTypeAgainst(target);

        if (damageType == DamageType.Explosive) {
            return damageExplosiveModifierAgainst(target);
        }
        else if (damageType == DamageType.Concussive) {
            return damageConcussiveModifierAgainst(target);
        }
        else {
            return 1;
        }
    }

    // =========================================================

    private static double damageExplosiveModifierAgainst(AUnitType target) {
        if (target.isSmall()) {

            return 0.5;
        }
        else if (target.isLarge()) {

            return 1;
        }
        else {

            return 0.75;
        }
    }

    private static double damageConcussiveModifierAgainst(AUnitType target) {
        if (target.isSmall()) {
            return 1;
        }
        else if (target.isLarge()) {
            return 0.25;
        }
        else {
            return 0.5;
        }
    }
}
