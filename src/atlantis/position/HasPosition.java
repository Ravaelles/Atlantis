package atlantis.position;

/**
 * This interface helps ease problems of overriding native bridge classes like e.g. BaseLocation which doesn't
 * have public constructor. Instead ABaseLocation can use this interface.
 */
public interface HasPosition {
    public APosition getPosition();
    public int getX();
    public int getY();
}
