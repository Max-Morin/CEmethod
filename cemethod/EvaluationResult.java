package cemethod;

/**
 *
 * The return type of a call to CESolver.test().
 */
public class EvaluationResult {
	/**
	 * The mean of the test trials.
	 */
	public final double mean;
	/**
	 * The standard deviation of the test trials.
	 */
	public final double standardDeviation;

	EvaluationResult(double mean, double stddev) {
		this.mean = mean;
		standardDeviation = stddev;
	}
}
