package experimentseswc;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Set;

public class BushyTree  {

    public static BinaryTree getBushyTree(ArrayList<Predicate> ps) {

        HashMap<String, Integer> info = new HashMap<String, Integer>();
        HashSet<Leaf> leafs = createLeafs(ps, info);
     /* for (Leaf l : leafs) {
            if (l.infoJoins==null) {
                 System.out.println("leaf con infojoin null.. " +l);
            }
        }*/

        PriorityQueue<BinaryTree> pq = new PriorityQueue<BinaryTree>(leafs);
        ArrayList<BinaryTree> others =  new ArrayList<BinaryTree>();
        while (pq.size() > 1) {
            boolean done = false;
            BinaryTree l = pq.poll();
            //System.out.println("l_infojoin: "+l.infoJoins);
            for (BinaryTree r : pq ) {

                if (shareAtLeastOneVar(l,r)) {
                    //System.out.println("r_infojoin: "+r.infoJoins);
                    pq.remove(r);
                    BinaryTree n = new Branch(l, r);
                    //System.out.println("n_infojoin: "+n.infoJoins);
                    pq.add(n);
                    done = true;
                    break;
                }
            }
            if (!done) { 
                others.add(l);
            }
        }
        if (pq.size() == 1) {
            BinaryTree bt = pq.poll();
            for (BinaryTree e : others) {
                bt = new Branch(bt, e);
            }
            return bt;

        } else if (others != null) {
            while (others.size() > 1) {
                BinaryTree l = others.remove(0);
                BinaryTree r = others.remove(0);
                BinaryTree n = new Branch(l, r);
                others.add(n);
            }
            if (others.size() > 0) {
                return others.remove(0);
            }
        }
        return null;
    }

    private static HashSet<Leaf> createLeafs (ArrayList<Predicate> ps, 
                                              HashMap<String, Integer> info) {
        for (Predicate p : ps) {
            HashSet<String> l = new HashSet<String>(p.getVars());
            for (String e : l) {
                int v = 1;
                if (info.containsKey(e)) {
                    v = info.get(e) + 1;
                }
                info.put(e, v);
            }
        }
        HashSet<String> el = new HashSet<String>();
        Set<String> ks = info.keySet();
        for (String k : ks) {
            int v = info.get(k) - 1;
            info.put(k, v);
            if (v <= 0) {
                el.add(k);
            }
        }
        for (String e : el) {
            info.remove(e);
        }
        HashSet<Leaf> ls = new HashSet<Leaf>();

        for (Predicate s : ps) {

            ArrayList<String> e = new ArrayList<String> ();
            ArrayList<String> l = s.getVars();
            for (String v : l) {
                if (info.containsKey(v)) {
                    e.add(v);
                }
            }
            ls.add(new Leaf(s, e, info));
        }
        return ls;
    }

    private static boolean shareAtLeastOneVar (BinaryTree l, BinaryTree r) {

        HashSet<String> vars = new HashSet<String>(l.getJoinVariables());
        vars.retainAll(r.getJoinVariables());
        return vars.size() > 0;
    }
}
