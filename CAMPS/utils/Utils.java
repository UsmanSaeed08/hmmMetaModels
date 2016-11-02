package CAMPS.utils;

import CAMPS.hmm.SecOrderHMM;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;
import java.io.*;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.Opdf;
import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;

/**
 * class with useful methods
 * 
 * @author hartmann
 *
 */
public class Utils {
	
	public static void setGlobalVariables(String initFile){
		Properties properties = new Properties();
	    try {
	        properties.load(new FileInputStream(Global.getDirectory() + "config.txt"));
	        
	        Global.dbConnection = properties.getProperty("dbConnection");
	        Global.dbUser       = properties.getProperty("dbUser");
	        Global.dbPassword   = properties.getProperty("dbPassword");
	        
	        Global.tmsCores     = properties.getProperty("tmsCores");
	    	Global.cluster      = properties.getProperty("cluster");  		 
	    	Global.proteins 	= properties.getProperty("proteins");	    
	    	Global.clusterInfo  = properties.getProperty("clusterInfo");
	    	Global.clusterTrack  = properties.getProperty("clusterTrack");
	    	
	    	Global.tmsCores_code 			= properties.getProperty("tmsCores_code");
	    	Global.tmsCores_clusterId 		= properties.getProperty("tmsCores_clusterId");
	    	Global.tmsCores_clusterThreshold= properties.getProperty("tmsCores_clusterThreshold");
	    	Global.tmsCores_length 			= properties.getProperty("tmsCores_length");
	    	Global.tmsCores_begin			= properties.getProperty("tmsCores_begin");
	    	Global.tmsCores_end 			= properties.getProperty("tmsCores_end");
	    	
	    	Global.cluster_code 			= properties.getProperty("cluster_code");
	    	Global.cluster_clusterId 		= properties.getProperty("cluster_clusterId");
	    	Global.cluster_clusterThreshold = properties.getProperty("cluster_clusterThreshold");
	    	Global.cluster_protId 			= properties.getProperty("cluster_protId");

	    	Global.proteins_sequence= properties.getProperty("proteins_sequence");	    	
	    	Global.proteins_protId 	= properties.getProperty("proteins_protId");	
	    	
	    	Global.clusterInfo_clusterThreshold = properties.getProperty("clusterInfo_clusterThreshold");
	    	Global.clusterInfo_numberMembers = properties.getProperty("clusterInfo_numberMembers");
	    	Global.clusterInfo_clusterId = properties.getProperty("clusterInfo_clusterId");
	    	
	    	Global.clusterTrack_clusterThreshold = properties.getProperty("clusterTrack_clusterThreshold");	    	
	    	Global.clusterTrack_clusterId = properties.getProperty("clusterTrack_clusterId");
	    	Global.clusterTrack_childClusterThreshold = properties.getProperty("clusterTrack_childClusterThreshold");	    	
	    	Global.clusterTrack_childClusterId = properties.getProperty("clusterTrack_childClusterId");
	    } catch (IOException e) {
	    	System.err.println(e.getMessage());
	    }
			
	}
		
	/**
	 * sets all transition probabilites in an HMM to zero
	 * 
	 * @param hmm HMM to use
	 * @param states number of states in the model
	 */
	public static void clear(Hmm hmm, int states){
		for(int i = 0; i < states; i++){
			for(int j = 0; j < states; j++){
				hmm.setAij(i,j,0);
			}				
		}
	}
	
	
	/**
	 * writes a HMM into a file
	 * 
	 * @param hmm HMM to use
	 * @param fileName file name name of the file
	 */
	public static void writeHmmIntoFile(String dir, Hmm<ObservationInteger> hmm, String fileName, String secOrder){
		FileOutputStream fout;	
		int nbObservations = 20;
		try
		{
			// Open an output stream
			//fout = new FileOutputStream (Global.getDirectory() + dir + "/" + fileName);
			fout = new FileOutputStream (Global.getDirectory() + dir + fileName);
			//fout = new FileOutputStream (dir + fileName);
			//fout = new FileOutputStream (Global.getDirectory() + fileName);
//			fout = new FileOutputStream (dir + "/" + fileName);
					    		    
		    // Print a line of text
		    PrintStream ps = new PrintStream(fout);
		    
		    int nbStates = hmm.nbStates();
		    ps.println("#states");
		    ps.println(nbStates);
		    ps.println("#secOrder");
		    ps.println(secOrder);
		    ps.println("#observables");
		    ps.println(nbObservations);
		    ps.println("#P_i");
		    for(int i=0;i<nbStates-1;i++){
		    	ps.print(hmm.getPi(i) + ",");
		    }
		    ps.println(hmm.getPi(nbStates-1));
		    ps.println("#A_ij");		    
		    for(int i=0;i<nbStates;i++){
		    	for(int j=0; j<nbStates-1; j++){
		    		double aij = hmm.getAij(i, j); 
		    		ps.print( aij + ",");
		    	}
		    	ps.println(hmm.getAij(i, nbStates-1));
		    }
		    ps.println("#IntegerDistribution");		    
		    for(int i=0;i<nbStates;i++){
		    	Opdf<ObservationInteger> opdf = hmm.getOpdf(i);		    	
		    	for(int j=0; j<nbObservations-1; j++){		    		
		    		ps.print(opdf.probability(new ObservationInteger(j)) + ",");
		    	}
		    	ps.println(opdf.probability(new ObservationInteger(nbObservations -1)));
		    }
		    

		    // Close our output stream
		    ps.close();
		    fout.close();		
		}
		// Catches any error conditions
		catch (IOException e)
		{
			System.err.println ("Unable to write to file");
//			System.err.println (Global.getDirectory()+ dir + "/" + fileName);
			System.err.println (dir + "/" + fileName);
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * creates a new HMM out of the paramters from a given file
	 * 
	 * @param fileName parameter file for the HMM
	 * @return the new generated HMM
	 * @throws Exception file not found Exception
	 */
	public static SecOrderHMM loadHmmFromFile(String dir, String fileName)throws Exception{
		Hmm<ObservationInteger> hmm = null;		
		String[] secOrder = null;
		int region=0; int i = 0; int nbStates = 0;int observables = 0;
//		 Open an output stream
		
		try{
			BufferedReader reader = null;
			//reader = new BufferedReader(new FileReader(Global.getDirectory() + dir + "/" + fileName));				
			reader = new BufferedReader(new FileReader(dir + "/" + fileName));
	
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
								break;
						case 1: observables = new Integer(line);
								hmm = new Hmm<ObservationInteger>(nbStates, new OpdfIntegerFactory(observables));
								break;
						case 2: String[] pi = line.split(",");
								for(int j=0; j<nbStates; j++){
									hmm.setPi(j, new Double(pi[j]));
								}
								break;
						case 3: String[] aj = line.split(",");
								for(int j=0; j<nbStates; j++){
									hmm.setAij(i, j, new Double(aj[j]));
								}
								i++;
								break;
						case 4: String[] intDistS = line.split(",");
								double[] intDist = new double[observables];
								for(int j=0; j<observables; j++){
									intDist[j] = new Double(intDistS[j]);
								}
								hmm.setOpdf(i, new OpdfInteger(intDist));
								i++;
								break;
						case 5: secOrder = line.split(",");
								break;
					}
				}
				
			}			
			reader.close();
		}catch(Exception e){
//			System.err.println(e.toString());
			e.printStackTrace();
			return null;
		}		
		String cluster_id = "";
		try{
			cluster_id = fileName.substring(fileName.indexOf("_")+1, fileName.indexOf("."));			
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
		return new SecOrderHMM(hmm, secOrder, cluster_id);
	}	
}
