package atlantis.map.scout;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.game.CameraCommander;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.region.ARegion;
import atlantis.map.region.ARegionBoundary;
import atlantis.map.base.Bases;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.Color;

public class ScoutManager extends Manager {
    public ScoutManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isScout();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            AvoidEnemies.class,
            ScoutRunning.class,
            TryFindingEnemy.class,
            RoamAroundEnemyBase.class,
            ScoutFreeBases.class,
//            TestRoamingAroundBase.class,
        };
    }

    @Override
    protected Manager handle() {
        unit.setTooltipTactical("Scout...");

        if (unit.isRepairing()) return usedManager(this, "UhmRepairing");

        return handleSubmanagers();
    }
}
