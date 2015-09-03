package cemethod;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * Implements the normal distribution under the assumption that
 * each component has the same variance and that they all are
 * independent, i.e. the covariance matrix is a multiple of the
 * identity matrix.
 */
public class NormalDistribution implements Distribution {
	/**
	 * The dimensionality of the distribution.
	 */
	public final int dim;
	/**
	 * The vector of mean values.
	 */
	private final double[] means;
	/**
	 * The variance, common to each component of the vector.
	 */
	private double var;
	/**
	 * The RNG used to draw samples from this distribution.
	 */
	private final RandomGenerator r;


	/**
	 * @param var the variance of the distribution.
	 * @param r the source of randomness when sampling.
	 * @param mean the mean.
	 * @param var the variance.
	 */
	public NormalDistribution(RandomGenerator r, double[] mean, double var) {
		dim = mean.length;
		means = mean;
		this.var = 10;
		this.r = r;
	}

	@Override
	public void fitTo(double[][] samples, double noise) {
		int nsamples = samples.length;
		for(double[] sample : samples) {
			for(int i = 0; i < dim; i++) {
				means[i] += sample[i] / nsamples;
			}
		}
		double nvar = 0;
		for(double[] sample : samples) {
			for(int i = 0; i < dim; i++) {
				double si = sample[i];
				nvar += (si - means[i]) * (si - means[i]);
			}
		}
		var = nvar / nsamples / dim + noise;
	}

	@Override
	public double getVar() {
		return var;
	}

	@Override
	public double[] getMeans() {
		return Arrays.copyOf(means, dim);
	}

	@Override
	public double[] sample() {
		double[] ret = new double[dim];
		for(int i = 0; i < dim; i++) {
			ret[i] = means[i] + r.nextGaussian() * Math.sqrt(var);
		}
		return ret;
	}
}
