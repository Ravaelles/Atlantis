package atlantis.map.analysis;

import atlantis.map.position.HasPosition;
import atlantis.map.position.APosition;
import atlantis.map.ReachableTiles;

/**
 * Ground connectivity from the live map: the engine {@code GroundLink} for
 * {@code BaseRoles}. One named place so every roles-consumer reuses the
 * same definition of "reachable" instead of growing its own anonymous
 * class (OCP: the next consumer wires this, not a copy).
 */
public final class EngineGroundLink implements BaseRoles.GroundLink {

    @Override
    public boolean connected(BaseRoles.BaseNode a, BaseRoles.BaseNode b) {
        return ReachableTiles.between(a.position(), b.position());
    }

    @Override
    public double groundDistance(BaseRoles.BaseNode a, BaseRoles.BaseNode b) {
        return a.position().groundDistanceTo(b.position());
    }
}
