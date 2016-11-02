package CAMPS.hmm;

import CAMPS.utils.*;
import java.util.*;
import be.ac.ulg.montefiore.run.jahmm.*;

/**
 * This class creates a HMM for a given transmembrane region
 * 
 * @author hartmann
 *
 */
public class TM extends SuperHMM{	
	/**
	 * creates an initial HMM for a given transmembrane region
	 * 
	 * @param fam Family for which the HMM should be generated
	 * @param number the number of the transmembrane region that should be considered
	 * @return the generated HMM
	 */
	public Hmm<ObservationInteger> create(int states){		
		Hmm<ObservationInteger> hmm = new Hmm<ObservationInteger>(states, new OpdfIntegerFactory(20));
		Utils.clear(hmm,states);
		
		for(int i=0; i< states; i++){
			if(i == 0){
				hmm.setPi(i,1);
			}else{
				hmm.setPi(i, 0);
			}
			
			hmm.setOpdf(i, new OpdfInteger(new double[] {0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05}));			
						
			for(int j = i+1; j < hmm.nbStates(); j++){
				hmm.setAij(i, j, 1.0 /(hmm.nbStates() - i));
			}
		}		
		return hmm;		
	}
	
	public Hmm<ObservationInteger> createSilent(int states){		
		Hmm<ObservationInteger> hmm = new Hmm<ObservationInteger>(2* states - 2 , new OpdfIntegerFactory(20));
		Utils.clear(hmm,states);
		
		for(int i=0; i< states; i++){
			if(i == 0){
				hmm.setPi(i,1);
				hmm.setAij(i, i+1, 0.9);
				hmm.setAij(i, i+states, 0.1);
			}else{
				hmm.setPi(i, 0);
				if(i < states-2){
					hmm.setAij(i, i+1, 0.9);
					hmm.setAij(i, i+states, 0.1);
					hmm.setAij(i+states-1, i+1, 0.5);
					hmm.setAij(i+states-1, i+states, 0.5);
				}else if(i == states-2){
					hmm.setAij(i, i+1, 1);
					hmm.setAij(i+states-1, i+1, 1);
				}
			}			
			hmm.setOpdf(i, new OpdfInteger(new double[] {0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05}));			
		}		
		return hmm;		
	}
	
	/** trains the initial HMM of the transmembrane region
	 * 
	 * @param initHmm the inital HMM
	 * @param fam the family the HMM should be trained for
	 * @param number the number of the transmembrane region that should be considered
	 * @return the trained HMM 
	 */
	public Hmm<ObservationInteger> train(Hmm<ObservationInteger> initHmm, Family fam, int number){
		ArrayList<Protein> proteins = fam.getMembers();
		List<List<ObservationInteger>> sequences = new ArrayList<List<ObservationInteger>>();
		ArrayList<Integer> maxList = new ArrayList<Integer>();
		try{			
			for(int i=0; i < proteins.size(); i++){
				/*
				 * extract TMH Helix sequences 
				 */			
				if(proteins.get(i).getNbTMH()-1 < number)continue;
				ArrayList<int[]> tmh = proteins.get(i).getTmh();
				int begin = tmh.get(number)[0];
				if(begin > 0){begin--;}
				int end = tmh.get(number)[1];
				if(end-begin < 2)continue;
//				System.out.println("TM " + number + "  " + begin + "\t" + end + "\t" + proteins.get(i).getCode());
				String line = proteins.get(i).getSequence().substring(begin, end).toUpperCase();
				
//				System.out.println(line + " " + line.length());
//				if(proteins.get(i).getCode().equals("aad_gi_50083511")){
//					System.out.println(number + "  " + proteins.get(i).getCode() + " " + line);
//				}
				maxList.add(end-begin); 
				ArrayList<ObservationInteger> seq = new ArrayList<ObservationInteger>();
				try{
					for(int j = 0; j < line.length(); j++){
						int num =  ASMapping.asToInt(line.charAt(j));
						seq.add(new ObservationInteger(num));					
					}
					sequences.add(seq);				
				}catch(Exception e){
					System.err.println(e.toString());
				}				
			}
		}catch(Exception e){
			System.err.println(e.toString());
		}		
		Hmm<ObservationInteger> trained = train(initHmm, sequences);
		Collections.sort(maxList);
		
//		System.out.println("helix lengths: " + maxList);
		
		trained = addEmissionPseudocounts(trained, sequences.size());
		trained = addTransissionPseudocounts(trained);
		return trained;		
	}
	
	public Hmm<ObservationInteger> trainSilent(Hmm<ObservationInteger> initHmm, Family fam, int number){
		ArrayList<Protein> proteins = fam.getMembers();
		List<List<ObservationInteger>> sequences = new ArrayList<List<ObservationInteger>>();
		try{
//			System.out.println("Groesse: " + proteins.size());
			for(int i=0; i < proteins.size(); i++){
				/*
				 * extract TMH Helix sequences 
				 */				
				ArrayList<int[]> tmh = proteins.get(i).getTmh();
				int begin = tmh.get(number)[0];
				if(begin > 0){begin--;}
				int end = tmh.get(number)[1];
				if(end-begin < 5)continue;
//				System.out.println("TM " + number + "  " + begin + "\t" + end + "\t" + proteins.get(i).getCode());
				String line = proteins.get(i).getSequence().substring(begin, end).toUpperCase();
//				if(proteins.get(i).getCode().equals("aad_gi_50083511")){
//					System.out.println(number + "  " + proteins.get(i).getCode() + " " + line);
//				}
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
//		System.out.println(train(initHmm, sequences));
//		Hmm<ObservationInteger> trained = train(initHmm, sequences);
		Hmm<ObservationInteger> trained = trainSilent(initHmm, sequences);
		
		trained = addEmissionPseudocounts(trained, sequences.size());
		trained = addTransissionPseudocounts(trained);
		return trained;		
	}
}
