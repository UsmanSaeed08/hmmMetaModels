package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;

import CAMPS.hmm.ASMapping;
import CAMPS.hmm.BuildHMM;
import CAMPS.hmm.Family;
import CAMPS.hmm.Protein;
import CAMPS.hmm.SecOrderHMM;
import CAMPS.utils.DBAdaptor;
import CAMPS.utils.Database;
import CAMPS.utils.Global;
import CAMPS.utils.Utils;

public class MakenTestHmm {

	/**
	 * @param args
	 */

	private static final Connection CAMPS_CONNECTION = DBAdaptor.getConnection("CAMPS4");
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Utils.setGlobalVariables("config.txt");
		System.out.print("Hello");
		
		// get the cluster for which to make hmm
		// e.g. clusid ? clusthresh 50 
		String cluster_id ="850";
		int clusid = 850;
		Double threshold = 12.0;
		Family fam = new Family(cluster_id + "" ,threshold);

		// array of seqid in this cluster
		ArrayList<String[]> clusterArray = getBigClusterArray(clusid, threshold);
		// get 50% train - 50% test
		int perc_train = 50;

		int size = clusterArray.size();
		int noTrain = (size*perc_train)/100; 
		int noTest =  size - noTrain;

		ArrayList<String[]> clusterArrayTrain = new ArrayList<String[]>();
		ArrayList<String[]> clusterArrayTest = new ArrayList<String[]>();
		int x =0;
		for (x =0; x<=noTrain-1 ; x++){
			clusterArrayTrain.add(clusterArray.get(x));
		}
		for(;x<=size-1;x++){
			clusterArrayTest.add(clusterArray.get(x));
		}
		//
		//clusterArrayTest.add(new String[]{"485827", null, "MAPKQKTIYPWSRRNNIYDPPTPLDSLLESPLRTLVFYLHAILLYFRGTSFKPPRNKPAVRVVCISDTHCNKLPIPPGDLLIHAGDLTNAGTVEEIQAQIDWLDQQPHREKVFICGNHDSYFDPKSRKPEDKKRKLNFKSLHYLENKAITLKFKGGRKLKVYGSPDIPQCGGSDFAFQYQRHLAPWENRIPKDTDVLITHSPPRHHLDINLGCKSLLEEIWKVKPRLHVFGHIHSGHGREAVFWDKGQEAYERLMERKRGGIIMDLMPSFAWVDAVKVIWYGVKGILWQRLMVGPAGGNGGLMINAAVVYQSSTDVGNPVEVVEL"});
		//
		//clusterArrayTest.add(new String[]{"2260173", null, "MGLLSYVGLRRRNPWEPMTLIDHFLSSPLTYIAYLFYHIVlllrgrpflpprNKPAIRVVCISDTHDLKVDIPRGDILIHAGDLTDGGTVSDIQKQLDWLKEQPHPIKVVVAGNHDSWFDQKSRPEEDARSGAKPDMDGLIYLESGLTVQKVKGRTVNIFGVPDIPEIGPKEFAFQYATDNHPWLSKVPPQTDILVTHCPPKHHLDLGLGDSNLLREVWRVKPRLHVFGHVHCAYGKESVFFDNFQQTYERIMSRPRRGPILDFIPNEAWLDYLRFLAQGIHAVAWKWIMSGPGSNNGSLMVNAAQMKGNSGKVKSRAVVVEI"});
		//clusterArrayTest.add(new String[]{"1921046", null, "LSIDGDLITTSASDVDHITMELGQREQLMEDNPVAEQLRIETIKSIAHKISTKRQIREQLARTVHRRATRAKSIGLLRRYRYGAKRYVARAARLAQCFVTNFEPFYGSMKQIEGHFGSRISAYFKFlrrllvlnlvlalFVGSFVIFPQLLAGPEGPDARQAFQLRDLLTGEGYLSDSVMYYGSYSNRSFTLVPGTAEYSLPHAYFltitilllATFVFVSVSMGHAYRISFIESSATVQNILTHKIVCSWDYGIANGKAARLKHATILSELRDYLAQRNRTPAPVGRWQRLRTETLNavahatvlaliaavaaclwavlDRFGPADHFAAWSALYVSLASNGTMAALGYVCQLLGRLERYRHGATQLNINLLRHFLLQLTIVAVLFAHWLTTRPPTGCWETAIGQELYRLLVVDFFISTVLLSAVRGVRYLLHARYGPARGRAGPLAPYLTPPPFCIETHSLGLVYNQTLLWFGVlfappllllvalklllvfYVNKCELMYLCQPPARLWRSSQTQTlflvlvfvslfgvllTHGYLIMQVPVSEGCGPFRGTQYMYQLFMQGILKLREEHLFWRAFVYITKPAIIGGVLLTMGVATYYLRAKSRAQIAkvkllkellcleakdkeFLLANLSKVARGKDCTGEQLDRIELVGGGRLNGPADRYYTDPCATWRYEESPRKPDVgagtasagttnstssssgYQ"});
		
