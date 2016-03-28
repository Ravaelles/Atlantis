package atlantis.util;

import bwapi.WeaponType;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class WeaponUtil {

    public static double getDamageNormalized(WeaponType weapon) {
        if (weapon.equals(WeaponType.Psi_Blades)) {
            return 16;
        } else {
            return weapon.damageAmount() * weapon.damageFactor();
        }
    }

}
