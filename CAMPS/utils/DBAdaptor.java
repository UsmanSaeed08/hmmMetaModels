package CAMPS.utils;
/*
 * DBAdaptor
 * 
 * Version 1.0
 * 
 * 2007-01-22
 */


import java.util.Date;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;


/**
 * @author sneumann
 *
 */
public class DBAdaptor {
	
	/**
	 * Establishes a connection to the specified database.
	 * The connection parameters are extracted from the
	 * config file.
	 * 
	 * @param dbname name of the database
	 * @return database connection
	 */
	public static Connection getConnection(String dbname) {
		Connection conn = null;
		Date a = Calendar.getInstance().getTime();
		//System.out.println("\nGetting Connection for: " + dbname +" at "+ a);
		
		try {			
			
			dbname = dbname.toLowerCase();			
			//ConfigFile cf = ConfigFile.getInstance();

			ConfigFile cf = new ConfigFile("/home/users/saeed/workspace/CAMPS3/Common/config/config.xml");	//with databases
			//ConfigFile cf = new ConfigFile("/home/users/saeed/workspace/CAMPS3/config/config.xml");			//with linkouts
			
			//ConfigFile cf = new ConfigFile("F:/CAMPS external Projects/Common/config/config.xml");

			String host = cf.getProperty("mysql:"+dbname+":host");
			String url = host + cf.getProperty("mysql:"+dbname+":database");
			String user = cf.getProperty("mysql:"+dbname+":user");
			String pwd = cf.getProperty("mysql:"+dbname+":pwd");
	
			
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			DriverManager.setLoginTimeout(0);
			conn = DriverManager.getConnection(url, user, pwd);
			
			
			
			//String url = "jdbc:mysql://141.40.43.211/test_camps3";
			//String url = "jdbc:mysql://praktikum.bio.wzw.tum.de:3306/test_camps3";
	          // Class.forName ("com.mysql.jdbc.Driver");
	           //conn = DriverManager.getConnection (url,"usman_mpgroup","-rockstar1234-");
	           //System.out.println ("Database connection established");
			
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return conn;
	}
	
	/**
	 * Establishes a connection to the specified database using
	 * the specified connection parameters.
	 * 
	 * @param dbname name of the database
	 * @param host 
	 * @param user
	 * @param password
	 * @return
	 */
	public static Connection getConnection(String dbname, String host, String user, String password) {
		Connection conn = null;
		try {			
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(host+dbname, user, password);
			
			String info = "mysql -h " +host + " -u " +user +" -pxxx " +dbname;
			
			System.out.println("\tYou are now connected to: " +info);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	/**
	 * Creates an index on the specified table on the specified columns in the
	 * specified MySQL database. 
	 * 
	 * @param dbname	- name of the MySQL database
	 * @param table		- name of the table 
	 * @param columns	- set of column names the index will be build on
	 * @param indexName	- name of the index
	 */
	public static void createIndex(String dbname, String table, String[] columns, String indexName) {
		Connection conn = null;
		
		Date a = Calendar.getInstance().getTime();
		System.out.println("\n Creating new Indexes: " + a);
		try {
			
			String cols = "";
			for(String column: columns) {
				cols += "," + column;
			}
			cols = cols.substring(1);
			
			conn = getConnection(dbname);
			Statement stm = conn.createStatement();
			stm.executeUpdate("ALTER TABLE " +table+" ADD INDEX "+indexName+ " ("+cols+")");
			stm.close();
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
					conn = null;
				} catch (SQLException e) {					
					e.printStackTrace();
				}
			}
		}
	}

}
