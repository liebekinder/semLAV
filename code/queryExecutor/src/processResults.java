import java.io.*;

// java processResults /home/gabriela/gun2012/code/expfiles/resultsExperiments12122012/300views output /home/gabriela/gun2012/code/expfiles/resultsExperiments12122012/newResults/
public class processResults {

    public static void main(String args[]) throws Exception {

        String dir = args[0]; //"/home/gabriela/gun2012/code/expfiles/berlinDataSet2/dataset10_5";
        String dir2 = args[1]; // which folder to process
        String outDir = args[2]; // where the output must go..
        File f = new File(dir);
        File[] content = f.listFiles();
        String setup = dir.substring(dir.lastIndexOf("/")+1);
        if (content != null) {

            for (File g : content) {
                if (g.isDirectory() && g.getName().startsWith(dir2)) { //"outputDataset10query")) {
                    String a = getApproach(g.getName());
                    processFolder(g, outDir, ("data"+setup+a));    
                }
            }
        }
    }

    public static String getApproach(String s) {

        if (s.endsWith("P")) {
            return "LLP";
        } else if (s.endsWith("N")) {
            return "GUN";
        } else if (s.endsWith("A")) {
            return "JENA";
        } else {
            return "ERROR";
        }
    }

    public static void processFolder(File g, String outDir, String name) throws Exception {

        String dirName = g.getAbsolutePath();
        String approach = getApproach(g.getName());
        String timeFile = dirName+"/TimeTable"+approach;
        //System.out.println("timeFile: "+timeFile);
        String[] timeEC = readTimes(timeFile, "EQUIVALENCECLASSSORT");
        String[] timeN = readTimes(timeFile, "NOTHING");
	System.out.println("times de nothing: "+timeN.length);
        File[] content = g.listFiles();
        String y = dirName.substring(dirName.indexOf("q"));
	y = y.substring(5);
        if (content != null) {
            for (File h : content) {
                if (h.isDirectory() && !h.isHidden()) {
                    try {
                        processFolder2(h, timeEC, timeN, outDir, y, approach, name);    
                    } catch (Exception e) {
                        System.err.println("Problems with "+g.getAbsolutePath());
                    }
                }
            }
        }
    }

    public static String[] readTimes(String fileName, String fs) throws Exception {

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
        String[] times = new String[k];
        br = new BufferedReader(new FileReader(fileName));
        k = 0;
        l = br.readLine();
        while (l != null) {
            if (l.indexOf(fs) >= 0) {
                //System.out.println(l);
                //System.out.println(l.lastIndexOf(" "));
                //System.out.println(l.lastIndexOf("\t"));
                times[k] = l.substring(l.lastIndexOf(" ")+1);
                k++;
            }
            l = br.readLine();            
        }
        br.close();
        return times;
    }

    public static String[] getRecall(String fileName, int k) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String l = br.readLine();
        String[] recalls = new String[k];
        int i = 0;
        while (l != null) {
            recalls[i++] = l.substring(l.lastIndexOf(" ")+1);
            l = br.readLine();
        }
        br.close();

        return recalls;
    }

    public static int[] getSortedAnswersSize(String fileName, int k) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String l = br.readLine();
        int[] sizes = new int[k];
        int i = 0;
        while (l != null) {
            sizes[i++] = Integer.parseInt(l);
            l = br.readLine();
        }
        java.util.Arrays.sort(sizes);
        br.close();

        return sizes;
    }

    public static String[] getModelSize(String fileName, int k) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String l = br.readLine();
        String[] sizes = new String[k];
        int i = 0;
        System.out.println(fileName);
        System.out.println(l);
        while (l != null) {
            sizes[i++] = l.replace('+', '\t');
            l = br.readLine();
            System.out.println(l);
        }
        br.close();

        return sizes;
    }

    public static void processFolder2(File g, String[] timeEC, String[] timeN, String outDir, String y, String approach, String name) throws Exception {

        String dirName = g.getName();
        System.out.println(dirName);
        String dirPath = g.getAbsolutePath();
        String[] times = dirName.endsWith("NOTHING") ? timeN : (dirName.endsWith("EQUIVALENCECLASSSORT") ? timeEC : null);
        String x = (dirName.endsWith("EQUIVALENCECLASSSORT")) ? "EC" : (dirName.endsWith("NOTHING") ? "N" : "ERROR");
        //int answerSize[] = getSortedAnswersSize(dirPath+"/AnswersSize", times.length);
        //String modelSize[] = getModelSize(dirPath+"/info2", times.length);
        String recall[] = getRecall(dirPath+"/Recall", times.length);
        //String recallM[] = getRecall(dirPath+"/RecallMains", times.length);
        //String recallD[] = getRecall(dirPath+"/RecallDesserts", times.length);
        String numQuery = y.substring(0, y.indexOf(approach));
        String outputName = outDir+"/"+name+"_Query"+numQuery+"_"+x+".dat";
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(outputName, true), "UTF-8"));
        output.write("# "+outputName);
        output.newLine();
        output.write("# RW\tExecution Time (in milliseconds)\tRecall");
        output.newLine();

        for (int k = 0; k < recall.length; k++) {
            output.write((k+1)+"\t"+times[k]+"\t"+recall[k]/*+"\t"+recallM[k]+"\t"+recallD[k]+"\t"+modelSize[k]*/); //+"\t"+answerSize[k]);
            output.newLine();
        } 
        output.flush();
        output.close();
    }
}
