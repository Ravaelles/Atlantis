package atlantis.units.special;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.TerranCloakableManager;
import atlantis.combat.micro.terran.vessel.TerranScienceVessel;
import atlantis.combat.micro.terran.TerranVulture;
import atlantis.combat.micro.terran.wraith.TerranWraith;
import atlantis.combat.micro.terran.infantry.TerranInfantry;
import atlantis.combat.micro.terran.tank.TerranTank;
import atlantis.combat.micro.transport.ATransportManager;
import atlantis.combat.micro.zerg.overlord.ZergOverlordManager;
import atlantis.protoss.ProtossCorsair;
import atlantis.protoss.ProtossShieldBattery;
import atlantis.protoss.ht.ProtossHighTemplar;
import atlantis.protoss.observer.ProtossObserver;
import atlantis.protoss.reaver.ProtossReaver;
import atlantis.protoss.shuttle.ProtossShuttle;
import atlantis.protoss.shuttle.ProtossShuttleEmpty;
import atlantis.protoss.reaver.reaver_with_shuttle.ProtossShuttleWithReaver;
import atlantis.units.AUnit;

public class SpecialUnitsManager extends Manager {
    public SpecialUnitsManager(AUnit unit) {
        super(unit);
    }

    protected Class<? extends Manager>[] managers() {
        Class[] raceSpecific;

        if (unit.isTerran()) {
            raceSpecific = new Class[]{
                TerranTank.class,
                TerranInfantry.class,
                TerranWraith.class,
                TerranVulture.class,
                TerranCloakableManager.class,
                TerranScienceVessel.class,
            };
        }
        else if (unit.isProtoss()) {
            raceSpecific = new Class[]{
                ProtossShuttle.class,
                ProtossShuttleWithReaver.class,
                ProtossShuttleEmpty.class,
                ProtossCorsair.class,
                ProtossHighTemplar.class,
                ProtossObserver.class,
                ProtossReaver.class,
                ProtossShieldBattery.class,
            };
        }
        else {
            raceSpecific = new Class[]{
                ZergOverlordManager.class,
            };
        }

        Class[] generic = new Class[]{
            ATransportManager.class
        };

        return mergeManagers(raceSpecific, generic);
    }

}
