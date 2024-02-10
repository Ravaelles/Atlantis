package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;

public class WouldBlockChokeBySieging extends Manager {
    private static Cache<Boolean> cache = new Cache<>();

    public WouldBlockChokeBySieging(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return cache.get(
            "applies",
            5,
            () -> {
                if (!unit.isTankUnsieged()) return false;

                if (unit.enemiesNear().tanks().inRadius(13.2, unit).count() > 0) return false;

                if (unit.enemiesNear().groundUnits().inRadius(8.2, unit).count() > 0) return false;

                if (
                    Select.enemy().combatBuildings(false).inRadius(8.2, unit).notEmpty()
                ) return false;

                AChoke choke = Chokes.nearestChoke(unit, "MAIN");
                if (choke != null && choke.width() <= 2.5) {
                    if (unit.distTo(choke) < -1) {
                        System.out.println("unit.distTo(choke) = " + unit.distTo(choke));
                    }
                    return unit.distTo(choke) < -1.8;
                }

                return false;
            }
        );
    }

    protected Manager handle() {
        unit.setTooltip("DoNotBlockChoke");
        return usedManager(this);
    }
}
