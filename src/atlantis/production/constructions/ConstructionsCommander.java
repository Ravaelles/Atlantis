package atlantis.production.constructions;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.production.constructions.builders.TerranKilledBuilderCommander;
import atlantis.production.constructions.commanders.ConstructionStatusChanger;
import atlantis.production.constructions.commanders.ConstructionThatLooksBugged;
import atlantis.production.constructions.commanders.ConstructionUnderAttack;
import atlantis.production.constructions.commanders.IdleBuildersFix;
import atlantis.units.AUnit;

import java.util.ArrayList;

public class ConstructionsCommander extends Commander {
    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{
            TerranKilledBuilderCommander::new,
            ConstructionStatusChanger::new,
            ConstructionUnderAttack::new,
            ConstructionThatLooksBugged::new,
            IdleBuildersFix::new,
        };
    }

    public static ArrayList<AUnit> builders() {
        ArrayList<AUnit> units = new ArrayList<>();

        for (Construction order : ConstructionRequests.constructions) {
            if (order.builder() != null && order.builder().isAlive()) {
                units.add(order.builder());
            }
        }

        return units;
    }
}
