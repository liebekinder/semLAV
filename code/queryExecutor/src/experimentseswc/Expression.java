package experimentseswc;
import java.util.ArrayList;
import java.util.HashMap;
public abstract class Expression {

    public abstract void replace (String var, String value);

    public abstract void replaceAll (String var, String value);

    public abstract Expression copy();

    public abstract ArrayList<String> getExistentialVars(ArrayList<String> vs);

    public abstract ArrayList<String> getVars(ArrayList<String> vs);

    public abstract HashMap<String, String> getConstantsMapping();

    public abstract String toString();
}
