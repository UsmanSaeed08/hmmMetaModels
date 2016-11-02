package CAMPS.hmm;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;

public class SecOrderHMM {

		private Hmm<ObservationInteger> hmm;
		private String[] secOrder;
		private String cluster_id;
		private double threshold;
		
		public SecOrderHMM(){
			
		}
		
		public SecOrderHMM(Hmm<ObservationInteger> hmm, String[] secOrder, String cluster_id){
			this.hmm = hmm;
			this.secOrder = secOrder;
			this.cluster_id = cluster_id;
		}
	
		public void setThreshold(double threshold){
			this.threshold = threshold;
		}
		public void setClusterId(String cluster_id){
			this.cluster_id = cluster_id;
		}	
		public double getThreshold(){
			return threshold;
		}
		public Hmm<ObservationInteger> getHmm(){
			return hmm;
		}
		public String[] getSecOrder(){
			return secOrder;
		}
		public void setSecOrder(String[] s){
			secOrder = s;
		}
		public String getClusterId(){
			return cluster_id;
		}
}
