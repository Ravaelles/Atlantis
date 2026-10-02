package atlantis.units.special;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.combat.micro.terran.TerranCloakableManager;
import atlantis.combat.micro.terran.vessel.TerranScienceVessel;
import atlantis.combat.micro.terran.TerranVulture;
import atlantis.combat.micro.terran.wraith.TerranWraith;
import atlantis.combat.micro.terran.infantry.TerranInfantry;
import atlantis.combat.micro.terran.tank.TerranTank;
import atlantis.combat.micro.transport.ATransportManager;
import atlantis.combat.micro.zerg.overlord.ZergOverlordManager;
import atlantis.protoss.ShieldBattery;
import atlantis.protoss.arbiter.Arbiter;
import atlantis.protoss.corsair.Corsair;
import atlantis.protoss.ht.HighTemplar;
import atlantis.protoss.observer.Observer;
import atlantis.protoss.reaver.Reaver;
import atlantis.protoss.shuttle.Shuttle;
import atlantis.units.AUnit;

public class SpecialUnitsManager extends Manager {
    public SpecialUnitsManager(AUnit unit) {
        super(unit);
    }

    protected ManagerFactory[] managers() {
        ManagerFactory[] raceSpecific;

        if (unit.isTerran()) {
            raceSpecific = new ManagerFactory[]{
                TerranTank::new,
                TerranInfantry::new,
                TerranWraith::new,
                TerranVulture::new,
                TerranCloakableManager::new,
                TerranScienceVessel::new,
            };
        }
        else if (unit.isProtoss()) {
            raceSpecific = new ManagerFactory[]{
                Shuttle::new,
                Corsair::new,
                Reaver::new,
                Observer::new,
                ShieldBattery::new,
                HighTemplar::new,
                Arbiter::new,
            };
        }
        else {
            raceSpecific = new ManagerFactory[]{
                ZergOverlordManager::new,
            };
        }

        ManagerFactory[] generic = new ManagerFactory[]{
            ATransportManager::new
        };

        return mergeManagers(raceSpecific, generic);
    }

    protected static ManagerFactory[] mergeManagers(ManagerFactory[] raceSpecific, ManagerFactory[] generic) {
        ManagerFactory[] merged = new ManagerFactory[raceSpecific.length + generic.length];
        System.arraycopy(raceSpecific, 0, merged, 0, raceSpecific.length);
        System.arraycopy(generic, 0, merged, raceSpecific.length, generic.length);
        return merged;
    }
}
