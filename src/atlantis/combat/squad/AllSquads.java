package atlantis.combat.squad;

import java.util.ArrayList;

public class AllSquads
{
    /**
     * "Alpha" - main army, the first one.
     *
     * "Beta" - created after some time, its mission is to defend main + natural.
     *
     * "Delta" - all air units go there by default.
     */
    protected static ArrayList<Squad> allSquads = new ArrayList<>();

    public static ArrayList<Squad> all() {
        return allSquads;
    }
}
