package CAMPS.hmm;

import CAMPS.utils.*;
import java.util.*;
import be.ac.ulg.montefiore.run.jahmm.*;

/**
 * This class creates the final HMM model out of different componenent HMMs.
 * A component HMM could be a smaller HMM trained on a specific tranmembrane region or a specific loop
 * 
 * 
 * @author hartmann
 *
 */
public class FinalHMM extends SuperHMM{
	ArrayList<Hmm<ObservationInteger>> components;	
	Family fam;
	/**
	 * builds a new class 
	 * 
	 * @param components HMMs that build up the finalHMM model
	 */
	public FinalHMM(ArrayList<Hmm<ObservationInteger>> components, Family fam){
		this.components = components;
		this.fam = fam;
	}	
	
	/**
	 * builds the new HMM model out of the component HMMs.
	 * 
	 * @return the final HMM model
	 */
	public Hmm<ObservationInteger> create(){
		int states = 0;
		for(int i = 0; i < components.size(); i++){
			Hmm<ObservationInteger> tmp = components.get(i);			
			states += tmp.nbStates();
		}
		Hmm<ObservationInteger> hmm = new Hmm<ObservationInteger>(states, new OpdfIntegerFactory(20));
		Utils.clear(hmm,states);
		for(int i = 0; i<states; i++) 
			hmm.setPi(i,0);
		hmm.setPi(0,1);
		System.out.println("States: " + states);
		int currentHMM = 0;
		int curStateNb = 0;
		
		for(int i=0; i< states; i++){			
			if(components.get(currentHMM).nbStates() == curStateNb){
				currentHMM++;
				curStateNb = 0;
			}		
			// copy observations and transition probabilites from component HMMs into final HMM
			if(curStateNb < components.get(currentHMM).nbStates()-1){
				hmm.setOpdf(i, components.get(currentHMM).getOpdf(curStateNb));
				for(int j = curStateNb; j < components.get(currentHMM).nbStates(); j++){
					double prob = components.get(currentHMM).getAij(curStateNb, j);
					hmm.setAij(i, i+j-curStateNb, prob);
				}				
			}
			else if(i < states-1){
				hmm.setOpdf(i, components.get(currentHMM).getOpdf(curStateNb));
				// Helix-Loop transition always one
				if(currentHMM % 2 == 1){
					hmm.setAij(i,i+1,1);
				}// Loop transition to helix after next helix possible
				else{					
					double missedHelix = getHelixMissing(fam, currentHMM/2);
					if(1 >= components.size() - currentHMM-2 || currentHMM == 0){
						hmm.setAij(i,i+1,1.0);						
					}else{
						hmm.setAij(i,i+1,1-missedHelix);						
					}
					
					int k = 0;
//					System.out.println(currentHMM + " " + components.size());
					if(currentHMM < components.size() - 3){
						k += components.get(currentHMM+1).nbStates() + components.get(currentHMM+2).nbStates();						
						if(currentHMM == 0){							
							hmm.setPi(0,1-missedHelix);
							hmm.setPi(k+1, missedHelix);
						}else{
							hmm.setAij(i, i+k+1, missedHelix);
						}
					}
					
				}			
			}else{
				hmm.setOpdf(i, components.get(currentHMM).getOpdf(curStateNb));				
			}
			curStateNb++;
		}
		//add pseudocounts to transitions to successive helices	
		double eta = Global.helixPseudocount;
		currentHMM = 0;
		curStateNb = 0;
		for(int i=0; i< states; i++){		
			double nbTrans = (components.size()-currentHMM-3)/2 + 1;			
			if(components.get(currentHMM).nbStates() == curStateNb){
				currentHMM++;
				curStateNb = 0;
			}
			
			if(curStateNb >= components.get(currentHMM).nbStates()-1 && i < states -1 && currentHMM % 2 != 1){
				int k = 0;
//				System.out.println("nbTrans:" + nbTrans);
				if(1<components.size() - currentHMM-2){
//					System.out.print("oldTrans1: " + hmm.getAij(i,i+1));
					hmm.setAij(i,i+1,(hmm.getAij(i,i+1) + eta)/(nbTrans*eta+1));
//					System.out.println("\tnewTrans1: " + hmm.getAij(i,i+1));					
				}
				for(int j = 1; j < components.size() - currentHMM-2; j+=2){					
					k += components.get(currentHMM + j).nbStates() + components.get(currentHMM+j+1).nbStates();
//					System.out.print("oldTrans: " + hmm.getAij(i,i+k+1));
					hmm.setAij(i, i+k+1, (hmm.getAij(i,i+k+1) + eta)/(nbTrans*eta+1));
//					System.out.println("\tnewTrans: " + hmm.getAij(i,i+k+1));
				}
			}
			curStateNb++;
		}
		return hmm;		
	}		
	
