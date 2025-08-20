package atlantis.units.attacked_by;

import atlantis.game.A;
import atlantis.map.bullets.ABullet;
import atlantis.units.AUnit;
import atlantis.util.PauseAndCenter;
import bwapi.Color;

public class MissedBullets {
    public static void print() {
        for (ABullet bullet : Bullets.knownBullets()) {
            AUnit attacker = bullet.attacker();
            if (attacker == null || !attacker.isOur()) {
                continue;
            }

            AUnit target = bullet.target();
            if (target == null) {
                yes(bullet, "bullet has no target: " + bullet);
                continue;
            }
            else if (target.hp() <= 0) {
                yes(bullet, "target has 0 hp, visible: " + target.effVisible() + ": " + target);
                continue;
            }

//            int numOfBullets = Bullets.existingAgainst(target).size();
//            if (numOfBullets <= 1) continue;
//
//            if (target.isDeadMan()) {
//                yes(bullet, "target is dead man (" + target.hp() + "hp, " + numOfBullets + " bulls): " + target);
//                target.paintCircle(9, Color.White);
//            }
        }
    }

    private static void yes(ABullet bullet, String message) {
        A.errPrintln("@" + A.now + ": Bullet#" + bullet.id() + ": " + message);
    }
}
