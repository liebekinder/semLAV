import java.io.*;

public class processFile {

    public static void main(String args[]) throws Exception {

        String fileIn = args[0];
        String fileOut = args[1];
        extractInfo(fileIn, fileOut);    
    }

    public static void extractInfo(String in, String out) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(in));
        String l = null;
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(out), "UTF-8"));
        do {
            l = br.readLine();
            if (l == null) {
                break;
            } else if (l.indexOf("model size") < 0) {
                continue;
            }
            int i = l.lastIndexOf(" ");
            String s = l.substring(i+1);
            l = br.readLine();
            while (l != null && l.indexOf("model size") >= 0) {
                i = l.lastIndexOf(" ");
                s = s + "+" + l.substring(i+1);
                l = br.readLine();
            }
            output.write(s);
            output.newLine();
        } while (l != null);
        output.flush();
        output.close(); 
        br.close();
    }
}
