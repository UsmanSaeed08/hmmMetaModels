package camps_Scoring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeMap;

public class MakeDistFuncFiles {

	/**
	 * @param args
	 */
/*	private static final String HMM_DIR = "camps_data/HMMs/";
    private static final String SCORESFILES = "camps_data/metaModelsScores/";
    private static final boolean ONLY_HMMS = false;
*/
    public static void main(String[] args) throws IOException {
        String fileName;
        /*System.out.println("Preparing common serialization file for all hmms...");
        for (File f2 : new File("camps_data/HMMs/").listFiles()) {
            fileName = f2.getName();
            if (!fileName.endsWith(".hmm")) continue;
            System.out.println(fileName);
            HMMGraphData hmm = MakeDistFuncFiles.loadHmmData(f2);
            MakeDistFuncFiles.serializeObjectToFile((Serializable)hmm, new File("camps_data/HMMs/", fileName + ".serialized"));
        }*/
        System.out.println("Preparing distribution function files for nonmemeber scores...");
        //F:\Scratch\campsWebsite\scores
        for (File f2 : new File("/home/users/saeed/scores/").listFiles()) {
            if (!f2.getName().endsWith("_nonmembers.scores")) continue;
            fileName = f2.getName(); // CMSC0559_nonmembers.scores
            String outFileName = fileName.substring(0, fileName.lastIndexOf(46)) + ".distfunc";
            System.out.println(fileName+"\t"+outFileName);
            //System.out.println(outFileName);
            MakeDistFuncFiles.processScoreFile(f2, new File("/home/users/saeed/scores/", outFileName));
        }
    }
    
    /*
    private static void serializeObjectToFile(Serializable obj, File out) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(out));
        ObjectOutputStream os = new ObjectOutputStream(bos);
        os.writeObject(obj);
        os.close();
    }

    private static HMMGraphData loadHmmData(File file) {
        int region = 0;
        int i = 0;
        int nbStates = 0;
        int observables = 0;
        String alphabet = MakeDistFuncFiles.getAlphabet();
        double[] startTransitions = null;
        double[][] stateEmissions = null;
        Map[] stateTransitions = null;
        try {
            String line;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            block9 : while ((line = reader.readLine()) != null) {
                if (line.startsWith("#states")) {
                    region = 0;
                    continue;
                }
                if (line.startsWith("#observables")) {
                    region = 1;
                    continue;
                }
                if (line.startsWith("#P_i")) {
                    region = 2;
                    continue;
                }
                if (line.startsWith("#A_ij")) {
                    region = 3;
                    continue;
                }
                if (line.startsWith("#IntegerDistribution")) {
                    region = 4;
                    i = 0;
                    continue;
                }
                if (line.startsWith("#secOrder")) {
                    region = 5;
                    continue;
                }
                switch (region) {
                    case 0: {
                        nbStates = new Integer(line);
                        startTransitions = new double[nbStates];
                        stateEmissions = new double[nbStates][alphabet.length()];
                        stateTransitions = new Map[nbStates];
                        break;
                    }
                    case 1: {
                        observables = new Integer(line);
                        if (observables == alphabet.length()) break;
                        throw new IllegalStateException();
                    }
                    case 2: {
                        String[] pi = line.split(",");
                        for (int j = 0; j < nbStates; ++j) {
                            startTransitions[j] = Double.parseDouble(pi[j]);
                        }
                        continue block9;
                    }
                    case 3: {
                        String[] aj = line.split(",");
                        TreeMap<Integer, Double> transRow = stateTransitions[i];
                        if (transRow == null) {
                            stateTransitions[i] = transRow = new TreeMap<Integer, Double>();
                        }
                        for (int j = 0; j < nbStates; ++j) {
                            double transProbab = Double.parseDouble(aj[j]);
                            if (transProbab <= 0.0) continue;
                            transRow.put(j, transProbab);
                        }
                        ++i;
                        break;
                    }
                    case 4: {
                        String[] intDistS = line.split(",");
                        for (int j = 0; j < observables; ++j) {
                            stateEmissions[i][j] = Double.parseDouble(intDistS[j]);
                        }
                        ++i;
                        break;
                    }
                }
            }
            reader.close();
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return new HMMGraphData((double[])startTransitions, stateEmissions, stateTransitions);
    }

    private static String getAlphabet() {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < 20; ++i) {
            try {
                ret.append(ASMapping.intToAS(i));
                continue;
            }
            catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return ret.toString();
    }
    */

    public static void processScoreFile(File scoresFile, File outputFile) throws IOException {
        String line;
        BufferedReader br = new BufferedReader(new FileReader(scoresFile));
        TreeMap<Double, Integer> map = new TreeMap<Double, Integer>();
        int countScores = 0;
        while ((line = br.readLine()) != null) {
            String[] content = line.split("\t");
            String scoreStr = content[0].trim();
            if (scoreStr.equals("NaN")) continue;
            if (scoreStr.equals("-Infinity")) continue;
            ++countScores;
            double currentScore = Double.parseDouble(scoreStr);
            Integer localCount = (Integer)map.get(currentScore = (double)Math.round(currentScore * 1000.0) / 1000.0);
            if (localCount == null) {
                localCount = 1;
            } else {
                Integer n = localCount;
                Integer n2 = localCount = Integer.valueOf(localCount + 1);
            }
            map.put(currentScore, localCount);
        }
        br.close();
        int countScores2 = 0;
        TreeMap<Double, Integer> map2 = new TreeMap<Double, Integer>();
        for (Double key : map.descendingKeySet()) {
            int localCount = (Integer)map.get(key);
            map2.put(key, countScores2+=localCount);
        }
        if (countScores != countScores2) {
            throw new IllegalStateException("Not good: " + countScores + ", " + countScores2);
        }
        PrintWriter pw = new PrintWriter(outputFile);
        for (Double key2 : map.keySet()) {
            pw.println(key2 + "\t" + map2.get(key2) + "\t" + map.get(key2));
        }
        pw.close();
    }

}
