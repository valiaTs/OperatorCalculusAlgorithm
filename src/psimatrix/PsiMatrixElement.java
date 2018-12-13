package psimatrix;

import java.util.LinkedList;
import java.util.List;


/**
 * Element of a Psi matrix, used in operator calculus for oriented multi-weighted graphs.
 * 
 * @author valia
 */


public class PsiMatrixElement {

	private static final String ERR_NULL_OWNER =
			"A matrix element cannot be created without any owner!";
	private static final String ERR_NULL_PATH_ELEMENT =
			"Cannot process a null path element!";
	private static final String ERR_WEIGHT_DIM_MISMATCH =
			"Cannot clone an element between Psi matrices" +
			" with different weight dimensions (src=%d, dst=%d)!";
	private static final String ERR_SIZE_MISMATCH =
			"Cannot clone an element between Psi matrices" +
			" with different sizes (src=%d, dst=%d)!";

	private static final String ZERO_ELEMENT =
			" -- zero -- ";

	private final PsiMatrix owner;
	private List<PathElement> paths;

	/**
	 * Reference constructor.
	 * 
	 * @param owner Psi matrix owning this element.
	 * @throws NullPointerException if <tt>owner</tt> is <tt>null</tt>.
	 */
	public PsiMatrixElement(PsiMatrix owner) throws NullPointerException {
		if (owner == null)
			throw new NullPointerException(ERR_NULL_OWNER);
		this.owner = owner;
		this.paths = new LinkedList<PathElement>();
	}

	/**
	 * @return <tt>true</tt> if the element is "zero" (doesn't contain
	 *          any valid path), <tt>false</tt> otherwise.
	 */
	public boolean isZero() {
		return this.paths.isEmpty();
	}

	/**
	 * Empty this matrix element (i.e.: removes all of its path elements).
	 */
	public void clear() {
		// free the elements to remove from the pool
		for (PathElement pe : this.paths)
			PathElementPool.getInstance().freePath(pe);
		this.paths.clear();
	}

	/**
	 * Add a path to this matrix element.
	 * 
	 * @param pe path element to add.
	 * @throws NullPointerException if <tt>pe</tt> is <tt>null</tt>.
	 */
	public void addPath(PathElement pe) throws NullPointerException {
		if (pe == null)
			throw new NullPointerException(ERR_NULL_PATH_ELEMENT);
		this.paths.add(pe);
	}

	/**
	 * @return a list of all the <tt>PathElement</tt>s contained
	 *          in the current Psi matrix element.
	 */
	public List<PathElement> getPaths() {
		return this.paths;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		boolean zero = true;
		StringBuilder sb = new StringBuilder();
		for (PathElement pe: this.paths) {
			if (pe.isZero()) continue;
			if (!zero) sb.append(" + ");
			zero = false;
			sb.append(pe.toString());
		}
		if (zero) return ZERO_ELEMENT;
		return sb.toString();
	}

	/**
	 * Clone a matrix element from a Psi matrix to another one.
	 * 
	 * @param otherOwner Psi matrix containing the new element;
	 *                 can be the same as the source one.
	 * @return the newly created element for the given other matrix.
	 * @throws IllegalArgumentException if the two owning matrices are not
	 *                                   compatible (that is: have different
	 *                                   weight dimension or size).
	 */
	public PsiMatrixElement cloneInto(PsiMatrix otherOwner){
		PsiMatrixElement other = new PsiMatrixElement(otherOwner);
		// check compatibility between the two owning matrices
		if (this.owner.getWeightDimension() != otherOwner.getWeightDimension())
			throw new IllegalArgumentException(String.format(
					ERR_WEIGHT_DIM_MISMATCH,
					this.owner.getWeightDimension(),
					otherOwner.getWeightDimension()));
		if (this.owner.getSize() != otherOwner.getSize())
			throw new IllegalArgumentException(String.format(
					ERR_SIZE_MISMATCH,
					this.owner.getSize(),
					otherOwner.getSize()));
		// clone paths one after another
		for (PathElement pe: this.paths) {
			if (pe.isZero()) continue;
			other.addPath(PathElementPool.getInstance().getCloneOf(pe));
		}
		return other;
	}

}
