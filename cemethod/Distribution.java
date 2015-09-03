package cemethod;

/**
 * A probability distribution for usage with CESolver.
 */
public interface Distribution {
	/**
	 * Fits a new normal distribution to the samples given,
	 * adding the specified amount of noise.
	 * @param samples anarray of samples to fit to.
	 * @param noise the noise to add to the variance.
	 */
	void fitTo(double[][] samples, double noise);

	/**
	 * @return the variance of this distribution.
	 */
	double getVar();

	/**
	 * @return means of this distribution.
	 */
	double[] getMeans();

	/**
	 * @return a sample from this distribution.
	 */
	double[] sample();
}
