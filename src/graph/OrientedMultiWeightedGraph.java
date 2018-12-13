package graph;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 
 * 	Represents a weighted, oriented graph.
 * 
 * @author valia
**/

public class OrientedMultiWeightedGraph {

	private static final String ERR_ID_ALREADY_USED =
			"This ID (%s) is already used by another vertex in the graph!";
	private static final String ERR_NULL_VERTEX =
			"Cannot process null vertex!";
	private static final String ERR_NULL_WEIGHT =
			"Cannot create an edge with null weight!";
	private static final String ERR_UNKNOWN_VERTEX =
			"Unknown vertex \"%s\"!";
	private static final String ERR_NULL_DIMENSION_WEIGHT =
			"No elements for weight in line:\n\"%s\"!\n";
	private static final String ERR_WEIGHT_DIMENSION_CHANGED =
			"Weight dimension changed in line:\n\"%s\"\n" +
			"(Dim = %s, should be %d)!";
	private static final String ERR_NOT_VALID_DOUBLE =
			"Illegal value \"%s\" (not a number) in line:\n\"%s\"!\n";
	private static final String ERR_EDGE_REDEFINED =
			"Existing edge redefined in line:\n\"%s\"!\n";

	/* Weight dimension, that is: number of elements in the metrics of edges */
	private int weightDim;

	/* list of composing vertices */
	private List<GraphVertex> vertices;

	/* list of edges */
	private List<GraphEdge> edges;
	
	/* list of demands */
	private ArrayList<Demands> wl;

	/**
	 * Constructor.
	 */
	public OrientedMultiWeightedGraph(int weightDimension) {
		this.weightDim = weightDimension;
		// create lists
		this.vertices = new ArrayList<GraphVertex>();
		this.edges = new ArrayList<GraphEdge>();
	}

	/**
	 * @return the weight dimension for this graph.
	 */
	public int getWeightDimension() {
		return this.weightDim;
	}

	/**
	 * Add a new vertex to this graph.
	 * 
	 * @param index index number to give to the new vertex in this graph.
	 * @return the newly added vertex.
	 * @throws IllegalArgumentException if <tt>index</tt> is negative,
	 *                                   or already used by another
	 *                                   vertex in this graph.
	 */
	public synchronized GraphVertex addVertex(String id)
	throws IllegalArgumentException
	{
		GraphVertex v = new GraphVertex(this, id);
		if (findVertex(id) != null)
			throw new IllegalArgumentException(
					String.format(ERR_ID_ALREADY_USED, id));
		this.vertices.add(v);
		return v;
	}

	/**
	 * Add a new edge to this graph.
	 * 
	 * @param source source vertex of this new edge.
	 * @param destination destination vertex of this new edge.
	 * @param weight weight (metrics) of this new edge in the graph.
	 * @return the newly added edge.
	 * @throws NullPointerException if <tt>source</tt>, <tt>destination</tt>
	 *                               or <tt>weight</tt> is <tt>null</tt>.
	 * @throws IllegalArgumentException if <tt>owner</tt> doesn't own
	 *                                  <tt>source</tt> or <tt>destination</tt>.
	 * @throws IllegalArgumentException if <tt>weight</tt> doesn't have
	 *                                   the wanted dimension.
	 */
	public synchronized GraphEdge addEdge(GraphVertex source,
	                                      GraphVertex destination,
	                                      double[] weight)
	{
		if ((source == null) || (destination == null))
			throw new NullPointerException(ERR_NULL_VERTEX);
		if (!this.vertices.contains(source))
			throw new IllegalArgumentException(String.format(
					ERR_UNKNOWN_VERTEX,
					source.getID()));
		if (!this.vertices.contains(destination))
			throw new IllegalArgumentException(String.format(
					ERR_UNKNOWN_VERTEX,
					destination.getID()));
		if (weight == null)
			throw new NullPointerException(ERR_NULL_WEIGHT);
		if (weight.length != this.weightDim)
			throw new IllegalArgumentException(String.format(
					ERR_WEIGHT_DIMENSION_CHANGED,
					weight.length, this.weightDim));
		GraphEdge e = new GraphEdge(this, source, destination, weight);
		this.edges.add(e);
		return e;
	}

	/**
	 * @return the number of vertices (nodes) currently composing the graph.
	 */
	public int getVerticesNumber() {
		return this.vertices.size();
	}

