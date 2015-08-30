package cemethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * This class implements the noisy cross-entropy method for optimization, see e.g.
 * http://ie.technion.ac.il/CE/files/papers/Learning%20Tetris%20Using%20the%20Noisy%20Cross-Entropy%20Method.pdf .
 */
public final class CESolver {
	/**
	 * Queue to send problems to workers.
	 */
	private final LinkedBlockingQueue<Subproblem> q;
	/**
	 * Queue to get results from workers.
	 */
	private final LinkedBlockingQueue<Perf> qq;
	/**
	 * Worker threads.
	 */
	private final List<CEWorker> workers;
	/**
	 * Current distribution. 
	 */
	private GeneralNormalDistribution d;
	/**
	 * Sample size per iteration.
	 */
	private int samples;
	/**
	 * Samples used for next iteration.
	 */
	private int elites;
	/**
	 * Maximal number of iterations before stopping.
	 */
	private int maxIterations;
	/**
	 * Minimal variance before stopping.
	 */
	private double minVariance;
	/**
	 * Problem considered.
	 */
	private CEProblem problem;
	/**
	 * RNG used.
	 */
	private final RandomGenerator r;
	/**
	 * Noise change per iteration.
	 */
	private double noiseStep;
	/**
	 * Noise added first iteration.
	 */
	private double initialNoise;

	/**
	 * @return the maxIterations
	 */
	public int getMaxIterations() {
		return maxIterations;
	}

	/**
	 * @param maxIterations the maxIterations to set
	 */
	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	/**
	 * @param threads The number of threads to use when solving problems.
	 * @param r The RNG to use for generating samples.
	 */
	public CESolver(int threads, RandomGenerator r) {
		q = new LinkedBlockingQueue<Subproblem>();
		qq = new LinkedBlockingQueue<Perf>();
		workers = new ArrayList<CEWorker>();
		for(int i = 0; i < threads; i++) {
			workers.add(new CEWorker(q, qq));
			workers.get(i).start();
		}
		this.r = r;
	}

	/**
	 * @return the number of samples to generate each iteration.
	 */
	public int getSamples() {
		return samples;
	}

	/**
	 * @param samples the number of samples to generate each iteration.
	 */
	public void setSamples(int samples) {
		this.samples = samples;
	}

	/**
	 * @return the step by which the noice is reduced every iteration.
	 */
	public double getNoiseStep() {
		return noiseStep;
	}

	/**
	 * @param noiseStep the step by which the noice should be reduced every iteration.
	 */
	public void setNoiseStep(double noiseStep) {
		this.noiseStep = noiseStep;
	}

	/**
	 * @return the initial noise level.
	 */
	public double getInitialNoise() {
		return initialNoise;
	}

	/**
	 * @param initialNoise the initial noise level to use.
	 */
	public void setInitialNoise(double initialNoise) {
		this.initialNoise = initialNoise;
	}

	/**
	 * @return the number of elite samples per iteration.
	 */
	public int getElites() {
		return elites;
	}

	/**
	 * @param elites the number of elite samples per iteration.
	 */
	public void setElites(int elites) {
		this.elites = elites;
	}

	/**
	 * @return the variance used as stopping criterion
	 */
	public double getMinVariance() {
		return minVariance;
	}

	/**
	 * @param minVariance the variance to be used as stopping criterion.
	 */
	public void setMinVariance(double minVariance) {
		this.minVariance = minVariance;
	}

	/**
	 * @return the problem to solve.
	 */
	public CEProblem getProblem() {
		return problem;
	}

	/**
	 * @param problem the problem to solve.
	 */
	public void setProblem(CEProblem problem) {
		this.problem = problem;
	}

	/**
	 * Useful with threads = 1 for deterministic execution.
	 * @param n the seed for the RNG.
	 */
	public void seed(long n) {
		r.setSeed(n);
	}

	/**
	 * Shuts down all threads used by this solver.
	 */
	public void shutdown() {
		for(CEWorker w : workers) {
			w.interrupt();
		}
	}

	/**
	 * @return The found maximum value.
	 * @throws InterruptedException In case it is interrupted while working.
	 */
	public double[] solve() throws InterruptedException {
		double[] best = null;
		d = new GeneralNormalDistribution(problem.dimension(), r);
		int save = elites / 2;
		double dist;
		double[] oldMean = d.getMeans();
		double[] mean;
		List<Point> params = new ArrayList<Point>();
		for(int i = 0; i < samples; i++) {
			params.add(new Point(d.sample()));
		}

		for(int iter = 1; iter <= maxIterations && d.avgVar() > minVariance; iter++) {
			for(int i = save; i < samples; i++) {
				params.set(i, new Point(d.sample()));
			}
			for(int i = 0; i < samples; i++) {
				q.add(new Subproblem(problem, params.get(i).par, i));
			}
			for(int i = 0; i < samples; i++) {
				Perf perf = qq.take();
				params.get(perf.index).performance = perf.performance;
			}

			Collections.sort(params);
			double noise = initialNoise + noiseStep * iter;
			List<Point> eliteSamples = params.subList(0, elites);
			System.out.println("Standard deviation of scores: " + scoreDeviation(eliteSamples));
			d.fitTo(eliteSamples, noise > 0 ? noise : 0);
			mean = d.getMeans();
			dist = l2(mean, oldMean);
			oldMean = mean;
			print(iter, params, dist);
			best = params.get(0).par;
		}
		return best;
	}

	/**
	 * Evaluates the parameter vector v several times, averaging the results.
	 * This is useful to test the fitness of a parameter vector
	 * for a stochastic function.
	 * @param v the parameter vector to evaluate.
	 * @param trials the number of trials to do.
	 * @return the mean and standard deviation of the trials.
	 * @throws InterruptedException if interrupted.
	 */
	public EvaluationResult evaluateParameters(double[] v, int trials) throws InterruptedException {
		if(trials < 2) { throw new IllegalArgumentException(
			"Must run at least two trials to compute sample standard deviation."); }
		Perf perf;
		double m = 0;
		double s = 0;
		for(int i = 0; i < trials; i++) {
			q.add(new Subproblem(problem, v, i));
		}
		// see https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm
		for(int i = 1; i <= trials; i++) {
			perf = qq.take();
			double x = perf.performance;
			double delta = x - m;
			m = m + delta / i;
			s = s + delta * (x - m);
		}
		return new EvaluationResult(m, s / (trials - 1));
	}

	private static double scoreDeviation(List<Point> l) {
		double mean = 0;
		for(Point p : l) {
			mean += p.performance;
		}
		mean /= l.size();
		double var = 0;
		for(Point p : l) {
			var += (p.performance - mean) * (p.performance - mean);
		}
		return Math.sqrt(var / l.size());
	}

	private void print(int i, List<Point> params, double dist) {
		System.out.println("Done with iteration " + i + ": ");
		for(Point point : params) {
			System.out.print((int)point.performance + " ");
		}
		System.out.println();
		System.out.println("Mean moved: " + dist);
		System.out.println("New mean: " + Arrays.toString(d.getMeans()));
		System.out.println("New variance: " + d.avgVar());
		System.out.println();
	}

	// Computes the l2 distance beteen a and b, compensated for length.
	private static double l2(double[] a, double[] b) {
		double ans = 0;
		for(int i = 0; i < a.length; i++) {
			ans += (a[i] - b[i]) * (a[i] - b[i]);
		}
		return Math.sqrt(ans / a.length);
	}
}
