package experimentseswc;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Set;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;

public class CountingStream extends Thread {

    private InputStream is;
    private Counter counter;

    public CountingStream (InputStream is, Counter c) {
        this.is = is;
        this.counter = c;
    }

    public void run () {

        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line=br.readLine())!= null) {
                //System.out.println("considering rewriting: "+line);
                if (!line.equals("")) {
                    this.counter.increase();
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
