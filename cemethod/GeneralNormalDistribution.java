package cemethod;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.correlation.Covariance;

/**
 * 
 * A normal distribution where the covariance matrix may be any nonsingular
 * matrix. Theoretically, there is no need to have nonsingular matrices but
 * apache commons cannot work with singular covariance matrices.
 * This distribution is only appropriate to use if the dimension of the
 * problem is greater than the number of elite samples used.
 */
public class GeneralNormalDistribution implements Distribution {
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
	 * @param r the source of randomness when sampling.
	 * @param means the means of the distribution.
	 * @param var the variance of the distribution.
	 */
	public GeneralNormalDistribution(RandomGenerator r, double[] means, double var) {
		dim = means.length;
		double[][] covarianceMatrix = new double[dim][dim];
		for(int i = 0; i < dim; i++) {
			covarianceMatrix[i][i] = var;
		}
		this.r = r;
		d = new MultivariateNormalDistribution(this.r, means, covarianceMatrix);
	}

	@Override
	public void fitTo(double[][] samples, double noise) {
		int nsamples = samples.length;
		double[] means = new double[dim];
		double[][] covarianceMatrix;
		for(double[] sample : samples) {
			for(int i = 0; i < dim; i++) {
				means[i] += sample[i] / nsamples;
			}
		}
		covarianceMatrix = new Covariance(samples, false).getCovarianceMatrix().getData();
		for(int i = 0; i < dim; i++) {
			covarianceMatrix[i][i] += noise;
		}
		d = new MultivariateNormalDistribution(r, means, covarianceMatrix);
	}

	@Override
	public double getVar() {
		double ans = 0;
		double[] a = d.getStandardDeviations();
		for(double x : a) {
			ans += x * x;
		}
		return ans / dim;
	}

	@Override
	public double[] getMeans() {
		return d.getMeans();
	}

	@Override
	public double[] sample() {
		return d.sample();
	}
}
