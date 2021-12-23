package atlantis.combat.missions;

import atlantis.position.APosition;
import atlantis.position.HasPosition;
import atlantis.util.A;

/**
 * POSITION - where units should concentrate around. Can be offensive or defensive. Usually around a choke.
 *
 * LOOKING FROM - Units can position themselves from the wrong side of focus point.
 * This point should tell units from which side they should stand.
 */
public class AFocusPoint extends APosition {

    private APosition fromSide = null;

    public AFocusPoint(HasPosition position) {
        super(position.position());
    }

    public AFocusPoint(HasPosition position, HasPosition fromSide) {
        super(position.position());
        this.fromSide = fromSide.position();

        if (position.position().equals(this.fromSide)) {
            System.err.println("AFocusPoint got fromSide being the same point.");
            A.printStack();
            this.fromSide = null;
        }
    }

    public APosition fromSide() {
        return fromSide;
    }
}