	/*
	 * calculates fraction of helix helixNb which are not present in the dataset
	 */	
	private double getHelixMissing(Family fam, int helixNb){		
		ArrayList<Protein> prots = fam.getMembers();
		double missing = 0;
		for(Protein prot : prots){
			if(prot.getCode().equals("5674038")){
				System.out.println("bla");
			}
			ArrayList<int[]> tmh = prot.getTmh();
			// if added below ****
			if (tmh.size()-1>=helixNb){
				int[] th = tmh.get(helixNb);
//				System.out.println(th[0] + " " + th[1]);
				if(th[0] > th[1] - 2){
					missing++;
					System.out.println("Helix " + helixNb + " is missing in " + prot.getCode());
				}
			}
			
		}
		return missing / prots.size();
	}
	
	public Hmm<ObservationInteger> train(Hmm<ObservationInteger> initHmm, Family fam){
		ArrayList<Protein> proteins = fam.getMembers();
		List<List<ObservationInteger>> sequences = new ArrayList<List<ObservationInteger>>();
		try{			
			for(int i=0; i < proteins.size(); i++){				
				String line = proteins.get(i).getSequence().toUpperCase();				
				ArrayList<ObservationInteger> seq = new ArrayList<ObservationInteger>();
				for(int j = 0; j < line.length(); j++){
					int num =  ASMapping.asToInt(line.charAt(j));
					seq.add(new ObservationInteger(num));					
				}				
				sequences.add(seq);		
			}
		}catch(Exception e){
			System.err.println(e.toString());
		}			
		return train(initHmm, sequences);		
	}
	
	
	public Hmm<ObservationInteger> train(Hmm<ObservationInteger> initHmm, Family fam, int number){
		ArrayList<Protein> proteins = fam.getMembers();
		List<List<ObservationInteger>> sequences = new ArrayList<List<ObservationInteger>>();
		try{
//			System.out.println("Groesse: " + proteins.size());
			for(int i=0; i < proteins.size(); i++){
//			for(int i=0; i < 1; i++){
				/*
				 * extract block sequences 
				 */				
				ArrayList<int[]> tmh = proteins.get(i).getTmh();
				int begin = 0;
				if(number != 3){
					begin = tmh.get(number-4)[1];
				}				
				int end = proteins.get(i).getSequence().length();				
				if(number < fam.getNumberTMH()){
					end = tmh.get(number-1)[1];
				}
				if(end-begin < 5)continue;
//				System.out.println(begin + "\t" + end + "\t" + proteins.get(i).getCode());
				String line = proteins.get(i).getSequence().substring(begin, end).toUpperCase();
//				System.out.println(number + "  " +fam.getNumberTMH() + "  " +proteins.get(i).getCode() + " " + line);
				ArrayList<ObservationInteger> seq = new ArrayList<ObservationInteger>();
				for(int j = 0; j < line.length(); j++){
					int num =  ASMapping.asToInt(line.charAt(j));
					seq.add(new ObservationInteger(num));						
				}				
				sequences.add(seq);		
			}
		}catch(Exception e){
			System.err.println(e.toString());
		}			
		return train(initHmm, sequences);		
	}
	
}
