package CAMPS.utils;

import java.util.ArrayList;
import java.util.Hashtable;

public class ProteinSeq {

	// have to make a data structure in which it is hash within a hash
	// because one cluster has many seqids
	//public int seqId;						// sequence id of protein under view
	public Hashtable<Integer, String> seq_obj = new Hashtable<Integer, String>();
	// key is the seqId and value is tmhPositions & helix length
	public ProteinSeq(){
		this.seq_obj = new Hashtable<Integer, String>(); 
	}
	
}
