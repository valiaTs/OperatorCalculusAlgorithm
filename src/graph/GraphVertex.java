package graph;

/**
 * 
 * 	Represents a vertex in a graph.
 * 
 * @author valia
**/

public class GraphVertex {
		
	private static final String ERR_NO_OWNER =
			"Cannot create a vertex out of any graph (i.e.: without owner)!";
	private static final String ERR_NO_ID =
			"Cannot create a vertex without ID!";


	
	/* the graph that owns this vertex */
	private final OrientedMultiWeightedGraph owner;

	/* ID of the vertex in its graph */
	private final String id;
	
	/**
	 * Vertex reference constructor.
	 * 
	 * @param owner the graph that will own this new vertex.
	 * @param id the arbitrary ID you want to give to this node.
	 * @throws NullPointerException if <tt>owner</tt> or <tt>id</tt>
	 *                               is <tt>null</tt>.
	 */
	public GraphVertex (OrientedMultiWeightedGraph owner, String id) throws  
	NullPointerException, IllegalArgumentException {
		if (owner==null)
			throw new NullPointerException(ERR_NO_OWNER);
		if (id==null)
			throw new NullPointerException(ERR_NO_ID);
		this.owner = owner;
		this.id = id;
	}
	
	/**
	 * @return the <tt>Graphe</tt> that owns this vertex.
	 */
	public OrientedMultiWeightedGraph getOwner() {
		return this.owner;
	}
	
	/**
	 * @return the identifier of this vertex.
	 */
	public String getID() {
		return this.id;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GraphVertex)) return false;
		GraphVertex other = (GraphVertex) obj;
		// the vertices are equal iif they have
		//  the same index and the same owner
		return ((this.owner.equals(other.owner)) &&
				(this.id.equals(other.id)));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.owner.hashCode() + this.id.hashCode();
	}

}
