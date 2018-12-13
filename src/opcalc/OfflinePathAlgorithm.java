package opcalc;

import java.util.ArrayList;
import java.util.List;

import graph.Demands;
import graph.GraphVertex;
import psimatrix.PathElement;
import psimatrix.PathElementPool;
import psimatrix.PsiMatrix;
import psimatrix.PsiMatrixElement;

/**
 *  offline path selection algorithm implementation,
 *  following the "operator calculus approach for
 *  multi-constrained routing in wireless sensor
 *  networks" (Nefzi, Schott, Song, Staples, Tsiontsiou).
 * 
 * @author valia
 */

public class OfflinePathAlgorithm {

	private static final String ERR_NULL_BASE_MATRIX =
			"Cannot compute on a null Psi matrix!";
	private static final String ERR_NULL_ELEMENTS =
			"Cannot multiply null elements!";

	private static final String ITERATION_NUMBER =
			"\nPerforming iteration (matrix multiplication) %d...\n";
	private static final String OBTAINED_ZERO_MATRIX =
			"Obtained a zero matrix: Stopping computation here.\n\n";

	private final PsiMatrix baseMatrix;
	private PsiMatrix workMatrix;
	private PsiMatrix destMatrix;
	private double[] constraints;
	private GraphVertex source;
	private GraphVertex destination;

	/**
	 * Reference constructor, for the offline path algorithm
	 *  implementation.
	 * 
	 * @param matrix base Psi matrix on which computations
	 *                shall be performed.
	 * @throws NullPointerException if <tt>matrix</tt> is <tt>null</tt>.
	 */
	public OfflinePathAlgorithm(PsiMatrix matrix, Demands demands) {
		if (matrix == null)
			throw new NullPointerException(ERR_NULL_BASE_MATRIX);
		this.baseMatrix = matrix;
		constraints = demands.getWeight();
		source = demands.getSrc();
		destination = demands.getDest();
	}

	/**
	 * Perform the computations on the given Psi matrix,
	 *  following the operator calculus algorithm
	 */
	public void performComputation() {
		try {
			this.workMatrix = (PsiMatrix) this.baseMatrix.clone();
			// max number of iterations = "size" of matrix minus one
			for (int iter = 1; iter < this.baseMatrix.getSize(); iter++) {
				this.destMatrix = new PsiMatrix(
						this.baseMatrix.getSize(),
						this.baseMatrix.getWeightDimension());
				System.out.println(String.format(ITERATION_NUMBER, iter));
				System.out.flush();
				performMatrixMultiplication();
				if (this.destMatrix.isZero()) {
					// if we got a "zero" matrix, no need to go on
					System.out.println(OBTAINED_ZERO_MATRIX);
					break;
				}
				System.out.println(this.destMatrix.toString());
				this.workMatrix.clear();
				this.workMatrix = (PsiMatrix) this.destMatrix.clone();
				System.gc();
			}
		} catch (CloneNotSupportedException e) {
			// won't actually happen;
		} finally {
			this.workMatrix.clear();
			this.workMatrix = null;
			this.destMatrix.clear();
			this.destMatrix = null;
			System.gc();
		}
	}

	/*
	 * Perform the multiplication of 'workMatrix' by 'baseMatrix';
	 *  result is stored in 'destMatrix', i.e. it is like we could write:
	 *         destMatrix = workMatrix * baseMatrix;
	 */
	private void performMatrixMultiplication() {
		int sideSize = this.workMatrix.getSize();
		for (int lig = 0; lig < sideSize; lig++) {
			for (int col = 0; col < sideSize; col++) {
				performElementMultiplication(lig, col);
			}
		}
	}

