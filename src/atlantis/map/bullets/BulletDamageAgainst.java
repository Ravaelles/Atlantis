package atlantis.map.bullets;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.WeaponUtil;
import bwapi.WeaponType;

public class BulletDamageAgainst {
    public static int forBullet(ABullet bullet) {
//        double damageModifier = WeaponUtil.damageModifier(
//            target.type(), AUnit.createFrom(bullet.getSource()).type()
//        );
        AUnit target = bullet.target();
        AUnit attacker = bullet.attacker();
        WeaponType weapon = attacker.weaponAgainst(target);

        double damage = WeaponUtil.damageNormalized(weapon);
        double damageModifier = WeaponUtil.damageModifier(attacker.type(), target.type());

        return (int) (damage * damageModifier);
    }

//    public static int against(AUnit target, Bullet bullet) {
////        double damageModifier = WeaponUtil.damageModifier(
////            target.type(), AUnit.createFrom(bullet.getSource()).type()
////        );
//        AUnitType attacker = AUnit.createFrom(bullet.getSource()).type();
//        WeaponType weapon = target.weaponAgainst(target);
//
//        double damage = WeaponUtil.damageNormalized(weapon);
//        double damageModifier = WeaponUtil.damageModifier(attacker, target.type());
//
//        return (int) (damage * damageModifier);
//    }
}
