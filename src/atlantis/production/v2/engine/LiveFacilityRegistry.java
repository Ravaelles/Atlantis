package atlantis.production.v2.engine;

import atlantis.production.v2.ProducerFacility;
import atlantis.production.v2.ProducerFacilityRegistry;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ProducerFacilityRegistry} answered from the live game: every unit of
 * the type, each with its own id and the absolute frame it frees up. A facility
 * being built is free at its completion; one training, researching or
 * upgrading is free when that work finishes.
 */
public final class LiveFacilityRegistry implements ProducerFacilityRegistry {

    private final int now;

    public LiveFacilityRegistry(int now) {
        this.now = now;
    }

    @Override
    public List<ProducerFacility> facilitiesOf(String typeId) {
        List<ProducerFacility> facilities = new ArrayList<>();
        AUnitType type = typeId == null ? null : AUnitType.getByName(typeId);
        if (type == null)
            return facilities;

        for (AUnit unit : Select.ourWithUnfinished().ofType(type).list()) {
            if (!unit.isAlive())
                continue;
            facilities.add(new ProducerFacility(unit.id(), typeId, now + busyFor(unit)));
        }
        return facilities;
    }

    /** Frames until the facility can take new work. */
    static int busyFor(AUnit unit) {
        if (!unit.isCompleted())
            return Math.max(0, unit.getRemainingBuildTime());

        int busy = 0;
        if (!unit.hasNothingInQueue())
            busy = Math.max(busy, unit.remainingTrainTime());
        if (unit.isResearching())
            busy = Math.max(busy, unit.remainingResearchTime());
        if (unit.isUpgrading())
            busy = Math.max(busy, unit.remainingUpgradeTime());
        return Math.max(0, busy);
    }
}
