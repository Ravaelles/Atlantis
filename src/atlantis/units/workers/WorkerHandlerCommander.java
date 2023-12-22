package atlantis.units.workers;

import atlantis.architecture.Commander;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class WorkerHandlerCommander extends Commander {
    @Override
    protected void handle() {
        for (AUnit worker : Select.ourWorkers().list()) {
            (new WorkerManager(worker)).invoke(this);
        }
    }
}
