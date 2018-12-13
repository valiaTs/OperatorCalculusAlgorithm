package graph;

import java.io.File;
import java.io.IOException;

/**
 * Test class for the graph package.
 * 
 * @author valia
 */

public class TestGraph {

	int weightnumber;
	
	private static void testWithFile(String filename, String demandsfile) throws IOException {
		System.out.println(" ******** " + filename);
		System.out.println(" ******** " + demandsfile);
		System.out.println("\n\n");
		OrientedMultiWeightedGraph g = new OrientedMultiWeightedGraph(1);
		g.loadFromFile(new File(filename));
		g.loadDemandsFromFile(new File(demandsfile));
		System.out.println(g.toString());
		System.out.println("\n\n");
	}
	
	public static void main(String args[]) {
		try {
			testWithFile("simpleGraph.txt", "demands.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
