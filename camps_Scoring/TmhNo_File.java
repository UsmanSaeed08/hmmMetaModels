package camps_Scoring;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;

import CAMPS.utils.*;
import java.sql.PreparedStatement;

public class TmhNo_File {

	/**
	 * @param args
	 */
	private static final Connection connection = DBAdaptor.getConnection("CAMPS4");
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		System.out.print("XxX\n");
		String file_name = "F:/scClustersAndTMSRange.txt";
		makeFile(file_name);

	}

	private static void makeFile(String file_name) {
		// TODO Auto-generated method stub
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file_name)));
			PreparedStatement pstmGetSc = connection.prepareStatement("SELECT code,cluster_id,cluster_threshold from cp_clusters where type=\"sc_cluster\"");
			PreparedStatement pstmGetTmh = connection.prepareStatement("SELECT tms_range from clusters_mcl_nr_info where" +
					" cluster_id=? and cluster_threshold=?");
			
			ResultSet rsSc = pstmGetSc.executeQuery();
			while(rsSc.next()){
				String code = rsSc.getString(1);
				Integer id = rsSc.getInt(2);
				Float thresh = rsSc.getFloat(3);
				
				pstmGetTmh.setInt(1, id);
				pstmGetTmh.setFloat(2, thresh);
				ResultSet rsTmh = pstmGetTmh.executeQuery();
				String tmh = "";
				while(rsTmh.next()){
					tmh = rsTmh.getString(1);
				}
				rsTmh.close();
				// write to file below
				bw.write(code+"\t"+tmh.trim()+"\t"+id+"\t"+thresh);
				System.out.print(code+"\t"+tmh+"\t"+id+"\t"+thresh+"\n");
				bw.newLine();
			}
			bw.close();
			rsSc.close();
			pstmGetTmh.close();
			pstmGetSc.close();			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
