package opcalc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import graph.Demands;
import graph.OrientedMultiWeightedGraph;
import psimatrix.PsiMatrix;


/**
 * Test class for path computation (via operator calculus) related code.
 * 
 * @author valia
 */

public class TestPathAlgorithm {

private static void testWithFile(String filename, String filename2) throws IOException {
		
		
		System.out.println(" ******** " + filename);
		System.out.println("\n\n");
		OrientedMultiWeightedGraph g = new OrientedMultiWeightedGraph(1);
		g.loadFromFile(new File(filename));
		ArrayList<Demands> demands = g.loadDemandsFromFile(new File(filename2));
		System.out.println(g.toString());
		PsiMatrix psi = PsiMatrix.computePsiMatrixForGraph(g);
		System.out.println(psi.toString());
		for (Demands demand : demands){
			OfflinePathAlgorithm algo = new OfflinePathAlgorithm(psi, demand);
			algo.performComputation();
		}
	}

	public static void main(String[] args) {
		try {
			testWithFile("simpleGraph.txt", "demands.txt");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
