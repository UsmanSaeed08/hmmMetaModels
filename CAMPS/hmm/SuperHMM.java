package CAMPS.hmm;

import CAMPS.utils.*;
import java.util.*;
import be.ac.ulg.montefiore.run.jahmm.*;
import be.ac.ulg.montefiore.run.jahmm.learn.*;

/**
 * this class is the super class for all HMMs
 * 
 * @author hartmann
 *
 */
public class SuperHMM {
	/**
	 * trains an HMM to fit to given sequences
	 * 
	 * @param initHMM the HMM to be trained
	 * @param sequences the sequences to which the parameters should be fitted 
	 */
	public Hmm<ObservationInteger> train(Hmm<ObservationInteger> initHmm, List<List<ObservationInteger>> sequences){
		BaumWelchScaledLearner bwl = new BaumWelchScaledLearner();
		ArrayList<Integer> silentStates = new ArrayList<Integer>(); 
//		BaumWelchLearner bwl = new BaumWelchLearner();		
		Hmm<ObservationInteger> learntHmm = bwl.learn(initHmm, sequences, silentStates);
		
		return learntHmm;
	}
	
	public Hmm<ObservationInteger> trainSilent(Hmm<ObservationInteger> initHmm, List<List<ObservationInteger>> sequences){
		BaumWelchScaledLearner bwl = new BaumWelchScaledLearner();
		int states = (initHmm.nbStates() + 2) / 2;
		ArrayList<Integer> silentStates = new ArrayList<Integer>();
		for(int i = states; i < initHmm.nbStates(); i++){
			silentStates.add(new Integer(i));
		}
//		BaumWelchLearner bwl = new BaumWelchLearner();
		Hmm<ObservationInteger> learntHmm = bwl.learn(initHmm, sequences, silentStates);
		
		return learntHmm;
	}
		
	public Hmm<ObservationInteger> addEmissionPseudocounts(Hmm<ObservationInteger> trained, int nbSequences){		
		double[] density = trained.getDensity();
		
		// if a state emits more than 1000 symbols it is not necessary to add pseudocounts
		for(int i= 0; i < trained.nbStates(); i++){
//			if(trained.nbStates() == 11 && i == 5){
//				System.out.println(density);
//			}
			Opdf<ObservationInteger> observation= trained.getOpdf(i);	
//			System.out.println(density[i]);
			// if a state emits more than 200 symbols it is not necessary to add pseudocounts
			if(density[i] < 100){
				observation.addPseudocounts(density[i]);
			}
			trained.setOpdf(i, observation);
		}
		return trained;
		
	}
	public Hmm<ObservationInteger> addTransissionPseudocounts(Hmm<ObservationInteger> trained){
		double eta = Global.transitionPseudocount;
		for(int i=0; i< trained.nbStates(); i++){					
			for(int j = i+1; j < trained.nbStates(); j++){
				trained.setAij(i, j, (trained.getAij(i, j) + eta) /((trained.nbStates()-i-1)*eta + 1));
			}
		}		
		return trained;
	}
}
