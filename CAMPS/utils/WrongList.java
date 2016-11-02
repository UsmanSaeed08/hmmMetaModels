package CAMPS.utils;
import java.util.*;

public class WrongList implements Comparable<Object>{
	private String name;
	private ArrayList<String[]> wrongList;
	
	public WrongList(String name){
		this.name = name;
		this.wrongList = new ArrayList<String[]>();
	}
	
	public void addData(String[] data){
		this.wrongList.add(data);
	}
	
	public String getName(){
		return name;
	}
	
	public ArrayList<String[]> getList(){
		return wrongList;
	}
	
	
	
	public int compareTo(Object b){
		WrongList tmp = (WrongList)b;
		return (tmp.getName().compareTo(this.name));			
	}
}
