package psimatrix;

/**
 * 
 * @author valia
 */


public class PathElementPool {

	private static final String ERR_POOL_EXHAUSTED =
			"Path element pool (size=%d) exhausted !";
	private static final String ERR_UNKNOWN_ELEMENT =
			"Path element %x was not allocated from the pool!";

	public static final int DEFAULT_POOL_SIZE = 100;
	public static final int DEFAULT_WEIGHT_DIMENSION = 3;
	public static final int DEFAULT_PATH_SIZE = 10;

	private static int weightDimension = DEFAULT_WEIGHT_DIMENSION;
	private static int maxPathSize = DEFAULT_PATH_SIZE;

	private class PoolElement {
		boolean free;
		PathElement elmt;
	}
	PoolElement[] pool;

	/*
	 * Default constructor.
	 */
	private PathElementPool() {
		this(maxPathSize * maxPathSize * maxPathSize);
	}

//	private ArrayList<PathElement> pool; 

	/*
	 * Reference constructor.
	 * @param poolSize initial size (in elements) of the pool.
	 */
	private PathElementPool(int poolSize) {
		pool = new PoolElement[poolSize];
		for (int i = 0; i < poolSize; i++) {
			pool[i] = new PoolElement();
			pool[i].elmt = new PathElement(maxPathSize, weightDimension);
			pool[i].free = true;
		}
	}

	/**
	 * Get a "new" (actually a free) element form the pool.
	 * 
	 * @return an usable, as-new element from the pool.
	 */
	public synchronized PathElement getNewPath() {
		for (int i = 0; i < pool.length; i++)
			if (pool[i].free) {
				pool[i].free = false;
				return pool[i].elmt;
			}
		throw new OutOfMemoryError(String.format(
				ERR_POOL_EXHAUSTED,
				pool.length));
	}

	/**
	 * Get a clone (allocated from the pool) of the given element.
	 * 
	 * @param pe source element to be cloned.
	 * @return an usable element from the pool, with the same values
	 *          as the source element <tt>pe</tt>.
	 */
	public PathElement getCloneOf(PathElement pe) {
		PathElement newpe = getNewPath();
		newpe.cloneFrom(pe);
		return newpe;
	}

	/**
	 * "Free" (release) an element allocated from the pool.
	 * 
	 * @param pe element of the pool to release.
	 * @throws IllegalArgumentException if <tt>pe</tt> was not allocated
	 *                                   from the pool.
	 */
	public void freePath(PathElement pe) {
		for (int i = 0; i < pool.length; i++)
		  if (pool[i].elmt == pe) {
			  pool[i].free = true;
			  pool[i].elmt.setZero(true);
			  return;
		  }
		throw new IllegalArgumentException(String.format(
				ERR_UNKNOWN_ELEMENT,
				System.identityHashCode(pe)));
	}

	private static PathElementPool instance;

	public synchronized static void setWidthDimension(int value) {
		if (weightDimension == value) return;
		instance = null;
		System.gc();
		weightDimension = value;
	}

	public synchronized static void setMaxPathSize(int value) {
		if (maxPathSize == value) return;
		instance = null;
		System.gc();
		maxPathSize = value;
	}

	public synchronized static PathElementPool getInstance() {
		if (instance == null) {
			instance = new PathElementPool();
		}
		return instance;
	}

}
