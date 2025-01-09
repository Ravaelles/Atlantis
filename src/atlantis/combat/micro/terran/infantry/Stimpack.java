package atlantis.combat.micro.terran.infantry;

import atlantis.architecture.Manager;
import atlantis.information.tech.ATech;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;
import bwapi.TechType;

public class Stimpack extends Manager {
    public Stimpack(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isMarine() && ATech.isResearched(stimTech());
    }

    @Override
    protected Manager handle() {
        if (shouldUse()) {
            if (useStim()) {
                return usedManager(this);
            }
        }

        return null;
    }

    private boolean shouldUse() {
        if (unit.hp() <= 20 || unit.isStimmed()) return false;

        Selection enemies = unit.enemiesNear().inRadius(9, unit);

        if (
            enemies.atLeast(Enemy.zerg() ? 3 : 2)
        ) {
            if (unit.lastActionMoreThanAgo(5, Actions.USING_TECH)) {
                if (Select.ourOfType(AUnitType.Terran_Medic).inRadius(5, unit).havingEnergy(40).atLeast(2)) return true;
            }
        }

        if (Enemy.protoss() && unit.hp() >= 40 && unit.id() % 3 == 0 && unit.enemiesNearInRadius(4) >= 2) return true;

        return false;
    }

    private boolean useStim() {
        unit.useTech(stimTech());
        return true;
    }

    private TechType stimTech() {
        return TechType.Stim_Packs;
    }
}

