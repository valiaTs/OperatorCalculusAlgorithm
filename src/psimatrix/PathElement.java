package psimatrix;

import java.util.ArrayList;

/**
 * Representation of a possible path (or part of path) in the given reference graph
 * 
 * @author valia
 */

public class PathElement {

	private static final String ERR_MAX_VTX_NUM_TOO_SMALL =
			"Max vertex number too small (must be at least 1)!";
	private static final String ERR_WEIGHT_DIM_TOO_SMALL =
			"Weight dimension too small (must be at least 1)!";
	private static final String ERR_NULL_IDS =
			"Cannot allocate a node without ID!";
	private static final String ERR_TOO_MUCH_IDS =
			"Too much nodes for the path (is %d, should be %d)!";
	private static final String ERR_NULL_WEIGHT =
			"Cannot allocate a node with null weight (set it to zero instead)!";
	private static final String ERR_BAD_WEIGHT_DIM =
			"Bad weight dimension (is %d, should be %d)!";
	private static final String ERR_NULL_SOURCE =
			"Cannot process a null path element!";

	/* if true, this element is zero (i.e.: no possible path) */
	private boolean zero;

	/* IDs of the nodes, crossed by the path
	 *  represented by this element */
	private String[] ids;
	/* number of IDs actually composing the path
	 *  (i.e.: number of valid elements in the 'ids' array). */
	private int nbIds;
	private final int maxNbIds;

	/* weights of the path represented by this element */
	private double[] weights;
	private final int wDim;

	/**
	 * Reference constructor.
	 * 
	 * @param maxVtxNum maximum number of vertices that can be contained
	 *                   in a path, should usually be set to the number
	 *                   of vertices (nodes) contained in the processed graph.
	 * @param weightDim dimension of the weight (constraint given by the
	 *                   processed graph).
	 * @throws IllegalArgumentException if <tt>maxVtxNum</tt> or
	 *                                   <tt>weightDim</tt> is less than 1.
	 */
	PathElement(int maxVtxNum, int weightDim) {
		if (maxVtxNum < 1)
			throw new IllegalArgumentException(ERR_MAX_VTX_NUM_TOO_SMALL);
		if (weightDim < 1)
			throw new IllegalArgumentException(ERR_WEIGHT_DIM_TOO_SMALL);
		this.maxNbIds = maxVtxNum;
		this.wDim = weightDim;
		setZero(true);
	}

	/**
	 * @return <tt>true</tt> if the element is "zero" (doesn't correspond
	 *          to a valid path), <tt>false</tt> if it has significative
	 *          values.
	 */
	public boolean isZero() {
		return this.zero;
	}

	/**
	 * Set the nodes to the "zero" value, if needed.
	 * @param value <tt>true</tt> to zero out this element,
	 *              <tt>false</tt> to flag it as "significative".
	 */
	public void setZero(boolean value) {
		if (value == this.zero) return;  // nothing to do
		if (value) {
			this.ids = null;
			this.nbIds = 0;
			this.weights = null;
		} else {
			this.ids = new String[this.maxNbIds];
			for (int i = 0; i < this.ids.length; i++)
				this.ids[i] = null;
			this.nbIds = 0;
			this.weights = new double[this.wDim];
		}
		this.zero = value;
	}

	/**
	 * @param list pre-existing list to reuse (in order to spare memory),
	 *         can be <tt>null</tt>.
	 * @return the list of IDs (nodes) associated with this element,
	 *          if the element is zero'ed, the returned list is empty.
	 */
	public ArrayList<String> getIds(ArrayList<String> list) {
		if (list == null)
			list = new ArrayList<String>(this.maxNbIds);
		list.clear();
		if (!this.zero)
			for (int n = 0; n < this.nbIds; n++)
				list.add(n, this.ids[n]);
		return list;
	}


