package CAMPS.utils;

import java.sql.*;
import java.util.*;


/**
 * This class extracts the data from the database
 * 
 * @author hartmann
 *
 */
public class Database
{
	/**
	 * Returns the position of the transmembrane helices of a given protein. This positions are different for 
	 * different clusters, making it crucial to specify the cluster with the cluster id and the cluster_threshold 
	 * 
	 * @param code_id code_id for a protein
	 * @param cluster_id cluster_id of a cluster
	 * @param threshold cluster_threshold used to create the cluster
	 * @return an Array with information about the position of the transmembrane helices {begin, end}
	 */
	/*
	public static ArrayList<int[]> getTMHPositions(String code_id, int cluster_id, double threshold){
		ArrayList<int[]> clusterArray = new ArrayList<int[]>();

		String query = "SELECT " + Global.tmsCores_begin + "," + Global.tmsCores_end +
				" FROM "  + Global.tmsCores +
				" WHERE " + Global.tmsCores_clusterId +" = " +cluster_id+
				" AND "   + Global.tmsCores_clusterThreshold + "=" + threshold + 
				" AND "   + Global.tmsCores_code + "='" + code_id + "'" +
				" ORDER BY " + Global.tmsCores_begin;
		try
		{
			Class.forName( "com.mysql.jdbc.Driver" );
			String[] data = getLoginData();

			Connection connection = null;
			try{
				connection = DriverManager.getConnection( data[0],data[1],data[2]);
			}catch(Exception e){
				connection = DriverManager.getConnection( "jdbc:mysql://localhost:3306/CAMPS4",data[1],data[2] );
				System.out.print("\n Error in getting connection \n ");
				System.exit(0);
			}
			Statement statement = connection.createStatement();

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
			connection.close();
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

	/**
	 * Returns information about the length of the transmembrane helices of a given protein.
	 *  This length distribution should be the same for all proteins in the same cluster
	 * 
	 * @param code_id code_id for a protein
	 * @param cluster_id cluster_id of a cluster
	 * @param threshold cluster_threshold used to create the cluster
	 * @return an ArrayList with the lengths of the transmembrane helices
	 
	public static ArrayList<Integer> getTMHLength(String code_id, int cluster_id, double threshold){
		ArrayList<Integer> clusterArray = new ArrayList<Integer>();
		String query = "SELECT " + Global.tmsCores_length +
				" FROM "  + Global.tmsCores +
				" WHERE " + Global.tmsCores_clusterId + " = " + cluster_id +
				" AND "   + Global.tmsCores_clusterThreshold + "=" + threshold +
				" AND "   + Global.tmsCores_code + "='" + code_id +	"'";
		try
		{
			Class.forName( "com.mysql.jdbc.Driver" );
			String[] data = getLoginData();

			Connection connection = null;
			try{
				connection = DriverManager.getConnection( data[0],data[1],data[2]);
			}catch(Exception e){
				connection = DriverManager.getConnection( "jdbc:mysql://localhost:3306/CAMPS4",data[1],data[2] );
				System.out.print("\n Error in getting connection \n ");
				System.exit(0);
			}
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			while (result.next()) 
			{
				int length = result.getInt(Global.tmsCores_length);				
				clusterArray.add(length);
			}

			result.close();
			statement.close();
			connection.close();
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

	public static ArrayList<Integer> getBigClusterIDs(double threshold){
		ArrayList<Integer> clusterArray = new ArrayList<Integer>();

		//		String query = "SELECT distinct " + Global.cluster_clusterId + ",  count(" + Global.cluster_code + ")" +
		//					  " FROM " + Global.cluster +
		//					  " WHERE " + Global.cluster_clusterThreshold + " = " + threshold + " and redundant=\"No\"" +
		//					  " GROUP BY " + Global.cluster_clusterId + " HAVING count(*) >= 15";
		String query = "SELECT distinct " + Global.clusterInfo_clusterId + 
				" FROM " + Global.clusterInfo +
				" WHERE " + Global.clusterInfo_clusterThreshold + " = " + threshold + 
				" AND " + Global.clusterInfo_numberMembers + " >= 15";
		System.out.println(query);
		try
		{
			Class.forName( "com.mysql.jdbc.Driver" );
			String[] data = getLoginData();

			Connection connection = null;
			try{
				//connection = DriverManager.getConnection( data[0],data[1],data[2]);
				String host = "jdbc:mysql://127.0.0.1/";
				String url = "jdbc:mysql://127.0.0.1/CAMPS4";
				String user = "usman_mpgroup";
				String pwd = "-rockstar1234-";			
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				DriverManager.setLoginTimeout(0);
				connection = DriverManager.getConnection(url, user, pwd);
			}catch(Exception e){
				connection = DriverManager.getConnection( "jdbc:mysql://localhost:3306/CAMPS4",data[1],data[2] );
				System.out.print("\n Error in getting connection \n ");
				System.exit(0);
			}
			Statement statement = connection.createStatement();

			ResultSet result = statement.executeQuery(query);
			while (result.next()) 
			{
				String cluster_id = result.getString(Global.clusterInfo_clusterId);
				//				System.out.println(camperCode);
				clusterArray.add(new Integer(cluster_id));
			}

			result.close();
			statement.close();
			connection.close();
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

	public static ArrayList<String[]> getBigClusterArray(int cluster_id, double threshold){
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
			String[] data = getLoginData();

			Connection connection = null;
			try{
				connection = DriverManager.getConnection( data[0],data[1],data[2]);
			}catch(Exception e){
				connection = DriverManager.getConnection( "jdbc:mysql://localhost:3306/CAMPS4",data[1],data[2] );
				System.out.print("\n Error in getting connection \n ");
				System.exit(0);
			}
			Statement statement = connection.createStatement();
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
			connection.close();
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

	/*
	 * original implementation of Holger Hartmann (replaced by getSplitClusterArray())
	 
	public static ArrayList<Integer> getSplitClusterArray_old(int cluster_id, double thresholdOld, double thresholdNew){
		ArrayList<Integer> clusterArray = new ArrayList<Integer>();		

		String query = "SELECT DISTINCT cl1." + Global.cluster_clusterId + 
				" FROM " + Global.cluster + " as cl1, " + Global.cluster + " as cl2" +
				" WHERE cl1." + Global.cluster_code + "= cl2." + Global.cluster_code + 
				" AND cl1." + Global.cluster_clusterThreshold + " = " + thresholdNew + 
				" AND cl2." + Global.cluster_clusterThreshold + " = " + thresholdOld + 
				" AND cl2." + Global.cluster_clusterId + " = " + cluster_id + 
				" GROUP BY " + Global.cluster_clusterId + " HAVING count(*) >= 15";
		try
		{
			Class.forName( "com.mysql.jdbc.Driver" );
			String[] data = getLoginData();

			Connection connection = null;
			try{
				connection = DriverManager.getConnection( data[0],data[1],data[2]);
			}catch(Exception e){
				connection = DriverManager.getConnection( "jdbc:mysql://localhost:3306/CAMPS4",data[1],data[2] );
				System.out.print("\n Error in getting connection \n ");
				System.exit(0);
			}
			Statement statement = connection.createStatement();


			ResultSet result = statement.executeQuery(query);
			while (result.next()) 
			{
				String clusterId = result.getString(Global.cluster_clusterId);		
				clusterArray.add(new Integer(clusterId));
			}

			result.close();
			statement.close();
			connection.close();
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

	public static ArrayList<Integer> getSplitClusterArray(int cluster_id, double threshold){
		ArrayList<Integer> clusterArray = new ArrayList<Integer>();		

		String query = "SELECT DISTINCT track." + Global.clusterTrack_childClusterId +
				" FROM " + Global.clusterTrack + " as track, " + Global.clusterInfo + " as info" +
				" WHERE track." + Global.clusterTrack_childClusterId + "=info." + Global.clusterInfo_clusterId +
				" AND track." + Global.clusterTrack_childClusterThreshold + "=info." + Global.clusterInfo_clusterThreshold +
				" AND track." + Global.clusterTrack_clusterId + "=" + cluster_id +
				" AND track." + Global.clusterTrack_clusterThreshold + "=" + threshold +
				" AND info." + Global.clusterInfo_numberMembers + ">=15" +
				" ORDER BY track." + Global.clusterTrack_childClusterId;
		try
		{
			Class.forName( "com.mysql.jdbc.Driver" );
			String[] data = getLoginData();

			Connection connection = null;
			try{
				connection = DriverManager.getConnection( data[0],data[1],data[2]);
			}catch(Exception e){
				connection = DriverManager.getConnection( "jdbc:mysql://localhost:3306/CAMPS4",data[1],data[2] );
				System.out.print("\n Error in getting connection \n ");
				System.exit(0);
			}
			Statement statement = connection.createStatement();


			ResultSet result = statement.executeQuery(query);
			while (result.next()) 
			{
				String clusterId = result.getString(Global.clusterTrack_childClusterId);		
				clusterArray.add(new Integer(clusterId));
			}

			result.close();
			statement.close();
			connection.close();
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

	public static double[] getClusterThresholds(){
		ArrayList<Double> clusterThresholds = new ArrayList<Double>();		

		String query = "SELECT distinct cast(" + Global.clusterInfo + "." + Global.clusterInfo_clusterThreshold + 
				" as decimal(5,2)) as threshold from " + Global.clusterInfo + 
				" order by threshold desc";
		try
		{
			Class.forName( "com.mysql.jdbc.Driver" );
			String[] data = getLoginData();

			Connection connection = null;
			try{
				//connection = DriverManager.getConnection( data[0],data[1],data[2]);
				String host = "jdbc:mysql://127.0.0.1/";
				String url = "jdbc:mysql://127.0.0.1/CAMPS4";
				String user = "usman_mpgroup";
				String pwd = "-rockstar1234-";			
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				DriverManager.setLoginTimeout(0);
				connection = DriverManager.getConnection(url, user, pwd);
			}catch(Exception e){
				connection = DriverManager.getConnection( "jdbc:mysql://localhost:3306/CAMPS4",data[1],data[2] );
				System.out.print("\n Error in getting connection \n ");
				System.exit(0);
			}
			
			Statement statement = connection.createStatement();

			ResultSet result = statement.executeQuery(query);
			while (result.next()) 
			{
				double threshold = result.getDouble("threshold");		
				clusterThresholds.add(threshold);
			}

			result.close();
			statement.close();
			connection.close();
		}
		catch(java.lang.ClassNotFoundException e)
		{
			System.err.println("JDBC-ODBC-Treiber nicht gefunden");
		}
		catch(java.sql.SQLException e)
		{
			System.err.println("Fehler beim Abfragen der Datenbank");
			System.err.println(query);
			System.err.println(e.getMessage());
		}		
		double[] thresholds = new double[clusterThresholds.size()+1];		
		if(clusterThresholds.get(0) < 0){
			int i = 0;
			for(double thres : clusterThresholds){			
				thresholds[i++] = thres;
			}
			thresholds[i]=-1000;
		}else{
			int i = clusterThresholds.size();
			thresholds[i--]= 1000;
			for(double thres : clusterThresholds){			
				thresholds[i--] = thres;
			}			
		}
		return thresholds;
	}


	private static String[] getLoginData(){		
		return new String[]{Global.dbConnection, Global.dbUser, Global.dbPassword};		
	}
	*/
	
