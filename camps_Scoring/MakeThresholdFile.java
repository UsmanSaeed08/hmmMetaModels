package camps_Scoring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import CAMPS.utils.DBAdaptor;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Median;



public class MakeThresholdFile {

	/**
	 * @param args
	 */

	private static final Connection connection = DBAdaptor.getConnection("CAMPS4");
	private static ArrayList<String> codes = new ArrayList<String>(); // all codes in sc	
	private static ArrayList<Double> cutOffs = new ArrayList<Double>();

	private static double CutOffforThisCluster;
	private static int TP = 0;
	private static int TN = 0;
	private static int Total = 275189;

	private static Double Total_Specificity = 0d;
	private static Double Total_Sensitivity = 0d;
	private static Double Total_Accuracy = 0d;
	private static Double Total_Clusters = 0d;

	private static boolean goingGood = true;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//System.out.print("Check\n");
		String path = "/home/users/saeed/scores/";
		//String path = "F:/Scratch/campsWebsite/";
		// get SC codes
		//setCutOffValues();
		System.out.println("#Code\t"+"Threshold\t"+"Accuracy\t"+"Sensitivity\t"+"Specificity\t"+"Mean Members\t"+"Median Members\t"+"SD Members\t"
		+"Mean NONMembers\t"+"Median NONMembers\t"+"SD NONMembers\t");
		GetSCclusters();
		Run(path);
	}
	private static double[] convertDoubles(List<Double> doubles)
	{
		int i = 0;
		double[] ret = new double[doubles.size()];
		Iterator<Double> iterator = doubles.iterator();
		while(iterator.hasNext())
		{
			ret[i] = iterator.next().doubleValue();
			i++;
		}
		return ret;
	}

	private static void Run(String p) {
		// TODO Auto-generated method stub
		try{
			DecimalFormat df = new DecimalFormat("#.####");
			df.setRoundingMode(RoundingMode.CEILING);
			setCutOffValues();
			for(int i =0;i<=codes.size()-1;i++){
				//CMSC1520_members.scores
				//CMSC1520_nonmembers.scores
				String clus = codes.get(i);
				String members_file = p+clus+"_members.scores";
				String NoNmembers_file = p+clus+"_nonmembers.scores";

				Double member_mean =0d;
				Double NONmember_mean =0d;

				Double member_median =0d;
				Double NONmember_median =0d;

				Double member_SD =0d;
				Double NONmember_SD =0d;

				// get memberScore
				ArrayList<Double> memberScores = getScores(members_file);
				ArrayList<Double> NONmemberScores = getScores(NoNmembers_file);
				if(memberScores.size()>0){
					double[] ms = convertDoubles(memberScores);
					double[] nms = convertDoubles(NONmemberScores);

					Mean objMean = new Mean();
					member_mean = objMean.evaluate(ms,0,memberScores.size());
					NONmember_mean = objMean.evaluate(nms,0,NONmemberScores.size());

					Median objMedian = new Median();
					member_median = objMedian.evaluate(ms,0,memberScores.size());
					NONmember_median = objMedian.evaluate(nms,0,NONmemberScores.size());

					StandardDeviation objSD = new StandardDeviation();
					member_SD = objSD.evaluate(ms,0,memberScores.size());
					NONmember_SD = objSD.evaluate(nms,0,NONmemberScores.size());

					Double specificity = calculateSpecificity(NONmemberScores); // returns the specficity over at-least 90% 
					//& sets the CutOffforThisCluster i.e. the threshold score for this cluster
					Double sensitivity = calculateSensitivity(memberScores,CutOffforThisCluster); // returns the found sensitivity for this CutOff

					if(goingGood){
						Double accuracy = (double)TP + (double)TN;
						accuracy = (accuracy/(double)Total)*100d;
						//if(!Double.isNaN(sensitivity) && !Double.isNaN(specificity) && !Double.isNaN(accuracy) && CutOffforThisCluster>-3d){
						if(!Double.isNaN(sensitivity) && !Double.isNaN(specificity) && !Double.isNaN(accuracy)){
							SetVariablesToGetMean(specificity,sensitivity,accuracy);
						}

						System.out.println(clus+"\t"+df.format(CutOffforThisCluster)+"\t"+df.format(accuracy)+"\t"+df.format(sensitivity)+"\t"+df.format(specificity)
								+"\t"+df.format(member_mean)+"\t"+df.format(member_median)+"\t"+
								df.format(member_SD)+"\t"+df.format(NONmember_mean)+"\t"+df.format(NONmember_median)+"\t"+df.format(NONmember_SD));
					}
					else{
						System.out.println(clus+"\t"+"-3"+"\t"+"-"+"\t"+"-"+"\t"+"-"
								+"\t"+"-"+"\t"+"-"+"\t"+
								"-"+"\t"+"-"+"\t"+"-"+"\t"+"-");
						goingGood = true;
					}
				}
				else{ // due to NaN -  direct correct and min threshold of -3
					// final thresh is -3
					System.out.println(clus+"\t"+"-3"+"\t"+"-"+"\t"+"-"+"\t"+"-"
							+"\t"+"-"+"\t"+"-"+"\t"+
							"-"+"\t"+"-"+"\t"+"-"+"\t"+"-");
					// report
				}

			}
			GetMeanSensitivityEtc();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}



	private static void GetMeanSensitivityEtc() {
		// TODO Auto-generated method stub
		Total_Specificity = Total_Specificity/Total_Clusters;
		Total_Sensitivity = Total_Sensitivity/Total_Clusters;
		Total_Accuracy = Total_Accuracy/Total_Clusters;
		//System.out.println("Mean Specificity: "+Total_Specificity);
		//System.out.println("Mean Sensitivity: "+Total_Sensitivity);
		//System.out.println("Mean Accuracy: "+Total_Accuracy);
	}
	private static void SetVariablesToGetMean(Double sp,
			Double se, Double acc) {
		// TODO Auto-generated method stub
		Total_Sensitivity = Total_Sensitivity + se;
		Total_Specificity = Total_Specificity + sp;
		Total_Accuracy = Total_Accuracy + acc;

		Total_Clusters = Total_Clusters + 1; 
	}
	private static Double calculateSensitivity(ArrayList<Double> memberScores,
			double cutOffforThisCluster2) {
		// TODO Auto-generated method stub
		int count = 0;
		//cutOffforThisCluster2 = -3d;
		Double sensitivity = 0d;
		for(int i =0;i<=memberScores.size()-1;i++){
			if(memberScores.get(i) >= cutOffforThisCluster2){ // i.e. the member score is greater than cutoff 
				//-- closer to zero is greater than farther from zero
				count++;	//T.P
			}
		}
		if(count>0){
			double temp =(double)count/(double)(memberScores.size()); 
			sensitivity = temp*100d;
			TP = count;
			return sensitivity;
		}
		else{
			return 300d;
		}
	}
	private static void setCutOffValues() {
		// TODO Auto-generated method stub
		for(double i = -1d; i>= -3.1d;){
			cutOffs.add(i);
			i = i - 0.01;
		}
		/*
		for(int x = 0;x<=cutOffs.size()-1;x++){
			System.out.println(cutOffs.get(x));
		}*/
	}
	private static Double calculateSpecificity(ArrayList<Double> nONmemberScores) {
		// TODO Auto-generated method stub
		/*
		 * Based on different cut offs from -1.0 to -3.0 checks the specifictiy and return where it is over 90% 
		 * and sets that score cut off for this cluster  
		 */
		//for each cut off score - get the sensitivity
		// specificity = Number of non members that score below cut off

		for(int i =0;i<=cutOffs.size()-1;i++){
			Double currentCutOff = cutOffs.get(i);
			int numberofNoNmembers = nONmemberScores.size()-1;
			int proteinsScoringBelowCutOff = 0;
			Double CurrentSpecificity = 0d;

			for(int j = 0; j<= nONmemberScores.size()-1;j++){
				if(nONmemberScores.get(j) >= currentCutOff){  //*******************??????????????????????************
					// basically counting false positives here....
					// false positives would score more closer to zero ... and thus should be a value > cutoff
					// i.e. if cut off is -2.5 and score is -2.2 for non member.. then the non member is a false positive
					proteinsScoringBelowCutOff ++; // true negative
				}
			}
			if(proteinsScoringBelowCutOff > 0 ){
				double temp = (double)proteinsScoringBelowCutOff/(double)numberofNoNmembers;
				//temp = 1 - temp;
				CurrentSpecificity =  temp*100;
				if(CurrentSpecificity >=90){
					// report the score cutOff and break
					CutOffforThisCluster = currentCutOff;
					TN = proteinsScoringBelowCutOff;
					return CurrentSpecificity;
				}}
		}
		CutOffforThisCluster = -3;
		goingGood = false;
		return 0d;
	}
	private static ArrayList<Double> getScores(String file) {
		// TODO Auto-generated method stub
		ArrayList<Double> scores = new ArrayList<Double>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(file)));
			String l = "";
			while((l=br.readLine())!=null){
				if(!l.isEmpty()){
					String[] p = l.split("\t"); 
					String d = p[0].trim();
					// what if D is NaN
					if(d!="NaN"){
						Double score = Double.parseDouble(d);
						if(!Double.isNaN(score)){
							scores.add(score);
						}
					}
				}
			}
			br.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return scores;
	}

	private static void GetSCclusters() {
		// TODO Auto-generated method stub
		try{
			//System.out.println("Populating sc clusters");
			//System.out.flush();
			//codes.add("CMSC1329");/*
			PreparedStatement pstmGetSc = connection.prepareStatement("SELECT code from cp_clusters where type=\"sc_cluster\" order by cluster_id" );
			ResultSet rsSc = pstmGetSc.executeQuery();
			while(rsSc.next()){
				String code = rsSc.getString(1);
				codes.add(code);
			}
			rsSc.close();
			pstmGetSc.close();//*/
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
