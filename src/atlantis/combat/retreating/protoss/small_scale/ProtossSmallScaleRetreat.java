package atlantis.combat.retreating.protoss.small_scale;

import atlantis.architecture.Manager;
import atlantis.combat.retreating.protoss.ProtossStartRetreat;
import atlantis.combat.retreating.protoss.should.ProtossShouldRetreat;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class ProtossSmallScaleRetreat extends Manager {
    private Selection friends;
    private Selection enemies;

    public ProtossSmallScaleRetreat(AUnit unit) {
        super(unit);

        friends = ProtossShouldRetreat.friends(unit);
        enemies = ProtossShouldRetreat.enemies(unit);
    }

    @Override
    public boolean applies() {
        return shouldSmallScaleRetreat();
    }

    @Override
    protected Manager handle() {
        AUnit enemy = enemy();
        if (enemy == null) return null;

        if ((new ProtossStartRetreat(unit)).startRetreatingFrom(enemy)) return usedManager(this);

        return null;
    }

    private AUnit enemy() {
        return enemies.first();
    }

    public boolean shouldSmallScaleRetreat() {
        if (unit.isRanged()) return asRanged(unit, friends, enemies);

        return asMelee(unit, friends, enemies);
    }

    protected boolean asMelee(AUnit unit, Selection friends, Selection enemies) {
//        if (unit.combatEvalRelative() >= 1.2) return false;

        double radius = 1.6;
        if (ProtossSmallScaleEvaluate.meleeOverpoweredInRadius(unit, friends, enemies, radius)) {
            unit.setTooltip("PSC:A");

            String message = "@" + A.now() + " PSC" + A.digit(radius)
                + ": " + A.digit(ProtossSmallScaleEvaluate.ourMeleeStrength(unit, friends, radius))
                + "_vs_" + A.digit(ProtossSmallScaleEvaluate.enemyMeleeStrength(unit, enemies, radius));
//            System.err.println(message);
            unit.addLog(message);

            return true;
        }

//        if (meleeOverpoweredInRadius(unit, friends, enemies, 3.2)) {
//            unit.setTooltip("PSC:B");
//            return true;
//        }

        return false;
    }

    protected boolean asRanged(AUnit unit, Selection friends, Selection enemies) {
        if (enemies.onlyMelee() && unit.shieldDamageAtMost(30)) return false;

//        if (unit.combatEvalRelative() <= 1.06 && unit.friendsInRadiusCount(5) < enemies.count()) return true;
        if (unit.hp() <= 40 && unit.friendsInRadiusCount(5) < enemies.count()) return true;

        return false;
    }
}
