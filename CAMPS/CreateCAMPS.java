package CAMPS;

import CAMPS.hmm.ASMapping;
import CAMPS.utils.*;

import java.sql.Connection;
import java.util.*;
import java.io.*;

import util.hmm.HMMGraph;
import util.hmm.HMMGraphData;

public class CreateCAMPS {
	/**
	 * @param args
	 */		
	//private static final Connection connection = Database.getCon();
	private static final Connection connection = DBAdaptor.getConnection("CAMPS4");
	
	public static void main(String[] args) {
		try{
			//testCase();
			//System.exit(0);
			int i = 0;
			if(args.length == 4){
				i = new Integer(args[0]);
				Global.directory = args[1];
				Global.HMMDir = args[2];
				Global.TMPDir = args[3];
			}
			
			Utils.setGlobalVariables("config.txt");
			Global.thresholds = Database.getClusterThresholds(connection);			

			if(i<33){
				System.out.println("Generate HMMs Threshold: " + Global.thresholds[i]);
			}
			ArrayList<Integer> splitList = new ArrayList<Integer>();
			ArrayList<Integer> cluster_ids = new ArrayList<Integer>();
			
			if(i==0){
				// get cluster_ids for initial clusters
				cluster_ids = Database.getBigClusterIDs(Global.thresholds[i],connection);
//				ArrayList<Integer> tmpCluster = new ArrayList<Integer>();
//				tmpCluster = Database.getBigClusterIDs(Global.thresholds[i]);
//				for(int nb = 0; nb < 20; nb++){
//					cluster_ids.add(tmpCluster.get(nb));
//				}				
				System.out.println("size:" + cluster_ids.size());
			}
			else{
				// add clusters that are not homoegenous enough (last test round) to splitList
				splitList = readClusterIdFromFile(Global.thresholds[i-1]);			
				// remove HMMs from this bad clusters
				removeBadHMMs(splitList, Global.thresholds[i-1]);
				//in last iteration only the wrong HMMs have to be deleted
				if(Math.abs(Global.thresholds[i])==1000) return;
				if(i>32) return;
				// update wrongList with splitted clusters
				updateWrongList(splitList, Global.thresholds[i-1], Global.thresholds[i]);
				// calculate the cluster_ids of the subclusters of the bad clusters (splitlist)
				cluster_ids = CreateCampsCluster.createSplitCluster(splitList, Global.thresholds[i-1], Global.thresholds[i], connection);
				// create serialized hmms
				// run one time only... cuz files would be serialized after one run
				
			}
						
			System.out.println("split " + splitList + " into " + cluster_ids);
						
			// create new HMMs for the new splitted clusters, some clusters are too big and have 
			// therefore no assigned TM blocks. This clusters have to be splitted in every case -> splitlist

			// to check am making list small *********
			//ArrayList<Integer> cluster_ids_small = new ArrayList<Integer>();
			//for(int x =602;x<=670;x++){
				//cluster_ids_small.add(cluster_ids.get(x));
			//}
			splitList = CreateCampsCluster.createHMMsLevel1(cluster_ids, Global.thresholds[i], Global.HMMDir, connection);
			//splitList = CreateCampsCluster.createHMMsLevel1(cluster_ids_small, Global.thresholds[i], Global.HMMDir, connection);
			for (File ff : new File(Global.getDirectory() + Global.HMMDir).listFiles()) {
				String fileName = ff.getName();
				if (fileName.endsWith(".hmm")) {
					System.out.println(fileName+" Serialized");
					HMMGraphData hmmD = loadHmmDataGraph(ff);
					serializeObjectToFile(hmmD, new File(Global.getDirectory() + Global.HMMDir, fileName + ".serialized"));					
				}
			}
			// no HMM for clusters from splitList are constructed, they have to be removed fom cluster_ids 
			for(Integer cluster_id : splitList){	
				cluster_ids.remove(cluster_id);	
				//cluster_ids_small.remove(cluster_id);
			}
			//write the newly constructed HMMs into CAMPS_cluster0.0_$i.txt
			writeClusterIdToFile(0, cluster_ids);	
			//writeClusterIdToFile(0, cluster_ids_small);
			//write the clusters which have to be split in the next round into CAMPS_cluster$thresholds[i].txt
			writeClusterIdToFile(Global.thresholds[i], splitList);
			
			System.out.println("Clusters which have to be split in the next Round:\n" + splitList);			
		}catch(Exception e){
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}
		finally{
			try{
				connection.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}

	}
	private static void serializeObjectToFile(Serializable obj, File out) throws IOException {
		OutputStream bos = new BufferedOutputStream(new FileOutputStream(out));
		ObjectOutputStream os = new ObjectOutputStream(bos);
		os.writeObject(obj);
		os.close();
	}
	private static String getAlphabet() {
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < 20; i++) {
			try {
				ret.append(ASMapping.intToAS(i));
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
		return ret.toString();
	}
	
	private static HMMGraphData loadHmmDataGraph(File file) {
		int region=0; int i = 0; int nbStates = 0;int observables = 0;
		//Open an output stream
		String alphabet = getAlphabet();
		double[] startTransitions = null;
		double[][] stateEmissions = null;
		Map<Integer, Double>[] stateTransitions = null;
		try{
			BufferedReader reader = new BufferedReader(new FileReader((file)));
			String line;
			while((line = reader.readLine()) != null){
				if(line.startsWith("#states")){
					region=0;
				}else if(line.startsWith("#observables")){
					region=1;
				}else if(line.startsWith("#P_i")){
					region=2;
				}else if(line.startsWith("#A_ij")){
					region=3;
				}else if(line.startsWith("#IntegerDistribution")){
					region=4;i=0;
				}else if(line.startsWith("#secOrder")){
					region=5;
				}else{
					switch(region){
					case 0: nbStates = new Integer(line);
					startTransitions = new double[nbStates];
					stateEmissions = new double[nbStates][alphabet.length()];
					//noinspection unchecked
					stateTransitions = new Map[nbStates];
					break;
					case 1: observables = new Integer(line);
					if (observables != alphabet.length())
						throw new IllegalStateException();
					break;
					case 2: String[] pi = line.split(",");
					for(int j=0; j<nbStates; j++){
						startTransitions[j] = Double.parseDouble(pi[j]);
					}
					break;
					case 3: String[] aj = line.split(",");
					Map<Integer, Double> transRow = stateTransitions[i];
					if (transRow == null) {
						transRow = new TreeMap<Integer, Double>();
						stateTransitions[i] = transRow;
					}
					for(int j=0; j<nbStates; j++){
						double transProbab = Double.parseDouble(aj[j]);
						if (transProbab > 0) {
							transRow.put(j, transProbab);
						}
					}
					i++;
					break;
					case 4: String[] intDistS = line.split(",");
					for(int j=0; j<observables; j++){
						stateEmissions[i][j] = Double.parseDouble(intDistS[j]);
					}
					i++;
					break;
					case 5: //secOrder = line.split(",");
						break;
					}
				}

			}
			reader.close();
		}catch(Exception e){
			e.printStackTrace();
			throw new IllegalStateException(e);
		}
		return new HMMGraphData(startTransitions, stateEmissions, stateTransitions);
	}
	
	public static void removeBadHMMs(ArrayList<Integer> splitList, double threshold){
		for(int i : splitList){
			File delFile = new File(Global.getDirectory() + 
					Global.HMMDir + "/cluster_" + threshold + "_" + i + ".hmm");
			File delFileSerial = new File(Global.getDirectory() + 
					Global.HMMDir + "/cluster_" + threshold + "_" + i + ".hmm.serialized");
			delFile.delete();
			delFileSerial.delete();
		}
	}
	public static ArrayList<Integer> readClusterIdFromFile(double thres){
		ArrayList<Integer> cluster = new ArrayList<Integer>();
		try{
			for(int i = 0; i < Global.rounds; i++){
				BufferedReader reader = null;
				reader = new BufferedReader(new FileReader(Global.getDirectory() + Global.TMPDir + "CAMPS_cluster"+thres+"_"+i+".txt"));				
				
				String line;						
				while((line = reader.readLine()) != null){
					if(!line.equals("")){
						try{
							Integer id = new Integer(line);
							if(!cluster.contains(id)){
								cluster.add(id);
							}
						}catch(Exception e){System.err.println(e.toString());}
					}
				}			
				reader.close();
			}
		}catch(Exception e){
			System.err.println(e.toString());
			e.printStackTrace(System.err);
			return null;
		}		
			
		return cluster;
	}
	public static void writeClusterIdToFile(double thres, ArrayList<Integer> splitCluster){
		FileOutputStream fout;		
		try
		{
			int max = 1;
			if(thres == 0){max = Global.rounds;}
			for(int round = 0;round<max;round++){
				String outputFile = "CAMPS_cluster" + thres +"_" + round + ".txt";
				//Open an output stream
			
				fout = new FileOutputStream (Global.getDirectory() + Global.TMPDir + outputFile, false);
				PrintStream ps = new PrintStream(fout);
				for(int  i: splitCluster){				
					ps.println(i);    
				}		
				ps.close();
				fout.close();
			}
		}catch(Exception e){
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}	
	}
	
	public static void updateWrongList(ArrayList<Integer> splitCluster, double oldThreshold, double newThreshold){
		try{
			FileOutputStream fout = new FileOutputStream (Global.getDirectory() + Global.TMPDir + "CAMPS_wrongList.txt", false);
			PrintStream ps = new PrintStream(fout);			
			for(int round=0; round<Global.rounds; round++){
				if(!new File(Global.getDirectory() + Global.TMPDir + "CAMPS_wrongList_"+round+".txt").exists()) continue;
				BufferedReader reader = new BufferedReader(new FileReader(Global.getDirectory() + Global.TMPDir + "CAMPS_wrongList_"+round+".txt"));
				String line;						
				while((line = reader.readLine()) != null){
					if(!line.equals("")){
						// 962860:4479;100.0:10458;100.0:21047;100.0:16997;100.0:22365;100.0:
						// 962860:
						//4479;100.0
						//10458;100.0
						//21047;100.0
						//16997;100.0
						//22365;100.0
						String[] data = line.split(":");
						ps.print(data[0] + ":");
						for(int i=1; i< data.length; i++){
							String[] c = data[i].split(";");
							System.out.println(c[1] + "\t" + oldThreshold + "  |   "+ c[0] + "\t" + splitCluster.toString());
							if(new Double(c[1]) == oldThreshold && splitCluster.contains(new Integer(c[0]))){
								ArrayList<Integer> clusterList = new ArrayList<Integer>();								
								//clusterList.addAll(Database.getSplitClusterArray(new Integer(c[0]), oldThreshold, newThreshold));
								clusterList.addAll(Database.getSplitClusterArray(new Integer(c[0]), oldThreshold,connection));
								for(Integer clusterId : clusterList){
									ps.print(clusterId + ";" + newThreshold + ":");
								}
							}else{
								ps.print(c[0] + ";" + c[1] + ":");
							}
						}
						ps.println();
					}
				}				
				reader.close();
				new File(Global.getDirectory() + Global.TMPDir + "CAMPS_wrongList_"+round+".txt").delete();
			}
			fout.close();
		}catch(Exception e){
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}
	}
	/**
	 * The function testCase is a replica of main. It was made to replicate the zero number of states 
	 * exception during hmm building process. 
	 */
	private static void testCase(){
		try{
			int i = 6; // as thresh round 6 has problem 
				//i = new Integer(args[0]);
				//Global.directory = args[1]; -- set default of laptop
				//Global.HMMDir = args[2];
				//Global.TMPDir = args[3];
			
			Utils.setGlobalVariables("config.txt");
			Global.thresholds = Database.getClusterThresholds(connection);			

			if(i<33){
				System.out.println("Generate HMMs Threshold: " + Global.thresholds[i]);
			}
			ArrayList<Integer> splitList = new ArrayList<Integer>();
			ArrayList<Integer> cluster_ids = new ArrayList<Integer>();
			
			if(i==0){
				// get cluster_ids for initial clusters
				cluster_ids = Database.getBigClusterIDs(Global.thresholds[i],connection);
//				ArrayList<Integer> tmpCluster = new ArrayList<Integer>();
//				tmpCluster = Database.getBigClusterIDs(Global.thresholds[i]);
//				for(int nb = 0; nb < 20; nb++){
//					cluster_ids.add(tmpCluster.get(nb));
//				}				
				System.out.println("size:" + cluster_ids.size());
			}
			else{
				// add clusters that are not homoegenous enough (last test round) to splitList
				//splitList = readClusterIdFromFile(Global.thresholds[i-1]); -- removed for checking
				
				// remove HMMs from this bad clusters
				//removeBadHMMs(splitList, Global.thresholds[i-1]); -- removed for checking
				
				//in last iteration only the wrong HMMs have to be deleted
				if(Math.abs(Global.thresholds[i])==1000) return;
				if(i>32) return;
				// update wrongList with splitted clusters
				//updateWrongList(splitList, Global.thresholds[i-1], Global.thresholds[i]);
				// calculate the cluster_ids of the subclusters of the bad clusters (splitlist)
				
				splitList.add(19628); // the cluster id creating problem
				splitList.add(3925); // the cluster id creating problem
				splitList.add(587); // the cluster id creating problem
				
				// adding 3925 in split list.. as it is the parent of 7310
				// and 7310 is causing problem...
				// so now it should split 3925 into 7310 and error should be regenerated
				
				cluster_ids = CreateCampsCluster.createSplitCluster(splitList, Global.thresholds[i-1], Global.thresholds[i], connection);
				// create serialized hmms
				// run one time only... cuz files would be serialized after one run
				
			}
						
			System.out.println("split " + splitList + " into " + cluster_ids);
						
			// create new HMMs for the new splitted clusters, some clusters are too big and have 
			// therefore no assigned TM blocks. This clusters have to be splitted in every case -> splitlist

			// to check am making list small *********
			//ArrayList<Integer> cluster_ids_small = new ArrayList<Integer>();
			//for(int x =602;x<=670;x++){
				//cluster_ids_small.add(cluster_ids.get(x));
			//}
			splitList = CreateCampsCluster.createHMMsLevel1(cluster_ids, Global.thresholds[i], Global.HMMDir, connection);
			//splitList = CreateCampsCluster.createHMMsLevel1(cluster_ids_small, Global.thresholds[i], Global.HMMDir, connection);
			for (File ff : new File(Global.getDirectory() + Global.HMMDir).listFiles()) {
				String fileName = ff.getName();
				if (fileName.endsWith(".hmm")) {
					System.out.println(fileName+" Serialized");
					HMMGraphData hmmD = loadHmmDataGraph(ff);
					serializeObjectToFile(hmmD, new File(Global.getDirectory() + Global.HMMDir, fileName + ".serialized"));					
				}
			}
			// no HMM for clusters from splitList are constructed, they have to be removed fom cluster_ids 
			for(Integer cluster_id : splitList){	
				cluster_ids.remove(cluster_id);	
				//cluster_ids_small.remove(cluster_id);
			}
			//write the newly constructed HMMs into CAMPS_cluster0.0_$i.txt
			writeClusterIdToFile(0, cluster_ids);	
			//writeClusterIdToFile(0, cluster_ids_small);
			//write the clusters which have to be split in the next round into CAMPS_cluster$thresholds[i].txt
			writeClusterIdToFile(Global.thresholds[i], splitList);
			
			System.out.println("Clusters which have to be split in the next Round:\n" + splitList);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
