package atlantis.combat.squad;

import java.util.ArrayList;

public class AllSquads {
    /**
     * "Alpha" - main army, the first one.
     * <p>
     * "Omega" - created after some time, its mission is to defend main + natural.
     * <p>
     * "Delta" - all air units go there by default.
     */
    protected static ArrayList<Squad> allSquads = new ArrayList<>();

    public static ArrayList<Squad> all() {
        return allSquads;
    }

    public static ArrayList<Squad> allClone() {
        return (ArrayList<Squad>) AllSquads.all().clone();
    }
}
