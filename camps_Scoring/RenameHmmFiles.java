package camps_Scoring;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import CAMPS.utils.DBAdaptor;

public class RenameHmmFiles {

	/**
	 * @param args
	 */
	private static final Connection connection = DBAdaptor.getConnection("CAMPS4");
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		run();
	}
	
	
	private static void run() {
		// TODO Auto-generated method stub
		try{
			PreparedStatement pstmGetSc = connection.prepareStatement("SELECT code,cluster_id,cluster_threshold from cp_clusters where type=\"sc_cluster\" order by cluster_id");
			ResultSet rsSc = pstmGetSc.executeQuery();
			int count =0;
			while(rsSc.next()){
				count ++;
				String code = rsSc.getString(1);
				Integer id = rsSc.getInt(2);
				Float thresh = rsSc.getFloat(3);
				//CMSC0001.hmm.serialized
				//cluster_100.0_769.hmm.serialized
				//String path = "F:/SC_Clust_postHmm/RunMetaModel_gef/RunMetaModel_gef/HMMs/CAMPS4_1/";
				String path = "/home/users/saeed/hmms/";
				String oldName = "cluster_"+thresh.toString()+"_"+id.toString()+".hmm";
				File f1 = new File(path+oldName); //old file1
				File f2 = new File(path+oldName+".serialized"); //old file1
				// File (or directory) with new name
				File f1New = new File(path+code+".hmm"); // new file name
				File f2New = new File(path+code+".hmm.serialized"); // new file name

				if (f1New.exists())
					throw new java.io.IOException("file exists");
				if (f2New.exists())
					throw new java.io.IOException("file exists");
				// Rename file (or directory)
				f1.renameTo(f1New);
				f2.renameTo(f2New);
				System.out.println(code + "--- "+count);
				
			}
			
			rsSc.close();
			pstmGetSc.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
