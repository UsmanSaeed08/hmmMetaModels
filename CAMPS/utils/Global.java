package CAMPS.utils;

public class Global {	
	//has to be false if used with the grid system
	public static boolean local = false;
	
	//////////////////////////////////////////////////////////
	// parameters set as command line parameters
	//////////////////////////////////////////////////////////
	
	// home directory
	//public static String directory = "/home/users/sneumann/workspace/MetaModels4CAMPS3_HCTEST/";
	//public static String directory = "F:/RunMetaModel/";				//lappy
	//public static String directory = "/scratch/usman/RunMetaModel/";	//office
	public static String directory = "/localscratch/CAMPS/RunMetaModel2/";				//reRun
	
	//directory for storing the resulting HMMs
	//public static String HMMDir = "HMMs/CAMPS3_nr40/";
	public static String HMMDir = "HMMs/CAMPS4_1/";			// lappy
	//public static String HMMDir = "HMMs/CAMPS3_nr40/";	// office
	
	//directory for storing temporary files
	//public static String TMPDir = "tmp/CAMPS3_nr40/";
	public static String TMPDir = "tmp/CAMPS4_1/";			// lappy
	//public static String TMPDir = "tmp/CAMPS4_1/";		// office
	
	//////////////////////////////////////////////////////////////
	// parameters in config file
	/////////////////////////////////////////////////////////////
	
	public static String dbConnection = "";  	// jdbc:mysql://tum-mysql2.binfo.wzw.tum.de/CAMPS1
	public static String dbUser = "";		 	// pedant
	public static String dbPassword = "";    	//
	
	
	// tables in config file	
	public static String tmsCores = ""; 		// table with tms cores
	public static String cluster = "";  		// table with all initial clusters * clusters_mcl
	public static String proteins = ""; 		// table with protein sequences * sequences2
	public static String clusterInfo = "";
	public static String clusterTrack = "";

	
	// fields in table tmsCores
	public static String tmsCores_code = "";
	public static String tmsCores_clusterId = "";
	public static String tmsCores_clusterThreshold = "";
	public static String tmsCores_length = "";
	public static String tmsCores_begin= "";
	public static String tmsCores_end = "";
	
	// fields in table cluster
	public static String cluster_code = "";
	public static String cluster_clusterId = "";
	public static String cluster_clusterThreshold = "";
	public static String cluster_protId = "";

	// fields in table proteins	
	public static String proteins_sequence = "";	
	public static String proteins_protId = "";
	
	// fields in table cluster_info
	public static String clusterInfo_clusterThreshold = "";
	public static String clusterInfo_numberMembers = "";
	public static String clusterInfo_clusterId = "";
	
	// fields in table cluster_track
	public static String clusterTrack_clusterThreshold = "";
	public static String clusterTrack_clusterId = "";
	public static String clusterTrack_childClusterThreshold = "";
	public static String clusterTrack_childClusterId = "";
	
		
	//////////////////////////////////////////////////////
	// optimized parameters:
	//////////////////////////////////////////////////////
	
	public static int loopSize = 12;						//loop size parameter
	public static double transitionPseudocount = 0.005;		//transition pseudocount Parameter inside distinct HMMs 
	public static double helixPseudocount = 0.001;			// helix Pseudocount parameter for transitions between distinct HMMs
	public static int SD = 2;								// SD parameter. loop is conserved if residues range at max with 2 residues inside 1SD			 
	
	public static int cv = 10;								//number of cross validation rounds in Test step			   
	public static double maxFalse = 0.05;					//cluster is ok, if less than 5 % of the members are misclassified
	
	public static double scoreThreshold = -3;				// worst score to be ok for classification		 
	//public static double confidenceThreshold = 0.00;		// minimum distance to second best meta-model
	// above is the default thresh, while below is the new
	// thresh, it is because, small differences are allowed
	// for the hmms of the same cluster and
	// using this sequence tests against other hmms.
	// this is done because of a very high number of TM proteins
	// with only 1 up to 3 TM helices, therefore, making classification a great challenge
	
	public static double confidenceThreshold = -0.05;		// minimum distance to second best meta-model
	// the above confidence works in a way that 0 should be max possible
	
	//public static int rounds = 90;							//number of processors calculating		
	public static int rounds = 42;	 						//number of processors calculating
	// this number is always +1 to the number of rounds in the MasterSkript calling TestHMM
	// i.e. in Masterskrpt count starts from zero.. but here it starts from 1
	// because this number here is used to get the cluster ids for each round from the array. 
		
	public static double[] thresholds;
	
	public static String getDirectory(){
		if(local) return "";
		else return directory;
	}
}
