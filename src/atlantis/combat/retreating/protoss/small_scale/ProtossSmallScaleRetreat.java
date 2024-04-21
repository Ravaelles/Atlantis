package atlantis.combat.retreating.protoss.small_scale;

import atlantis.architecture.Manager;
import atlantis.combat.retreating.protoss.ProtossStartRetreat;
import atlantis.combat.retreating.protoss.ProtossTooBigBattleToRetreat;
import atlantis.combat.retreating.protoss.should.ProtossShouldRetreat;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import bwapi.Color;

public class ProtossSmallScaleRetreat extends Manager {
    public static final double RADIUS_LG = 4;
    public static final double RADIUS_SM = 1.6;
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

        if ((new ProtossStartRetreat(unit)).startRetreatingFrom(enemy)) {
//            unit.paintCircleFilled(14, Color.Red);
            return usedManager(this);
        }

        return null;
    }

    private static boolean isOverpoweredByEnemyMelee(AUnit unit, Selection friends, Selection enemies) {
        return ProtossSmallScaleEvaluate.meleeOverpoweredInRadius(unit, friends, enemies, RADIUS_SM)
            && ProtossSmallScaleEvaluate.meleeOverpoweredInRadius(unit, friends, enemies, RADIUS_LG);
    }

    private AUnit enemy() {
        return enemies.first();
    }

    public boolean shouldSmallScaleRetreat() {
        if (unit.isMissionSparta()) return false;
        if (unit.enemiesNear().combatUnits().groundUnits().empty()) return false;
        if (ProtossTooBigBattleToRetreat.doNotRetreat(unit)) return false;
        if (unit.combatEvalRelative() >= 1.35 && unit.hp() >= 24) return false;

        if (unit.isRanged()) return asRanged(unit, friends, enemies);
        return asMelee(unit, friends, enemies);
    }


    protected boolean asMelee(AUnit unit, Selection friends, Selection enemies) {
//        if (unit.combatEvalRelative() >= 1.2) return false;
        if (enemies.inRadius(RADIUS_LG, unit).count() <= 0) return false;

        if (isOverpoweredByEnemyMelee(unit, friends, enemies)) {
            unit.setTooltip("PSC:A");

            String message = "@" + A.now() + " PSC" + A.digit(RADIUS_SM)
                + ": " + A.digit(ProtossSmallScaleEvaluate.ourMeleeStrength(unit, friends, RADIUS_SM))
                + "_vs_" + A.digit(ProtossSmallScaleEvaluate.enemyMeleeStrength(unit, enemies, RADIUS_SM));
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
        if (unit.cooldown() <= 12) return false;

        if (enemies.onlyMelee()) {
            if (unit.shieldDamageAtMost(30)) return false;
            if (unit.meleeEnemiesNearCount(2) == 0) return false;
        }

//        if (unit.combatEvalRelative() <= 1.06 && unit.friendsInRadiusCount(5) < enemies.count()) return true;
        if (unit.hp() <= 40 && unit.friendsInRadiusCount(5) < enemies.count()) return true;

        return false;
    }
}
