package atlantis.combat.micro.avoid.margin.protoss;

import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;

public class KeepDragoonBattleLineVsRanged extends HasUnit {
    private final AUnit attacker;

    public KeepDragoonBattleLineVsRanged(AUnit unit, AUnit attacker) {
        super(unit);
        this.attacker = attacker;
    }

    public boolean dontSeparateVsRanged() {
        if (vsProtoss()) return true;

        return false;
    }

    private boolean vsProtoss() {
        if (!Enemy.protoss()) return false;
        if (!attacker.isDragoon()) return false;
        if (!unit.isDragoon()) return false;
        if (unit.cooldown() >= 11 && unit.shields() <= 22) return false;
        if (unit.hp() <= 42) return false;

        return situationFavorable() || ourZealotsNearEnemy();
    }

    private boolean ourZealotsNearEnemy() {
        return attacker.enemiesNear().zealots().inRadius(2, unit).count() > 0;
    }

    private boolean situationFavorable() {
        return countRangedNearForSide(unit, attacker) >= countRangedNearForSide(attacker, unit);
    }

    private int countRangedNearForSide(AUnit unit, AUnit against) {
        return (unit.isRanged() ? 1 : 0) + unit.friendsNear().ranged().inRadius(8, against).count();
    }
}
