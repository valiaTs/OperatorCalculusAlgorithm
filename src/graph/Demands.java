package graph;

import java.util.Arrays;

/**
 * Represents the demands of the graph. Source, destination and the constraints.
 * 
 * @author valia
 */

public class Demands {

	private GraphVertex src, dest ;
	private double[] weight ;

	public Demands (GraphVertex s, GraphVertex d, double[] w) {
		src = s;
		dest = d;
		weight = w;
	}
	
	public GraphVertex getSrc() {
		return src;
	}
	
	public GraphVertex getDest() {
		return dest;
	}
	
	public double[] getWeight() {
		return weight;
	}
	
	public String toString() {
		if (this==null)
			return " ";
		else
			return "Demands: [" + getSrc() + " -> " + getDest() + " ]" + Arrays.toString(weight);
	}
	
}