		// ************* Now we have sequences for each cluster, get their data and make HMM for each cv round*****

		for(int cvRound = 0; cvRound < 10; cvRound ++){
			// for every cv round, train hmm and test hmm
			// *********** train HMM ******************
			int nbHelix = 0;
			for(int j=0; j<clusterArrayTrain.size(); j++){
				String[] tmp = (String[])clusterArrayTrain.get(j); // camperCode, description, sequence, tmss
				if(tmp[2].contains("X") || tmp[2].contains("Z")){
					continue;
				}
				ArrayList<int[]> tmhPosition = getTMHPositions(tmp[0],clusid,threshold);				
				if(fam.gethelixLength() == null || fam.gethelixLength().size() == 0){
					ArrayList<Integer> helixLength = getTMHLength(tmp[0], clusid, threshold);
					// set the Helix length in the family
					if(helixLength.size() > 0){
						//System.out.println(helixLength);
						fam.setHelixLength(helixLength);
					}
				}			
				// if no helix is assigned to this protein, ignore it
				if(tmhPosition.size() != 0){	
					if(nbHelix == 0){
						nbHelix = tmhPosition.size();
					}else if(nbHelix != tmhPosition.size()){
						throw new Exception("different numbers of TMH in cluster with cluster_id=" + cluster_id);
					}
					fam.addProtein(new Protein(tmhPosition, tmp[2], tmp[0], tmp[1]));

				}
			}
			BuildHMM start = new BuildHMM();	
			Hmm<ObservationInteger> result = start.doIt(fam);


			ArrayList<Integer> helixLength = fam.gethelixLength();
			ArrayList<Integer> loopLength = fam.getLoopLength();	
			StringBuffer secOrder = new StringBuffer();
			for(int i = 0; i < loopLength.get(0); i++){
				secOrder.append("L,");
			}
			for(int i = 0; i<helixLength.size(); i++){
				for(int k = 0; k < helixLength.get(i); k++){
					secOrder.append("H,");
				}
				for(int k = 0; k < loopLength.get(i+1); k++){
					secOrder.append("L,");
				}				
			}
			System.out.println(secOrder);	
			//write MM to file
			// print only if it is right... else not 
			/*
								String dir = "F:/RunMetaModel/NewTest/";
								Utils.writeHmmIntoFile(dir, result, "cluster_" + threshold + "_" + fam.getName() + ".hmm", secOrder.toString());
			 */
			System.out.println("Cluster " + cluster_id + " ready");

			SecOrderHMM hmmtoTest= new SecOrderHMM(result, null, cluster_id);
			double[][] farr = null;
			int States = result.nbStates();


			// HMM Built
			// ************ Test HMM ******************
			double max = Double.NEGATIVE_INFINITY;
			
			for (int j=0; j<clusterArrayTest.size(); j++){
				// get the sequence
				String[] tmp = (String[])clusterArrayTest.get(j); // camperCode, description, sequence
				if(tmp[2].contains("X") || tmp[2].contains("Z")){
					continue;
				}
				// String 2 in tmp is test sequence
				int sequenceLength = tmp[2].length();
				
				farr = checkForStateCount(farr, States, sequenceLength);
				double tempScore = score(tmp[2],hmmtoTest);
				
				if (tempScore>max){
					max = tempScore;
				}
				System.out.print("\n"+tmp[0]+"    score: "+tempScore+" ");

			}
			System.out.print("\n Max Score    "+max);

		}


	}


	
	
	
    private static double[][] checkForStateCount(double[][] farr, int stateCount, int sequenceLength) {
        if (farr == null || farr.length < stateCount) {
            farr = new double[stateCount][sequenceLength + 1];
        }
        return farr;
    }

	private static double score(String s1, SecOrderHMM secOrderHMM) throws Exception{
//		System.out.println(s1);
		Hmm<ObservationInteger> hmm = secOrderHMM.getHmm();
		ArrayList<ObservationInteger> seq = new ArrayList<ObservationInteger>();			
		for(int i = 0; i < s1.length(); i++){
			try{
				int number =  ASMapping.asToInt(s1.charAt(i));					
				seq.add(new ObservationInteger(number));
			}catch(Exception e){
				e.printStackTrace(System.err);
			}
		}			
				
		double score = hmm.lnProbability(seq);		
		return score;		
	}

	private static ArrayList<Integer> getTMHLength(String code_id, int cluster_id, double threshold){
		ArrayList<Integer> clusterArray = new ArrayList<Integer>();
		String query = "SELECT " + Global.tmsCores_length +
				" FROM "  + Global.tmsCores +
				" WHERE " + Global.tmsCores_clusterId + " = " + cluster_id +
				" AND "   + Global.tmsCores_clusterThreshold + "=" + threshold +
				" AND "   + Global.tmsCores_code + "='" + code_id +	"'";
		try
		{

			Statement statement = CAMPS_CONNECTION.createStatement();
			ResultSet result = statement.executeQuery(query);
			while (result.next()) 
			{
				int length = result.getInt(Global.tmsCores_length);				
				clusterArray.add(length);
			}

			result.close();
			statement.close();

		}catch(java.sql.SQLException e)
		{
			System.err.println("Fehler beim Abfragen der Datenbank");
			System.err.println(query);
			System.err.println(e.getMessage());
		}

		return clusterArray;
	}

	private static ArrayList<int[]> getTMHPositions(String code_id, int cluster_id, double threshold){
		ArrayList<int[]> clusterArray = new ArrayList<int[]>();

		String query = "SELECT " + Global.tmsCores_begin + "," + Global.tmsCores_end +
				" FROM "  + Global.tmsCores +
				" WHERE " + Global.tmsCores_clusterId +" = " +cluster_id+
				" AND "   + Global.tmsCores_clusterThreshold + "=" + threshold + 
				" AND "   + Global.tmsCores_code + "='" + code_id + "'" +
				" ORDER BY " + Global.tmsCores_begin;


		Statement statement;
		try {
			statement = CAMPS_CONNECTION.createStatement();


			ResultSet result = statement.executeQuery(query);
			while (result.next()) 
			{
				int begin = result.getInt(Global.tmsCores_begin);
				int end = result.getInt(Global.tmsCores_end);			
				if(end == begin)
					clusterArray.add(new int[]{begin,end});
				else
					clusterArray.add(new int[]{begin,end-1});				
			}			
			result.close();
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return clusterArray;
	}


	private static ArrayList<String[]> getBigClusterArray(int cluster_id, double threshold){
		ArrayList<String[]> clusterArray = new ArrayList<String[]>();
		String query = "SELECT DISTINCT " + Global.cluster + "." + Global.cluster_code + ", " + 
				Global.proteins + "." + Global.proteins_sequence +
				" FROM "  + Global.cluster + ", "+ Global.proteins + ", " + Global.tmsCores + 
				" WHERE " + Global.cluster + "." + Global.cluster_protId + "=" + Global.proteins + "." + Global.proteins_protId + 
				" AND "   + Global.cluster + "." + Global.cluster_code +	"=" + Global.tmsCores + "." + Global.tmsCores_code + 
				" AND "   + Global.cluster + "." + Global.cluster_clusterId + " = " + cluster_id + 
				" AND "   + Global.cluster + "." + Global.cluster_clusterThreshold + " = " + threshold + 
				" AND "   + Global.tmsCores + "."+ Global.tmsCores_clusterId + "=" + cluster_id + 
				" AND "   + Global.tmsCores + "."+ Global.tmsCores_clusterThreshold + "=" + threshold;		
		try
		{
			Class.forName( "com.mysql.jdbc.Driver" );

			Statement statement = CAMPS_CONNECTION.createStatement();
			ResultSet result = statement.executeQuery(query);
			while (result.next()) 
			{
				String camperCode = result.getString(Global.cluster_code);
				String sequence = result.getString(Global.proteins_sequence);	
				String description = "";
				//				System.out.println(camperCode);
				clusterArray.add(new String[]{camperCode, description, sequence});
			}

			result.close();
			statement.close();

		}catch(java.lang.ClassNotFoundException e)
		{
			System.err.println("JDBC-ODBC-Treiber nicht gefunden");
		}catch(java.sql.SQLException e)
		{
			System.err.println("Fehler beim Abfragen der Datenbank");
			System.err.println(query);
			System.err.println(e.getMessage());
		}

		return clusterArray;
	}

}
