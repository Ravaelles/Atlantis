package atlantis.map.bullets;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.attacked_by.Bullets;
import atlantis.units.select.Select;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SaveUnitsAndBulletsInfoToFile {
    private static String previousUnits = "";
    private static String previousBullets = "";

    public static void save() {
        String content = "";
        String currentUnits = unitsString();
        String currentBullets = bulletsString();

        content += "@" + A.now() + "--------------\n";

        if (!previousUnits.equals(currentUnits)) {
            content += currentUnits;
            previousUnits = currentUnits;
        }
        if (!previousBullets.equals(currentBullets)) {
            content += currentBullets;
            previousBullets = currentBullets;
        }

        String file = "units_n_bullets.txt";
        if (A.now() <= 0) A.saveToFile(file, content, true);
        else A.appendToFile(file, content);
    }

    private static String unitsString() {
        String content = "HP;"
            + "Unit;"
            + "IsDeadMan;"
            + "\n";

        List<AUnit> units = Select.all().list();
        Collections.sort(units);
        for (AUnit unit : units) {
            if (unit.isNeutral()) continue;

            content += unit.hp() + ";"
                + unit.typeWithUnitId() + ";"
                + (DeadMan.isDeadMan(unit) ? "YES" : "") + ";"
                + "\n";
        }

        return content;
    }

    private static String bulletsString() {
//        List<Bullet> bullets = AGame.get().getBullets();
        Collection<ABullet> bullets = Bullets.knownBullets();

        if (bullets.isEmpty()) return "";

        String content = "Bullet;"
            + "Shooter;"
            + "Target;"
//            + "Exists;"
//            + "Visible;"
            + "\n";

        for (ABullet bullet : bullets) {
            AUnit shooter = bullet.attacker();
            AUnit target = bullet.target();

            content += "_" + bullet.id() + ";"
                + shooter + ";"
                + target + ";"
//                + bullet.exists() + ";"
//                + bullet.isVisible() + ";"
                + "\n";
        }

        return content;
    }
}
