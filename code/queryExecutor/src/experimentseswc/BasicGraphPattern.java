package experimentseswc;
import java.util.ArrayList;
import java.util.HashMap;
public abstract class BasicGraphPattern {

    public abstract void replace (String var, String value);

    public abstract void replaceAll (String var, String value);

    public abstract ArrayList<String> getExistentialVars(ArrayList<String> vs);

    public abstract ArrayList<String> getVars(ArrayList<String> vs);

    public abstract String toString();

    public abstract BasicGraphPattern copy();

    public static boolean isVar(String v) {
        return v.startsWith("?") || v.startsWith("$");
    }

    public abstract String toPredicate();

    public abstract HashMap<String, String> getConstantsMapping();

    public static boolean isExistentialVar(String v, ArrayList<String> vs) {
        return (isVar(v) && !vs.contains(v));
    }
}
