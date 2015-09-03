package cemethod;

import java.util.Arrays;

/**
 * A point to be evaluated by the function that is being optimized.
 */
class Point implements Comparable<Point> {
	/**
	 * The point.
	 */
	double[] vec;
	/**
	 * A cached measure of performance. 
	 */
	double performance;

	/**
	 * @param vector
	 */
	Point(double[] vector) {
		vec = Arrays.copyOf(vector, vector.length);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Point o) {
		return -new Double(performance).compareTo(new Double(o.performance));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Arrays.toString(vec);
	}
}
