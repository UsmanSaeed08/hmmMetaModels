package test;

import java.sql.Connection;
import java.util.ArrayList;

import CAMPS.utils.DBAdaptor;
import CAMPS.utils.Database;
import CAMPS.utils.Global;
import CAMPS.utils.Utils;

public class DatabaseTEST {

	/**
	 * @param args
	 */
	//private static final Connection connection = Database.getCon();
	private static final Connection connection = DBAdaptor.getConnection("CAMPS4");
	
	public static void main(String[] args) {
		
		//Global.directory = "/home/users/sneumann/workspace/MetaModels4CAMPS3/";
		Global.directory = "F:/RunMetaModel/";
						
		Utils.setGlobalVariables("config.txt");
		Global.thresholds = Database.getClusterThresholds(connection);
		
//		int clusterId = 2;
//		double clusterThreshold = 5;
//		double clusterThresholdNew = 6;
		
//		int clusterId = 698;
//		double clusterThreshold = 13;
//		double clusterThresholdNew = 14;
		
		int clusterId = 18262;
		double clusterThreshold = 55;
		double clusterThresholdNew = 60;
		
		System.out.println("List according to Holgers inital implementation:\n");
		ArrayList<Integer> list_old = Database.getSplitClusterArray_old(clusterId, clusterThreshold, clusterThresholdNew,connection);
		for(Integer i: list_old) {
			System.out.println("\t"+i.intValue());
		}
		
		System.out.println("\nList according to my implementation using clusters_mcl_track:\n");
		ArrayList<Integer> list_new = Database.getSplitClusterArray(clusterId, clusterThreshold,connection);
		for(Integer i: list_new) {
			System.out.println("\t"+i.intValue());
		}

	}

}
