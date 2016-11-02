package CAMPS.utils;

import java.util.ArrayList;
import java.util.Hashtable;

public class tmsCoresInfo {

	// have to make a data structure in which it is hash within a hash
	// because one cluster has many seqids
	//public int seqId;						// sequence id of protein under view
	public Hashtable<Integer, Seq> seq_obj = new Hashtable<Integer, Seq>();
	// key is the seqId and value is tmhPositions & helix length
	public tmsCoresInfo(){
		this.seq_obj = new Hashtable<Integer, Seq>(); 
	}
	
}
