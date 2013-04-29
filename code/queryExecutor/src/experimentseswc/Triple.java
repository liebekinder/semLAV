package experimentseswc;
import java.util.ArrayList;
import java.util.HashMap;
public class Triple extends BasicGraphPattern {

    String subject;
    String predicate;
    String object;

    public Triple (String s, String p, String o) {
        this.subject = s;
        this.predicate = p;
        this.object = o;
    }
    public Triple(Triple t) {
        this.subject = t.subject;
        this.predicate = t.predicate;
        this.object = t.object;
    }

    public BasicGraphPattern copy() {

        return new Triple(this);
    }

    public void replace (String var, String value) {

        if (!isVar(var)) {
            return;
        }
        if (this.subject.equals(var)) {
            this.subject = value;
        }
        if (this.predicate.equals(var)) {
            this.predicate = value;
        }
        if (this.object.equals(var)) {
            this.object = value;
        }
    }

    public void replaceAll (String var, String value) {

        if (isVar(this.subject) && this.subject.substring(1).equals(var)) {
            this.subject = value;
        }
        if (isVar(this.object) && this.predicate.substring(1).equals(var)) {
            this.predicate = value;
        }
        if (isVar(this.object) && this.object.substring(1).equals(var)) {
            this.object = value;
        }
    }

    public ArrayList<String> getExistentialVars(ArrayList<String> vs) {

        ArrayList<String> evs = new ArrayList<String>();
        if (isExistentialVar(this.subject, vs) && !evs.contains(this.subject)) {
            evs.add(this.subject);
        }
        if (isExistentialVar(this.predicate, vs) && !evs.contains(this.predicate)) {
            evs.add(this.predicate);
        }
        if (isExistentialVar(this.object, vs) && !evs.contains(this.object)) {
            evs.add(this.object);
        }
        return evs;
    }

    public ArrayList<String> getVars(ArrayList<String> vs) {

        ArrayList<String> evs = new ArrayList<String>();
        if (isVar(this.subject) && !evs.contains(this.subject) && !vs.contains(this.subject)) {
            evs.add(this.subject);
        }
        if (isVar(this.predicate) && !evs.contains(this.predicate) && !vs.contains(this.predicate)) {
            evs.add(this.predicate);
        }
        if (isVar(this.object) && !evs.contains(this.object) && !vs.contains(this.object)) {
            evs.add(this.object);
        }
        return evs;
    }

    public String toString() {

        return this.subject + " " + this.predicate + " " + this.object;
    }

    public String toPredicate() {

        String s = transform(this.subject);
        String p = transform(this.predicate);
        String o = transform(this.object);
        return p+"("+s+", "+o+")";
    }

    public HashMap<String, String> getConstantsMapping() {

        HashMap<String, String> hm = new HashMap<String, String>();
        if (!isVar(this.subject)) {
            hm.put(transform(this.subject), this.subject);
        }       
        if (!isVar(this.predicate)) {
            hm.put(transform(this.predicate), this.predicate);
        }
        if (!isVar(this.object)) {
            hm.put(transform(this.object), this.object);
        }
        return hm;
    }

    public static String transform(String s) {

        if (isVar(s)) {
            return s.substring(1);
        } else if (s.startsWith("<")) {
            int i = s.lastIndexOf("/");
            int j = s.lastIndexOf("#");
            j = Math.max(Math.max(i, j), 0);
            return s.substring(j+1, s.length()-1).toLowerCase().trim();
        } else if (s.lastIndexOf(":")>=0) {
            return s.substring(s.lastIndexOf(":")+1, s.length()).toLowerCase().trim();
        } else {
            return s.toLowerCase().trim();
        }
    }
}
