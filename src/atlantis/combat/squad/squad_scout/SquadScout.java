package atlantis.combat.squad.squad_scout;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.game.player.Enemy;

public class SquadScout extends Manager {
    public SquadScout(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (true) return false;

        return unit.isSquadScout()
            && allowedToUseScout()
            && !unit.isSpecialMission()
            && beNormalUnitInsteadOfScout();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            SquadScoutSafety.class,
            SquadScoutProceed.class,
        };
    }

    // =========================================================

    private boolean beNormalUnitInsteadOfScout() {
        return unit.hp() >= 40 && unit.friendsNear().atLeast(6) && unit.enemiesNearInRadius(5) > 0;
    }

    private boolean allowedToUseScout() {
        if (Enemy.terran()) return false;
        if (A.seconds() >= 800) return false;

        if (unit.friendsNear().inRadius(5, unit).combatUnits().atLeast(8)) return false;

//        return ArmyStrength.ourArmyRelativeStrength() >= 120 && unit.hp() >= 30;
        return unit.hp() >= 30;
    }
}
