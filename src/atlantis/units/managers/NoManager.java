package atlantis.units.managers;

public class NoManager extends Manager {
    private static NoManager instance = new NoManager();

    public static Manager getInstance() {
        return instance;
    }
}