	/**
	 * Sets the array of IDs constituting the path
	 *  associated with this element.
	 * @param list new IDs (i.e.: path) for this element.
	 * @throws NullPointerException if <tt>list</tt> is <tt>null</tt>.
	 * @throws IllegalArgumentException if <tt>list</tt> contains
	 *                                   too much elements (nodes).
	 */
	public void setIds(ArrayList<String> list) {
		if (list == null)
			throw new NullPointerException(ERR_NULL_IDS);
		int len = list.size();
		if (len == 0)
			throw new IllegalArgumentException(ERR_NULL_IDS);
		if (len > this.maxNbIds)
			throw new IllegalArgumentException(String.format(
					ERR_TOO_MUCH_IDS, len, this.maxNbIds));
		setZero(false);
		for (int n = 0; n < len; n++)
			this.ids[n] = list.get(n);
		for (int n = len; n < this.ids.length; n++)
			this.ids[n] = null;
		this.nbIds = len;
	}

	/**
	 * @return the multi-dimensional weight associated with this element.
	 * <br/>
	 * WARNING: this is a mutable reference to the element's internal array. 
	 * <br/>
	 * Returns <tt>null</tt> if this element is zero'ed.
	 */
	public double[] getWeight() {
		return this.weights;
	}

	/**
	 * Sets the multi-dimensional weight of the element
	 * @param w new values to give to the weight.
	 * <br/>
	 * Note that this is a deep copy (we don't simply change a reference:
	 *  actual values in the arrays are copied).
	 * @throws NullPointerException if <tt>w</tt> is <tt>null</tt>.
	 * @throws IllegalArgumentException if <tt>w</tt> doesn't have
	 *                                   the expected dimension
	 *                                   (i.e.: number of elements).
	 */
	public void setWeight(double[] w) {
		if (w == null)
			throw new NullPointerException(ERR_NULL_WEIGHT);
		if (w.length != this.wDim)
			throw new IllegalArgumentException(String.format(
					ERR_BAD_WEIGHT_DIM, w.length, this.wDim));
		setZero(false);
		System.arraycopy(w, 0, this.weights, 0, w.length);
	}

	/**
	 * Copy the values from another path element to this one,
	 *  so that the latter practically becomes a clone of the former.
	 * 
	 * @param source the element to "clone".
	 * @throws NullPointerException if <tt>source</tt> is <tt>null</tt>.
	 * @throws IllegalArgumentException if <tt>source</tt> doesn't have
	 *                                   the expected weight dimension,
	 *                                   or has too much IDs.
	 */
	public void cloneFrom(PathElement source) {
		if (source == null)
			throw new NullPointerException(ERR_NULL_SOURCE);
		// "zero" case
		if (source.isZero()) {
			setZero(true);
			return;
		}
		setZero(false);
		// copy IDs
		if (source.nbIds > this.maxNbIds)
			throw new IllegalArgumentException(String.format(
					ERR_TOO_MUCH_IDS, source.nbIds, this.maxNbIds));
		for (int i = 0; i < source.nbIds; i++)
			this.ids[i] = source.ids[i];
		for (int i = source.nbIds; i < this.ids.length; i++)
			this.ids[i] = null;
		this.nbIds = source.nbIds;
		// copy weight
		if (source.wDim != this.wDim)
			throw new IllegalArgumentException(String.format(
					ERR_BAD_WEIGHT_DIM, source.wDim, this.wDim));
		for (int i = 0; i < this.wDim; i++)
			this.weights[i] = source.weights[i];
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (isZero()) return "";
		StringBuilder sb = new StringBuilder("(");
		boolean first = true;
		for (double d: getWeight()) {
			if (!first) sb.append(";");
			sb.append(String.format("%f", d));
			first = false;
		}
		sb.append(")[");
		first = true;
		for (int i = 0; i < this.nbIds; i++) {
			String s = this.ids[i];
			if (!first) sb.append("->");
			sb.append(String.format("%s", s));
			first = false;
		}
		sb.append("]");
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		PathElementPool.getInstance().freePath(this);
	}

}
