package atlantis.map.high;

import atlantis.Atlantis;
import atlantis.map.AMap;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;

public class FindHighGround {
    public static APosition findNear(HasPosition near, int radius) {
        if (near == null) return null;

        APosition position = null;
        APosition current = near.position();

        for (int tx = current.tx() - radius; tx <= current.tx() + radius; tx++) {
            for (int ty = current.ty() - radius; ty <= current.ty() + radius; ty++) {
                position = APosition.create(tx, ty).makeValid();
//                System.err.println("tx " + tx + " ty " + ty + " / " + Atlantis.game().getGroundHeight(tx, ty));

                if (position.isWalkable() && position.isBuildable() && position.isHighGround()) {
                    return position;
                }
            }
        }

        return null;
    }
}
