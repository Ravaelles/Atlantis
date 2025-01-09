package atlantis.production.constructions;

import atlantis.architecture.Commander;
import atlantis.production.constructions.builders.TerranKilledBuilderCommander;
import atlantis.production.constructions.commanders.ConstructionStatusChanger;
import atlantis.production.constructions.commanders.ConstructionThatLooksBugged;
import atlantis.production.constructions.commanders.ConstructionUnderAttack;
import atlantis.production.constructions.commanders.IdleBuildersFix;
import atlantis.units.AUnit;

import java.util.ArrayList;

public class ConstructionsCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            TerranKilledBuilderCommander.class,
            ConstructionStatusChanger.class,
            ConstructionUnderAttack.class,
            ConstructionThatLooksBugged.class,
            IdleBuildersFix.class,
        };
    }

//    private boolean isItSafeToAssignNewBuilderTo(Construction construction) {
//        if (construction.buildingType().isBunker()) return true;
//
//        HasPosition position = construction.buildingUnit() != null
//            ? construction.buildingUnit() : construction.buildPosition();
//
//        if (position == null) {
//            System.err.println("Null position in isItSafeToAssignNewBuilderTo");
//            System.err.println(construction);
//            return false;
//        }
//
//        if (
//            EnemyUnits.discovered().combatUnits().inRadius(8, position).empty()
//                || (construction.buildingType().isCombatBuilding() && Select.our().inRadius(7, position).atLeast(2))
//                || A.hasMinerals(700)
//        ) return true;
//
//        return false;
//    }

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
