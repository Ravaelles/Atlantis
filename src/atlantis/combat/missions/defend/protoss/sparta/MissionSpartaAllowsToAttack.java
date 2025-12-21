package atlantis.combat.missions.defend.protoss.sparta;

import atlantis.combat.missions.generic.MissionAllowsToAttackEnemyUnit;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class MissionSpartaAllowsToAttack extends MissionAllowsToAttackEnemyUnit {
    public MissionSpartaAllowsToAttack(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean allowsToAttackEnemyUnit(AUnit enemy) {
        if (unit.eval() <= 0.85 && unit.cooldown() >= 4 && unit.distTo(enemy) <= 2 && unit.friendsInRadiusCount(0.5) == 0) {
            return false;
        }

        if (Count.basesWithUnfinished() >= 2) return true;
        if (unit.hasWeaponRangeToAttack(enemy, 0.2)) return true;

        AUnit main = Select.main();
        if (main == null) return true;

        AChoke mainChoke = Chokes.mainChoke();
        if (mainChoke == null) return true;

        double enemyToMain = enemy.groundDistToMain();
        double chokeToMain = mainChoke.groundDistToMain();

        return enemyToMain < chokeToMain;
    }
}
