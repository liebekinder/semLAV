import java.io.*;
import java.util.ArrayList;

// java processResultsSemLAVThreaded $PATH_TO_FOLDERS $FOLDERS_BEGIN_WITH $RESULTS_FILE $WHOLEANSWERSIZES
public class processResultsSemLAVThreaded {

    public static void main(String args[]) throws Exception {

        String dir = args[0];
        String foldersStarting = args[1];
        String outputName = args[2];
        File f = new File(dir);
        File[] content = f.listFiles();
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                               new FileOutputStream(outputName, true), "UTF-8"));
        output.write("# "+outputName);
        output.newLine();
        output.write("# Query\tExecution Time first answer\tExecution Time to achieve recall 1.00 (msecs)" 
                     + "\tExecution Time to include all the relevant views (msecs)");
        output.newLine();
        output.flush();
        output.close();
        int[][] sizes = getSizes(args[3]);

        if (content != null) {

            for (File g : content) {
                if (g.isDirectory() && g.getName().startsWith(foldersStarting)) {
                    output = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(outputName, true), "UTF-8"));
                    processFolder(g, sizes, output);    
                    output.flush();
                    output.close();
                }
            }
        }
    }

    public static void processFolder(File g, int[][] sizes, BufferedWriter output) throws Exception {

        String dirName = g.getAbsolutePath();
        //String timeFile = dirName+"/TimeTable";
        //System.out.println("timeFile: "+timeFile);
        //int[] timeN = readTimes(timeFile, "NOTHING");
        //System.out.println("times de nothing: "+timeN.length);
        File[] content = g.listFiles();
        String y = dirName.substring(dirName.indexOf("q"));
        y = y.substring(5);
        int i = y.indexOf("GUN");
        if (i > -1) {
            y = y.substring(0, i);
        }
        if (content != null) {
            for (File h : content) {
                if (h.isDirectory() && !h.isHidden()) {
                    try {
                        processFolder2(h, y, sizes, output);
                    } catch (Exception e) {
                        System.err.println("Problems with "+g.getAbsolutePath());
                    }
                }
            }
        }
    }

    public static int[][] getSizes(String fileName) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String l = br.readLine();
        int k = 0;
        while (l != null) {
            k++;
            l = br.readLine();
        }
        br.close();
        int[][] sizes = new int[k][2];
        br = new BufferedReader(new FileReader(fileName));
        k = 0;
        l = br.readLine();
        while (l != null) {
            sizes[k][0] = Integer.parseInt(l.substring(0, l.lastIndexOf("\t")));
            sizes[k][1] = Integer.parseInt(l.substring(l.lastIndexOf("\t")+1));
            k++;
            l = br.readLine();            
        }
        br.close();
        return sizes;
    }

    public static boolean getData(String fileName, ArrayList<int[]> data) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String l = br.readLine();
        boolean incomplete = false;
        while (l != null) {
            if (!l.startsWith("#")) {
                int i = l.indexOf("\t");
                int j = l.lastIndexOf("\t");
                int time = Integer.parseInt(l.substring(0, i));
                int answers = Integer.parseInt(l.substring(i+1, j));
                int numberViews = Integer.parseInt(l.substring(j+1));
                int[] d = {time, answers, numberViews};
                data.add(d);
            } else if (l.indexOf("incomplete")>-1) {
                incomplete = true;
            }
            l = br.readLine();
        }
        br.close();

        return incomplete;
    }

    public static void processFolder2(File g, String y, int[][] sizes,
                                      BufferedWriter output) throws Exception {

        String dirName = g.getName();
        String dirPath = g.getAbsolutePath();
        ArrayList<int[]> data = new ArrayList<int[]>();
        boolean incomplete = getData(dirPath+"/throughput", data);
        int f = getPosWholeAnswer(data, sizes, y);
        int e = getPosFirstAnswer(data);
        String s1 = "N.A.";
        if (e >-1) {
            s1 = data.get(e)[0]+"";
        }
        String s2 = "N.A.";
        if (f >-1) {
            s2 = data.get(f)[0]+"";
        }
        String s3 = "N.A.";
        if (!incomplete) {
            s3 = data.get(data.size()-1)[0]+"";
        }
        output.write(y+"\t"+s1+"\t"+s2+"\t"+s3);
        output.newLine(); 
    }

    private static int getPosFirstAnswer(ArrayList<int[]> data) {

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i)[1]>0) {
                return i;
            }
        }
        return -1;
    }

    private static int getPosWholeAnswer(ArrayList<int[]> data, int[][] sizes, String y) {

        int p = Integer.parseInt(y);
        int wholeAnswer = 0;
        for (int i = 0; i < sizes.length; i++) {
            if (sizes[i][0]==p) {
                wholeAnswer = sizes[i][1];
                break;
            }
        }
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i)[1]>=wholeAnswer) {
                return i;
            }
        }
        return -1;
    }
}
