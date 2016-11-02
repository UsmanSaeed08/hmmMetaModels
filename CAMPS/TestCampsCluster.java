package CAMPS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.io.*;



import util.hmm.HMMGraph;
import util.hmm.HMMGraphData;
import util.hmm.HMMMath;


import CAMPS.utils.*;
import CAMPS.hmm.*;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;

public class TestCampsCluster {

	private static org.apache.commons.lang.StringUtils Stutil;
	private static boolean loaded;
	public static double directCorrect = -2.5;
	private static int numberOfHelices =0;
	private static int thisClusNumberofHelices = 0;
	//	public static int maxTestSequences = 500;
	static Hashtable<Integer, tmsCoresInfo> db_data = new Hashtable<Integer, tmsCoresInfo>();
	static Hashtable<Integer, ProteinSeq> db_Bigdata = new Hashtable<Integer, ProteinSeq>();
	private static ArrayList<HMMGraph> HMMGraphs = new ArrayList<HMMGraph>();
	private static ArrayList<String> notLoadedClusters = new ArrayList<String>(); // clusid_thresold

	private static void populate_db_Bigdata(double threshold){

		// get sequence id from clusters_mcl and sequence from sequences2 where 
		String query = "SELECT DISTINCT " + Global.cluster + "." + Global.cluster_code + ", " + 
				Global.proteins + "." + Global.proteins_sequence + ", " +  Global.cluster + "." + Global.cluster_clusterId +
				" FROM "  + Global.cluster + ", "+ Global.proteins + ", " + Global.tmsCores + 
				" WHERE " + Global.cluster + "." + Global.cluster_protId + "=" + Global.proteins + "." + Global.proteins_protId + 
				" AND "   + Global.cluster + "." + Global.cluster_code +	"=" + Global.tmsCores + "." + Global.tmsCores_code + 
				" AND "   + Global.cluster + "." + Global.cluster_clusterId + " = " + Global.tmsCores + "."+ Global.tmsCores_clusterId + 
				" AND "   + Global.cluster + "." + Global.cluster_clusterThreshold + " = " + threshold +  
				" AND "   + Global.tmsCores + "."+ Global.tmsCores_clusterThreshold + "=" + threshold;	

		try
		{
			Connection connection = null;
			try{
				/*
				//String host = "jdbc:mysql://127.0.0.1/";
				String url = "jdbc:mysql://127.0.0.1/CAMPS4";
				String user = "usman_mpgroup";
				String pwd = "-rockstar1234-";			
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				DriverManager.setLoginTimeout(0);
				connection = DriverManager.getConnection(url, user, pwd);
				 */
				connection = DBAdaptor.getConnection("CAMPS4");
			}
			catch (Exception e){
				e.printStackTrace();
			}
			System.out.println("\tYou are now connected to: CAMPS4");
			Statement statement = connection.createStatement();

			ResultSet result = statement.executeQuery(query);
			while (result.next()) 
			{
				// alternatively I could have extracted all the seq ids with 
				// respective cluster ids, and then looked for there tms begin and ends
				// but that would have caused repitition in sql query
				// more time required to communicate with server
				// so it is accepted to have redundant runs in loop than to have redundant 
				// queries to db

				// sequenceid, sequence, clusterid
				// Global.cluster + "." + Global.cluster_code	seqId
				// Global.proteins + "." + Global.proteins_sequence  sequence
				// Global.cluster + "." + Global.cluster_clusterId clusterId

				int seqid = result.getInt(Global.cluster + "." + Global.cluster_code);	// seqId
				String seq = result.getString(Global.proteins + "." + Global.proteins_sequence);	// Seq
				int clusid = result.getInt(Global.cluster + "." + Global.cluster_clusterId);		// clusId
				// cluster id
				// seq id

				if (db_Bigdata.containsKey(clusid)){	// contains the clusterid
					ProteinSeq temp2 = db_Bigdata.get(clusid);	// to get the particular
					// hashtable for this cluster

					if (temp2.seq_obj.containsKey(seqid)){	// the cluster contains this seq
						//String temp = temp2.seq_obj.get(seqid);
						//temp2.seq_obj.put(seqid, temp);

						// if the cluster already contains the sequence id
						// then there is no need to add it again. So just skip it.
					}
					else{									// the cluster does not contain this seq						
						temp2.seq_obj.put(seqid, seq);

						db_Bigdata.put(clusid, temp2);	// **** Check ??? Not in db_data ???
					}

				}
				else{								// does not contain the clusterid

					ProteinSeq temp2 = new ProteinSeq();
					temp2.seq_obj.put(seqid, seq);

					db_Bigdata.put(clusid, temp2);
				}
			}			
			result.close();
			statement.close();
			connection.close();
			System.out.print("\n TM Cores info from DB retreived \n");
		}catch(java.sql.SQLException e)
		{
			System.err.println("Fehler beim Abfragen der Datenbank");
			System.err.println(query);
			System.err.println(e.getMessage());
		}

	}

