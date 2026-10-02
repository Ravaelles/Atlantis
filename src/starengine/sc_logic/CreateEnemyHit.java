package starengine.sc_logic;

import atlantis.game.A;
import atlantis.util.AConsole;
import tests.fakes.FakeUnit;

public class CreateEnemyHit {
    public static void createHit(FakeUnit attacker, FakeUnit target) {
        int damage = attacker.damageAgainst(target);
        target.hp -= damage;

//        AConsole.println(attacker + " hits " + target + " for " + damage + " hp (" + target.hp + " left)");

        if (target.hp <= 0) unitIsDead(target);
    }

    private static void unitIsDead(FakeUnit unit) {
        // Do nothing
    }
}
