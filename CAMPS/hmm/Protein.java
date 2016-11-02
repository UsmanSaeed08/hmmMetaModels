package CAMPS.hmm;

import java.util.ArrayList;

/**
 * This class holds information about a single transmembrane protein
 * 
 * @author hartmann
 */
public class Protein {
	private ArrayList<int[]> tmh;	// begin, end
	private String sequence;
	private String code;
	private String description;	
	
	/**
	 * Creates a new Protein
	 * 
	 * @param tmh start and end positions of the transmembrane helices in the protein
	 * @param sequence sequence of the protein
	 * @param code database code of the protein
	 * @param description description of the protein
	 */
	public Protein(ArrayList<int[]> tmh, String sequence, String code, String description){
		this.tmh = tmh;
		this.sequence = sequence;
		this.code = code;
		this.description = description;
	}
	
	/**
	 * Returns start and end positions of the transmembrane helices of the protein 
	 * 
	 * @return start and end position of the transmembraen helices
	 */
	public ArrayList<int[]> getTmh(){
		return tmh;
	}
	
	/**
	 * Returns the sequence of the protein
	 * 
	 * @return sequence of the protein
	 */
	public String getSequence(){
		return sequence;
	}
	
	/**
	 * Returns the number of transmembrane helices, should be the same as Family.getNumberTMH()
	 * 
	 * @return number of transmembrane helices
	 */
	public int getNbTMH(){
		return tmh.size();
	}
	
	/**
	 * Returns the database code of the protein
	 * 
	 * @return database code of the protein
	 */
	public String getCode(){
		return code;
	}
	
	/**
	 * Returns the description of the protein
	 * 
	 * @return description of the protein
	 */
	public String getDescription(){
		return description;
	}
}
