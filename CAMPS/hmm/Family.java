package CAMPS.hmm;

import java.util.*;

/**
 * This class holds all Proteins of a given Cluster with a specific Threshold. Such a Cluster is called a Family
 * 
 * @author hartmann
 */
public class Family {
	private ArrayList<Protein> member;
	private String name;
	private double threshold;
	private ArrayList<Integer> helixLength;
	private ArrayList<Integer> loopLength;
	
	/**
	 * Builds a new Family
	 * 
	 * @param name name of the Family (e.g. cluster_id)
	 * @param threshold cluster_threshold
	 */
	public Family(String name, double threshold){
		this.name = name;
		this.threshold = threshold;
		member = new ArrayList<Protein>();
		loopLength = new ArrayList<Integer>();
	}
	
	/**
	 * Sets the <i>helixLength</i> value associated with the Family. 
	 * Every protein in a family has roughly the same number of transmembrane helices. 
	 * The lengths of the consensus helical regions is stored in the ArrayList helixLength.
	 * 
	 * @param helixLength List, holding the length of the transmembrane helices in this Family.
	 */
	public void setHelixLength(ArrayList<Integer> helixLength){
		this.helixLength = helixLength;		
	}
	
	/**
	 * Returns the number of transmembrane helices
	 * 
	 * @return the number of transmembrane helices associated with the family
	 */
	public int getNumberTMH(){
		return helixLength.size();
	}
	
	/**
	 * Returns information about helix lengths in the family
	 * 
	 * @return an ArrayList with information about the helix lengths in the family
	 */
	public ArrayList<Integer>gethelixLength(){
		return helixLength;
	}
	
	/**
	 * Returns the threshold used to get the family members
	 * 
	 * @return the threshold used to get the family members
	 */
	public double getThreshold(){
		return threshold;
	}
	
	/**
	 * Returns the name of the family
	 * 
	 * @return the name of the family
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * adds a protein to the family
	 * 
	 * @param p a Protein which should be added to the family
	 */
	public void addProtein(Protein p){
		member.add(p);
	}
	
	/**
	 * Returns the members of family (the proteins added before)
	 * 
	 * @return an ArrayList with the proteins added before
	 */
	public ArrayList<Protein> getMembers(){
		return member;
	}
	
	/**
	 * Returns how many members belong to the family
	 * 
	 * @return numberOfMembers belonging to the family
	 */
	public int numberOfMembers(){
		return member.size();
	}	
	
	public void addLoopLength(int length){
		loopLength.add(length);
	}
	public ArrayList<Integer> getLoopLength(){
		return loopLength;
	}
}