	/**
	 * Get the n-th vertex in the graph
	 * 
	 * @param index (internal) order number of the vertex to return
	 * @return the desired vertex.
	 * @throws IndexOutOfBoundsException if the given index is out of range
	 *                                    (that is: less than 0, or equals
	 *                                     or grater than the number of
	 *                                     vertices in the graph).
	 * @see fr.loria.graph.OrientedMultiWeightedGraph#getVerticesNumber
	 */
	public GraphVertex getVertex(int index) {
		return this.vertices.get(index);
	}

	/**
	 * Find a vertex in the graph.
	 * 
	 * @param index index defining the vertex to find.
	 * @return the vertex corresponding to the given <tt>index</tt>,
	 *          or <tt>null</tt> if there is no such vertex in the graph.
	 */
	public GraphVertex findVertex(String id) {
		for (GraphVertex v: this.vertices)
			if (v.getID().equals(id))
				return v;
		return null;
	}

	/**
	 * Find the possible edge between two vertices.
	 * 
	 * @param src source vertex for the edge to find.
	 * @param dest destination vertex for the edge to find.
	 * @return the edge linking the given two vertices,
	 *          or <tt>null</tt> if there is no such edge.
	 * @throws NullPointerException if <tt>src</tt> or <tt>dest</tt>
	 *                               is <tt>null</tt>.
	 */
	public GraphEdge findEdgeForVertices(GraphVertex src, GraphVertex dest)
	throws NullPointerException
	{
		if ((src == null) || (dest == null))
			throw new NullPointerException(ERR_NULL_VERTEX);
		for (GraphEdge e: this.edges) {
			if (e.getSourceVertex().equals(src) &&
					e.getDestinationVertex().equals(dest))
				return e;
		}
		return null;
	}

