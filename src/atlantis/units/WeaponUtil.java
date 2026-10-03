package atlantis.units;

import atlantis.util.cache.Cache;
import bwapi.DamageType;
import bwapi.WeaponType;

/**
 * Weapon numbers and the damage modifiers between two unit types.
 *
 * <p>Lives in {@code atlantis.units} rather than {@code atlantis.util} because it
 * reads unit data through {@link UnitStats}: {@code util} is the shared kernel
 * and must not point upward at {@code units} (ArchitectureBoundaryTest).</p>
 */
public class WeaponUtil {

    private static Cache<Double> cacheDouble = new Cache<>();
    private static Cache<Integer> cacheInt = new Cache<>();

    public static int damageNormalized(WeaponType weapon) {
        return cacheInt.get(
            "damageNormalized:" + weapon.name(),
            1,
            () -> {
                if (weapon.equals(WeaponType.Psi_Blades)) {
                    // A Zealot swings two blades: 8 damage each, 16 per attack.
                    // That is the weapon's real damage, not a patch for missing
                    // data - the table has one blade, the attack has two.
                    return UnitStats.weaponDamageNormalized(weapon) * 2;
                }
                else {
//                    System.err.println("weapon = " + weapon);
//                    System.err.println("weapon.damageAmount() = " + weapon.damageAmount());
//                    System.err.println("weapon.damageFactor() = " + weapon.damageFactor());
                    return atlantis.units.UnitStats.weaponDamageNormalized(weapon);
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
