package atlantis.production.dynamic.reinforce.terran.turrets;

import atlantis.game.A;
import atlantis.map.base.Bases;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

public class TurretsForNonMain extends TerranTurret {
    private final int MIN_TURRETS_PER_BASE = 2;

    public boolean buildIfNeeded() {
        if (A.everyFrameExceptNthFrame(73)) return false;
        if (!Have.engBay()) return false;
        if (Count.bases() == 1) return false;

        if (handleReinforcePosition(turretForNatural(), 7)) {

            return true;
        }

        if (handleTurretForAllBases()) {

            return true;
        }

        return false;
    }

    // =========================================================

    private boolean handleTurretForAllBases() {
        if (!Enemy.zerg()) return false;

        if (exceededExistingAndInProduction()) return false;

        int maxDist = 12;

        for (AUnit base : Select.ourBases().list()) {
            if (base.isLifted()) continue;

            int existing = Count.existingOrPlannedBuildingsNear(type(), maxDist, base.position());

            if (existing < MIN_TURRETS_PER_BASE) {
                APosition minerals = Select.minerals().inRadius(maxDist, base).center();
                if (minerals != null) {
                    AddToQueue.withHighPriority(type(), base.translateTilesTowards(4, minerals))
                        .setMaximumDistance(maxDist);
                    return true;
                }
            }
        }

        return false;
    }

    protected HasPosition turretForNatural() {
        APosition natural = Bases.natural();
        if (natural == null) {
            return null;
        }

        AChoke choke = Chokes.nearestChoke(natural);
        if (choke == null) {
            return null;
        }

        return natural.translateTilesTowards(choke, 10);
    }

}
