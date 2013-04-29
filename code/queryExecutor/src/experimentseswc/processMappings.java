import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

public class processMappings {

    public static void main (String args[]) {

        try {
            String nameIn = args[0];
            String nameOut = args[1];

            BufferedReader br = new BufferedReader(new FileReader(nameIn));
            String l = br.readLine();

            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(nameOut), 
                                                         "UTF-8"));
            while (l != null) {
                String m = processLine(l);
                output.write(m);
                output.newLine();
                l = br.readLine();
            }
            output.flush();
            output.close();
        }  catch (Exception e) {
			e.printStackTrace(System.out);
        }
    }

    public static String processLine(String l) {

        StringTokenizer st = new StringTokenizer(l, " \t\n\r\f:-(),", true);
        String ns = "";
        String end = "";
        if (st.hasMoreTokens()) {
            String t = st.nextToken();
            int p = t.indexOf("w");
            end = "_"+t.substring(p+1);
            ns = ns + t;
        }

        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            char c = t.charAt(0);
            if (Character.isLetter(c) && Character.isUpperCase(c)) {
                t = t + end;
            }
            ns = ns + t;
        }
        return ns;
    }
}
