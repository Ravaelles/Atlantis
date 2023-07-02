package atlantis.units.managers;

public class NullManager extends Manager {
    private static NullManager instance = new NullManager();

    public static Manager getInstance() {
        return instance;
    }
}
