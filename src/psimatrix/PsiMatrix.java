package psimatrix;

import java.util.ArrayList;

import graph.GraphEdge;
import graph.GraphVertex;
import graph.OrientedMultiWeightedGraph;


/**
 * 
 * @author valia
 */


public class PsiMatrix {
	
	private static final String ERR_SIZE_TOO_SMALL =
			"Matrix size too small (must be at least 1)!";
	private static final String ERR_WEIGHT_DIM_TOO_SMALL =
			"Weight dimension too small (must be at least 1)!";

	/* Psi matrix are square, this we only store one dimension */
	private int size;
	/* expected weight dimension */
	private int wDim;
	/* actual content of the matrix */
	private PsiMatrixElement[][] content;

	/**
	 * Constructor
	 * @param size size of the new Psi matrix (actually, Psi matrices
	 *              are square: thus, the new matrix will be
	 *              <tt>size</tt>*<tt>size</tt>).
	 * @throws IllegalArgumentException if <tt>size</tt> is less than 1.
	 */
	public PsiMatrix(int size, int weightDim) {
		if (size < 1)
			throw new IllegalArgumentException(ERR_SIZE_TOO_SMALL);
		if (weightDim < 1)
			throw new IllegalArgumentException(ERR_WEIGHT_DIM_TOO_SMALL);
		this.content = new PsiMatrixElement[size][];
		for (int i = 0; i < size; i++) {
			this.content[i] = new PsiMatrixElement[size];
			for (int j = 0; j < size; j++) {
				this.content[i][j] = new PsiMatrixElement(this);
			}			
		}
		this.size = size;
		this.wDim = weightDim;
	}

	/**
	 * @return the side size of the matrix (actual matrix size is n*n).
	 */
	public int getSize() {
		return this.size;
	}

	/**
	 * @return the dimension of the weight used in the elements of this matrix.
	 */
	public int getWeightDimension() {
		return this.wDim;
	}

	/**
	 * Return the wanted element of the matrix.
	 * @param i vertical index (line) of the wanted element.
	 * @param j horizontal index (column) of the wanted element.
	 * @return the matrix element at line <tt>i</tt> and column <tt>j</tt>.
	 */
	public PsiMatrixElement getElement(int i, int j) {
		return this.content[i][j];
	}

	/**
	 * Indicates is this matrix "is zero", that is: if all of its elements
	 *  are equal to zero.
	 * 
	 * @return <tt>true</tt> if all of the matrix elements are in "zero
	 *          state"; <tt>false</tt> otherwise.
	 * @see fr.loria.psimatrix.PsiMatrix#clear()
	 */
	public boolean isZero() {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (!(this.content[i][j].isZero())) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Sets all of the elements of the matrix to zero.
	 * @see fr.loria.psimatrix.PsiMatrix#isZero()
	 */
	public void clear() {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				this.content[i][j].clear();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(String.format(
				"Psi-matrix @%x (weight dimension = %d):\n",
				this.hashCode(), this.wDim));
		sb.append("====================\n\n");
		for (int lig = 0; lig < this.size; lig++) {
			for (int col = 0; col < this.size; col++) {
				PsiMatrixElement e = this.getElement(lig, col);
				sb.append(String.format("%s\t", e.toString()));
			}
			sb.append("\n");
		}
		sb.append("\n");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		PsiMatrix other = new PsiMatrix(this.size, this.wDim);

		for (int lig = 0; lig < this.size; lig++)
			for (int col = 0; col < this.size; col++)
				other.content[lig][col] =
						this.content[lig][col].cloneInto(other);

		return other;
	}

	/**
	 * Constructs the Psi matrix corresponding to the given graph.
	 * 
	 * @param g the oriented, multi-weighted graph from which
	 *           a psi matrix shall be generated.
	 * @return the Psi matrix describing this graph.
	 */
	public static PsiMatrix computePsiMatrixForGraph(
			OrientedMultiWeightedGraph g)
	{
		PathElementPool.setMaxPathSize(g.getVerticesNumber());
		PathElementPool.setWidthDimension(g.getWeightDimension());

		double[] w = new double[g.getWeightDimension()];
		// max possible path length = number of nodes (vertices)
		int numVtx = g.getVerticesNumber();
		// new Psi matrix object
		PsiMatrix m = new PsiMatrix(numVtx, g.getWeightDimension());
		// template for id list
		ArrayList<String> ids = new ArrayList<String>(1);
		// fill the new Psi matrix;
		// columns correspond to destination vertices
		for (int col = 0; col < numVtx; col++) {
			GraphVertex dstVertex = g.getVertex(col);
			// the basic Psi matrix elements
			//  only have two IDs 
			ids.clear();
			ids.add(null);ids.add(null);
			ids.set(1, dstVertex.getID());

			// lines correspond to source vertices
			for (int lig = 0; lig < numVtx; lig++) {
				GraphVertex srcVertex = g.getVertex(lig);
				ids.set(0, srcVertex.getID());

				PsiMatrixElement l = m.getElement(lig, col);
				l.clear();
				// Psi matrix elements correspond to (potential) edges
				GraphEdge e = g.findEdgeForVertices(srcVertex, dstVertex);
				if (e != null) {
					// description of an edge
					PathElement pe = PathElementPool.getInstance().getNewPath();
					pe.setWeight(e.getWeight(w));
					pe.setIds(ids);
					l.addPath(pe);
				}
			}
		}
		// fill complete
		return m;
	}

}
