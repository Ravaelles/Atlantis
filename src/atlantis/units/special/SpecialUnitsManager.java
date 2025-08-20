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
import atlantis.protoss.ShieldBattery;
import atlantis.protoss.arbiter.Arbiter;
import atlantis.protoss.corsair.Corsair;
import atlantis.protoss.ht.HighTemplar;
import atlantis.protoss.observer.Observer;
import atlantis.protoss.reaver.Reaver;
import atlantis.protoss.shuttle.Shuttle;
import atlantis.units.AUnit;

import java.util.Arrays;
import java.util.stream.Stream;

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
                Shuttle.class,
                Corsair.class,
                Reaver.class,
                Observer.class,
                ShieldBattery.class,
                HighTemplar.class,
                Arbiter.class,
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

    protected static Class[] mergeManagers(Class[] raceSpecific, Class[] generic) {
        return Stream.concat(Arrays.stream(raceSpecific), Arrays.stream(generic)).toArray(Class[]::new);
    }
}
