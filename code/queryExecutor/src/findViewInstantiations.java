import java.util.*;
import java.io.*;

class findViewInstantiations {

    public static void main (String args[]) {

        try {

            String nameIn = args[0];
            String nameOut = args[1];
	    BufferedReader br = new BufferedReader(new FileReader(nameIn));
	    String l = br.readLine();
	    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(nameOut, true), "UTF-8"));
	    HashSet<String> hs = new HashSet<String>();
	    while (l != null) {
                HashSet<String> aux = processLine(l);
		hs.addAll(aux);
		l = br.readLine();
	    }
	    for (String e : hs) {
                bw.write(e);
	        bw.newLine();
	    }
	    bw.flush();
	    bw.close();
        } catch (Exception e) {
            e.printStackTrace(System.out);
	}
    }

    public static HashSet<String> processLine(String l) {

        StringTokenizer st = new StringTokenizer(l, " \t\n\r\f:-)", false);
	HashSet<String> res = new HashSet<String>();

	while (st.hasMoreTokens()) {
            String t = st.nextToken();
            if (t.startsWith(",")) {
	        t = t.substring(1);
            }
	    if (t.startsWith("view")) {
                StringTokenizer st2 = new StringTokenizer(t, "(,", true);
                String res2 = st2.nextToken();
		boolean include = false;
		while (st2.hasMoreTokens()) {
                    String arg = st2.nextToken();
                    char c = arg.charAt(0);
		    if ((Character.isLetter(c) && Character.isLowerCase(c))|| Character.isDigit(c)) {
                        include = true;
		    }
		}
		if (include) {
		    res.add(t+")");
		}
	    }
	}
	return res;
    }
}
