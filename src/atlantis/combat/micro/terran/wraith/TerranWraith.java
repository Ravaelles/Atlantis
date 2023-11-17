package atlantis.combat.micro.terran.wraith;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.air.RunForYourLife;
import atlantis.terran.repair.managers.GoToRepair;
import atlantis.terran.repair.managers.UnitBeingReparedManager;
import atlantis.units.AUnit;

public class TerranWraith extends Manager {
    public TerranWraith(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isWraith() && TerranWraith.noAntiAirBuildingNearby(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            RunForYourLife.class,
            UnitBeingReparedManager.class,
            GoToRepair.class,
        };
    }

    public static boolean noAntiAirBuildingNearby(AUnit unit) {
        return unit.enemiesNear()
            .combatBuildingsAntiAir()
            .inRadius(7.7 + Math.max(2.5, unit.woundPercent() / 35), unit)
            .empty();
    }
}
