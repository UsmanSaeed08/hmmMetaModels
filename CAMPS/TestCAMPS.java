package CAMPS;

import java.sql.Connection;
import java.util.*;
import CAMPS.utils.*;

import java.io.*;


public class TestCAMPS {
	//private static final Connection connection = Database.getCon();
	private static final Connection connection = DBAdaptor.getConnection("CAMPS4");
	
	public static void main(String[] args){		
		//int i = 2;
		int i = 0;
		int round = 0;
		if(args.length == 5){
			i = new Integer(args[0]);
			round = new Integer(args[1]);
			Global.directory = args[2];
			Global.HMMDir = args[3];
			Global.TMPDir = args[4];
		}		
		Utils.setGlobalVariables("config.txt");
		Global.thresholds = Database.getClusterThresholds(connection);
		
		System.out.println("Test round: " + round + ", Threshold: " + Global.thresholds[i]);
		
		ArrayList<Integer> splitList = new ArrayList<Integer>(); 					
		//read the cluster_ids created in this round 
		//older clusters are not considered anymore, as they are considered as homogeneous enough		
		ArrayList<Integer> cluster_idsTmp = readClusterIdFromFile(0, round);
		
		ArrayList<Integer> cluster_ids = new ArrayList<Integer>();
		//seperate the clusters which have to be tested to the different threads
		for(int pos = 0; pos < cluster_idsTmp.size(); pos++){
			if(pos % Global.rounds == round){
				cluster_ids.add(cluster_idsTmp.get(pos));				
			}
		}		
		//test the new clusters and write the clusters which are not homogeneous enough into splitlist
		splitList.addAll(TestCampsCluster.testCluster(cluster_ids, Global.thresholds[i], round, connection));
		System.out.println("splitList " + splitList);
		// write into CAMPS_cluster$threshold[i].txt which clusters are not homogeneous enough
		writeClusterIdToFile(Global.thresholds[i], round, splitList);
		try{
			connection.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public static ArrayList<Integer> readClusterIdFromFile(double thres, int round){
		ArrayList<Integer> cluster = new ArrayList<Integer>();
		try{
			BufferedReader reader = null;
			reader = new BufferedReader(new FileReader(Global.getDirectory() + Global.TMPDir + "CAMPS_cluster"+thres+"_"+round+".txt"));				
			
			String line;						
			while((line = reader.readLine()) != null){
				if(!line.equals("")){
					try{
						cluster.add(new Integer(line));
					}catch(Exception e){System.err.println(e.toString());}
				}
			}			
			reader.close();			
		}catch(Exception e){			
			e.printStackTrace(System.err);
			return null;
		}		
			
		return cluster;
	}
	
	public static void writeClusterIdToFile(double thres, int round, ArrayList<Integer> splitCluster){
		FileOutputStream fout;		
		try
		{
			String outputFile = "CAMPS_cluster" + thres +"_" + round +".txt";
		    // Open an output stream
			
			fout = new FileOutputStream (Global.getDirectory() + Global.TMPDir + outputFile, true);
			PrintStream ps = new PrintStream(fout);
			for(int  i: splitCluster){
				ps.println(i);    
			}			
			ps.close();
			fout.close();
		}catch(Exception e){
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}	
	}
}



