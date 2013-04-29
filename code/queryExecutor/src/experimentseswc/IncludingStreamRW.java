package experimentseswc;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Set;
import java.util.HashMap;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;

public class IncludingStreamRW extends Thread {

    private InputStream is;
    private Catalog catalog;
    private Model graphUnion;
    private Set loadedViews;
    private Counter counter;
    private int numViews;
    private HashMap<String, String> constants;

    public IncludingStreamRW (InputStream is, Catalog c, Model gu, Set lvs, 
                              Counter rc, int nvs, HashMap<String, String> cs) {
        this.is = is;
        this.catalog = c;
        this.graphUnion = gu;
        this.loadedViews = lvs;
        this.counter = rc;
        this.numViews = nvs;
        this.constants = cs;
    }

    public void run () {

        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line=br.readLine())!= null) {
                //System.out.println("considering rewriting: "+line);
                Rewrite rew = new Rewrite(line);
                this.counter.increase();
                for(Predicate pred : rew.getGoals()){
                    if(loadedViews.add(pred.getName())){
                        graphUnion.enterCriticalSection(Lock.WRITE);
                        try {
                            graphUnion.add(catalog.getModel(pred, constants));
                        } catch (java.lang.OutOfMemoryError oome) {
                            System.err.println("Error during execution: "
                                              +"out of memory.");
                            return;
                        } finally {
                            graphUnion.leaveCriticalSection();
                        }
                    }
                }
                if (loadedViews.size() == numViews) {
                    break;
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
