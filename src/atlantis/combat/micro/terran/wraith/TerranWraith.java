package atlantis.combat.micro.terran.wraith;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.micro.avoid.protoss.ProtossAvoidEnemies;
import atlantis.combat.micro.terran.air.RunForYourLife;
import atlantis.terran.repair.managers.GoToRepairAsAirUnit;
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
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            WraithBeingReparedManager::new,
            RunForYourLife::new,
            ProtossAvoidEnemies::new,
            GoToRepairAsAirUnit::new,

            // Attack-related
            WraithChangeLocationIfRanTooLong::new,
            AttackOtherAirUnits::new,
            AttackSpecificEnemiesNearBases::new,
            AttackSpecificEnemies::new,
            AttackWorkersWhenItMakesSense::new,
            AttackTargetInRangeIfRanTooLong::new,
            SeparateFromOtherWraiths::new,
            AttackTargetInRange::new,
            MoveAsLooksIdle::new,
        };
    }

    public static boolean noAntiAirBuildingNearby(AUnit unit) {
        return unit.enemiesNear()
            .combatBuildingsAntiAir()
            .inRadius(7.7 + Math.max(2.5, unit.woundPercent() / 35), unit)
            .empty();
    }
}
