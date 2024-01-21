package atlantis.combat.squad.squad_scout;

import atlantis.architecture.Manager;
import atlantis.combat.managers.*;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.avoid.special.AvoidSpellsAndMines;
import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.special.SpecialUnitsManager;
import atlantis.util.Enemy;
import bwapi.Color;

public class SquadScout extends Manager {
    public SquadScout(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return allowedToUseScout()
            && !unit.isSpecialMission()
            && unit.isSquadScout();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            SquadScoutSafety.class,
            SquadScoutProceed.class,
        };
    }

    // =========================================================

    private boolean allowedToUseScout() {
        if (Enemy.terran()) return false;
        if (A.seconds() >= 800) return false;

//        return ArmyStrength.ourArmyRelativeStrength() >= 120 && unit.hp() >= 30;
        return unit.hp() >= 30;
    }
}