	/*
	 * Perform the multiplication of an element of 'workMatrix'
	 *  by the appropriate elements of 'baseMatrix'.
	 * 
	 * @param lig line of the element to multiply.
	 * @param col column of the element to multiply.
	 */
	private void performElementMultiplication(int lig, int col)
	throws NullPointerException
	{
		int sideSize = this.workMatrix.getSize();
		List<PathElement> validPaths = new ArrayList<PathElement>(sideSize);
		validPaths.clear();
		for (int i = 0; i < sideSize; i++) {
			// perform the "sub-multiplication" of w(lig,i)*b(i,col)
			PsiMatrixElement w = this.workMatrix.getElement(lig, i);
			PsiMatrixElement b = this.baseMatrix.getElement(i, col);
			// get the valid path elements corresponding to this "association"
			List<PathElement> foundPaths = buildValidPaths(w, b);
			// add the found path elements to the global list
			for (PathElement pe : foundPaths)
				validPaths.add(pe);
		}
		
		PsiMatrixElement tgtElmt = this.destMatrix.getElement(lig, col);
		// free the old pathElements
		tgtElmt.clear();
		// store the newly computed path elements
		for (PathElement pe: validPaths)
			tgtElmt.addPath(pe);
	}

	/*
	 * Check if the association of the two given matrix elements
	 *  corresponds to a potential valid path.
	 * 
	 * @param w left element (of the work matrix) of the association;
	 *           can correspond to one or many vertices (i.e.: a valid path).
	 * @param b right element (of the base matrix) of the association;
	 *           corresponds to only one node of the graph (vertex).
	 * @return the array of the valid paths corresponding to the association
	 *          of these two elements; if no valid path could be found,  the
	 *          returned array will be empty (length == 0),  but this method
	 *          never returns <tt>null</tt>.
	 * @throws NullPointerException if <tt>w</tt> or <tt>b</tt>
	 *                               is <tt>null</tt>.
	 */
	
	private List<PathElement> buildValidPaths(PsiMatrixElement w,
	                                          PsiMatrixElement b)
	throws NullPointerException
	{
		if ((w == null) || (b == null))
			throw new NullPointerException(ERR_NULL_ELEMENTS);

		ArrayList<String> leftNodes = null, rightNodes = null,
				newNodes = new ArrayList<String>(this.workMatrix.getSize());
		// list of found-valid path elements
		List<PathElement> paths = new ArrayList<PathElement>();
		// no path can cross a null element
		if ((w.isZero()) || (b.isZero())) return paths;

		// try to concatenate nodes-lists, one after another
		for (PathElement leftPE: w.getPaths()) {
			leftNodes = leftPE.getIds(leftNodes);
			if (leftNodes.get(0)==source.getID()){
			for (PathElement rightPE: b.getPaths()) {
				rightNodes = rightPE.getIds(rightNodes);
				// check if the two paths can concatenate,
				//  i.e.: if the last node of the source path
				//        is the first node of the destination path
				String lastLeft = leftNodes.get(leftNodes.size() - 1);
				String firstRight = rightNodes.get(0);
				if (!(lastLeft.equals(firstRight))) continue;
				// ensure there is no "cycle" in the new path,
				//  i.e.: there is no common vertex (apart from the one
				//        checked hereabove) in the left and right parts
				boolean cycle = false;
				for (int r = 1; r < rightNodes.size(); r++)
					for (int l = 0; l < leftNodes.size() - 1; l++)
						if (leftNodes.get(l).equals(rightNodes.get(r))) {
							cycle = true;
							break;
						}
				if (cycle) continue;
				// we have a valid new path, add it to the list
				PathElement newPE = PathElementPool.getInstance().getNewPath();
				newNodes.clear();
				newNodes.addAll(leftNodes);
				for (int i = 1; i < rightNodes.size(); i++)
					newNodes.add(rightNodes.get(i));
				newPE.setIds(newNodes);
				// compute new weight
				int wDim = this.baseMatrix.getWeightDimension();
				double[] leftW = leftPE.getWeight();
				double[] rightW = rightPE.getWeight();
				double[] newW = new double[wDim];
				for (int i = 0; i < wDim; i++)
					newW[i] = leftW[i] + rightW[i];
				
				if (constraintsOK(newW)){	
					newPE.setWeight(newW);
					paths.add(newPE);
					if (newPE.getIds(newNodes).get(newPE.getIds(newNodes).size()-1) == destination.getID())
						System.out.println("Path Found " + newPE.getIds(newNodes).toString());
					
				}
			}
		}else continue;
		}

		return paths;
	}
	
	public boolean constraintsOK (double[] values) { 
		
		for (int i=0; i<values.length; i++){
			if (constraints[i] >= values[i])
				continue;
			else
				return false;
		}
		return true;
	}

}
