package cemethod;

import java.util.List;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.correlation.Covariance;

/**
 * 
 * A normal distribution where the covariance matrix may be any nonsingular
 * matrix. Theoretically, there is no need to have nonsingular matrices but
 * apache commons cannot work with singular covariance matrices.
 */
class GeneralNormalDistribution {
	/**
	 * The dimensionality of the distribution.
	 */
	public final int dim;
	/**
	 * The apache commons implementation of the distribution.
	 */
	private MultivariateNormalDistribution d;
	/**
	 * The RNG used to draw samples from this distribution.
	 */
	private final RandomGenerator r;

	/**
	 * @param dimension the dimensionality of the distribution.
	 * @param r the source of randomness when sampling.
	 */
	public GeneralNormalDistribution(int dimension, RandomGenerator r) {
		dim = dimension;
		double[] means = new double[dim];
		double[][] covarianceMatrix = new double[dim][dim];
		for(int i = 0; i < dim; i++) {
			means[i] = 0;
			covarianceMatrix[i][i] = 100;
		}
		this.r = r;
		d = new MultivariateNormalDistribution(this.r, means, covarianceMatrix);
	}

	/**
	 * Fits a new normal distribution to the samples given,
	 * adding the specified amount of noise. If noise is less than 0.05,
	 * 0.05 will be used instead to avoid getting a singular covariance matrix.
	 * @param samples the samples to fit to.
	 * @param noise the noise to add to the diagonal of the covariance matrix.
	 */
	public void fitTo(List<Point> samples, double noise) {
		int nsamples = samples.size();
		double[] means = new double[dim];
		double[][] covarianceMatrix;
		for(Point sample : samples) {
			for(int i = 0; i < dim; i++) {
				means[i] += sample.par[i] / nsamples;
			}
		}
		double[][] arr = new double[samples.size()][];
		for(int i = 0; i < samples.size(); i++) {
			arr[i] = samples.get(i).par;
		}
		covarianceMatrix = new Covariance(arr, false).getCovarianceMatrix().getData();
		for(int i = 0; i < dim; i++) {
			covarianceMatrix[i][i] += noise + 0.05;
		}
		/*for(double[] a : covarianceMatrix) {
			for(double f : a) {
				System.out.printf("%07.5f ", f);
			}
			System.out.println();
		}*/
		d = new MultivariateNormalDistribution(r, means, covarianceMatrix);
	}

	/**
	 * 
	 * @return The average variance of this distribution.
	 */
	public double avgVar() {
		double ans = 0;
		double[] a = d.getStandardDeviations();
		for(double x : a) {
			ans += x * x;
		}
		return ans / dim;
	}

	/**
	 * @return The means in each dimension (the diagonal of the covariance matrix).
	 */
	public double[] getMeans() {
		return d.getMeans();
	}

	/**
	 * 
	 * @return A sample from the distribution.
	 */
	public double[] sample() {
		return d.sample();
	}
}
