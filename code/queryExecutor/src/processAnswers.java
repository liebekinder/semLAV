import java.io.*;

public class processAnswers {

    public static void main(String args[]) throws Exception {

        String dir = args[0]; //"/home/gabriela/gun2012/code/expfiles/berlinDataSet2/dataset10_5";
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

        String dirName = g.getAbsolutePath();
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(dirName+"/answerSize", true), "UTF-8"));
        File[] content = g.listFiles();
        if (content != null) {
            for (File h : content) {
                if (h.isFile() && h.getName().startsWith("solutions")) {
                    String s = processFile(h);
                    output.write(s);
                    output.newLine();
                    //h.deleteOnExit();
                }
            }
        }
        output.flush();
        output.close(); 
    }

    public static String processFile(File h) throws Exception {
        //System.out.println(h.getAbsolutePath());
        int i = System.getProperty("user.dir").indexOf("/code/") + 6;
        String path = System.getProperty("user.dir").substring(0, i)+"expfiles/scripts/";
        ProcessBuilder pb = new ProcessBuilder(path+"calculateNumber.sh", h.getAbsolutePath());
		try {
			Process p = pb.start();
            p.waitFor();
            InputStream is = p.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String s = br.readLine();
            return s;
        } catch (IOException ex) {
			ex.printStackTrace(System.out);
			return null;
		} catch (InterruptedException iex){
			iex.printStackTrace(System.out);
			return null;
        }
    }
}
