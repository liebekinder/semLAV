package experimentseswc;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.shared.Lock;

import java.util.concurrent.TimeUnit;
import java.util.HashSet;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.File;

public class QueryingStream extends Thread {

    private Model graphUnion;
    private Reasoner reasoner;
    private Query query;
    //private HashSet<ArrayList<String>> solutionsGathered;
    private Timer timer;
    private Timer executionTimer;
    private Counter counter;
    private BufferedWriter info;
    private int time = 1000;
    private int lastValue = 0;
    private String dir;
    private boolean queried = false;
    private Counter ids;
    Timer wrapperTimer;
    Timer graphCreationTimer;

    public QueryingStream (Model gu, Reasoner r, Query q, 
                           HashSet<ArrayList<String>> sgs, Timer et, Timer t, 
                           Counter c, BufferedWriter i, String dir, Timer wrapperTimer, Timer graphCreationTimer, Counter ids) {
        this.graphUnion = gu;
        this.reasoner = r;
        this.query = q;
        //this.solutionsGathered = sgs;
        this.executionTimer = et;
        this.timer = t;
        this.counter = c;
        this.info = i;
        this.dir = dir;
        this.wrapperTimer = wrapperTimer;
        this.graphCreationTimer = graphCreationTimer;
        this.ids = ids;
    }

    private void evaluateQuery() {

        Model m = graphUnion;
        if (reasoner != null) {
            m = ModelFactory.createInfModel (reasoner, m);
        }
        if (this.counter.getValue() != this.lastValue) {            			m.enterCriticalSection(Lock.READ);
            int tempValue = this.counter.getValue();
            int id = this.ids.getValue();
            this.ids.increase();
            String fileName = this.dir + "/solution"+id;
            try {
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                                               new FileOutputStream(fileName), "UTF-8"));

            //this.lastValue = this.counter.getValue();
            //fileName = this.dir + "/solution"+this.lastValue;
            executionTimer.resume();
            QueryExecution result = QueryExecutionFactory.create(query.toString(), m);
            for (ResultSet rs = result.execSelect(); rs.hasNext();) {
                QuerySolution binding = rs.nextSolution();
                ArrayList<String> s = new ArrayList<String>();
                for (String var : query.getVars()) {
                    String val = binding.get(var).toString();
                    s.add(val);
                }
                executionTimer.stop();
                if (!queried) {
                    message(id + "\t" + tempValue + "\t" + TimeUnit.MILLISECONDS.toMillis(wrapperTimer.getTotalTime())
                            + "\t" + TimeUnit.MILLISECONDS.toMillis(graphCreationTimer.getTotalTime())
                            + "\t" + TimeUnit.MILLISECONDS.toMillis(executionTimer.getTotalTime())
                            + "\t" + TimeUnit.MILLISECONDS.toMillis(timer.getTotalTime())
                            + "\t" + graphUnion.size()
                            + "\t1");
                    time = 10;
                    queried = true;
                }
                //timer.stop();
                output.write(s.toString());
                output.newLine();
                executionTimer.resume();
                //solutionsGathered.add(s);
            }
            executionTimer.stop();
            m.leaveCriticalSection();
            timer.stop();
            output.flush();
            output.close();
            message(id + "\t" + tempValue + "\t" + TimeUnit.MILLISECONDS.toMillis(wrapperTimer.getTotalTime()) 
                                            + "\t" + TimeUnit.MILLISECONDS.toMillis(graphCreationTimer.getTotalTime())
                                            + "\t" + TimeUnit.MILLISECONDS.toMillis(executionTimer.getTotalTime())
                                            + "\t" +  TimeUnit.MILLISECONDS.toMillis(timer.getTotalTime())
                                            + "\t" + graphUnion.size());
            timer.resume();
            this.lastValue = tempValue;
            } catch (java.io.IOException ioe) {
                System.err.println("problems writing to "+fileName);
            } catch (java.lang.OutOfMemoryError oome) {
                Main.deleteDir(new File(fileName));
                System.out.println("out of memory while querying");
                //evaluateQuery();
            }
        }
    }

    private void message(String s) {
        synchronized(timer) {
            timer.stop();
            try {
                info.write(s);
                info.newLine();
                info.flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            timer.resume();
        }
    }

    public void run () {

        try {
            while (true) {
                Thread.sleep(time);
                evaluateQuery();
            }
        } catch (InterruptedException ie) {
            //System.out.println("Query evaluation ended");
        } finally {
            evaluateQuery();
        }
    }
}
