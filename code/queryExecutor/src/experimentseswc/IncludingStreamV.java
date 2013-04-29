package experimentseswc;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;

public class IncludingStreamV extends Thread {

    private InputStream is;
    private Model graphUnion;
    private Counter includedViews;
    private String pref;
    private String sufi;

    public IncludingStreamV (InputStream is, Model gu, Counter iv, String pref, String sufi) {

        this.is = is;
        this.graphUnion = gu;
        this.includedViews = iv;
        this.pref = pref;
        this.sufi = sufi;
    }

    public void run () {

        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String v = null;
            while ((v=br.readLine())!= null) {
                graphUnion.enterCriticalSection(Lock.WRITE);
                try {
                    graphUnion = FileManager.get().readModel(graphUnion, pref+v+sufi);
                    includedViews.increase();
                } catch (java.lang.OutOfMemoryError oome) {
                    System.err.println("Error during execution: "
                                      +"out of memory.");
                    return;
                } finally {
                    graphUnion.leaveCriticalSection();
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
