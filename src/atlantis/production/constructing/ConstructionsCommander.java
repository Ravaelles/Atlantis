package atlantis.production.constructing;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.builders.TerranKilledBuilderCommander;
import atlantis.production.constructing.commanders.ConstructionStatusChanger;
import atlantis.production.constructing.commanders.ConstructionThatLooksBugged;
import atlantis.production.constructing.commanders.ConstructionUnderAttack;
import atlantis.production.constructing.commanders.IdleBuildersFix;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
