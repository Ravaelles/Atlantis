package atlantis.production.dynamic.supply;

import atlantis.architecture.Commander;
import atlantis.architecture.Manager;
import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.race.MyRace;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.build.BuildOrderSettings;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.zerg.ProduceZergUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.TimeMoment;
import atlantis.util.We;

public class SupplyCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            ProtossSupplyCommander.class,
            TerranSupplyCommander.class,
            ZergSupplyCommander.class,
        };
    }
}