	/**
	 * Load the graph data from the given source file.
	 * 
	 * @param srcFile file containing the oriented, multi-dimensional
	 *  weighted graph definition.<br/>
	 * It is a text file, whose content describes the edges
	 *  of the graph (and thus, implicitely, the vertices).<br/>
	 * Each line of the file describes an edge, using this format:<br/>
	 * <pre>
	 *  i   j   w1   w2...
	 * </pre>
	 * where:
	 * <ul>
	 * <li> <tt>i</tt> is the index of the source vertex,
	 * <li> <tt>j</tt> is the index of the destination vertex,
	 * <li> <tt>w1</tt>, <tt>w2</tt>, and so on... are the elements
	 *       of the multi-dimensional weighted for this edge. 
	 * </ul>
	 * All the elements of the line are separated by spaces.<br/>
	 * WARNING: every line must have the same number of elements,
	 *  (that is: the weights of all the edges of a graph must strictly
	 *   have the same dimension), otherwise, the loading of the file
	 *  fails with an <tt>IllegalFormatException</tt> (see below).
	 * @throws IllegalArgumentException if the given source file doesn't
	 *                                   respect the format described above.
	 * @throws IOException if some error prevents the source file
	 *                      from being correctly read.
	 */
	public void loadFromFile(File srcFile)
			throws IllegalArgumentException, IOException
			{
				ArrayList<Double> wl = new ArrayList<Double>();
				double[] wa = null;
				int wDim = 0;
				BufferedReader rdr = new BufferedReader(new FileReader(srcFile));
				try {
					String line = rdr.readLine();
					// read the file line by line
					while (line != null) {
						// tokenize line
						String[] tokens = line.split("[ \t]");
						// source vertex
						String srcId = tokens[0];
						GraphVertex srcVtx = findVertex(srcId);
						if (srcVtx == null)
							srcVtx = addVertex(srcId);
						// dest vertex
						String destId = tokens[1];
						GraphVertex destVtx = findVertex(destId);
						if (destVtx == null)
							destVtx = addVertex(destId);
						// check weight dimension
						if (tokens.length < 3)
							throw new IllegalArgumentException(
									String.format(ERR_NULL_DIMENSION_WEIGHT,
											line));
						if (wDim == 0) {
							// first line: set weight dimension
							wDim = tokens.length - 2;
							this.weightDim = wDim;
							wa = new double[wDim];
						} else {
							// dimension has changed!
							if ((tokens.length - 2) != wDim)
								throw new IllegalArgumentException(
										String.format(ERR_WEIGHT_DIMENSION_CHANGED,
												line, tokens.length - 2, wDim));
						}
						// weight elements
						wl.clear();
						for (int n = 2; n < tokens.length; n++) {
							double d;
							try {
								d = Double.parseDouble(tokens[n]);
							} catch (NumberFormatException nfe) {
								throw new IllegalArgumentException(
										String.format(ERR_NOT_VALID_DOUBLE,
												tokens[n], line));
							}
							wl.add(d);
						}
						// store new edge in graph
						if (findEdgeForVertices(srcVtx, destVtx) == null) {
							for (int n = 0; n < wl.size(); n++) wa[n] = wl.get(n);
							addEdge(srcVtx, destVtx, wa);
						} else {
							// edge already exists
							throw new IllegalArgumentException(
									String.format(ERR_EDGE_REDEFINED, line));
						}
						// next line
						line = rdr.readLine();
					}
				} finally {
					// finished: close the source file
					rdr.close();
					System.gc();
				}
			}

			
			public ArrayList<Demands> loadDemandsFromFile(File srcFile)
					throws IllegalArgumentException, IOException
			{
				wl = new ArrayList<Demands>();
				Demands demand;
				double[] d = null;
				int wDim = 0;
				BufferedReader rdr = new BufferedReader(new FileReader(srcFile));
				try {
					String line = rdr.readLine();
					// read the file line by line
					while (line != null) {
						// tokenize line
						String[] tokens = line.split("[ \t]");
						// source vertex
						String srcId = tokens[0];
						GraphVertex srcVtx = findVertex(srcId);
						if (srcVtx == null)
							srcVtx = addVertex(srcId);
						// dest vertex
						String destId = tokens[1];
						GraphVertex destVtx = findVertex(destId);
						if (destVtx == null)
							destVtx = addVertex(destId);
						// check weight dimension
						if (tokens.length < 3)
							throw new IllegalArgumentException(
									String.format(ERR_NULL_DIMENSION_WEIGHT,
											line));
						if (wDim == 0) {
							// first line: set weight dimension
							wDim = tokens.length - 2;
							this.weightDim = wDim;
						} else {
							// dimension has changed!
							if ((tokens.length - 2) != wDim)
								throw new IllegalArgumentException(
										String.format(ERR_WEIGHT_DIMENSION_CHANGED,
												line, tokens.length - 2, wDim));
						}
						// weight elements
						d = new double[tokens.length - 2];
						for (int n = 0; n < tokens.length -2; n++) {
							try {
								d[n] = Double.parseDouble(tokens[n+2]);
							} catch (NumberFormatException nfe) {
								throw new IllegalArgumentException(
										String.format(ERR_NOT_VALID_DOUBLE,
												tokens[n], line));
							}
							
						}
						
						demand = new Demands(srcVtx, destVtx, d);
						wl.add(demand);
						// next line
						line = rdr.readLine();
					}
					
					
				} finally {
					// finished: close the source file
					rdr.close();
					System.gc();
				}
				return wl;
			}
			
			
			/*
			 * (non-Javadoc)
			 * @see java.lang.Object#toString()
			 */
			@Override
			public String toString() {
				StringBuilder sb = new StringBuilder(String.format(
						"Graph @%x\n",
						this.hashCode()));
				sb.append("===============\n");
				sb.append(String.format(
						"\n%d vertices:",
						this.vertices.size()));
				for (GraphVertex v: this.vertices) {
					sb.append(String.format(
							" [%s]",
							v.getID()));
				}
				sb.append(String.format(
						"\n\nEdges (weight dimension = %d):\n",
						this.weightDim));
				for (GraphEdge e: this.edges) {
					sb.append(String.format(
							"[%s -> %s]: (",
							e.getSourceVertex().getID(),
							e.getDestinationVertex().getID()));
					boolean first = true;
					for (double d: e.getWeight(null)) {
						if (!first) sb.append(";");
						sb.append(String.format(
								"%f",
								d));
						first = false;
					}
					sb.append(")\n");
				}
		//		sb.append("\n");
		//		sb.append("Demands\n");
		//		sb.append("===============\n\n");
				
		/*		for (Demands dem : wl){
				sb.append(String.format(
						"[%s -> %s]: %s",
						dem.getSrc().getID(),
						dem.getDest().getID(), 
						Arrays.toString(dem.getWeight())));
				sb.append("\n"); 
				}
					
				sb.append("\n\n\n");   */
				return sb.toString();
			}

		}
