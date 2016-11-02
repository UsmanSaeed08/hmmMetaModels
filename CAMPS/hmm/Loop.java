package CAMPS.hmm;

import CAMPS.utils.*;
import java.util.*;
import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;

/**
 * This class creates a HMM for a given loop region
 * 
 * @author hartmann
 *
 */
public class Loop extends SuperHMM{
	/**
	 * creates an initial HMM for a given Loop region
	 * 
	 * @param fam Family for which the HMM should be generated
	 * @param number the number of a transmembrane region that follows the loop region to consider
	 * @return the generated HMM
	 */	
	
	public Hmm<ObservationInteger> create(Family fam, int number){
		int loopSize = Global.loopSize;
		int SD = Global.SD;
		Integer max = null;		
		ArrayList<Integer> maxList = new ArrayList<Integer>();
		ArrayList<Protein> proteins = fam.getMembers();	
		for(int i=0; i < proteins.size(); i++){						
			/*
			 * extract sequence before the first TMH, after the last TMH or in between 2 TMH
			 */
			if(proteins.get(i).getNbTMH() < number)continue;
			ArrayList<int[]> tmh = proteins.get(i).getTmh();
			
			int begin = 0;
			int end = proteins.get(i).getSequence().length();
			if(number==0){
				end = tmh.get(number)[0]-1;
			}			
			else{
				begin = tmh.get(number-1)[1];
				if(number < proteins.get(i).getNbTMH()){
					end = tmh.get(number)[0]-1;
				}
			}
			Integer size = new Integer(end-begin);
			if(size==-1)size=0;
//			System.out.println(size);
			maxList.add(size);
		}	
		Collections.sort(maxList);
		double mean = calculateMean(maxList);
		double sd = calculateSD(maxList, mean);
//		System.out.println("Loop Lengths: " + maxList);
//		System.out.println("Mean: " + mean + "\tSD: " + sd);
		// longest loop in sequences
		max = maxList.get(maxList.size()-1);			
		
		int states = loopSize;
		// if longest loop shorter than size, use this loop length, otherwise use size
		if(max < loopSize){
			states = max;
		// if a longer loop has a very conserved loop length, use this loop length
		}
		else if(sd < SD && mean > loopSize){
			states = (int)Math.round(mean - 0.5);
		}	
//		if(states == 0){states = 1;}
		// if shorter loop than size, no globular state is used
		if(states < loopSize){
			fam.addLoopLength(states);
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
					hmm.setAij(i, j, 1.0 /(hmm.nbStates() - i - 1));
				}	
			}		
			return hmm;
		}else{
			states++;
			fam.addLoopLength(states);
			Hmm<ObservationInteger> hmm = new Hmm<ObservationInteger>(states, new OpdfIntegerFactory(20));
			Utils.clear(hmm, states);
			for(int i=0; i< states; i++){
				if(i == 0){
					hmm.setPi(i,1);
				}else{
					hmm.setPi(i, 0);
				}			
				hmm.setOpdf(i, new OpdfInteger(new double[] {0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05}));
				if( i== (states-1)/2){
					hmm.setAij(i, i, 1.0 /(hmm.nbStates() - i));
				}
				for(int j = i+1; j < states; j++){
					if( i== (states-1)/2){
						hmm.setAij(i, j, 1.0 /(hmm.nbStates() - i));
					}
					else{
						hmm.setAij(i, j, 1.0 /(hmm.nbStates() - i - 1));
					}
				}	
			}
			return hmm;
		}
	}
	
	public Hmm<ObservationInteger> train(Hmm<ObservationInteger> initHmm, Family fam, int number){		
//		System.out.println("number" + number);
		ArrayList<Protein> proteins = fam.getMembers();
		List<List<ObservationInteger>> sequences = new ArrayList<List<ObservationInteger>>();		
		try{
//			System.out.println("Groesse: " + proteins.size());
			for(int i=0; i < proteins.size(); i++){						
				/*
				 * extract sequence before the first TMH, after the last TMH or in between 2 TMH
				 */
				if(proteins.get(i).getNbTMH() < number)continue;
				ArrayList<int[]> tmh = proteins.get(i).getTmh();
				int begin = 0;
				int end = proteins.get(i).getSequence().length();
				if(number==0){
					end = tmh.get(number)[0]-1;
				}
				else{
					begin = tmh.get(number-1)[1];
					if(number < proteins.get(i).getNbTMH()){
						end = tmh.get(number)[0]-1;
					}
				}				
				if(end - begin <=1) continue;				
				String line = proteins.get(i).getSequence().substring(begin, end).toUpperCase();				
//				System.out.println(number + "  " + proteins.get(i).getCode() + " " + line);
				ArrayList<ObservationInteger> seq = new ArrayList<ObservationInteger>();				
				
				// Loop Lengths
//				System.out.println(line  + " " + line.length());
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
		Hmm<ObservationInteger> trained;
		
		trained = train(initHmm, sequences);
		trained = addEmissionPseudocounts(trained, sequences.size());
		trained = addTransissionPseudocounts(trained);
		
		return trained;		
	}

	
	
	/**
	 * trains an initial HMM of the loop region
	 * 
	 * @param initHmm the inital HMM
	 * @param fam the family the HMM should be trained for
	 * @param number the number of a transmembrane region that follows the loop region to consider
	 * @return the trained HMM 
	 */
	public Hmm<ObservationInteger> train2(Hmm<ObservationInteger> initHmm, Family fam, int number){
		int loopSize = Global.loopSize;
//		System.out.println("number" + number);
		ArrayList<Protein> proteins = fam.getMembers();
		List<List<ObservationInteger>> sequences = new ArrayList<List<ObservationInteger>>();
		ArrayList<Integer> seqGlobular = new ArrayList<Integer>();
		try{
//			System.out.println("Groesse: " + proteins.size());
			for(int i=0; i < proteins.size(); i++){						
				/*
				 * extract sequence before the first TMH, after the last TMH or in between 2 TMH
				 */
				if(proteins.get(i).getNbTMH() < number)continue;
				ArrayList<int[]> tmh = proteins.get(i).getTmh();
				int begin = 0;
				int end = proteins.get(i).getSequence().length();
				if(number==0){
					end = tmh.get(number)[0]-1;
				}
				else{
					begin = tmh.get(number-1)[1];
					if(number < proteins.get(i).getNbTMH()){
						end = tmh.get(number)[0]-1;
					}
				}				
				if(end - begin <=1) continue;				
				String line = proteins.get(i).getSequence().substring(begin, end).toUpperCase();
//				String line = proteins.get(i).getSequence().substring(0, 180).toUpperCase();
//				System.out.println(number + "  " + proteins.get(i).getCode() + " " + line);
				ArrayList<ObservationInteger> seq = new ArrayList<ObservationInteger>();				
				
				// Loop Laengen
//				System.out.println(line  + " " + line.length());
//				 for loops longer than size-1 the HMM is trained only on the first and last 5 amino acids
				
				if(line.length() > loopSize){
					for(int j = 0; j < loopSize/2; j++){
						int num =  ASMapping.asToInt(line.charAt(j));						
						seq.add(new ObservationInteger(num));
					}
					for(int j = loopSize/2; j < line.length() - loopSize/2; j++){
						int num =  ASMapping.asToInt(line.charAt(j));
						seqGlobular.add(num);
					}
					for(int j = line.length() - loopSize/2; j < line.length(); j++){
						int num =  ASMapping.asToInt(line.charAt(j));
						seq.add(new ObservationInteger(num));
					}					
				}else{
					for(int j = 0; j < line.length(); j++){
						int num =  ASMapping.asToInt(line.charAt(j));						
						seq.add(new ObservationInteger(num));					
					}		
				}
				sequences.add(seq);				
			}
		}catch(Exception e){
			System.err.println(e.toString());
		}
		Hmm<ObservationInteger> trained;
		
		trained = train(initHmm, sequences);
		trained = addEmissionPseudocounts(trained, sequences.size());
		trained = addTransissionPseudocounts(trained);
		
		// insert self transition state into long loop regions
		if(initHmm.nbStates() == loopSize && !seqGlobular.isEmpty()){
			Hmm<ObservationInteger> trainedLoop = new Hmm<ObservationInteger>(loopSize + 1, new OpdfIntegerFactory(20));
			Utils.clear(trainedLoop, loopSize + 1);
			for(int i = 0; i < loopSize/2; i++){
				trainedLoop.setPi(i, trained.getPi(i));
				if(i != loopSize/2 -1){
					for(int j = 0; j< trained.nbStates(); j++){
						trainedLoop.setAij(i, j, trained.getAij(i, j));						
					}
				}
				trainedLoop.setOpdf(i, trained.getOpdf(i));				
			}
			trainedLoop.setAij(loopSize/2 - 1, loopSize/2, 0.95);
			trainedLoop.setAij(loopSize/2 - 1, loopSize/2 + 1, 0.05);
			trainedLoop.setAij(loopSize/2, loopSize/2, 0.95);
			trainedLoop.setAij(loopSize/2, loopSize/2 + 1, 0.05);
			double[] opdf = getGlobularOpdf(seqGlobular);
			OpdfInteger o = new OpdfInteger(opdf);			
			trainedLoop.setOpdf(loopSize/2, o);

			trainedLoop.setPi(loopSize/2,0);
			for(int i = loopSize/2 + 1; i < loopSize + 1; i++){
				trainedLoop.setPi(i, trained.getPi(i-1));
				for(int j = i; j< trained.nbStates(); j++){
					trainedLoop.setAij(i, j+1, trained.getAij(i-1, j));
				}
				trainedLoop.setOpdf(i, trained.getOpdf(i-1));
			}
			trained = trainedLoop;			
		}		
//		System.out.println(trained);
		return trained;		
	}
	private double[] getGlobularOpdf(ArrayList<Integer> seqGlobular){		
		double[] opdf = new double[20];
		for(int i = 0; i< seqGlobular.size(); i++){
			opdf[seqGlobular.get(i)]++;
		}
		for(int i = 0; i < opdf.length; i++){
			opdf[i] = (opdf[i] + 1)/ (seqGlobular.size() + 20);			
		}
		
		return opdf;
	}
	
	private double calculateMean(ArrayList<Integer> loopLengths){
		double sum = 0;
		for(int i : loopLengths){
			sum += i;
		}
		return sum / loopLengths.size();
	}
	
	private double calculateSD(ArrayList<Integer> loopLengths, double mean){
		double dev = 0;
		for(int i : loopLengths){
			dev += (i - mean) * (i - mean);
		}
		dev /= loopLengths.size();
		return Math.sqrt(dev);
	}
}
