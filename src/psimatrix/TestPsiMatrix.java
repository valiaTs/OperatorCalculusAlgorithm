package psimatrix;

import java.io.File;
import java.io.IOException;

import graph.OrientedMultiWeightedGraph;


/**
 * Test class for psi-matrix related code.
 * 
 * @author valia
 */


public class TestPsiMatrix {

	private static void testWithFile(String filename) throws IOException {
		System.out.println(" ******** " + filename);
		System.out.println("\n\n");
		OrientedMultiWeightedGraph g = new OrientedMultiWeightedGraph(1);
		g.loadFromFile(new File(filename));
		System.out.println(g.toString());
		System.out.println(PsiMatrix.computePsiMatrixForGraph(g).toString());
		System.out.println("\n\n");
	}

	public static void main(String[] args) {
		try {
			testWithFile("simpleGraph.txt");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
