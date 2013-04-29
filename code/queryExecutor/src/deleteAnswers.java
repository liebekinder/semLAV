import java.io.*;

public class deleteAnswers {

    public static void main(String args[]) throws Exception {

        String dir = args[0];//"/home/gabriela/gun2012/code/expfiles/berlinDataSet2/dataset10_5";
        String dir2 = args[1];
        File f = new File(dir);
        File[] content = f.listFiles();
        if (content != null) {
            for (File g : content) {
                if (g.isDirectory() && g.getName().startsWith(dir2)) { //"outputDataset10query")) {
                    processFolder(g);    
                }
            }
        }
    }

    public static void processFolder(File g) throws Exception {

        File[] content = g.listFiles();
        if (content != null) {
            for (File h : content) {
                if (h.isDirectory() && !h.isHidden()) {
                    processFolder2(h);    
                }
            }
        }
    }

    public static void processFolder2(File g) throws Exception {

        File[] content = g.listFiles();
        if (content != null) {
            for (File h : content) {
                if (h.isFile() && h.getName().startsWith("solutions")) {
                    boolean b = h.delete();
                    if (!b) {
                        System.out.println(h.getName()+" could not be deleted");
                    }
                }
            }
        }
    }
}
