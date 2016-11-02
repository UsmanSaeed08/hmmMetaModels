package camps_Scoring;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import util.hmm.HMMGraph;
import util.hmm.HMMGraphData;
import util.hmm.HMMMath;


import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;

import CAMPS.hmm.ASMapping;
import CAMPS.hmm.SecOrderHMM;
import CAMPS.utils.DBAdaptor;

public class ScoringMembersNnonMembers {

	/**
	 * @param args
	 */
	private static final Connection connection = DBAdaptor.getConnection("CAMPS4");
	private static final String HMM_DIR = "/home/proj/check/RunMetaModel_gef/HMMs/serialized/";
	//private static final String HMM_DIR = "F:/SC_Clust_postHmm/Results_hmm_new16Jan/RunMetaModel_gef/HMMs/serialized/";
	private static final String p = "/home/users/saeed/scores/"; // path to out files
	//private static final String p = "F:/SC_Clust_postHmm/scores/";

	private static ArrayList<String> codes = new ArrayList<String>(); // all codes in sc
	private static HashMap<Integer,String> seqidsMap = new HashMap<Integer,String>(); // all seqids are key and Value is sequence of the id 
	private static HashMap<Integer,String> seqidsMapToCode = new HashMap<Integer,String>(); // all seqids are key and Value is Code
	private static HashMap<String,ArrayList<Integer>> codeToSeqIds = new HashMap<String,ArrayList<Integer>>(); // key is code and seqids are value

	private static HashMap<String,String> codeToclusIdnThresh = new HashMap<String,String>(); // key is code and seqids are value

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//String a = "2";
		//String b = "3";
		Integer min = Integer.parseInt(args[0]);
		Integer max = Integer.parseInt(args[1]);
		//Integer min = Integer.parseInt(a);
		//Integer max = Integer.parseInt(b);

