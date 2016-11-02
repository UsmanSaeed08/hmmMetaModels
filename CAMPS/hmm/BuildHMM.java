package CAMPS.hmm;

import java.util.ArrayList;
import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;

/**
 * This class creates the distinct component HMMs, trains them to the sequences and builds the final 
 * HMM out of the different components.   
 * 
 * @author hartmann
 *
 */
public class BuildHMM {
	
	boolean global = false;
	/**
	 * Method to start the building process
	 * 
	 * @param fam Family for which the HMM should be build
	 * @return the final HMM trained on the Family
	 */
	public Hmm<ObservationInteger> doIt(Family fam){
		
		ArrayList<Hmm<ObservationInteger>> components = new ArrayList<Hmm<ObservationInteger>>();
		//System.out.println(fam.getNumberTMH());
		for(int i=0; i<fam.getNumberTMH(); i++){
//		for(int i=3; i<5; i++){
//			if(fam.gethelixLength().get(i) > 0){
				//System.out.println("Helix " + i);
				components.add(buildLoopHMM(fam, i));
				components.add(buildTMHelixHMM(fam, i));
//			}
		}		
		components.add(buildLoopHMM(fam, fam.getNumberTMH()));			
		Hmm<ObservationInteger> finalHMM = buildFinalHMM(components, fam);		
		
		return finalHMM;		
	}

	private Hmm<ObservationInteger> buildTMHelixHMM(Family fam, int number){
		TM tmHelix = new TM();		
//		Hmm<ObservationInteger> initHmm = tmHelix.createSilent(fam.gethelixLength().get(number));
//		Hmm<ObservationInteger> learntHmm = tmHelix.trainSilent(initHmm, fam, number);
		Hmm<ObservationInteger> initHmm = tmHelix.create(fam.gethelixLength().get(number));
		Hmm<ObservationInteger> learntHmm = tmHelix.train(initHmm, fam, number);
		return learntHmm;
	}
	
	private Hmm<ObservationInteger> buildLoopHMM(Family fam, int number){
		Loop loop = new Loop();
		Hmm<ObservationInteger> initHmm = loop.create(fam, number);	
		if(initHmm.nbStates() == 1) return initHmm;
		Hmm<ObservationInteger> learntHmm = loop.train(initHmm, fam, number);		
//		System.out.println(learntHmm);
		return learntHmm;
	}
	
	private Hmm<ObservationInteger> buildFinalHMM(ArrayList<Hmm<ObservationInteger>> components, Family fam){
		FinalHMM finalHMM = new FinalHMM(components, fam);
		Hmm<ObservationInteger> initHmm = finalHMM.create();	
		if(global){
			Hmm<ObservationInteger> learntHmm = finalHMM.train(initHmm, fam);
			return learntHmm;
		}else{
			return initHmm;
		}				
	}
}
