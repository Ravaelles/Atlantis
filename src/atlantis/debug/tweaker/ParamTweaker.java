package atlantis.debug.tweaker;

import java.util.ArrayList;

public class ParamTweaker {

    protected static ParamTweaker tweaker = null;
    private ArrayList<Param> params = new ArrayList<>();

    // =========================================================

    protected ParamTweaker() { }

    // =========================================================

    protected void initParamValues() {
        try {
            for (Param param : params) {
                param.setterCallable().call();
                System.out.println("Randomized `" + param.name() + "` with " + param.getterCallable().call());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================================================

    public static ParamTweaker get() {
        if (tweaker == null) {
            throw new RuntimeException("ParamTweaker has not been initialized.");
        }

        return tweaker;
    }

    public void addParam(Param param) {
        params.add(param);
    }

    public ArrayList<Param> params() {
        return params;
    }
}
