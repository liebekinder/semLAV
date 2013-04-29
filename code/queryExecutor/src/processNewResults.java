import java.io.*;

// java processNewResults $PATH_TO_FOLDERS $FOLDERS_BEGIN_WITH $RESULTS_FILE
public class processNewResults {

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
        output.write("# Query\tExecution Time first answer\tExecution Time to achieve recall 1.00 (secs)" 
                     + "\tExecution Time to include all the relevant views (secs)");
        output.newLine();
        output.flush();
        output.close();
        if (content != null) {

            for (File g : content) {
                if (g.isDirectory() && g.getName().startsWith(foldersStarting)) {
                    output = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(outputName, true), "UTF-8"));
                    processFolder(g, output);    
                    output.flush();
                    output.close();
                }
            }
        }
    }

    public static void processFolder(File g, BufferedWriter output) throws Exception {

        String dirName = g.getAbsolutePath();
        String timeFile = dirName+"/TimeTable";
        //System.out.println("timeFile: "+timeFile);
        int[] timeN = readTimes(timeFile, "NOTHING");
        //System.out.println("times de nothing: "+timeN.length);
        File[] content = g.listFiles();
        String y = dirName.substring(dirName.indexOf("q"));
        y = y.substring(5);
        if (content != null) {
            for (File h : content) {
                if (h.isDirectory() && !h.isHidden()) {
                    try {
                        processFolder2(h, timeN, y, output);    
                    } catch (Exception e) {
                        System.err.println("Problems with "+g.getAbsolutePath());
                    }
                }
            }
        }
    }

    public static int[] readTimes(String fileName, String fs) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String l = br.readLine();
        int k = 0;
        while (l != null) {
            if (l.indexOf(fs) >= 0) {
                k++;
            }
            l = br.readLine();
        }
        br.close();
        int[] times = new int[k];
        br = new BufferedReader(new FileReader(fileName));
        k = 0;
        l = br.readLine();
        while (l != null) {
            if (l.indexOf(fs) >= 0) {
                times[k] = Integer.parseInt(l.substring(l.lastIndexOf(" ")+1));
                k++;
            }
            l = br.readLine();            
        }
        br.close();
        return times;
    }

    public static double[] getRecall(String fileName, int k) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String l = br.readLine();
        double[] recalls = new double[k];
        int i = 0;
        while (l != null) {
            recalls[i++] = Double.parseDouble(l.substring(l.lastIndexOf(" ")+1));
            l = br.readLine();
        }
        br.close();

        return recalls;
    }

    public static void processFolder2(File g, int[] times, String y, 
                                      BufferedWriter output) throws Exception {

        String dirName = g.getName();
        String dirPath = g.getAbsolutePath();
        double recall[] = getRecall(dirPath+"/Recall", times.length);
        int f = getPosWholeAnswer(recall);
        int e = getPosFirstAnswer(recall);
        output.write(y+"\t"+times[e]+"\t"+times[f]+"\t"+times[times.length-1]);
        output.newLine(); 
    }

    private static int getPosFirstAnswer(double[] recall) {

        for (int i = 0; i < recall.length; i++) {
            if (recall[i]>0) {
                return i;
            }
        }
        return recall.length-1;
    }

    private static int getPosWholeAnswer(double[] recall) {

        for (int i = 0; i < recall.length; i++) {
            if (recall[i]==1.0) {
                return i;
            }
        }
        return recall.length-1;
    }
}
