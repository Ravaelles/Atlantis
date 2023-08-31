package atlantis.combat.managers;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.special.AvoidCriticalUnits;
import atlantis.combat.micro.avoid.special.AvoidSpellsAndMines;
import atlantis.combat.micro.generic.Unfreezer;
import atlantis.combat.micro.managers.DanceAfterShoot;
import atlantis.combat.micro.managers.StopAndShoot;
import atlantis.combat.micro.transport.TransportUnits;
import atlantis.combat.running.ShouldStopRunning;
import atlantis.units.AUnit;
import atlantis.units.interrupt.DontInterruptShootingUnits;

public class CombatManagerTopPriority extends Manager {
    public CombatManagerTopPriority(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isCombatUnit();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            Unfreezer.class,
            AvoidSpellsAndMines.class,
            AvoidCriticalUnits.class,
            DanceAfterShoot.class,
            StopAndShoot.class,
            DontInterruptShootingUnits.class,
            TransportUnits.class,
            ShouldStopRunning.class,
        };
    }
}