	private static void populate_db_data(double threshold){

		// Get TMH Positions

		String query = "SELECT " + Global.tmsCores_begin + "," + Global.tmsCores_end + "," + Global.tmsCores_clusterId + "," + Global.tmsCores_code + "," + "tms_core_id" +","+Global.tmsCores_length+ 
				" FROM "  + Global.tmsCores +
				" WHERE " + Global.tmsCores_clusterThreshold + "=" + threshold +
				" ORDER BY " + Global.tmsCores_code + "," + "tms_core_id"; // order by seqid & then the helix core number
		try
		{/*
			Class.forName( "com.mysql.jdbc.Driver" );
			String[] data = getLoginData();

			Connection connection = null;
			try{
			connection = DriverManager.getConnection( data[0],data[1],data[2]);
			}catch(Exception e){
			connection = DriverManager.getConnection( "jdbc:mysql://localhost:3306/CAMPS3",data[1],data[2] );
			System.out.print("\n Error in getting connection \n ");
			System.exit(0);
			}*/
			Connection connection = null;
			try{
				/*
				//String host = "jdbc:mysql://127.0.0.1/";
				String url = "jdbc:mysql://127.0.0.1/CAMPS4";
				String user = "usman_mpgroup";
				String pwd = "-rockstar1234-";			
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				DriverManager.setLoginTimeout(0);
				connection = DriverManager.getConnection(url, user, pwd);
				 */
				connection = DBAdaptor.getConnection("CAMPS4");
			}
			catch (Exception e){
				e.printStackTrace();
			}
			System.out.println("\tYou are now connected to: CAMPS4");
			Statement statement = connection.createStatement();

			ResultSet result = statement.executeQuery(query);
			while (result.next()) 
			{
				// alternatively I could have extracted all the seq ids with 
				// respective cluster ids, and then looked for there tms begin and ends
				// but that would have caused repitition in sql query
				// more time required to communicate with server
				// so it is accepted to have redundant runs in loop than to have redundant 
				// queries to db
				int begin = result.getInt(Global.tmsCores_begin);
				int end = result.getInt(Global.tmsCores_end);
				int seqid = result.getInt(Global.tmsCores_code);	// seqId
				int clusid = result.getInt(Global.tmsCores_clusterId);	// clusid
				int length = result.getInt(Global.tmsCores_length);		// length_aligned
				// cluster id
				// seq id

				if (db_data.containsKey(clusid)){	// contains the clusterid
					tmsCoresInfo temp2 = db_data.get(clusid);	// to get the particular
					// hashtable for this cluster

					if (temp2.seq_obj.containsKey(seqid)){	// the cluster contains this seq
						Seq temp = temp2.seq_obj.get(seqid);
						if(end == begin)
							temp.tmhPosition.add(new int[]{begin,end});
						else
							temp.tmhPosition.add(new int[]{begin,end-1});
						temp.helixLength.add(length);

						temp2.seq_obj.put(seqid, temp);
						db_data.put(clusid, temp2); // NEW ADDED **** ???
					}
					else{									// the cluster does not contain this seq
						Seq temp = new Seq();
						if(end == begin)
							temp.tmhPosition.add(new int[]{begin,end});
						else
							temp.tmhPosition.add(new int[]{begin,end-1});
						temp.helixLength.add(length);

						temp2.seq_obj.put(seqid, temp);
						db_data.put(clusid, temp2); // NEW ADDED **** ???
					}
				}
				else{								// does not contain the clusterid

					Seq temp = new Seq();
					if(end == begin)
						temp.tmhPosition.add(new int[]{begin,end});
					else
						temp.tmhPosition.add(new int[]{begin,end-1});

					temp.helixLength.add(length);

					tmsCoresInfo temp2 = new tmsCoresInfo();
					temp2.seq_obj.put(seqid, temp);
					db_data.put(clusid, temp2);
				}
			}			
			result.close();
			statement.close();
			connection.close();
			System.out.print("\n TM Cores info from DB retreived \n");
		}catch(java.sql.SQLException e)
		{
			System.err.println("Fehler beim Abfragen der Datenbank");
			System.err.println(query);
			System.err.println(e.getMessage());
		}
	}
	private static ArrayList<Integer> add2badClusters(ArrayList<Integer>ids2check,double thresh,ArrayList<Integer> bd){
		//ArrayList<Integer> list = new ArrayList<Integer>();
		try{
			for(int i =0;i<=notLoadedClusters.size()-1;i++){
				String[] s = notLoadedClusters.get(i).split("_");
				Integer id = new Integer(s[0]);
				Double threshold = new Double(s[1]);
				if(threshold == thresh){
					if(ids2check.contains(id)){
						if(!bd.contains(id)){
							bd.add(id);
						}
					}
				}
			}
			return bd;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return bd;
	}

	public static  ArrayList<Integer> testCluster(ArrayList<Integer> cluster_ids, double threshold, int round, Connection connection){
		ArrayList<Integer> badCluster = new ArrayList<Integer>();
		// add the clusters not loaded in this threshold in bad clusters


		System.out.println("\n\nClassifier_Camps:");
		System.out.println(Global.HMMDir);
		System.out.println("ClusterIds to train "+cluster_ids.size());
		//*****************
		// Serialize all the HMMs here. So they can be loaded when required.

		// run one time only... cuz files would be serialized after one run
		System.out.println("Loading HMMs");
		loaded = true;
		for (File ff : new File(Global.getDirectory() + Global.HMMDir).listFiles()) {
			String fileName = ff.getName();
			if (fileName.endsWith(".hmm")) {
				//System.out.println(fileName+" Serialized");
				//HMMGraphData hmmD = loadHmmDataGraph(ff);
				//serializeObjectToFile(hmmD, new File(Global.getDirectory() + Global.HMMDir, fileName + ".serialized"));
				HMMGraph hmmTemp = loadHMMsGraph(fileName, Global.getDirectory() + Global.HMMDir); // Reads the serialized HMM into Graph
				if(loaded){// means if the hmm was loaded sucessfully then add.. else return is null
					HMMGraphs.add(hmmTemp);	// would be used if trainedhmm doesnt work
				}
			}
		}
		System.out.println("Loaded HMMs:"+HMMGraphs.size());
		// **********************

		System.out.println("Populating db local");
		if (!cluster_ids.isEmpty()){
			populate_db_data(threshold);
			populate_db_Bigdata(threshold);
		}
		System.out.println("Db local Made");
		FileOutputStream fout;
		BufferedReader reader = null;
		try
		{			
			// proteins that were classified correctly in the last rounds are stored in correctSet
			// this proteins do not have to be tested again
			TreeSet<String> correctSet = new TreeSet<String>();
			// proteins that were classified not correctly in the last rounds are stored in the wrongList
			// the list contains the 5 best scoring HMMs in the list, which are the only ones that have to be tested again
			TreeSet<WrongList> wrongList = new TreeSet<WrongList>();

			// add data to correctList
			File f;
			for(int i=0; i<Global.rounds; i++){
				f = new File(Global.getDirectory() + Global.TMPDir + "CAMPS_correctList_"+i+ ".txt");
				if(f.exists() && !f.isDirectory()) {	// only if the correct list files exists else have to create them    
					reader = new BufferedReader(new FileReader(Global.getDirectory() + Global.TMPDir + "CAMPS_correctList_"+i+ ".txt"));
					String line;
					while((line = reader.readLine()) != null){
						correctSet.add(line);
					}
					reader.close();
				}
			}
			System.out.println("Correct List Read, lines:"+correctSet.size());

			// add data to wrongList
			f = new File(Global.getDirectory() + Global.TMPDir + "CAMPS_wrongList.txt");	// for the first time wronglist file does not exist
			if(f.exists() && !f.isDirectory()) {
				reader = new BufferedReader(new FileReader(Global.getDirectory() + Global.TMPDir + "CAMPS_wrongList.txt"));
				String line;
				while((line = reader.readLine()) != null){
					String[] data = line.split(":");
					WrongList w = new WrongList(data[0]);
					for(int i=1; i< data.length; i++){
						String[] c = data[i].split(";");
						w.addData(c);					
					}
					wrongList.add(w);
				}
				reader.close();
			}
			System.out.println("Wrong List Read, lines:"+wrongList.size());

			fout = new FileOutputStream (Global.getDirectory() + Global.TMPDir + "CAMPS_correctList_"+round+ ".txt", true);
			PrintStream ps = new PrintStream(fout, true);

			TestCampsCluster clas = new TestCampsCluster();	

			// load all HMMs, build in previous rounds
			ArrayList<SecOrderHMM> secOrderHMMs = clas.loadHMMs();
			//Global.getDirectory() + Global.HMMDir


			System.out.println("Now running training and testing");
			int x = 0;
			for(int cluster_id : cluster_ids){
				x ++;
				ArrayList<String> sequences = new ArrayList<String>();
				// load Sequences from the desired cluster with threshold
				sequences = clas.loadTestSequences(cluster_id, threshold, connection);				
				int notCorrect=0;
				for(int cvRound = 0; cvRound < Global.cv; cvRound ++){			
					// if too many sequences are classified as not correct, the test does not have to be continued 
					if(notCorrect * 1.0 / sequences.size() > Global.maxFalse){
						break;
					}
					double sumConfidence = 0;
					// create an HMM with cv-1/cv of the training sequences, e.g. 80%
					SecOrderHMM trainHMM = clas.getTrainHMM(cvRound, sequences, cluster_id, threshold);
					if(sequences == null){
						System.err.println("cluster_" + threshold + "_" + cluster_id + ".txt not found");
						continue;
					}
					//System.out.println("CV_Round:" + cvRound + ";Cluster:" + cluster_id + ";Threshold:" + threshold + ";Sequences:" + sequences.size());

					double correct = 0;
					double classified = 0;
					int count = 0;
					// test the test set consisting of 1/cv sequences, e.g. 20%
					// the condition below allows always only 10 percent of the sequences to go through...
					// therefore.. these 10 percent are used for testing the data
					for(int j = cvRound*sequences.size()/Global.cv; j < (cvRound+1)*sequences.size()/Global.cv ; j ++){
						double testSetScore = 0;
						count++;
						ArrayList<MaxList> maxList = new ArrayList<MaxList>();					
						double max = Double.NEGATIVE_INFINITY;
						int cluster = -1;
						double thres = 0;
						// Test if sequence could be classified with a smaller E-value threshold, stored in correctSet
						if(correctSet.contains(sequences.get(j).split(":")[0])){
							classified++;
							cluster = cluster_id;
							thres=threshold;
							max = 0;
						}else{
							// Test the sequences from the TestSet against the TrainingSet
							for(int k = 0; k < secOrderHMMs.size(); k++){
								// this loop is to only get the current thresh and cluster id for hmm under test
								// however, scoring is done using the trained hmm. 
								if(secOrderHMMs.get(k).getClusterId().equals(cluster_id+"") &&
										secOrderHMMs.get(k).getThreshold() == threshold){
									// three occurances of score in this class... so have to replace them all
									// 1. serialize the hmm under view...
									// 2. get state count 2d matrix
									// 3. make hmmgraph
									// 4. use hmm graph to call score
									// have to make hmmgraph and other classes related!

									//max = clas.score(sequences.get(j).split(":")[1], trainHMM);
									String testS = sequences.get(j).split(":")[1];

									// write trained hmm to a temp file
									String fname = "cluster_" + threshold + "_" + trainHMM.getClusterId() + ".hmm";
									trainHMM.setThreshold(threshold);
									trainHMM.setSecOrder(secOrderHMMs.get(k).getSecOrder());
									Utils.writeHmmIntoFile(Global.TMPDir, trainHMM.getHmm(),fname , trainHMM.getSecOrder().toString());
									// Now load this particular HMM
									// make a temp serialized temp file
									//load it back to hmmgraph
									HMMGraphData hmmD;
									HMMGraph hmmg;
									File f_hmm =  new File(Global.getDirectory()+Global.TMPDir+fname); 
									hmmD = loadHmmDataGraph(f_hmm);
									serializeObjectToFile(hmmD, new File(Global.getDirectory() + Global.HMMDir, f_hmm.getName() + ".serialized"));
									hmmg = loadHMMsGraph(f_hmm.getName(), Global.getDirectory() + Global.HMMDir); // Reads the serialized HMM into Graph
									hmmg.setCluster_id(trainHMM.getClusterId());
									hmmg.setThreshold(threshold);
									hmmg.setSecOrder(trainHMM.getSecOrder());
									// Delete the temp hmm file after loading data
									f_hmm.delete();
									//HMMGraph hmmg = loadHMM(fileName, HMM_DIR);	// LOAD THE TRAINED HMM HERE
									int sequenceLength = testS.length(); 
									double [][] farr = null;
									farr = checkForStateCount(farr, hmmg.getStates().length, sequenceLength);

									max = clas.score(testS, hmmg, farr);
									cluster = new Integer(secOrderHMMs.get(k).getClusterId());
									thres = new Double(secOrderHMMs.get(k).getThreshold());
									testSetScore = max;
									String[] sc = hmmg.getSecOrder();
									//String s =  sc.toString();
									String s = Stutil.join(sc);
									numberOfHelices = Stutil.countMatches(s, "LH");
									thisClusNumberofHelices = numberOfHelices; 
								}
							}	
						}
						double confidence = 1;
						// if the sequence belongs to the correctSet or the score with the training Set is high enough, 
						// don't test sequence against all the other HMMs						
						if(max > directCorrect){
							classified++;
						}else{
							// if not direct correct... then for all the hmms check if test seq is in wrong list
							// or not, in either case score them. So the whole idea is
							// that every other HMM would score less than this HMM. But the problem
							// now seems to be that sometimes other HMMs score better...
							for(int k = 0; k < secOrderHMMs.size(); k++){					
								double score = -10;		
								// if the HMM belongs to the same cluster, use the HMM build on the trainingSet
								if(secOrderHMMs.get(k).getClusterId().equals(cluster_id+"") &&
										secOrderHMMs.get(k).getThreshold() == threshold){
									//									score = clas.score(sequences.get(j).split(":")[1], trainHMM);
									score = testSetScore;
									numberOfHelices = thisClusNumberofHelices;
									//System.out.println("Score" + score+ "\t" + threshold + "\t" + cluster_id);
								}else{
									// test if sequence is stored in wrong List, to test against less HMMs									
									String name = sequences.get(j).split(":")[0];
									if(wrongList.contains(new WrongList(name))){										
										for(WrongList w : wrongList){
											if(w.getName().equals(name)){
												ArrayList<String[]> data = w.getList();
												// test against similar HMMs
												for(String[] s : data){
													if(secOrderHMMs.get(k).getThreshold() == new Double(s[1]) 
													&& secOrderHMMs.get(k).getClusterId().equals(s[0])){

														//old scoring function
														//score = clas.score(sequences.get(j).split(":")[1], secOrderHMMs.get(k));
														// calling scoring new
														HMMGraph hmm_ = HMMGraphs.get(k);
														if(hmm_.getThreshold() == new Double(s[1]) 
														&& hmm_.getCluster_id().equals(s[0])){

															String testSeq = sequences.get(j).split(":")[1];
															int sequenceLength = testSeq.length(); 
															double [][] farr = null;
															farr = checkForStateCount(farr, hmm_.getStates().length, sequenceLength);
															// test against all HMMs
															score = score(testSeq, hmm_, farr);
															String[] sc = hmm_.getSecOrder();
															String ss =  Stutil.join(sc);
															numberOfHelices = Stutil.countMatches(ss, "LH");
															// calling scoring end

															// simply replace the score function with new one and
															// hmm graph... same index of secorderhmms as both have same
															//	System.out.println("in Wrong: " + score + "\t" + s[1] + "\t" + s[0]);
														}
													}
												}
											}
										}										
									}else{										
										// test against loaded HMM
										// simply replace the score function with new one and
										// hmm graph... same index of secorderhmms as both have same... confirm this index thing

										//old scoring function
										//score = clas.score(sequences.get(j).split(":")[1], secOrderHMMs.get(k));

										// calling scoring
										HMMGraph hmm_ = HMMGraphs.get(k);
										String testSeq = sequences.get(j).split(":")[1];
										int sequenceLength = testSeq.length(); 
										double [][] farr = null;
										farr = checkForStateCount(farr, hmm_.getStates().length, sequenceLength);
										// test against all HMMs
										score = score(testSeq, hmm_, farr);
										String[] sc = hmm_.getSecOrder();
										String s =  Stutil.join(sc);
										numberOfHelices = Stutil.countMatches(s, "LH");
										//n = n + StringUtils.countMatches(s, "H,L");


										// calling scoring end


									}
								}	
								if((score + "").equals("NaN")){
									//System.out.println("NaN " + k + " " + secOrderHMMs.get(k).getClusterId());
								}else{
									//									System.out.println("add: " + score +"\t" + new Integer(secOrderHMMs.get(k).getClusterId()) + "\t" + secOrderHMMs.get(k).getThreshold());
									maxList.add(new MaxList(score,new Integer(secOrderHMMs.get(k).getClusterId()), secOrderHMMs.get(k).getThreshold()));
								}/*
								if( score > max){
									max = score;
									cluster = new Integer(secOrderHMMs.get(k).getClusterId());
									thres = new Double(secOrderHMMs.get(k).getThreshold());
								}*/
								if( score-0.5 > max){
									// i want to check the same number of Helices before assignment
									// over here
									/*
									if(numberOfHelices < 3 && score - 1.0 > max){	// so that many small HMMs are not treated differently
										score = score -1;
										max = score;
										cluster = new Integer(secOrderHMMs.get(k).getClusterId());
										thres = new Double(secOrderHMMs.get(k).getThreshold());
									}
									else{*/
									max = score;
									cluster = new Integer(secOrderHMMs.get(k).getClusterId());
									thres = new Double(secOrderHMMs.get(k).getThreshold());
									//	}
								}

							}
							Collections.sort(maxList);
							for(int i=0; i<5; i++){		// prints the top 5 scored clusters with threshs
								if(maxList.size() <= i) break;
								MaxList m = maxList.get(i);
								//System.out.println(m.max + "\t" + m.cluster + "\t" + m.threshold);
							}							
							confidence = max - maxList.get(1).max;						
							if(confidence >= Global.confidenceThreshold && max >= Global.scoreThreshold){
								classified++;
							}
						}	
						sumConfidence += confidence;
						//System.out.print("code:" + sequences.get(j).split(":")[0] + ";");	
						String descr = "";
						if(sequences.get(j).split(":").length>2) 
							descr=sequences.get(j).split(":")[2];
						if(cluster == cluster_id && thres == threshold && confidence >= Global.confidenceThreshold && max >= Global.scoreThreshold){
							correct++;
							//write into correctList correct classified protein
							ps.println(sequences.get(j).split(":")[0]);
							//System.out.println("SequenceClassified as: ");
							System.out.println("result:correct;score:" + max + ";confidence:" + new Double(confidence*100).intValue() / 100.0 + ";descr:" + descr);
						}else if(confidence >= Global.confidenceThreshold && max >= Global.scoreThreshold){
							notCorrect++;
							System.out.println("result:false-" + thres + "," + cluster + ";score:" + max + ";confidence:" + new Double(confidence*100).intValue() / 100.0 + ";descr:" + descr);
							clas.writeMaxList(sequences.get(j).split(":")[0], maxList, round);							
						}else{							
							notCorrect++;
							System.out.println("result:????-" + thres + "," + cluster + ";score:" + max + ";confidence:" + new Double(confidence*100).intValue() / 100.0 + ";descr:" + descr);
							clas.writeMaxList(sequences.get(j).split(":")[0], maxList, round);
						}		
						if(notCorrect * 1.0 / sequences.size() > Global.maxFalse){
							break;
						}
					}					
				}
				if(notCorrect * 1.0 / sequences.size() > Global.maxFalse){
					System.out.println("\n Since notCorect is:"+ notCorrect+" and total seq is "+sequences.size()+" cluster added to bad and split list "); 
					// See here what is sequence size.. as in..is the ratio that of total seq or of the training set
					// and the answer is that it is total sequence size.. that of which whole cluster comprises
					badCluster.add(cluster_id);
				}
				else{
					System.out.println("\n Cluster Classified as Correct "+"/"+cluster_id+ "\n");
				}
				System.out.println("\nCluster no "+x+"/"+cluster_ids.size()+ " complete\n");
			}
			System.out.println("\nBad Cluster Number "+"/"+badCluster.size()+ "\n");
			ps.close();
		}catch(Exception e){
			System.err.println(e.getMessage());			
			e.printStackTrace(System.err);
		}
		// adss the not loaded clusters from this threshold as bad clusters
		badCluster = add2badClusters(cluster_ids,threshold,badCluster); //-- move to after loading hmms
		return badCluster;
	}
	private static double[][] checkForStateCount(double[][] farr, int stateCount, int sequenceLength) {
		if (farr == null || farr.length < stateCount) {
			farr = new double[stateCount][sequenceLength + 1];
		}
		return farr;
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

	private static HMMGraph loadHMMsGraph(String clusterName, String HMM_DIR) {
		loaded = true;
		try{
			File hmmSerialized = new File(HMM_DIR, clusterName + ".serialized");
			InputStream bis = new BufferedInputStream(new FileInputStream(hmmSerialized));
			ObjectInputStream is = new ObjectInputStream(bis);
			Object obj = is.readObject();
			is.close();

			HMMGraphData data = (HMMGraphData)obj;
			HMMGraph hmm = new HMMGraph();
			hmm.init(data);
			return hmm;
		}
		//throws IOException, ClassNotFoundException, EOFException
		catch(IOException e){
			loaded = false;
			System.out.println("Unable to Load file "+clusterName);
			return null;
		}
		catch(ClassNotFoundException e){
			loaded = false;
			System.out.println("Unable to Load file "+clusterName);
			return null;
		}
	}

	// loads HMM and also the HMM graph and populates the ArrayList of HMMGraphs

	public ArrayList<SecOrderHMM> loadHMMs(){
		ArrayList<SecOrderHMM> secOrderHMMs = new ArrayList<SecOrderHMM>();		
		File f = new File(Global.getDirectory() + Global.HMMDir);
		File[] fileArray = f.listFiles();
		for(File file : fileArray){	
			if(!file.getName().endsWith(".hmm")) continue;
			SecOrderHMM tmp;
			HMMGraphData hmmD;
			HMMGraph hmmTemp;
			String clus = "";
			try{
				tmp = Utils.loadHmmFromFile(Global.getDirectory() + Global.HMMDir, file.getName());
				String[] name = file.getName().split("_|(\\.hmm)");
				tmp.setClusterId(name[2].trim());
				tmp.setThreshold(new Double(name[1].trim()));

				clus = name[2].trim()+"_"+name[1].trim();

				//hmmD = loadHmmDataGraph(file);
				//serializeObjectToFile(hmmD, new File(Global.getDirectory() + Global.HMMDir, file.getName() + ".serialized"));
				hmmTemp = loadHMMsGraph(file.getName(), Global.getDirectory() + Global.HMMDir); // Reads the serialized HMM into Graph
				hmmTemp.setCluster_id(name[2]);
				hmmTemp.setThreshold(new Double(name[1]));
				hmmTemp.setSecOrder(tmp.getSecOrder());

				//				System.out.println("id: " + tmp.getClusterId() + "\t" + tmp.getThreshold());
			}catch(Exception e){	
				System.err.println(file.getName() +" not found");
				// the cluster was not loaded, so add it to not loaded clusters and
				// then add it into the bad clusters to split in next round
				notLoadedClusters.add(clus);
				continue;
			}
			if(tmp != null){
				secOrderHMMs.add(tmp);
				//System.out.println(fileName+" Serialized");
				HMMGraphs.add(hmmTemp);	// would be used if trainedhmm doesnt work
			}
		}		
		return secOrderHMMs;
	}


	// serializes object to file
	private static void serializeObjectToFile(Serializable obj, File out) throws IOException {
		OutputStream bos = new BufferedOutputStream(new FileOutputStream(out));
		ObjectOutputStream os = new ObjectOutputStream(bos);
		os.writeObject(obj);
		os.close();
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
				e.printStackTrace(System.err);
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
	/*
	public double score(String s1, SecOrderHMM secOrderHMM) throws Exception{
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
	 */

	public ArrayList<String> loadTestSequences(int cluster_id, double threshold, Connection connection){
		ArrayList<String> seq = new ArrayList<String>();
		//ArrayList<String[]> data = Database.getBigClusterArray(cluster_id, threshold, connection);
		ArrayList<String[]> data = get_db_BigData(cluster_id, threshold);
		//		int counter = 0;
		for(String[] tmp : data){
			if(tmp[2].contains("X") || tmp[2].contains("x") || tmp[2].contains("Z") || tmp[2].contains("z"))
				continue;

			seq.add(tmp[0] + ":" + tmp[2] + ":" + tmp[1]);
			//			if(counter++ > maxTestSequences)
			//				break;
			//			System.out.println(tmp[0] + ":" + tmp[2] + ":" + tmp[1]);
		}
		return seq;		
	}

	public SecOrderHMM getTrainHMM(int cvRound, ArrayList<String> sequences, int cluster_id, double threshold){
		Family fam = new Family(cluster_id + "_" + cvRound ,0);			
		for(int i = 0; i<sequences.size(); i++){			
			String[] tmp = sequences.get(i).split(":");
			//ArrayList<int[]> tmhPosition = Database.getTMHPositions(tmp[0],cluster_id,threshold);				
			ArrayList<int[]> tmhPosition = getTMHPositions_db_data(tmp[0],cluster_id);
			//			if(tmhPosition.size() != 0){
			// the condition below makes 90% of the sequences to go through as statement being true
			// therefore using 90% of the sequences as training
			if(i < cvRound*sequences.size()*1.0/Global.cv || i > (cvRound+1)*sequences.size()*1.0/Global.cv){
				String descr = "";
				if(tmp.length > 2) descr = tmp[2];
				fam.addProtein(new Protein(tmhPosition, tmp[1], tmp[0], descr));
			}
			//			}
		}
		if(fam.getMembers().isEmpty()) return null;
		int nbHelices = 0;
		for(Protein prot : fam.getMembers()){
			nbHelices = Math.max(nbHelices, prot.getNbTMH());
		}

		int[] helixLengthArray = new int[nbHelices];
		for(Protein prot : fam.getMembers()){
			//			System.out.println(prot.getCode() + ": " + prot.getNbTMH() + " TMH");
			for(int i = 0; i < prot.getNbTMH(); i++){
				int[] tmh = prot.getTmh().get(i);
				helixLengthArray[i] = Math.max(helixLengthArray[i], tmh[1] - tmh[0] + 1);
			}
		}
		ArrayList<Integer> helixLength = new ArrayList<Integer>();
		for(int tmh: helixLengthArray){
			helixLength.add(tmh);
		}
		fam.setHelixLength(helixLength);	
		//System.out.println("Family Helix length:" + fam.gethelixLength());

		BuildHMM start = new BuildHMM();	
		Hmm<ObservationInteger> result = start.doIt(fam);

		return new SecOrderHMM(result, null, cluster_id+"");
	}

	public void writeMaxList(String name, ArrayList<MaxList> maxList, int round){
		FileOutputStream fout;		
		try
		{
			fout = new FileOutputStream (Global.getDirectory() + Global.TMPDir + "CAMPS_wrongList_"+round+ ".txt", true);
			PrintStream ps = new PrintStream(fout, true);

			ps.print(name+":");
			for(int i=0; i<5; i++){
				MaxList m = maxList.get(i);			
				ps.print(m.cluster + ";" + m.threshold + ":");
				//System.out.print(m.cluster + ";" + m.threshold + ":");
			}
			ps.println();
			ps.close();
		}catch(Exception e){
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}		
	}

	private static ArrayList<String[]> get_db_BigData(int cluster_id, double threshold){
		ArrayList<String[]> clusterArray = new ArrayList<String[]>();
		// clustercode
		// description
		// sequence
		if (db_Bigdata.containsKey(cluster_id)){
			ProteinSeq temp = db_Bigdata.get(cluster_id);
			Set<Integer> x  = temp.seq_obj.keySet();
			Object[] y = x.toArray();
			//System.out.print("Clusid: "+ cluster_id +"\n");
			for (int i = 0; i<=y.length-1;i++){
				String sequence = temp.seq_obj.get((Integer) y[i]);
				String camperCode = Integer.toString((Integer) y[i]);
				String description = "";

				//System.out.print("SeqId: "+ camperCode +"\n");
				//System.out.print("Sequence: "+ sequence +"\n");

				clusterArray.add(new String[]{camperCode, description, sequence});
			}
			//System.exit(0);

		}
		else {
			System.out.print(cluster_id + " NOT FOUND");
		}


		return clusterArray;
	}

	private static String[] getLoginData(){		
		return new String[]{Global.dbConnection, Global.dbUser, Global.dbPassword};		
	}
	private static ArrayList<Integer> getTMHLength_db_data(String str,
			int cluster_id) {
		// TODO Auto-generated method stub
		int sqid = Integer.parseInt(str);

		tmsCoresInfo obj = db_data.get(cluster_id);
		Seq obj_tmp = obj.seq_obj.get(sqid);
		return obj_tmp.helixLength;
	}

	private static ArrayList<int[]> getTMHPositions_db_data(String str,
			int cluster_id) {
		// TODO Auto-generated method stub
		int sqid = Integer.parseInt(str);

		tmsCoresInfo obj = db_data.get(cluster_id);
		Seq obj_tmp = obj.seq_obj.get(sqid);
		return obj_tmp.tmhPosition;

	}
}
