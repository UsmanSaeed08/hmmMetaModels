package CAMPS;

import CAMPS.hmm.*;
import CAMPS.utils.*;
import be.ac.ulg.montefiore.run.jahmm.*;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;


public class CreateCampsCluster {	
	static String dir= "";	

	static Hashtable<Integer, tmsCoresInfo> db_data = new Hashtable<Integer, tmsCoresInfo>();
	static Hashtable<Integer, ProteinSeq> db_Bigdata = new Hashtable<Integer, ProteinSeq>();
	// key --> ClusterId
	// value -->  SeqId, length align array, Begin and End arrays
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
				String host = "jdbc:mysql://127.0.0.1/";
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
		{
			/*
			Class.forName( "com.mysql.jdbc.Driver" );
			String[] data = getLoginData();

			Connection connection = null;
			try{
			connection = DriverManager.getConnection( data[0],data[1],data[2]);
			}catch(Exception e){
			connection = DriverManager.getConnection( "jdbc:mysql://localhost:3306/CAMPS3",data[1],data[2] );
			System.out.print("\n Error in getting connection \n ");
			System.exit(0);
			}

			 */
			Connection connection = null;
			try{
				/*
				String host = "jdbc:mysql://127.0.0.1/";
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
		}//catch(java.lang.ClassNotFoundException e)
		//{
		//	System.err.println("JDBC-ODBC-Treiber nicht gefunden");
		//	}
		catch(java.sql.SQLException e)
		{
			System.err.println("Fehler beim Abfragen der Datenbank");
			System.err.println(query);
			System.err.println(e.getMessage());
		}

	}

	public static ArrayList<Integer> createHMMsLevel1(ArrayList<Integer> cluster_ids, double threshold, String HMMDirectory,Connection connection) throws Exception{
		dir = HMMDirectory;
		System.out.println("\ncreateHMMsLevel1\t loopSize: " + Global.loopSize + " ," +
				" TransitionPseudocouns: " + Global.transitionPseudocount + ", directory = " + dir);
		ArrayList<Integer> emptyList = new ArrayList<Integer>();

		//PrintWriter writer = new PrintWriter(Global.directory+"TMHProb.txt", "UTF-8");

		// populate the tms_cores inside the ram making a hash table here, before the loop starts to 
		// process each member of the cluster id.
		// ******************************* CODE ADDED *******************
		System.out.print("\n TM Cores info To be retreived now \n");
		if (!cluster_ids.isEmpty()){
			populate_db_data(threshold);
			System.out.print("\n TM Cores info from DB retreived, Now Extract Seq \n");
			populate_db_Bigdata(threshold);
		}
		// ************************** ADDED CODE END ********************
		System.out.print("\n Extracting Seq complete now \n");
		int x =0;
		for(int cluster_id : cluster_ids){	
			int nbHelix = 0;
			// get data from the cluster members
			//ArrayList<String[]> clusterArray = Database.getBigClusterArray(cluster_id, threshold,connection);

			ArrayList<String[]> clusterArray = get_db_BigData(cluster_id, threshold);
			// create a family for a given cluster_id and threshold
			Family fam = new Family(cluster_id + "" ,threshold);
			for(int i=0; i<clusterArray.size(); i++){
				String[] tmp = (String[])clusterArray.get(i); // camperCode, description, sequence, tmss
				if(tmp[2].contains("X") || tmp[2].contains("Z")){
					continue;
				}
				// tmp[2]	Sequence
				// tmp[0]	Code	
				// tmp[1]	Description

				//ArrayList<int[]> tmhPosition = Database.getTMHPositions(tmp[0],cluster_id,threshold);				
				ArrayList<int[]> tmhPosition = getTMHPositions_db_data(tmp[0],cluster_id);
				// dont need to send threshold as populate, already got the data only for 
				// the current threshold
				if(fam.gethelixLength() == null || fam.gethelixLength().size() == 0){
					//ArrayList<Integer> helixLength = Database.getTMHLength(tmp[0], cluster_id, threshold);
					ArrayList<Integer> helixLength = getTMHLength_db_data(tmp[0], cluster_id);
					// set the Helix length in the family
					if(helixLength.size() > 0){
						System.out.println(helixLength);
						fam.setHelixLength(helixLength);
					}
				}			
				// if no helix is assigned to this protein, ignore it
				if(tmhPosition.size() != 0){	
					if(nbHelix == 0){
						nbHelix = tmhPosition.size();
					}else if(nbHelix != tmhPosition.size()){
						//writer.println(cluster_id +"\t"+threshold);	// added to write those clusters in file
						//writer.flush();
						//continue;	// I think we should let it in the formation so
						// as in training part it would automatically be out. 
						//throw new Exception("different numbers of TMH in cluster with cluster_id=" + cluster_id);
					}
					fam.addProtein(new Protein(tmhPosition, tmp[2], tmp[0], tmp[1]));
					//					System.out.println(tmp[0] + " : " + tmp[2]);
					//					System.out.println(fam.getNumberTMH());
				}
			}
			if(fam.getMembers().isEmpty()){
				emptyList.add(cluster_id);
				continue;
			}

			// build MM for the proteins in fam
			try{
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
				Utils.writeHmmIntoFile(dir, result, "cluster_" + threshold + "_" + fam.getName() + ".hmm", secOrder.toString());
				x++;
				System.out.println("Cluster " + cluster_id + " ready " +x+"/"+cluster_ids.size());
			}
			catch(Exception e){
				e.printStackTrace();
				System.out.println();
				//System.err.print("ADD CLUSTER TO BAD LSIT");
				System.out.println("Failed to Make HMM due to structural anamoly.");
				System.out.println("Theoretically, the clusters can not be considered further, as this exception occurs");
				System.out.println("when all members of the cluster have no states in the begining loop region. But since");
				System.out.println("hmm is not made. Added to splitlist...");
				System.out.println("Cluster " + cluster_id );
				emptyList.add(cluster_id);
				System.out.println();

			}
		}
		//	writer.close();
		// return list of clusters, for which no MM could be build
		return emptyList;
	}

	private static ArrayList<Integer> getTMHLength_db_data(String str,
			int cluster_id) {
		// TODO Auto-generated method stub
		int sqid = Integer.parseInt(str);

		tmsCoresInfo obj = db_data.get(cluster_id);
		Seq obj_tmp = obj.seq_obj.get(sqid);
		return obj_tmp.helixLength;
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
			System.out.print("Clusid: "+ cluster_id +"\n");
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

	private static ArrayList<int[]> getTMHPositions_db_data(String str,
			int cluster_id) {
		// TODO Auto-generated method stub
		int sqid = Integer.parseInt(str);

		tmsCoresInfo obj = db_data.get(cluster_id);
		Seq obj_tmp = obj.seq_obj.get(sqid);
		return obj_tmp.tmhPosition;

	}

	public static ArrayList<Integer>createSplitCluster(ArrayList<Integer> splitList, double thresholdOld, double thresholdNew, Connection connection){
		ArrayList<Integer> clusterList = new ArrayList<Integer>();
		for(int cluster_id : splitList){
			//clusterList.addAll(Database.getSplitClusterArray(cluster_id, thresholdOld, thresholdNew));
			clusterList.addAll(Database.getSplitClusterArray(cluster_id, thresholdOld, connection));
		}
		return clusterList;
	}
	private static String[] getLoginData(){		
		return new String[]{Global.dbConnection, Global.dbUser, Global.dbPassword};		
	}
}
