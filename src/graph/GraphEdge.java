package graph;


/**
 * 
 * 	Represents an edge in a weighted, oriented graph.
 * 
 * @author valia
**/

public class GraphEdge {

	private static final String ERR_NO_OWNER =
			"Cannot create an edge out of any graph (i.e.: without owner)!";
	private static final String ERR_NO_SRC =
			"Cannot create an edge without a source vertex!";
	private static final String ERR_NO_DEST =
			"Cannot create an edge without a destination vertex!";
	private static final String ERR_NO_WEIGHT =
			"Cannot create an edge in a weighted graph without weight!";
	private static final String ERR_BAD_SRC_OWNER =
			"The source vertex isn't owned by the expected graph!";
	private static final String ERR_BAD_DEST_OWNER =
			"The destination vertex isn't owned by the expected graph!";
	private static final String ERR_BAD_WEIGHT_DIM =
			"The given weight doesn't have the dimension expected for the graph!";

	/* source and destination vertices of this edge */
	private final GraphVertex src, dest;
	/* weight (metrics) for this edge */
	private final double[] weight;

	/**
	 * Reference constructor for an edge of a weighted, oriented graph.
	 * @param owner the graph that owns the edge and its vertices.
	 * @param source source vertex of this new edge.
	 * @param destination destination vertex of this new edge.
	 * @param weight weight (metrics) of this new edge in the graph.
	 * @throws NullPointerException if <tt>owner</tt>, <tt>source</tt>,
	 *                               <tt>dest</tt> or <tt>metrics</tt>
	 *                               is <tt>null</tt>.
	 * @throws IllegalArgumentException if <tt>owner</tt> doesn't own
	 *                                  <tt>source</tt> or <tt>destination</tt>.
	 * @throws IllegalArgumentException if <tt>weight</tt> doesn't have
	 *                                   the wanted dimension.
	 */
	GraphEdge(OrientedMultiWeightedGraph owner,
	          GraphVertex source, GraphVertex destination,
	          double[] weight)
	{
		if (owner == null)
			throw new NullPointerException(ERR_NO_OWNER);
		if (source == null)
			throw new NullPointerException(ERR_NO_SRC);
		if (!source.getOwner().equals(owner))
			throw new IllegalArgumentException(ERR_BAD_SRC_OWNER);
		this.src = source;
		if (destination == null)
			throw new NullPointerException(ERR_NO_DEST);
		if (!destination.getOwner().equals(owner))
			throw new IllegalArgumentException(ERR_BAD_DEST_OWNER);
		this.dest = destination;
		if (weight == null)
			throw new NullPointerException(ERR_NO_WEIGHT);
		if (weight.length != owner.getWeightDimension())
			throw new IllegalArgumentException(ERR_BAD_WEIGHT_DIM);
		this.weight = new double[weight.length];
		System.arraycopy(weight, 0, this.weight, 0, weight.length);
	}

	/**
	 * @return the source vertex of this edge.
	 */
	public GraphVertex getSourceVertex() {
		// GraphVertex is immutable, so the reference can be directly given
		return this.src;
	}

	/**
	 * @return the destination vertex of this edge.
	 */
	public GraphVertex getDestinationVertex() {
		// GraphVertex is immutable, so the reference can be directly given
		return this.dest;
	}

	/**
	 * Gets the multi-dimensional weight of the edge.
	 * <br/>
	 * WARNING: actually returns a copy of the weight data (since this is an
	 *           immutable class).
	 * 
	 * @param dest array to use a destination for the copy of weight data
	 *              (in order to spare memory) ; can be <tt>null</tt>,
	 *              in that case, a new destination array is created;
	 *              a new array is also created if the given
	 *              destination array is not of the adequate size.
	 * @return the (multi-dimensional) weight of this edge.<br/>
	 */
	public double[] getWeight(double[] dest) {
		if (dest == null)
			dest = new double[this.weight.length];
		if (dest.length != this.weight.length)
			dest = new double[this.weight.length];
		System.arraycopy(this.weight, 0, dest, 0, this.weight.length);
		return dest;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GraphEdge)) return false;
		GraphEdge other = (GraphEdge) obj;
		// two edges are equal if they have the same src & dest vertices
		return ((this.src.equals(other.src)) &&
				(this.dest.equals(other.dest)));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.src.getOwner().hashCode() +
				this.src.hashCode() +
				this.dest.hashCode();
	}

}
