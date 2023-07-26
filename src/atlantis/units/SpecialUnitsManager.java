package atlantis.units;

import atlantis.architecture.Manager;
import atlantis.combat.micro.protoss.PreventForMissionSparta;
import atlantis.combat.micro.protoss.ProtossShieldBattery;
import atlantis.combat.micro.terran.TerranCloakableManager;
import atlantis.combat.micro.terran.TerranScienceVessel;
import atlantis.combat.micro.terran.TerranVulture;
import atlantis.combat.micro.terran.infantry.TerranInfantry;
import atlantis.combat.micro.terran.tank.TerranTank;
import atlantis.combat.micro.transport.ATransportManager;
import atlantis.combat.micro.zerg.overlord.ZergOverlordManager;
import atlantis.protoss.ProtossHighTemplar;
import atlantis.protoss.ProtossObserver;

public class SpecialUnitsManager extends Manager {
    public SpecialUnitsManager(AUnit unit) {
        super(unit);
    }

    protected Class<? extends Manager>[] managers() {
        Class[] raceSpecific;

        if (unit.isTerran()) {
            raceSpecific = new Class[] {
                TerranTank.class,
                TerranInfantry.class,
                TerranVulture.class,
                TerranCloakableManager.class,
                TerranScienceVessel.class,
            };
        }
        else if (unit.isProtoss()) {
            raceSpecific = new Class[] {
                PreventForMissionSparta.class,
                ProtossShieldBattery.class,
                ProtossObserver.class,
                ProtossHighTemplar.class,
            };
        }
        else {
            raceSpecific = new Class[] {
                ZergOverlordManager.class,
            };
        }

        Class[] generic = new Class[] {
            ATransportManager.class
        };

        return mergeManagers(raceSpecific, generic);
    }

}