	// Second implimentation of Databases to avoid getting connections again and again.
	
	/*
	public static Connection getCon(){
		Connection conn = null;
		try{
			//connection = DriverManager.getConnection( data[0],data[1],data[2]);
			//String host = "jdbc:mysql://127.0.0.1/";
			String url = "jdbc:mysql://127.0.0.1/CAMPS4";
			//String url = "jdbc:mysql://localhost/CAMPS";
			String user = "usman_mpgroup";
			String pwd = "-rockstar1234-";			
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			DriverManager.setLoginTimeout(0);
			conn = DriverManager.getConnection(url, user, pwd);
		}catch(Exception e){
			System.out.print("\n Error in getting connection \n ");
			System.exit(0);
		}
		return conn;
	}*/
	

	/**
	 * Returns information about the length of the transmembrane helices of a given protein.
	 *  This length distribution should be the same for all proteins in the same cluster
	 * 
	 * @param code_id code_id for a protein
	 * @param cluster_id cluster_id of a cluster
	 * @param threshold cluster_threshold used to create the cluster
	 * @return an ArrayList with the lengths of the transmembrane helices
	 */
	

	public static ArrayList<Integer> getBigClusterIDs(double threshold, Connection connection){
		
		try {
			if (connection.isClosed()){
				
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ArrayList<Integer> clusterArray = new ArrayList<Integer>();

		//		String query = "SELECT distinct " + Global.cluster_clusterId + ",  count(" + Global.cluster_code + ")" +
		//					  " FROM " + Global.cluster +
		//					  " WHERE " + Global.cluster_clusterThreshold + " = " + threshold + " and redundant=\"No\"" +
		//					  " GROUP BY " + Global.cluster_clusterId + " HAVING count(*) >= 15";
		String query = "SELECT distinct " + Global.clusterInfo_clusterId + 
				" FROM " + Global.clusterInfo +
				" WHERE " + Global.clusterInfo_clusterThreshold + " = " + threshold + 
				" AND " + Global.clusterInfo_numberMembers + " >= 15";
		System.out.println(query);
		try
		{
			Statement statement = connection.createStatement();

			ResultSet result = statement.executeQuery(query);
			while (result.next()) 
			{
				String cluster_id = result.getString(Global.clusterInfo_clusterId);
				//				System.out.println(camperCode);
				clusterArray.add(new Integer(cluster_id));
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

	public static ArrayList<String[]> getBigClusterArray(int cluster_id, double threshold, Connection connection){
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
			Statement statement = connection.createStatement();
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
			
		}catch(java.sql.SQLException e)
		{
			System.err.println("Fehler beim Abfragen der Datenbank");
			System.err.println(query);
			System.err.println(e.getMessage());
		}

		return clusterArray;
	}

	/*
	 * original implementation of Holger Hartmann (replaced by getSplitClusterArray())
	 */
	public static ArrayList<Integer> getSplitClusterArray_old(int cluster_id, double thresholdOld, double thresholdNew, Connection connection){
		ArrayList<Integer> clusterArray = new ArrayList<Integer>();		

		String query = "SELECT DISTINCT cl1." + Global.cluster_clusterId + 
				" FROM " + Global.cluster + " as cl1, " + Global.cluster + " as cl2" +
				" WHERE cl1." + Global.cluster_code + "= cl2." + Global.cluster_code + 
				" AND cl1." + Global.cluster_clusterThreshold + " = " + thresholdNew + 
				" AND cl2." + Global.cluster_clusterThreshold + " = " + thresholdOld + 
				" AND cl2." + Global.cluster_clusterId + " = " + cluster_id + 
				" GROUP BY " + Global.cluster_clusterId + " HAVING count(*) >= 15";
		try
		{
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			while (result.next()) 
			{
				String clusterId = result.getString(Global.cluster_clusterId);		
				clusterArray.add(new Integer(clusterId));
			}

			result.close();
			statement.close();
			
		}
		catch(java.sql.SQLException e)
		{
			System.err.println("Fehler beim Abfragen der Datenbank");
			System.err.println(query);
			System.err.println(e.getMessage());
		}

		return clusterArray;
	}

	public static ArrayList<Integer> getSplitClusterArray(int cluster_id, double threshold, Connection connection){
		ArrayList<Integer> clusterArray = new ArrayList<Integer>();		

		String query = "SELECT DISTINCT track." + Global.clusterTrack_childClusterId +
				" FROM " + Global.clusterTrack + " as track, " + Global.clusterInfo + " as info" +
				" WHERE track." + Global.clusterTrack_childClusterId + "=info." + Global.clusterInfo_clusterId +
				" AND track." + Global.clusterTrack_childClusterThreshold + "=info." + Global.clusterInfo_clusterThreshold +
				" AND track." + Global.clusterTrack_clusterId + "=" + cluster_id +
				" AND track." + Global.clusterTrack_clusterThreshold + "=" + threshold +
				" AND info." + Global.clusterInfo_numberMembers + ">=15" +
				" ORDER BY track." + Global.clusterTrack_childClusterId;
		try
		{
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			while (result.next()) 
			{
				String clusterId = result.getString(Global.clusterTrack_childClusterId);		
				clusterArray.add(new Integer(clusterId));
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

	public static double[] getClusterThresholds(Connection connection){
		ArrayList<Double> clusterThresholds = new ArrayList<Double>();		

		String query = "SELECT distinct cast(" + Global.clusterInfo + "." + Global.clusterInfo_clusterThreshold + 
				" as decimal(5,2)) as threshold from " + Global.clusterInfo + 
				" order by threshold desc";
		try
		{
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			while (result.next()) 
			{
				double threshold = result.getDouble("threshold");		
				clusterThresholds.add(threshold);
			}

			result.close();
			statement.close();
			
		}
		
		catch(java.sql.SQLException e)
		{
			System.err.println("Fehler beim Abfragen der Datenbank");
			System.err.println(query);
			System.err.println(e.getMessage());
		}		
		double[] thresholds = new double[clusterThresholds.size()+1];		
		if(clusterThresholds.get(0) < 0){
			int i = 0;
			for(double thres : clusterThresholds){			
				thresholds[i++] = thres;
			}
			thresholds[i]=-1000;
		}else{
			int i = clusterThresholds.size();
			thresholds[i--]= 1000;
			for(double thres : clusterThresholds){			
				thresholds[i--] = thres;
			}			
		}
		return thresholds;
	}


	private static String[] getLoginData(){		
		return new String[]{Global.dbConnection, Global.dbUser, Global.dbPassword};		
	}
}
