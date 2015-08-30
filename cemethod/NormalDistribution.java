package cemethod;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Implements the normal distribution under the assumption that
 * each component has the same variance and that they all are
 * independent, i.e. the covariance matrix is a multiple of the
 * identity matrix.
 */
class NormalDistribution {
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
	private final double var;

	/**
	 * @param dimension the dimensionality of the distribution.
	 * @param var the variance of the distribution.
	 */
	public NormalDistribution(int dimension, double var) {
		dim = dimension;
		means = new double[dim];
		for(int i = 0; i < dim; i++) {
			means[i] = 0;
		}
		this.var = var;
	}

	/**
	 * Fits a new normal distribution to the samples given,
	 * adding the specified amount of noise.
	 * @param samples the samples to fit to.
	 * @param noise the noise to add to the variance.
	 */
	public NormalDistribution(List<Point> samples, double noise) {
		dim = samples.get(0).par.length;
		means = new double[dim];
		int nsamples = samples.size();
		for(Point sample : samples) {
			for(int i = 0; i < dim; i++) {
				means[i] += sample.par[i] / nsamples;
			}
		}
		double nvar = 0;
		for(Point sample : samples) {
			for(int i = 0; i < dim; i++) {
				double si = sample.par[i];
				nvar += (si - means[i]) * (si - means[i]);
			}
		}
		var = nvar / nsamples / dim + noise;
	}

	/**
	 * @return the variance of this distribution.
	 */
	public double getVar() {
		return var;
	}

	/**
	 * @return means of this distribution.
	 */
	public double[] getMean() {
		return Arrays.copyOf(means, dim);
	}

	/**
	 * @param r the source of randomness.
	 * @return a sample from this distribution.
	 */
	public double[] sample(Random r) {
		double[] ret = new double[dim];
		for(int i = 0; i < dim; i++) {
			ret[i] = means[i] + r.nextGaussian() * Math.sqrt(var);
		}
		return ret;
	}
}
