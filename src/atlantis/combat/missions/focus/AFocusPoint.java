package atlantis.combat.missions.focus;

import atlantis.game.A;
import atlantis.map.AChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;

/**
 * POSITION - where units should concentrate around. Can be offensive or defensive. Usually around a choke.
 *
 * LOOKING FROM - Units can position themselves from the wrong side of focus point.
 * This point should tell units from which side they should stand.
 */
public class AFocusPoint extends APosition {

    private APosition fromSide = null;
    private AChoke choke = null;
    private AUnit unit = null;
    private String name = null;

    // =========================================================

//    public AFocusPoint(HasPosition position) {
//        super(position.position());
//    }
//
//    public AFocusPoint(HasPosition position, String name) {
//        super(position.position());
//        this.name = name;
//    }

    public AFocusPoint(AUnit unit, String name) {
        this(unit.position(), null, name);
    }

    public AFocusPoint(HasPosition position, String name) {
        this(position, null, name);
    }

    public AFocusPoint(AChoke choke, HasPosition fromSide) {
        this(choke, fromSide, null);
    }

    public AFocusPoint(AChoke choke, HasPosition fromSide, String name) {
        super(choke.center());
        this.choke = choke;
        this.fromSide = fromSide.position();
        this.name = name;
    }

    public AFocusPoint(HasPosition position, HasPosition fromSide, String name) {
        super(position.position());
        this.fromSide = fromSide != null ? fromSide.position() : null;
        this.name = name;
    }

//    public AFocusPoint(HasPosition position, HasPosition fromSide) {
//        super(position.position());
//        this.fromSide = fromSide.position();
//
//        if (A.notUms() && position.position().equals(this.fromSide)) {
//            System.err.println("AFocusPoint got fromSide being the same point.");
//            A.printStackTrace();
//            this.fromSide = null;
//        }
//    }

    // =========================================================

    public boolean isValid() {
        return unit == null || unit.isAlive();
    }

    // =========================================================

    @Override
    public String toString() {
        return "Focus{" +
            "from=" + fromSide +
            ",choke=" + choke +
            ",name='" + name + '\'' +
            '}';
    }

    // =========================================================

    public APosition fromSide() {
        return fromSide;
    }

    public AChoke choke() {
        return choke;
    }

    public AUnit unit() {
        return unit;
    }

    public boolean isAroundChoke() {
        return choke != null;
    }

    public String getName() {
        return name;
    }
}
