package experimentseswc;

import java.util.HashSet;
import java.util.HashMap;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.BufferedWriter;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import java.util.concurrent.TimeUnit;

// This version intends to include views considering the arguments
public class IncludingStreamV2 extends Thread {

    private InputStream[] iss;
    private Model graphUnion;
    private Counter includedViews;
    private Catalog catalog;
    HashMap<String, String> constants;
    Timer wrapperTimer;
    Timer graphCreationTimer;
    Timer executionTimer;
    Timer totalTimer;
    BufferedWriter info2;
    Counter ids;

    public IncludingStreamV2 (InputStream[] iss, Model gu, Counter iv, Catalog c, 
                              HashMap<String, String> cs, Timer wrapperTimer, 
                              Timer graphCreationTimer, Timer executionTimer, 
                              Timer totalTimer, BufferedWriter info2, Counter ids) {

        this.iss = iss;
        this.graphUnion = gu;
        this.includedViews = iv;
        this.catalog = c;
        this.constants = cs;
        this.wrapperTimer = wrapperTimer;
        this.graphCreationTimer = graphCreationTimer;
        this.executionTimer = executionTimer;
        this.totalTimer = totalTimer;
        this.info2 = info2;
        this.ids = ids;
    }

    public void reset() {

        message(this.ids.getValue() + "\t" + this.includedViews.getValue() + "\t" 
                                              + TimeUnit.MILLISECONDS.toMillis(wrapperTimer.getTotalTime()) 
                                              + "\t" + TimeUnit.MILLISECONDS.toMillis(graphCreationTimer.getTotalTime())
                                              + "\t" + TimeUnit.MILLISECONDS.toMillis(executionTimer.getTotalTime())
                                              + "\t" +  TimeUnit.MILLISECONDS.toMillis(totalTimer.getTotalTime())
                                              + "\t" + graphUnion.size());        
        wrapperTimer.start();
        graphCreationTimer.start();
        executionTimer.start();
        includedViews.reset();
    }

    private void message(String s) {
        synchronized(totalTimer) {
            totalTimer.stop();
            try {
                info2.write(s);
                info2.newLine();
                info2.flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            totalTimer.resume();
        }
    }

    public void run () {

        try {
            InputStreamReader[] isrs = new InputStreamReader[iss.length];
            BufferedReader[] brs = new BufferedReader[isrs.length];
            HashSet<Predicate> includedViewsSet = new HashSet<Predicate>();
            boolean[] finished = new boolean[iss.length];
            for (int i = 0; i < iss.length; i++) {
                isrs[i] = new InputStreamReader(iss[i]);
                brs[i] = new BufferedReader(isrs[i]);
                finished[i] = false;
            }
            boolean finish = false;
            Predicate sentinel = new Predicate("end()");
            while (!finish) {
                finish = true;
                for (int i = 0; i < brs.length; i++) {
                    String v = null;
                    brs[i].mark(100);
                    if (finished[i]) {
                        continue;
                    }
                    if (!brs[i].ready()) {
                        //System.out.println("not ready");
                        finish = false;
                        continue;
                    }
                    if ((v=brs[i].readLine())!= null) {
                        Predicate view = new Predicate(v);
                        if (view.equals(sentinel)) {
                            finished[i] = true;
                            continue;
                        }
                        finish = false;
                        if (includedViewsSet.add(view)) {
                            //String viewName = view.getName();
                            graphUnion.enterCriticalSection(Lock.WRITE);
                            try {
                                //System.out.println("Including "+view);
                                wrapperTimer.resume();
                                Model tmp =  catalog.getModel(view, constants);
                                wrapperTimer.stop();
                                graphCreationTimer.resume();
                                graphUnion.add(tmp);
                                graphCreationTimer.stop();
                                //System.out.println("done");
                                includedViews.increase();
                                //System.out.println("considered views: "+ includedViews.getValue());
                            } catch (java.lang.OutOfMemoryError oome) {
                                brs[i].reset();
                                // Should reset also the previous ones
                                /*for (int j = 0; j < i; j++) {
                                    brs[j].reset();
                                }*/
                                reset();
                                graphUnion.removeAll();
                                finish = false;
                                includedViewsSet = new HashSet<Predicate>();
                                System.out.println("out of memory error");
                                // Should also reset all the ones that are empty
                                for (int j = 0; j != i && j < brs.length; j++) {
                                    if (finished[j]) {
                                        brs[j].reset();
                                        brs[j].reset();
                                        finished[j] = false;
                                        System.out.println("reset subgoal "+j);
                                    }
                                }
                                break;
                                // Should verify that this does not cause deadlock
                            } catch (com.hp.hpl.jena.n3.turtle.TurtleParseException tpe) {
                                //brs[i].reset();
                                // Should reset also the previous ones
                                /*for (int j = 0; j < i; j++) {
                                    brs[j].reset();
                                }*/
                                reset();
                                graphUnion.removeAll();
                                finish = false;
                                includedViewsSet = new HashSet<Predicate>();
                                System.out.println("jena exception");
                                for (int j = 0; j != i && j < brs.length; j++) {
                                    if (finished[j]) {
                                        brs[j].reset();
                                        brs[j].reset();
                                        finished[j] = false;
                                        System.out.println("reset subgoal "+j);
                                    }
                                }
                                break;
                                // Should also reset all the ones that are empty
                                // Should verify that this does not cause deadlock
                            } finally {
                                //System.out.println("going to leave cs");
                                graphUnion.leaveCriticalSection();
                                //System.out.println("going to leave cs -- done");
                            }
                        }
                    }
                }
            }
//            System.out.println("finish view inclusion!");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