		System.out.println();
		// get all sequence ids in sc clusters
		PopulateSeqidsAndCodes3(max); //3 uses file to get cluster members
		// Run the whole scoring thing
		closeConnection();
		Run(min,max);
	}

	private static void closeConnection() {
		// TODO Auto-generated method stub
		try{
			connection.close();
			System.out.println("connection closed");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private static void Run(int min, int max) {
		// TODO Auto-generated method stub
		try{
			// get all models

			if (max >codes.size()-1){
				max = codes.size()-1;
			}

			// for every cluster
			for(int i=min;i<=max;i++){
				String currentcluster = codes.get(i);
				ArrayList<Integer> memberIds = codeToSeqIds.get(currentcluster);
				ArrayList<Integer> NonmemberIds = getNonMemberIds(currentcluster);
				// for members get scores from all models
				System.out.println("Running members against: "+currentcluster + "    size   "+memberIds.size()+"    total: "+i+"/"+(max-min));
				System.out.flush();
				TraverseHmm(memberIds,currentcluster,1); // scores all the sequences in array against this HMM

				// for NON-members get scores from all models
				System.out.println("Running Non-members against: "+currentcluster + "    size   "+NonmemberIds.size()+"    total: "+i+"/"+(max-min));
				System.out.flush();
				TraverseHmm(NonmemberIds,currentcluster,2);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private static void TraverseHmm(ArrayList<Integer> memberIds, String currentcluster,int n) { // n ==1 if memberIds run against currentcluster
		// n ==2 then non members running 
		// TODO Auto-generated method stub
		try{
			//

			//CMSC0001_members.scores
			//CMSC0001_nonmembers.scores
			BufferedWriter bw = null;
			if(n ==1){
				bw = new BufferedWriter(new FileWriter(new File(p+currentcluster+"_members.scores")));
			}
			else if(n==2){
				bw = new BufferedWriter(new FileWriter(new File(p+currentcluster+"_nonmembers.scores")));
			}

			for(int i =0;i<=memberIds.size()-1;i++){
				int sq = memberIds.get(i);
				String sequence = seqidsMap.get(sq);
				int sequenceLength = sequence.length();
				sequence = sequence.toUpperCase();

				//ArrayList<MaxList> maxList = new ArrayList<MaxList>();
				//double max = Double.NEGATIVE_INFINITY;
				//double confidence = 1;
				double[][] farr = null;

				String clusterCode = currentcluster;

				HMMGraph hmm = loadHMM(clusterCode);
				farr = checkForStateCount(farr, hmm.getStates().length, sequenceLength);
				// test against all HMMs
				double score = score(sequence, hmm, farr);

				//System.out.println(score+"\t" +memberIds.get(i)+"\t"+clusterCode+"\t"+i);
				//System.out.println(score+"\t" +sq+"\t"+seqidsMapToCode.get(sq)+"\t"+i);
				bw.write(score+"\t" +sq+"\t"+seqidsMapToCode.get(sq));
				bw.newLine();
				/*
					if((score + "").equals("NaN")){
						//System.out.println("NaN " + k + " " + secOrderHMMs.get(k).getClusterId());
					}else{
						//double zscore = getZScore(clusterCode, score, thrMeanSdMap);
						//maxList.add(new MaxList(zscore,clusterCode));
						//model2score.put(clusterCode, Double.valueOf(score));
					}*/
			}
			bw.close();

		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Method calculates sequence probability in hmm-model (using forward algorithm).
	 * @param s1 amino acid text sequence
	 * @param hmm hidden markov model
	 * @param farr temporary array (prepared in checkForStateCount())
	 * @return sequence probability
	 */
	private static double score(String s1, HMMGraph hmm, double[][] farr) {
		int[] seq = new int[s1.length()];
		for(int i = 0; i < s1.length(); i++){
			try{
				seq[i] = ASMapping.asToInt(s1.charAt(i));
			}catch(Exception e){
				//e.printStackTrace(System.err);
			}
		}
		double score;
		try {
			score = hmm.calcLogProbab(seq, farr) / seq.length;
		} catch (Exception ignore) {
			score = HMMMath.LOG_NEG_INF;
		}
		return score;
	}
	/**
	 * Method checks (and extends) size of array used in hmm-forward algorithm (see HMMGraph).
	 * @param farr 2d double array with size stateCount * (sequenceLength+1)
	 * @param stateCount states in hmm
	 * @param sequenceLength letters in sequence
	 * @return old or new (if it was extended) instance of farr
	 */
	private static double[][] checkForStateCount(double[][] farr, int stateCount, int sequenceLength) {
		if (farr == null || farr.length < stateCount) {
			farr = new double[stateCount][sequenceLength + 1];
		}
		return farr;
	}
	private static ArrayList<Integer> getNonMemberIds(String currentcluster) {// get all members except of this cluster
		// TODO Auto-generated method stub
		ArrayList<Integer> Nonmems = new ArrayList<Integer>();
		for(int i = 0;i<=codes.size()-1;i++){
			String thisClus = codes.get(i);
			if(!thisClus.equals(currentcluster)){
				ArrayList<Integer> temp = codeToSeqIds.get(thisClus);
				for(int x= 0;x<=temp.size()-1;x++){
					Nonmems.add(temp.get(x));
				}
			}
		}

		return Nonmems;
	}
	private static void PopulateSeqidsAndCodes3(int fr){
		try{
			System.out.println("Populating sc clusters");
			System.out.flush();
			//HashMap<String,String> tempSc = new HashMap<String,String>(); // key thresh_id no val is code
			PreparedStatement pstmGetSc = connection.prepareStatement("SELECT code,cluster_id,cluster_threshold from cp_clusters where type=\"sc_cluster\" order by cluster_id");
			ResultSet rsSc = pstmGetSc.executeQuery();
			while(rsSc.next()){
				String code = rsSc.getString(1);
				Integer id = rsSc.getInt(2);
				Float thresh = rsSc.getFloat(3);
				if (!codeToclusIdnThresh.containsKey(code)){
					codeToclusIdnThresh.put(code, thresh.toString().trim()+"_"+id.toString().trim());
					codes.add(code);
					//tempSc.put(thresh.toString().trim()+"_"+id.toString().trim(), code);
				}
			}
			rsSc.close();
			pstmGetSc.close();
//16823971
			Thread.sleep(fr*50);
			System.out.println("Populating sc clusters member");
			System.out.flush();
			BufferedReader br = new BufferedReader(new FileReader(new File("/home/users/saeed/allSequeces.txt")));
			String l = "";
			while((l=br.readLine())!=null){
				if(!l.isEmpty()){
					String[] p = l.split("\t"); 
					String code = p[0].trim();
					Integer seqid = Integer.parseInt(p[1].trim());
					seqidsMapToCode.put(seqid, code);
					if(codeToSeqIds.containsKey(code)){
						ArrayList<Integer> tempseqids = codeToSeqIds.get(code);
						tempseqids.add(seqid);
						codeToSeqIds.put(code, tempseqids);
					}
					else{
						ArrayList<Integer> tempseqids = new ArrayList<Integer>();
						tempseqids.add(seqid);
						codeToSeqIds.put(code, tempseqids);
					}
				}
			}
			br.close();
			
			System.out.println("Populating sc clusters sequences");
			Thread.sleep(fr*50);
			System.out.flush();
			PreparedStatement pstmGetSequence = connection.prepareStatement("SELECT sequenceid,sequence from sequences2");
			ResultSet rsSeq = pstmGetSequence.executeQuery();
			String seq = "";
			while(rsSeq.next()){
				int x = rsSeq.getInt(1);
				seq = rsSeq.getString(2);
				if(seqidsMapToCode.containsKey(x)){
					seqidsMap.put(x, seq);
				}
			}
			rsSeq.close();
			pstmGetSequence.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	private static void PopulateSeqidsAndCodes2(int fr){
		try{
			System.out.println("Populating sc clusters");
			System.out.flush();
			HashMap<String,String> tempSc = new HashMap<String,String>(); // key thresh_id no val is code
			PreparedStatement pstmGetSc = connection.prepareStatement("SELECT code,cluster_id,cluster_threshold from cp_clusters where type=\"sc_cluster\" order by cluster_id");
			ResultSet rsSc = pstmGetSc.executeQuery();
			while(rsSc.next()){
				String code = rsSc.getString(1);
				Integer id = rsSc.getInt(2);
				Float thresh = rsSc.getFloat(3);
				if (!codeToclusIdnThresh.containsKey(code)){
					codeToclusIdnThresh.put(code, thresh.toString().trim()+"_"+id.toString().trim());
					codes.add(code);
					tempSc.put(thresh.toString().trim()+"_"+id.toString().trim(), code);
				}
			}
			rsSc.close();
			pstmGetSc.close();
//16823971
			Thread.sleep(fr*50);
			System.out.println("Populating sc clusters member");
			System.out.flush();
			PreparedStatement pstmGetSeqId = connection.prepareStatement("SELECT sequenceid,cluster_id,cluster_threshold from clusters_mcl");
			ResultSet rsSeqId = pstmGetSeqId.executeQuery();
			while(rsSeqId.next()){
				Integer seqid = rsSeqId.getInt(1);
				Integer clusid = rsSeqId.getInt(2);
				Float thr = rsSeqId.getFloat(3);
				String k = thr.toString().trim()+"_"+clusid.toString().trim();
				if(tempSc.containsKey(k)){
					String code = tempSc.get(k);
					seqidsMapToCode.put(seqid, code);
					
					if(codeToSeqIds.containsKey(code)){
						ArrayList<Integer> tempseqids = codeToSeqIds.get(code);
						tempseqids.add(seqid);
						codeToSeqIds.put(code, tempseqids);
					}
					else{
						ArrayList<Integer> tempseqids = new ArrayList<Integer>();
						tempseqids.add(seqid);
						codeToSeqIds.put(code, tempseqids);
					}
				}
			}
			rsSeqId.close();
			pstmGetSeqId.close();
			tempSc.clear();
			
			System.out.println("Populating sc clusters sequences");
			Thread.sleep(fr*50);
			System.out.flush();
			PreparedStatement pstmGetSequence = connection.prepareStatement("SELECT sequenceid,sequence from sequences2");
			ResultSet rsSeq = pstmGetSequence.executeQuery();
			String seq = "";
			while(rsSeq.next()){
				int x = rsSeq.getInt(1);
				seq = rsSeq.getString(2);
				if(seqidsMapToCode.containsKey(x)){
					seqidsMap.put(x, seq);
				}
			}
			rsSeq.close();
			pstmGetSequence.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	private static void PopulateSeqidsAndCodes(){
		try{

			PreparedStatement pstmGetSequence = connection.prepareStatement("SELECT sequenceid,sequence from sequences2");

			PreparedStatement pstmGetSc = connection.prepareStatement("SELECT code,cluster_id,cluster_threshold from cp_clusters where type=\"sc_cluster\" order by cluster_id");
			PreparedStatement pstmGetSeqId = connection.prepareStatement("SELECT sequenceid from clusters_mcl where" +
					" cluster_id=? and cluster_threshold=?");



			ResultSet rsSc = pstmGetSc.executeQuery();
			while(rsSc.next()){
				String code = rsSc.getString(1);
				Integer id = rsSc.getInt(2);
				Float thresh = rsSc.getFloat(3);

				//add to codeToclusIdnThresh
				//cluster_5.0_0.hmm
				codeToclusIdnThresh.put(code, thresh.toString().trim()+"_"+id.toString().trim());

				pstmGetSeqId.setInt(1, id);
				pstmGetSeqId.setFloat(2, thresh);
				ResultSet rsSeqId = pstmGetSeqId.executeQuery();
				Integer seqid = 0;

				ArrayList<Integer> seqids = new ArrayList<Integer>(); // all seqids for this cluster
				while(rsSeqId.next()){
					seqid = rsSeqId.getInt(1);
					seqids.add(seqid);

					//pstmGetSequence.setInt(1, seqid);
					ResultSet rsSeq = pstmGetSequence.executeQuery();
					String seq = "";
					while(rsSeq.next()){
						int x = rsSeq.getInt(1);
						if(x == seqid){
							seq = rsSeq.getString(2);
							break;
						}
					}
					rsSeq.close();
					seqidsMap.put(seqid, seq);
					seqidsMapToCode.put(seqid, code);
				}
				codeToSeqIds.put(code, seqids);
				codes.add(code);

				seqids = new ArrayList<Integer>();
				rsSeqId.close();
			}
			rsSc.close();
			pstmGetSequence.close();
			pstmGetSeqId.close();
			pstmGetSc.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Method loads hmm graph parameters from *.hmm.serialized file (prepeared in MetaModelClassification4Prepare)
	 * and construct hmm graph (HMMGraph).
	 * @param clusterName cluster code
	 * @return hmm graph
	 * @throws IOException in case of file errors
	 * @throws ClassNotFoundException in case of absense of class HMMGraphData in JVM
	 */
	private static HMMGraph loadHMM(String clusterName) throws IOException, ClassNotFoundException {
		//cluster_5.0_0.hmm.serialized
		//code, thresh.toString().trim()+"_"+id.toString().trim()
		String temp = codeToclusIdnThresh.get(clusterName);

		File hmmSerialized = new File(HMM_DIR, "cluster_"+temp + ".hmm.serialized");
		InputStream bis = new BufferedInputStream(new FileInputStream(hmmSerialized));
		ObjectInputStream is = new ObjectInputStream(bis);
		Object obj = is.readObject();
		is.close();
		HMMGraphData data = (HMMGraphData)obj;
		HMMGraph hmm = new HMMGraph();
		hmm.init(data);
		return hmm;
	}



}
