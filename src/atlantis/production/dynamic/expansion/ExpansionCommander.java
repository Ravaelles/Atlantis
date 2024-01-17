package atlantis.production.dynamic.expansion;

import atlantis.architecture.Commander;
import atlantis.combat.missions.MissionCommander;
import atlantis.combat.squad.commanders.SquadsCommander;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.production.constructing.position.base.NextBasePosition;
import atlantis.production.dynamic.expansion.protoss.ProtossExpansionCommander;
import atlantis.production.dynamic.expansion.secure.terran.SecuringBaseAsTerran;
import atlantis.production.dynamic.expansion.terran.TerranEarlyExpansion;
import atlantis.production.dynamic.expansion.terran.TerranExpansionCommander;
import atlantis.production.dynamic.expansion.zerg.ZergExpansionCommander;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ExpansionCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            ProtossExpansionCommander.class,
            TerranExpansionCommander.class,
            ZergExpansionCommander.class,
        };
    }
}
