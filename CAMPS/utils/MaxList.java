package CAMPS.utils;

public class MaxList implements Comparable<Object>{
	public double max;
	public int cluster;
	public double threshold;
	public MaxList(double max, int cluster, double threshold){
		this.max= max;
		this.cluster=cluster;
		this.threshold=threshold;
	}
	
	public int compareTo(Object b){
		MaxList tmp = (MaxList)b;
		if(tmp.max > this.max){
			return 1;
		}
		else if(tmp.max < this.max){
			return -1;
		}
		else return 0;
	}
}
