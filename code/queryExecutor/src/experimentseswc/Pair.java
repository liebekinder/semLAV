package experimentseswc;

public class Pair {

    private int pos;
    private String var;

    public Pair(int p, String v) {
        this.pos = p;
        this.var = v;
    }

    public boolean equals(Object o) {
        return (o != null) && (o instanceof Pair) && (((Pair) o).pos == this.pos) && (((Pair) o).var == this.var);
    }

    public String toString() {
        return "("+pos+", "+var+")";
    }
}
